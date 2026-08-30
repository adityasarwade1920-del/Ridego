package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import com.example.data.model.RideStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdOnce(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role ORDER BY createdAt DESC")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET walletBalance = walletBalance + :amount WHERE id = :userId")
    suspend fun updateWalletBalance(userId: String, amount: Double)

    @Query("UPDATE users SET isBlocked = :isBlocked WHERE id = :userId")
    suspend fun setUserBlocked(userId: String, isBlocked: Boolean)
}

@Dao
interface DriverProfileDao {
    @Query("SELECT * FROM driver_profiles WHERE driverId = :driverId")
    fun getDriverProfile(driverId: String): Flow<DriverProfileEntity?>

    @Query("SELECT * FROM driver_profiles WHERE driverId = :driverId")
    suspend fun getDriverProfileOnce(driverId: String): DriverProfileEntity?

    @Query("SELECT * FROM driver_profiles")
    fun getAllDrivers(): Flow<List<DriverProfileEntity>>

    @Query("SELECT * FROM driver_profiles WHERE isOnline = 1 AND docStatus = 'APPROVED'")
    fun getOnlineAvailableDrivers(): Flow<List<DriverProfileEntity>>

    @Query("SELECT * FROM driver_profiles WHERE isOnline = 1 AND docStatus = 'APPROVED' AND vehicleCategory = :category")
    suspend fun getOnlineDriversByCategory(category: VehicleCategory): List<DriverProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriverProfile(driver: DriverProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriverProfiles(drivers: List<DriverProfileEntity>)

    @Update
    suspend fun updateDriverProfile(driver: DriverProfileEntity)

    @Query("UPDATE driver_profiles SET isOnline = :isOnline WHERE driverId = :driverId")
    suspend fun setDriverOnline(driverId: String, isOnline: Boolean)

    @Query("UPDATE driver_profiles SET docStatus = :status WHERE driverId = :driverId")
    suspend fun updateDriverDocStatus(driverId: String, status: DriverDocStatus)

    @Query("UPDATE driver_profiles SET currentLat = :lat, currentLng = :lng WHERE driverId = :driverId")
    suspend fun updateDriverLocation(driverId: String, lat: Double, lng: Double)

    @Query("UPDATE driver_profiles SET todayEarnings = todayEarnings + :amount, totalEarnings = totalEarnings + :amount, totalRides = totalRides + 1 WHERE driverId = :driverId")
    suspend fun recordDriverEarnings(driverId: String, amount: Double)
}

@Dao
interface RideDao {
    @Query("SELECT * FROM rides WHERE id = :id")
    fun getRideById(id: String): Flow<RideEntity?>

    @Query("SELECT * FROM rides WHERE id = :id")
    suspend fun getRideByIdOnce(id: String): RideEntity?

    @Query("SELECT * FROM rides WHERE riderId = :riderId ORDER BY createdAt DESC")
    fun getRiderHistory(riderId: String): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE driverId = :driverId ORDER BY createdAt DESC")
    fun getDriverHistory(driverId: String): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides ORDER BY createdAt DESC")
    fun getAllRides(): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE status NOT IN ('COMPLETED', 'CANCELLED_BY_RIDER', 'CANCELLED_BY_DRIVER', 'NO_DRIVER_FOUND', 'PAYMENT_FAILED') ORDER BY createdAt DESC LIMIT 1")
    fun getActiveRideForRider(): Flow<RideEntity?>

    @Query("SELECT * FROM rides WHERE driverId = :driverId AND status NOT IN ('COMPLETED', 'CANCELLED_BY_RIDER', 'CANCELLED_BY_DRIVER', 'NO_DRIVER_FOUND', 'PAYMENT_FAILED') ORDER BY createdAt DESC LIMIT 1")
    fun getActiveRideForDriver(driverId: String): Flow<RideEntity?>

    @Query("SELECT * FROM rides WHERE status = 'REQUESTED' OR status = 'SEARCHING_DRIVER' ORDER BY createdAt DESC")
    fun getPendingRideRequests(): Flow<List<RideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRides(rides: List<RideEntity>)

    @Update
    suspend fun updateRide(ride: RideEntity)

    @Query("UPDATE rides SET status = :status WHERE id = :rideId")
    suspend fun updateRideStatus(rideId: String, status: RideStatus)

    @Query("UPDATE rides SET driverCurrentLat = :lat, driverCurrentLng = :lng WHERE id = :rideId")
    suspend fun updateRideDriverLocation(rideId: String, lat: Double, lng: Double)
}

@Dao
interface CityDao {
    @Query("SELECT * FROM cities WHERE isActive = 1")
    fun getActiveCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities")
    fun getAllCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE id = :id")
    suspend fun getCityById(id: String): CityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<CityEntity>)

    @Update
    suspend fun updateCity(city: CityEntity)
}

@Dao
interface PricingRuleDao {
    @Query("SELECT * FROM pricing_rules WHERE cityId = :cityId")
    fun getPricingRulesForCity(cityId: String): Flow<List<PricingRuleEntity>>

    @Query("SELECT * FROM pricing_rules")
    fun getAllPricingRules(): Flow<List<PricingRuleEntity>>

    @Query("SELECT * FROM pricing_rules WHERE cityId = :cityId AND vehicleCategory = :category LIMIT 1")
    suspend fun getPricingRule(cityId: String, category: VehicleCategory): PricingRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPricingRule(rule: PricingRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPricingRules(rules: List<PricingRuleEntity>)

    @Update
    suspend fun updatePricingRule(rule: PricingRuleEntity)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<WalletTransactionEntity>)
}

@Dao
interface PromoCodeDao {
    @Query("SELECT * FROM promo_codes WHERE isActive = 1")
    fun getActivePromoCodes(): Flow<List<PromoCodeEntity>>

    @Query("SELECT * FROM promo_codes")
    fun getAllPromoCodes(): Flow<List<PromoCodeEntity>>

    @Query("SELECT * FROM promo_codes WHERE code = :code LIMIT 1")
    suspend fun getPromoCode(code: String): PromoCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromoCode(promo: PromoCodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromoCodes(promos: List<PromoCodeEntity>)

    @Update
    suspend fun updatePromoCode(promo: PromoCodeEntity)
}

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places WHERE userId = :userId")
    fun getSavedPlaces(userId: String): Flow<List<SavedPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPlace(place: SavedPlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPlaces(places: List<SavedPlaceEntity>)

    @Query("DELETE FROM saved_places WHERE id = :id")
    suspend fun deleteSavedPlace(id: String)
}

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId")
    fun getContacts(userId: String): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<EmergencyContactEntity>)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteContact(id: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllRead(userId: String)
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTicketsForUser(userId: String): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}
