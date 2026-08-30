package com.example.data.repository

import com.example.data.local.RideGoDatabase
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CityEntity
import com.example.data.local.entity.DriverProfileEntity
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PricingRuleEntity
import com.example.data.local.entity.PromoCodeEntity
import com.example.data.local.entity.RideEntity
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.local.entity.SupportTicketEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WalletTransactionEntity
import com.example.data.model.DriverDocStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.RideStatus
import com.example.data.model.TicketStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.domain.DriverMatchingEngine
import com.example.domain.MatchedDriverCandidate
import com.example.domain.PricingEngine
import com.example.domain.RouteSimulationEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class RideGoRepository(private val db: RideGoDatabase) {

    // --- Users & Profiles ---
    fun getUser(userId: String): Flow<UserEntity?> = db.userDao().getUserById(userId)
    fun getAllUsers(): Flow<List<UserEntity>> = db.userDao().getAllUsers()
    fun getDrivers(): Flow<List<UserEntity>> = db.userDao().getUsersByRole(UserRole.DRIVER)
    fun getRiders(): Flow<List<UserEntity>> = db.userDao().getUsersByRole(UserRole.RIDER)

    suspend fun updateUser(user: UserEntity) = db.userDao().updateUser(user)
    suspend fun setUserBlocked(userId: String, isBlocked: Boolean) {
        db.userDao().setUserBlocked(userId, isBlocked)
        logAudit("USER_STATUS_CHANGE", "Admin", "User $userId blocked=$isBlocked")
    }

    // --- Drivers ---
    fun getDriverProfile(driverId: String): Flow<DriverProfileEntity?> = db.driverProfileDao().getDriverProfile(driverId)
    fun getAllDriverProfiles(): Flow<List<DriverProfileEntity>> = db.driverProfileDao().getAllDrivers()
    fun getOnlineDrivers(): Flow<List<DriverProfileEntity>> = db.driverProfileDao().getOnlineAvailableDrivers()

    suspend fun setDriverOnline(driverId: String, isOnline: Boolean) {
        db.driverProfileDao().setDriverOnline(driverId, isOnline)
    }

    suspend fun updateDriverDocStatus(driverId: String, status: DriverDocStatus) {
        db.driverProfileDao().updateDriverDocStatus(driverId, status)
        logAudit("DRIVER_DOC_STATUS", "Admin", "Driver $driverId status changed to $status")
    }

    suspend fun updateDriverLocation(driverId: String, lat: Double, lng: Double) {
        db.driverProfileDao().updateDriverLocation(driverId, lat, lng)
    }

    // --- Pricing & Cities ---
    fun getActiveCities(): Flow<List<CityEntity>> = db.cityDao().getActiveCities()
    fun getAllCities(): Flow<List<CityEntity>> = db.cityDao().getAllCities()
    fun getPricingRules(cityId: String): Flow<List<PricingRuleEntity>> = db.pricingRuleDao().getPricingRulesForCity(cityId)
    fun getAllPricingRules(): Flow<List<PricingRuleEntity>> = db.pricingRuleDao().getAllPricingRules()

    suspend fun updatePricingRule(rule: PricingRuleEntity) {
        db.pricingRuleDao().updatePricingRule(rule)
        logAudit("PRICING_UPDATED", "Admin", "Updated pricing for ${rule.vehicleCategory} in ${rule.cityId}")
    }

    suspend fun updateCity(city: CityEntity) {
        db.cityDao().updateCity(city)
        logAudit("CITY_UPDATED", "Admin", "City ${city.name} updated. Base Surge=${city.baseSurge}")
    }

    suspend fun addCity(city: CityEntity) {
        db.cityDao().insertCity(city)
        // Also insert default pricing rules for this city
        VehicleCategory.values().forEach { category ->
            db.pricingRuleDao().insertPricingRule(
                PricingRuleEntity(
                    id = "price_${city.id}_${category.name.lowercase()}",
                    cityId = city.id,
                    vehicleCategory = category,
                    baseFare = 35.0 * category.baseMultiplier,
                    perKmRate = 25.0 * category.baseMultiplier,
                    perMinuteRate = 1.0 * category.baseMultiplier,
                    minFare = 50.0 * category.baseMultiplier
                )
            )
        }
        logAudit("CITY_ADDED", "Admin", "Added new city ${city.name}")
    }

    // --- Promos ---
    fun getActivePromos(): Flow<List<PromoCodeEntity>> = db.promoCodeDao().getActivePromoCodes()
    fun getAllPromos(): Flow<List<PromoCodeEntity>> = db.promoCodeDao().getAllPromoCodes()
    suspend fun addPromoCode(promo: PromoCodeEntity) {
        db.promoCodeDao().insertPromoCode(promo)
        logAudit("PROMO_CREATED", "Admin", "Created promo code ${promo.code}")
    }
    suspend fun updatePromoCode(promo: PromoCodeEntity) {
        db.promoCodeDao().updatePromoCode(promo)
    }

    // --- Rides ---
    fun getRiderHistory(riderId: String): Flow<List<RideEntity>> = db.rideDao().getRiderHistory(riderId)
    fun getDriverHistory(driverId: String): Flow<List<RideEntity>> = db.rideDao().getDriverHistory(driverId)
    fun getAllRides(): Flow<List<RideEntity>> = db.rideDao().getAllRides()
    fun getActiveRideForRider(): Flow<RideEntity?> = db.rideDao().getActiveRideForRider()
    fun getActiveRideForDriver(driverId: String): Flow<RideEntity?> = db.rideDao().getActiveRideForDriver(driverId)
    fun getPendingRideRequests(): Flow<List<RideEntity>> = db.rideDao().getPendingRideRequests()
    fun getRideById(rideId: String): Flow<RideEntity?> = db.rideDao().getRideById(rideId)

    suspend fun createRideBooking(
        riderId: String,
        cityId: String,
        pickupName: String,
        pickupLat: Double,
        pickupLng: Double,
        destName: String,
        destLat: Double,
        destLng: Double,
        category: VehicleCategory,
        paymentMethod: PaymentMethod,
        promoCode: String?
    ): RideEntity {
        val rider = db.userDao().getUserByIdOnce(riderId) ?: error("Rider not found")
        val rule = db.pricingRuleDao().getPricingRule(cityId, category)
        val city = db.cityDao().getCityById(cityId)
        val promo = if (!promoCode.isNullOrBlank()) db.promoCodeDao().getPromoCode(promoCode.trim().uppercase()) else null

        val distanceKm = RouteSimulationEngine.calculateDistanceKm(pickupLat, pickupLng, destLat, destLng)
        val durationMin = RouteSimulationEngine.estimateDurationMinutes(distanceKm)
        val surgeMultiplier = city?.baseSurge ?: (rule?.surgeMultiplier ?: 1.0)

        val fare = PricingEngine.calculateFare(
            rule = rule,
            category = category,
            distanceKm = distanceKm,
            durationMinutes = durationMin,
            surgeMultiplier = surgeMultiplier,
            promo = promo
        )

        val rideId = "RG-" + (1000..9999).random()
        val randomOtp = (1000..9999).random().toString()

        val newRide = RideEntity(
            id = rideId,
            riderId = riderId,
            riderName = rider.name,
            riderPhone = rider.phone,
            riderRating = rider.rating,
            vehicleCategory = category,
            cityId = cityId,
            pickupName = pickupName,
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            destinationName = destName,
            destLat = destLat,
            destLng = destLng,
            distanceKm = fare.distanceKm,
            durationMinutes = fare.timeMinutes,
            baseFare = fare.baseFare,
            distanceFare = fare.distanceCharge,
            timeFare = fare.timeCharge,
            bookingFee = fare.bookingFee,
            surgeMultiplier = fare.surgeMultiplier,
            discountAmount = fare.discountAmount,
            taxAmount = fare.taxes,
            totalFare = fare.totalFare,
            driverEarnings = fare.driverEarnings,
            platformCommission = fare.platformCommission,
            paymentMethod = paymentMethod,
            paymentStatus = PaymentStatus.PENDING,
            status = RideStatus.SEARCHING_DRIVER,
            promoCode = promo?.code,
            otp = randomOtp,
            createdAt = System.currentTimeMillis()
        )

        db.rideDao().insertRide(newRide)

        // Add Notification
        sendNotification(
            riderId,
            "Ride Requested ($rideId)",
            "Searching for nearby ${category.displayName} drivers in $pickupName...",
            "RIDE_SEARCH"
        )

        logAudit("RIDE_REQUESTED", rider.name, "Booking $rideId created for ${fare.totalFare} ${city?.currencySymbol ?: "₹"}")
        return newRide
    }

    suspend fun matchAndAssignDriver(rideId: String): MatchedDriverCandidate? {
        val ride = db.rideDao().getRideByIdOnce(rideId) ?: return null
        val onlineProfiles = db.driverProfileDao().getOnlineDriversByCategory(ride.vehicleCategory)
        
        val driverPairs = onlineProfiles.map { profile ->
            val user = db.userDao().getUserByIdOnce(profile.driverId)
            Pair(profile, user)
        }

        val candidates = DriverMatchingEngine.rankCandidates(
            onlineDrivers = driverPairs,
            pickupLat = ride.pickupLat,
            pickupLng = ride.pickupLng,
            requestedCategory = ride.vehicleCategory
        )

        if (candidates.isEmpty()) {
            db.rideDao().updateRideStatus(rideId, RideStatus.NO_DRIVER_FOUND)
            sendNotification(ride.riderId, "No Drivers Available", "We couldn't find any nearby drivers. Please try again.", "RIDE_ALERT")
            return null
        }

        val chosen = candidates.first()
        val assignedRide = ride.copy(
            driverId = chosen.driverProfile.driverId,
            driverName = chosen.user?.name ?: "Professional Driver",
            driverPhone = chosen.user?.phone ?: "+91 98000 11111",
            driverRating = chosen.driverProfile.rating,
            vehicleModel = chosen.driverProfile.vehicleModel,
            vehicleNumber = chosen.driverProfile.vehicleNumber,
            vehicleColor = chosen.driverProfile.vehicleColor,
            driverCurrentLat = chosen.driverProfile.currentLat,
            driverCurrentLng = chosen.driverProfile.currentLng,
            status = RideStatus.DRIVER_ASSIGNED
        )
        db.rideDao().updateRide(assignedRide)

        sendNotification(
            ride.riderId,
            "Driver Assigned!",
            "${assignedRide.driverName} (${assignedRide.vehicleModel} • ${assignedRide.vehicleNumber}) is arriving in ${chosen.etaMinutes} mins. Share PIN ${assignedRide.otp} upon boarding.",
            "RIDE_UPDATE"
        )
        sendNotification(
            chosen.driverProfile.driverId,
            "New Ride Accepted!",
            "Pickup: ${ride.pickupName} • Distance: ${ride.distanceKm} km • Fare: ₹${ride.totalFare}",
            "DRIVER_RIDE"
        )

        logAudit("DRIVER_ASSIGNED", "MatchingEngine", "Driver ${assignedRide.driverName} assigned to ride $rideId")
        return chosen
    }

    suspend fun advanceRideStatus(rideId: String, nextStatus: RideStatus) {
        val ride = db.rideDao().getRideByIdOnce(rideId) ?: return
        val updatedRide = when (nextStatus) {
            RideStatus.RIDE_STARTED -> ride.copy(status = nextStatus, startedAt = System.currentTimeMillis())
            RideStatus.COMPLETED -> ride.copy(
                status = nextStatus,
                completedAt = System.currentTimeMillis(),
                paymentStatus = if (ride.paymentMethod != PaymentMethod.CASH) PaymentStatus.PAID else PaymentStatus.PAID
            )
            else -> ride.copy(status = nextStatus)
        }
        db.rideDao().updateRide(updatedRide)

        when (nextStatus) {
            RideStatus.DRIVER_ARRIVING -> {
                sendNotification(ride.riderId, "Driver on the Way", "${ride.driverName} is heading to pickup point.", "RIDE_UPDATE")
            }
            RideStatus.DRIVER_ARRIVED -> {
                sendNotification(ride.riderId, "Driver Arrived", "${ride.driverName} has arrived at pickup. Please meet at vehicle ${ride.vehicleNumber}.", "RIDE_ALERT")
            }
            RideStatus.RIDE_STARTED -> {
                sendNotification(ride.riderId, "Trip Started", "Have a safe ride! Emergency SOS is active.", "RIDE_UPDATE")
            }
            RideStatus.COMPLETED -> {
                sendNotification(ride.riderId, "Ride Completed", "Thank you for riding with RideGo! Total Fare: ₹${ride.totalFare}", "RIDE_COMPLETE")
                
                // Record driver earnings
                if (ride.driverId != null) {
                    db.driverProfileDao().recordDriverEarnings(ride.driverId, ride.driverEarnings)
                    db.walletDao().insertTransaction(
                        WalletTransactionEntity(
                            id = "tx_" + UUID.randomUUID().toString().take(8),
                            userId = ride.driverId,
                            amount = ride.driverEarnings,
                            type = TransactionType.CREDIT,
                            title = "Ride Payout (${ride.id})",
                            description = "Fare ₹${ride.totalFare} - Comm ₹${ride.platformCommission}",
                            referenceId = ride.id
                        )
                    )
                }

                // If paid with wallet, deduct rider wallet
                if (ride.paymentMethod == PaymentMethod.WALLET) {
                    db.userDao().updateWalletBalance(ride.riderId, -ride.totalFare)
                    db.walletDao().insertTransaction(
                        WalletTransactionEntity(
                            id = "tx_" + UUID.randomUUID().toString().take(8),
                            userId = ride.riderId,
                            amount = ride.totalFare,
                            type = TransactionType.DEBIT,
                            title = "Paid for Ride ${ride.id}",
                            description = "${ride.pickupName} -> ${ride.destinationName}",
                            referenceId = ride.id
                        )
                    )
                }
            }
            else -> {}
        }
        logAudit("RIDE_STATUS_CHANGE", "System", "Ride $rideId advanced to $nextStatus")
    }

    suspend fun cancelRide(rideId: String, byRider: Boolean, reason: String) {
        val ride = db.rideDao().getRideByIdOnce(rideId) ?: return
        val status = if (byRider) RideStatus.CANCELLED_BY_RIDER else RideStatus.CANCELLED_BY_DRIVER
        db.rideDao().updateRide(ride.copy(status = status, cancellationReason = reason))

        sendNotification(
            if (byRider) (ride.driverId ?: ride.riderId) else ride.riderId,
            "Ride Cancelled",
            "Ride $rideId was cancelled. Reason: $reason",
            "RIDE_ALERT"
        )
        logAudit("RIDE_CANCELLED", if (byRider) "Rider" else "Driver", "Ride $rideId cancelled: $reason")
    }

    suspend fun rateRide(rideId: String, isRider: Boolean, rating: Int, review: String?) {
        val ride = db.rideDao().getRideByIdOnce(rideId) ?: return
        val updated = if (isRider) {
            ride.copy(riderGivenRating = rating, riderGivenReview = review)
        } else {
            ride.copy(driverGivenRating = rating, driverGivenReview = review)
        }
        db.rideDao().updateRide(updated)
        logAudit("RIDE_RATED", if (isRider) "Rider" else "Driver", "Ride $rideId rated $rating stars")
    }

    // --- Wallet ---
    fun getWalletTransactions(userId: String): Flow<List<WalletTransactionEntity>> = db.walletDao().getTransactionsForUser(userId)
    
    suspend fun addMoneyToWallet(userId: String, amount: Double, method: String) {
        db.userDao().updateWalletBalance(userId, amount)
        db.walletDao().insertTransaction(
            WalletTransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = userId,
                amount = amount,
                type = TransactionType.CREDIT,
                title = "Added to Wallet",
                description = "Via $method • Instant Top-up",
                referenceId = "TOPUP-" + (1000..9999).random()
            )
        )
        sendNotification(userId, "Wallet Credited", "₹$amount successfully added to your RideGo Wallet.", "WALLET")
        logAudit("WALLET_TOPUP", userId, "Added ₹$amount via $method")
    }

    // --- Saved Places & Emergency Contacts ---
    fun getSavedPlaces(userId: String): Flow<List<SavedPlaceEntity>> = db.savedPlaceDao().getSavedPlaces(userId)
    suspend fun addSavedPlace(place: SavedPlaceEntity) = db.savedPlaceDao().insertSavedPlace(place)
    suspend fun deleteSavedPlace(id: String) = db.savedPlaceDao().deleteSavedPlace(id)

    fun getEmergencyContacts(userId: String): Flow<List<EmergencyContactEntity>> = db.emergencyContactDao().getContacts(userId)
    suspend fun addEmergencyContact(contact: EmergencyContactEntity) = db.emergencyContactDao().insertContact(contact)
    suspend fun deleteEmergencyContact(id: String) = db.emergencyContactDao().deleteContact(id)

    // --- Notifications & Support ---
    fun getNotifications(userId: String): Flow<List<NotificationEntity>> = db.notificationDao().getNotificationsForUser(userId)
    suspend fun sendNotification(userId: String, title: String, message: String, type: String) {
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "notif_" + UUID.randomUUID().toString().take(8),
                userId = userId,
                title = title,
                message = message,
                type = type
            )
        )
    }

    fun getAllTickets(): Flow<List<SupportTicketEntity>> = db.supportTicketDao().getAllTickets()
    fun getUserTickets(userId: String): Flow<List<SupportTicketEntity>> = db.supportTicketDao().getTicketsForUser(userId)
    suspend fun createSupportTicket(userId: String, userName: String, rideId: String?, subject: String, message: String) {
        db.supportTicketDao().insertTicket(
            SupportTicketEntity(
                id = "TKT-" + (1000..9999).random(),
                userId = userId,
                userName = userName,
                rideId = rideId,
                subject = subject,
                message = message,
                status = TicketStatus.OPEN
            )
        )
        sendNotification(userId, "Support Ticket Raised", "Ticket for '$subject' created. Support team will respond shortly.", "SUPPORT")
        logAudit("TICKET_CREATED", userName, "Raised ticket: $subject")
    }
    suspend fun resolveTicket(ticket: SupportTicketEntity, response: String) {
        db.supportTicketDao().updateTicket(ticket.copy(status = TicketStatus.RESOLVED, adminResponse = response))
        sendNotification(ticket.userId, "Ticket Resolved", "Your support ticket '${ticket.subject}' has been resolved: $response", "SUPPORT")
        logAudit("TICKET_RESOLVED", "Admin", "Ticket ${ticket.id} marked as resolved")
    }

    // --- Audit Logs ---
    fun getAuditLogs(): Flow<List<AuditLogEntity>> = db.auditLogDao().getAuditLogs()
    suspend fun logAudit(action: String, performedBy: String, details: String) {
        db.auditLogDao().insertLog(
            AuditLogEntity(
                id = "log_" + UUID.randomUUID().toString().take(8),
                action = action,
                performedBy = performedBy,
                details = details
            )
        )
    }
}
