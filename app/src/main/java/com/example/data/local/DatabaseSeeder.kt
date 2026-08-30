package com.example.data.local

import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CityEntity
import com.example.data.local.entity.DriverProfileEntity
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PricingRuleEntity
import com.example.data.local.entity.PromoCodeEntity
import com.example.data.local.entity.RideEntity
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WalletTransactionEntity
import com.example.data.model.DriverDocStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.RideStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {

    suspend fun seedInitialData(database: RideGoDatabase) = withContext(Dispatchers.IO) {
        val existingUsers = database.userDao().getUserByIdOnce("user_rider_1")
        if (existingUsers != null) return@withContext // already seeded

        // 1. Initial Users
        val users = listOf(
            UserEntity(
                id = "user_rider_1",
                role = UserRole.RIDER,
                name = "Aarav Sharma",
                phone = "+91 98765 43210",
                email = "aarav.sharma@example.com",
                emergencyContactName = "Sunita Sharma (Mother)",
                emergencyContactPhone = "+91 98765 00000",
                rating = 4.92,
                walletBalance = 750.0
            ),
            UserEntity(
                id = "user_driver_1",
                role = UserRole.DRIVER,
                name = "Rajesh Kumar",
                phone = "+91 91234 56780",
                email = "rajesh.driver@ridego.com",
                emergencyContactName = "Pooja Kumar (Wife)",
                emergencyContactPhone = "+91 91234 00000",
                rating = 4.88,
                walletBalance = 4250.0
            ),
            UserEntity(
                id = "user_driver_2",
                role = UserRole.DRIVER,
                name = "Vikram Singh",
                phone = "+91 98111 22334",
                email = "vikram.singh@ridego.com",
                rating = 4.95,
                walletBalance = 3100.0
            ),
            UserEntity(
                id = "user_driver_3",
                role = UserRole.DRIVER,
                name = "Sameer Deshmukh",
                phone = "+91 94222 33445",
                email = "sameer.d@ridego.com",
                rating = 4.79,
                walletBalance = 1890.0
            ),
            UserEntity(
                id = "user_driver_4",
                role = UserRole.DRIVER,
                name = "Amitabh Sen",
                phone = "+91 97333 44556",
                email = "amitabh.sen@ridego.com",
                rating = 4.90,
                walletBalance = 5600.0
            ),
            UserEntity(
                id = "user_driver_5",
                role = UserRole.DRIVER,
                name = "Ramesh Tukaram (Auto)",
                phone = "+91 98222 66778",
                email = "ramesh.auto@ridego.com",
                emergencyContactName = "Sunita Tukaram (Wife)",
                emergencyContactPhone = "+91 98222 00000",
                rating = 4.94,
                walletBalance = 2450.0
            ),
            UserEntity(
                id = "user_admin_1",
                role = UserRole.ADMIN,
                name = "Aditya Sarwade (Admin)",
                phone = "+91 99999 00001",
                email = "adityasarwade1920@gmail.com",
                rating = 5.0,
                walletBalance = 100000.0
            )
        )
        database.userDao().insertUsers(users)

        // 2. Driver Profiles
        val driverProfiles = listOf(
            DriverProfileEntity(
                driverId = "user_driver_1",
                vehicleModel = "Maruti Suzuki Swift Dzire",
                vehicleNumber = "MH 02 ER 8421",
                vehicleCategory = VehicleCategory.ECONOMY,
                vehicleColor = "Silver Metallic",
                licenseNumber = "MH02-2018-0091244",
                docStatus = DriverDocStatus.APPROVED,
                isOnline = true,
                currentLat = 19.0790,
                currentLng = 72.8750,
                totalRides = 384,
                rating = 4.88,
                todayEarnings = 1850.0,
                weeklyEarnings = 11400.0,
                totalEarnings = 142000.0
            ),
            DriverProfileEntity(
                driverId = "user_driver_2",
                vehicleModel = "Hyundai Creta SX (Comfort)",
                vehicleNumber = "MH 01 BK 4492",
                vehicleCategory = VehicleCategory.COMFORT,
                vehicleColor = "Phantom Black",
                licenseNumber = "MH01-2017-0043121",
                docStatus = DriverDocStatus.APPROVED,
                isOnline = true,
                currentLat = 19.0720,
                currentLng = 72.8810,
                totalRides = 512,
                rating = 4.95,
                todayEarnings = 2650.0,
                weeklyEarnings = 16800.0,
                totalEarnings = 210000.0
            ),
            DriverProfileEntity(
                driverId = "user_driver_3",
                vehicleModel = "Toyota Innova Crysta",
                vehicleNumber = "MH 03 DZ 1920",
                vehicleCategory = VehicleCategory.XL,
                vehicleColor = "Garnet Red",
                licenseNumber = "MH03-2019-0078129",
                docStatus = DriverDocStatus.APPROVED,
                isOnline = true,
                currentLat = 19.0830,
                currentLng = 72.8690,
                totalRides = 295,
                rating = 4.79,
                todayEarnings = 2900.0,
                weeklyEarnings = 18200.0,
                totalEarnings = 178000.0
            ),
            DriverProfileEntity(
                driverId = "user_driver_4",
                vehicleModel = "Mercedes-Benz E-Class",
                vehicleNumber = "MH 01 PR 0007",
                vehicleCategory = VehicleCategory.PREMIUM,
                vehicleColor = "Obsidian Black",
                licenseNumber = "MH01-2015-0011928",
                docStatus = DriverDocStatus.APPROVED,
                isOnline = true,
                currentLat = 19.0680,
                currentLng = 72.8730,
                totalRides = 189,
                rating = 4.90,
                todayEarnings = 3800.0,
                weeklyEarnings = 24500.0,
                totalEarnings = 320000.0
            ),
            DriverProfileEntity(
                driverId = "user_driver_5",
                vehicleModel = "Bajaj Compact 4S (Auto)",
                vehicleNumber = "MH 02 AU 3381",
                vehicleCategory = VehicleCategory.AUTO,
                vehicleColor = "Yellow & Green",
                licenseNumber = "MH02-2020-0081290",
                docStatus = DriverDocStatus.APPROVED,
                isOnline = true,
                currentLat = 19.0755,
                currentLng = 72.8765,
                totalRides = 620,
                rating = 4.94,
                todayEarnings = 1450.0,
                weeklyEarnings = 9800.0,
                totalEarnings = 89000.0
            )
        )
        database.driverProfileDao().insertDriverProfiles(driverProfiles)

        // 3. Cities
        val cities = listOf(
            CityEntity(
                id = "city_mumbai",
                name = "Mumbai",
                state = "Maharashtra",
                country = "India",
                currencySymbol = "₹",
                currencyCode = "INR",
                isActive = true,
                baseSurge = 1.0,
                centerLat = 19.0760,
                centerLng = 72.8777
            ),
            CityEntity(
                id = "city_bengaluru",
                name = "Bengaluru",
                state = "Karnataka",
                country = "India",
                currencySymbol = "₹",
                currencyCode = "INR",
                isActive = true,
                baseSurge = 1.2,
                centerLat = 12.9716,
                centerLng = 77.5946
            ),
            CityEntity(
                id = "city_delhi",
                name = "Delhi NCR",
                state = "Delhi",
                country = "India",
                currencySymbol = "₹",
                currencyCode = "INR",
                isActive = true,
                baseSurge = 1.0,
                centerLat = 28.6139,
                centerLng = 77.2090
            ),
            CityEntity(
                id = "city_london",
                name = "London",
                state = "Greater London",
                country = "United Kingdom",
                currencySymbol = "£",
                currencyCode = "GBP",
                isActive = true,
                baseSurge = 1.0,
                centerLat = 51.5074,
                centerLng = -0.1278
            )
        )
        database.cityDao().insertCities(cities)

        // 4. Configurable Pricing Rules per vehicle category & city (Standard ₹25/km base)
        val pricingRules = listOf(
            // Mumbai Pricing
            PricingRuleEntity(
                id = "price_mumbai_auto",
                cityId = "city_mumbai",
                vehicleCategory = VehicleCategory.AUTO,
                baseFare = 30.0,
                perKmRate = 20.0,
                perMinuteRate = 1.0,
                minFare = 40.0,
                bookingFee = 10.0,
                taxPercent = 5.0,
                cancellationFee = 30.0,
                platformCommissionPercent = 15.0,
                surgeMultiplier = 1.0
            ),
            PricingRuleEntity(
                id = "price_mumbai_eco",
                cityId = "city_mumbai",
                vehicleCategory = VehicleCategory.ECONOMY,
                baseFare = 40.0,
                perKmRate = 25.0, // Standard 1 km = ₹25
                perMinuteRate = 1.2,
                minFare = 50.0,
                bookingFee = 15.0,
                taxPercent = 5.0,
                cancellationFee = 50.0,
                platformCommissionPercent = 18.0,
                surgeMultiplier = 1.0
            ),
            PricingRuleEntity(
                id = "price_mumbai_comfort",
                cityId = "city_mumbai",
                vehicleCategory = VehicleCategory.COMFORT,
                baseFare = 60.0,
                perKmRate = 28.0,
                perMinuteRate = 2.0,
                minFare = 80.0,
                bookingFee = 20.0,
                taxPercent = 5.0,
                cancellationFee = 60.0,
                platformCommissionPercent = 20.0,
                surgeMultiplier = 1.0
            ),
            PricingRuleEntity(
                id = "price_mumbai_premium",
                cityId = "city_mumbai",
                vehicleCategory = VehicleCategory.PREMIUM,
                baseFare = 100.0,
                perKmRate = 35.0,
                perMinuteRate = 3.0,
                minFare = 150.0,
                bookingFee = 30.0,
                taxPercent = 5.0,
                cancellationFee = 100.0,
                platformCommissionPercent = 22.0,
                surgeMultiplier = 1.0
            ),
            PricingRuleEntity(
                id = "price_mumbai_xl",
                cityId = "city_mumbai",
                vehicleCategory = VehicleCategory.XL,
                baseFare = 90.0,
                perKmRate = 32.0,
                perMinuteRate = 2.5,
                minFare = 130.0,
                bookingFee = 25.0,
                taxPercent = 5.0,
                cancellationFee = 80.0,
                platformCommissionPercent = 20.0,
                surgeMultiplier = 1.0
            )
        )
        database.pricingRuleDao().insertPricingRules(pricingRules)

        // 5. Promo Codes
        val promoCodes = listOf(
            PromoCodeEntity(
                code = "FIRST50",
                title = "50% Off First Ride",
                description = "Get 50% discount up to ₹100 on your first RideGo booking!",
                discountPercent = 50.0,
                maxDiscount = 100.0,
                minRideValue = 80.0,
                cityId = "all"
            ),
            PromoCodeEntity(
                code = "RIDEGO20",
                title = "20% Daily Commute Discount",
                description = "Save 20% up to ₹75 on any Economy or Comfort ride",
                discountPercent = 20.0,
                maxDiscount = 75.0,
                minRideValue = 120.0,
                cityId = "all"
            ),
            PromoCodeEntity(
                code = "AIRPORT150",
                title = "Flat ₹150 Off Airport Trips",
                description = "Flat ₹150 off on XL and Premium rides to airport",
                fixedDiscount = 150.0,
                maxDiscount = 150.0,
                minRideValue = 400.0,
                cityId = "all"
            )
        )
        database.promoCodeDao().insertPromoCodes(promoCodes)

        // 6. Saved Places
        val savedPlaces = listOf(
            SavedPlaceEntity(
                id = "place_home",
                userId = "user_rider_1",
                name = "Home",
                address = "Flat 402, Sea Green Apts, Bandra West, Mumbai",
                lat = 19.0596,
                lng = 72.8295,
                iconType = "home"
            ),
            SavedPlaceEntity(
                id = "place_work",
                userId = "user_rider_1",
                name = "Work Office",
                address = "Tech Park Tower 3, BKC, Bandra East, Mumbai",
                lat = 19.0657,
                lng = 72.8687,
                iconType = "work"
            ),
            SavedPlaceEntity(
                id = "place_airport",
                userId = "user_rider_1",
                name = "Chhatrapati Shivaji Terminal 2",
                address = "International Airport, Sahar, Andheri East, Mumbai",
                lat = 19.0896,
                lng = 72.8656,
                iconType = "airport"
            )
        )
        database.savedPlaceDao().insertSavedPlaces(savedPlaces)

        // 7. Emergency Contacts
        val emergencyContacts = listOf(
            EmergencyContactEntity(
                id = "contact_1",
                userId = "user_rider_1",
                name = "Sunita Sharma",
                phone = "+91 98765 00000",
                relationship = "Mother"
            ),
            EmergencyContactEntity(
                id = "contact_2",
                userId = "user_rider_1",
                name = "Rohan Sharma",
                phone = "+91 98765 11111",
                relationship = "Brother"
            )
        )
        database.emergencyContactDao().insertContacts(emergencyContacts)

        // 8. Wallet Transactions
        val transactions = listOf(
            WalletTransactionEntity(
                id = "tx_101",
                userId = "user_rider_1",
                amount = 1000.0,
                type = TransactionType.CREDIT,
                title = "Added Money via UPI",
                description = "Google Pay Ref #UPI99812401",
                referenceId = "UPI99812401",
                timestamp = System.currentTimeMillis() - 86400000L * 3
            ),
            WalletTransactionEntity(
                id = "tx_102",
                userId = "user_rider_1",
                amount = 250.0,
                type = TransactionType.DEBIT,
                title = "Ride to BKC Tech Park",
                description = "Trip #RG-9021 • Maruti Dzire",
                referenceId = "RG-9021",
                timestamp = System.currentTimeMillis() - 86400000L * 2
            )
        )
        database.walletDao().insertTransactions(transactions)

        // 9. Past Rides
        val pastRides = listOf(
            RideEntity(
                id = "RG-9021",
                riderId = "user_rider_1",
                riderName = "Aarav Sharma",
                riderPhone = "+91 98765 43210",
                driverId = "user_driver_1",
                driverName = "Rajesh Kumar",
                driverPhone = "+91 91234 56780",
                driverRating = 4.88,
                vehicleModel = "Maruti Suzuki Swift Dzire",
                vehicleNumber = "MH 02 ER 8421",
                vehicleColor = "Silver",
                vehicleCategory = VehicleCategory.ECONOMY,
                cityId = "city_mumbai",
                pickupName = "Bandra West Promenade",
                pickupLat = 19.0596,
                pickupLng = 72.8295,
                destinationName = "BKC Tech Park Tower 3",
                destLat = 19.0657,
                destLng = 72.8687,
                distanceKm = 6.4,
                durationMinutes = 18,
                baseFare = 50.0,
                distanceFare = 89.6,
                timeFare = 27.0,
                bookingFee = 15.0,
                surgeMultiplier = 1.0,
                taxAmount = 9.08,
                totalFare = 250.0,
                driverEarnings = 205.0,
                platformCommission = 45.0,
                paymentMethod = PaymentMethod.WALLET,
                paymentStatus = PaymentStatus.PAID,
                status = RideStatus.COMPLETED,
                riderGivenRating = 5,
                riderGivenReview = "Super smooth drive and courteous driver!",
                createdAt = System.currentTimeMillis() - 86400000L * 2,
                completedAt = System.currentTimeMillis() - 86400000L * 2 + 1200000L
            ),
            RideEntity(
                id = "RG-8840",
                riderId = "user_rider_1",
                riderName = "Aarav Sharma",
                riderPhone = "+91 98765 43210",
                driverId = "user_driver_2",
                driverName = "Vikram Singh",
                driverPhone = "+91 98111 22334",
                driverRating = 4.95,
                vehicleModel = "Hyundai Creta SX",
                vehicleNumber = "MH 01 BK 4492",
                vehicleColor = "Black",
                vehicleCategory = VehicleCategory.COMFORT,
                cityId = "city_mumbai",
                pickupName = "Mumbai Airport T2",
                pickupLat = 19.0896,
                pickupLng = 72.8656,
                destinationName = "Bandra West",
                destLat = 19.0596,
                destLng = 72.8295,
                distanceKm = 11.2,
                durationMinutes = 28,
                baseFare = 80.0,
                distanceFare = 207.2,
                timeFare = 56.0,
                bookingFee = 20.0,
                surgeMultiplier = 1.2,
                taxAmount = 21.79,
                totalFare = 457.0,
                driverEarnings = 365.0,
                platformCommission = 92.0,
                paymentMethod = PaymentMethod.UPI,
                paymentStatus = PaymentStatus.PAID,
                status = RideStatus.COMPLETED,
                riderGivenRating = 5,
                riderGivenReview = "Punctual and very clean vehicle.",
                createdAt = System.currentTimeMillis() - 86400000L * 5,
                completedAt = System.currentTimeMillis() - 86400000L * 5 + 1800000L
            )
        )
        database.rideDao().insertRides(pastRides)

        // 10. Audit Log
        database.auditLogDao().insertLog(
            AuditLogEntity(
                id = "log_init",
                action = "SYSTEM_INITIALIZED",
                performedBy = "System Core",
                details = "RideGo platform database seeded with standard cities, drivers, pricing matrices and sample rides."
            )
        )
    }
}
