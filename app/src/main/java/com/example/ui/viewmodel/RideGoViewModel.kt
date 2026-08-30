package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DatabaseSeeder
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
import com.example.data.model.RideStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.data.repository.RideGoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RideGoUiState(
    val currentRole: UserRole = UserRole.RIDER,
    val currentRider: UserEntity? = null,
    val currentDriver: UserEntity? = null,
    val currentDriverProfile: DriverProfileEntity? = null,
    val selectedCity: CityEntity? = null,
    val activeRide: RideEntity? = null,
    val pendingIncomingDriverRide: RideEntity? = null,
    val nearbyDrivers: List<DriverProfileEntity> = emptyList(),
    val cities: List<CityEntity> = emptyList(),
    val pricingRules: List<PricingRuleEntity> = emptyList(),
    val activePromos: List<PromoCodeEntity> = emptyList(),
    val allPromos: List<PromoCodeEntity> = emptyList(),
    val savedPlaces: List<SavedPlaceEntity> = emptyList(),
    val emergencyContacts: List<EmergencyContactEntity> = emptyList(),
    val walletTransactions: List<WalletTransactionEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val riderHistory: List<RideEntity> = emptyList(),
    val driverHistory: List<RideEntity> = emptyList(),
    val allRides: List<RideEntity> = emptyList(),
    val allUsers: List<UserEntity> = emptyList(),
    val allDrivers: List<DriverProfileEntity> = emptyList(),
    val allTickets: List<SupportTicketEntity> = emptyList(),
    val auditLogs: List<AuditLogEntity> = emptyList()
)

class RideGoViewModel(application: Application) : AndroidViewModel(application) {
    private val db = RideGoDatabase.getDatabase(application)
    val repository = RideGoRepository(db)

