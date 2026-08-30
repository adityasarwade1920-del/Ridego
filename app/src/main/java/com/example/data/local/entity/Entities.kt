package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DriverDocStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.RideStatus
import com.example.data.model.TicketStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val role: UserRole,
    val name: String,
    val phone: String,
    val email: String,
    val profilePhotoUrl: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val rating: Double = 4.9,
    val isBlocked: Boolean = false,
    val isApproved: Boolean = true,
    val walletBalance: Double = 500.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "driver_profiles")
data class DriverProfileEntity(
    @PrimaryKey val driverId: String,
    val vehicleModel: String,
    val vehicleNumber: String,
    val vehicleCategory: VehicleCategory,
    val vehicleColor: String = "Pearl White",
    val licenseNumber: String,
    val govtIdNumber: String = "GOV-8829-X",
    val insurancePolicy: String = "INS-99201-POL",
    val docStatus: DriverDocStatus = DriverDocStatus.APPROVED,
    val isOnline: Boolean = true,
    val currentLat: Double,
    val currentLng: Double,
    val totalRides: Int = 142,
    val rating: Double = 4.85,
    val todayEarnings: Double = 1850.0,
    val weeklyEarnings: Double = 12400.0,
    val totalEarnings: Double = 98500.0,
    val acceptanceRate: Double = 94.0
)

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val state: String,
    val country: String,
    val currencySymbol: String = "₹",
    val currencyCode: String = "INR",
    val isActive: Boolean = true,
    val baseSurge: Double = 1.0,
    val centerLat: Double = 19.0760,
    val centerLng: Double = 72.8777
)

@Entity(tableName = "pricing_rules")
data class PricingRuleEntity(
    @PrimaryKey val id: String,
    val cityId: String,
    val vehicleCategory: VehicleCategory,
    val baseFare: Double,
    val perKmRate: Double,
    val perMinuteRate: Double,
    val minFare: Double,
    val bookingFee: Double = 15.0,
    val taxPercent: Double = 5.0,
    val cancellationFee: Double = 50.0,
    val freeCancellationWindowSeconds: Int = 180,
    val platformCommissionPercent: Double = 18.0,
    val surgeMultiplier: Double = 1.0
)

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey val id: String,
    val riderId: String,
    val riderName: String,
    val riderPhone: String,
    val riderRating: Double = 4.9,
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val driverRating: Double? = null,
    val vehicleModel: String? = null,
    val vehicleNumber: String? = null,
    val vehicleColor: String? = null,
    val vehicleCategory: VehicleCategory,
    val cityId: String,
    val pickupName: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val destinationName: String,
    val destLat: Double,
    val destLng: Double,
    val distanceKm: Double,
    val durationMinutes: Int,
    val baseFare: Double,
    val distanceFare: Double,
    val timeFare: Double,
    val bookingFee: Double,
    val surgeMultiplier: Double,
    val discountAmount: Double = 0.0,
    val taxAmount: Double,
    val totalFare: Double,
    val driverEarnings: Double,
    val platformCommission: Double,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val status: RideStatus = RideStatus.REQUESTED,
    val promoCode: String? = null,
    val otp: String = "4821",
    val riderGivenRating: Int? = null,
    val riderGivenReview: String? = null,
    val driverGivenRating: Int? = null,
    val driverGivenReview: String? = null,
    val driverCurrentLat: Double? = null,
    val driverCurrentLng: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val cancellationReason: String? = null
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Double,
    val type: TransactionType,
    val title: String,
    val description: String,
    val referenceId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "promo_codes")
data class PromoCodeEntity(
    @PrimaryKey val code: String,
    val title: String,
    val description: String,
    val discountPercent: Double = 0.0,
    val fixedDiscount: Double = 0.0,
    val maxDiscount: Double = 100.0,
    val minRideValue: Double = 100.0,
    val cityId: String = "all",
    val expiryTimestamp: Long = System.currentTimeMillis() + (30L * 24 * 3600 * 1000),
    val usageLimit: Int = 1000,
    val timesUsed: Int = 42,
    val isActive: Boolean = true
)

@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val iconType: String = "home" // home, work, airport, favorite
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val phone: String,
    val relationship: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val rideId: String? = null,
    val subject: String,
    val message: String,
    val status: TicketStatus = TicketStatus.OPEN,
    val adminResponse: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val action: String,
    val performedBy: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