    private val _currentRole = MutableStateFlow(UserRole.RIDER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _selectedCity = MutableStateFlow<CityEntity?>(null)
    val selectedCity: StateFlow<CityEntity?> = _selectedCity.asStateFlow()

    private val _uiState = MutableStateFlow(RideGoUiState())
    val uiState: StateFlow<RideGoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed database
            DatabaseSeeder.seedInitialData(db)

            // Start reactive observers
            observeDatabase()
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.getUser("user_rider_1").collect { rider ->
                _uiState.value = _uiState.value.copy(currentRider = rider)
            }
        }
        viewModelScope.launch {
            repository.getUser("user_driver_1").collect { driver ->
                _uiState.value = _uiState.value.copy(currentDriver = driver)
            }
        }
        viewModelScope.launch {
            repository.getDriverProfile("user_driver_1").collect { profile ->
                _uiState.value = _uiState.value.copy(currentDriverProfile = profile)
            }
        }
        viewModelScope.launch {
            repository.getActiveCities().collect { cities ->
                _uiState.value = _uiState.value.copy(cities = cities)
                if (_selectedCity.value == null && cities.isNotEmpty()) {
                    _selectedCity.value = cities.first()
                }
            }
        }
        viewModelScope.launch {
            repository.getActiveRideForRider().collect { activeRide ->
                _uiState.value = _uiState.value.copy(activeRide = activeRide)
            }
        }
        viewModelScope.launch {
            repository.getPendingRideRequests().collect { pendingList ->
                // If driver is online, dispatch the first pending request to them
                _uiState.value = _uiState.value.copy(pendingIncomingDriverRide = pendingList.firstOrNull())
            }
        }
        viewModelScope.launch {
            repository.getOnlineDrivers().collect { drivers ->
                _uiState.value = _uiState.value.copy(nearbyDrivers = drivers)
            }
        }
        viewModelScope.launch {
            repository.getAllPricingRules().collect { rules ->
                _uiState.value = _uiState.value.copy(pricingRules = rules)
            }
        }
        viewModelScope.launch {
            repository.getActivePromos().collect { promos ->
                _uiState.value = _uiState.value.copy(activePromos = promos)
            }
        }
        viewModelScope.launch {
            repository.getAllPromos().collect { promos ->
                _uiState.value = _uiState.value.copy(allPromos = promos)
            }
        }
        viewModelScope.launch {
            repository.getSavedPlaces("user_rider_1").collect { places ->
                _uiState.value = _uiState.value.copy(savedPlaces = places)
            }
        }
        viewModelScope.launch {
            repository.getEmergencyContacts("user_rider_1").collect { contacts ->
                _uiState.value = _uiState.value.copy(emergencyContacts = contacts)
            }
        }
        viewModelScope.launch {
            repository.getWalletTransactions("user_rider_1").collect { txs ->
                _uiState.value = _uiState.value.copy(walletTransactions = txs)
            }
        }
        viewModelScope.launch {
            repository.getNotifications("user_rider_1").collect { notifs ->
                _uiState.value = _uiState.value.copy(notifications = notifs)
            }
        }
        viewModelScope.launch {
            repository.getRiderHistory("user_rider_1").collect { history ->
                _uiState.value = _uiState.value.copy(riderHistory = history)
            }
        }
        viewModelScope.launch {
            repository.getDriverHistory("user_driver_1").collect { history ->
                _uiState.value = _uiState.value.copy(driverHistory = history)
            }
        }
        viewModelScope.launch {
            repository.getAllRides().collect { rides ->
                _uiState.value = _uiState.value.copy(allRides = rides)
            }
        }
        viewModelScope.launch {
            repository.getAllUsers().collect { users ->
                _uiState.value = _uiState.value.copy(allUsers = users)
            }
        }
        viewModelScope.launch {
            repository.getAllDriverProfiles().collect { drivers ->
                _uiState.value = _uiState.value.copy(allDrivers = drivers)
            }
        }
        viewModelScope.launch {
            repository.getAllTickets().collect { tickets ->
                _uiState.value = _uiState.value.copy(allTickets = tickets)
            }
        }
        viewModelScope.launch {
            repository.getAuditLogs().collect { logs ->
                _uiState.value = _uiState.value.copy(auditLogs = logs)
            }
        }
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        _uiState.value = _uiState.value.copy(currentRole = role)
    }

    fun selectCity(city: CityEntity) {
        _selectedCity.value = city
        _uiState.value = _uiState.value.copy(selectedCity = city)
    }

    // --- Rider Actions ---
    fun bookRide(
        pickupName: String,
        pickupLat: Double,
        pickupLng: Double,
        destName: String,
        destLat: Double,
        destLng: Double,
        category: VehicleCategory,
        paymentMethod: PaymentMethod,
        promoCode: String?
    ) {
        viewModelScope.launch {
            val cityId = _selectedCity.value?.id ?: "city_mumbai"
            val newRide = repository.createRideBooking(
                riderId = "user_rider_1",
                cityId = cityId,
                pickupName = pickupName,
                pickupLat = pickupLat,
                pickupLng = pickupLng,
                destName = destName,
                destLat = destLat,
                destLng = destLng,
                category = category,
                paymentMethod = paymentMethod,
                promoCode = promoCode
            )
            // Auto match after 2 seconds for smooth UX
            kotlinx.coroutines.delay(2000)
            repository.matchAndAssignDriver(newRide.id)
        }
    }

    fun autoMatchDriver(rideId: String) {
        viewModelScope.launch {
            repository.matchAndAssignDriver(rideId)
        }
    }

    fun advanceRideStatus(rideId: String, nextStatus: RideStatus) {
        viewModelScope.launch {
            repository.advanceRideStatus(rideId, nextStatus)
        }
    }

    fun cancelRide(rideId: String) {
        viewModelScope.launch {
            repository.cancelRide(rideId, byRider = _currentRole.value == UserRole.RIDER, reason = "User requested cancellation")
        }
    }

    fun submitRating(rideId: String, rating: Int, review: String) {
        viewModelScope.launch {
            repository.rateRide(rideId, isRider = true, rating = rating, review = review)
        }
    }

    fun addMoneyToWallet(amount: Double, method: String) {
        viewModelScope.launch {
            repository.addMoneyToWallet("user_rider_1", amount, method)
        }
    }

    fun addSavedPlace(place: SavedPlaceEntity) {
        viewModelScope.launch { repository.addSavedPlace(place) }
    }

    fun deleteSavedPlace(id: String) {
        viewModelScope.launch { repository.deleteSavedPlace(id) }
    }

    fun addEmergencyContact(contact: EmergencyContactEntity) {
        viewModelScope.launch { repository.addEmergencyContact(contact) }
    }

    fun deleteEmergencyContact(id: String) {
        viewModelScope.launch { repository.deleteEmergencyContact(id) }
    }

    fun createSupportTicket(subject: String, message: String, rideId: String?) {
        viewModelScope.launch {
            val rider = _uiState.value.currentRider
            repository.createSupportTicket(
                userId = rider?.id ?: "user_rider_1",
                userName = rider?.name ?: "Aarav Sharma",
                rideId = rideId,
                subject = subject,
                message = message
            )
        }
    }

    // --- Driver Actions ---
    fun toggleDriverOnline(isOnline: Boolean) {
        viewModelScope.launch {
            repository.setDriverOnline("user_driver_1", isOnline)
        }
    }

    fun acceptIncomingRide(rideId: String) {
        viewModelScope.launch {
            repository.matchAndAssignDriver(rideId)
        }
    }

    fun declineIncomingRide(rideId: String) {
        viewModelScope.launch {
            repository.cancelRide(rideId, byRider = false, reason = "Driver declined dispatch")
        }
    }

    fun verifyOtpAndStartRide(rideId: String, enteredOtp: String): Boolean {
        val ride = _uiState.value.activeRide ?: return false
        if (ride.otp == enteredOtp) {
            advanceRideStatus(rideId, RideStatus.RIDE_STARTED)
            return true
        }
        return false
    }

    fun instantDriverCashout() {
        viewModelScope.launch {
            repository.logAudit("DRIVER_CASHOUT", "Rajesh Kumar", "Instant payout processed to HDFC Bank ****8421")
        }
    }

    // --- Admin Actions ---
    fun updatePricingRule(rule: PricingRuleEntity) {
        viewModelScope.launch { repository.updatePricingRule(rule) }
    }

    fun updateCity(city: CityEntity) {
        viewModelScope.launch { repository.updateCity(city) }
    }

    fun addCity(city: CityEntity) {
        viewModelScope.launch { repository.addCity(city) }
    }

    fun addPromoCode(promo: PromoCodeEntity) {
        viewModelScope.launch { repository.addPromoCode(promo) }
    }

    fun updateDriverDocStatus(driverId: String, status: DriverDocStatus) {
        viewModelScope.launch { repository.updateDriverDocStatus(driverId, status) }
    }

    fun toggleUserBlocked(userId: String, isBlocked: Boolean) {
        viewModelScope.launch { repository.setUserBlocked(userId, isBlocked) }
    }

    fun resolveTicket(ticket: SupportTicketEntity, response: String) {
        viewModelScope.launch { repository.resolveTicket(ticket, response) }
    }
}
