package com.example.data.model

enum class UserRole {
    RIDER,
    DRIVER,
    ADMIN
}

enum class VehicleCategory(
    val displayName: String,
    val description: String,
    val capacity: Int,
    val iconName: String,
    val baseMultiplier: Double
) {
    AUTO("Auto Rickshaw", "Affordable 3-seater auto for quick city travel", 3, "auto_rickshaw", 0.8),
    ECONOMY("Economy", "Affordable everyday rides (₹25/km standard)", 4, "car_compact", 1.0),
    COMFORT("Comfort", "Spacious top-rated cars & AC", 4, "car_sedan", 1.2),
    PREMIUM("Premium", "Luxury vehicles & executive rides", 4, "car_luxury", 1.6),
    XL("RideGo XL", "Spacious SUVs for groups & luggage", 6, "car_suv", 1.4)
}

enum class RideStatus(val label: String, val stepIndex: Int) {
    REQUESTED("Requested", 0),
    SEARCHING_DRIVER("Searching Nearby Drivers", 1),
    DRIVER_ASSIGNED("Driver Assigned", 2),
    DRIVER_ARRIVING("Driver on the Way", 3),
    DRIVER_ARRIVED("Driver Arrived at Pickup", 4),
    RIDE_STARTED("On the Way to Destination", 5),
    RIDE_COMPLETED("Arrived at Destination", 6),
    PAYMENT_PENDING("Payment Pending", 7),
    COMPLETED("Ride Completed", 8),
    CANCELLED_BY_RIDER("Cancelled by Rider", -1),
    CANCELLED_BY_DRIVER("Cancelled by Driver", -1),
    NO_DRIVER_FOUND("No Drivers Found", -1),
    PAYMENT_FAILED("Payment Failed", -1)
}

enum class PaymentMethod(val title: String, val subtitle: String) {
    WALLET("RideGo Wallet", "Instant one-tap payment"),
    UPI("UPI (GPay / PhonePe / Paytm)", "Direct bank transfer via UPI"),
    CARD("Credit / Debit Card", "Visa, Mastercard, RuPay"),
    NET_BANKING("Net Banking", "All major banks"),
    CASH("Cash", "Pay cash directly to driver")
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}

enum class DriverDocStatus(val label: String) {
    PENDING("Pending Verification"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    SUSPENDED("Suspended")
}

enum class TransactionType {
    CREDIT,
    DEBIT
}

enum class TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED
}

data class LatLng(val latitude: Double, val longitude: Double)

data class RoutePoint(
    val lat: Double,
    val lng: Double,
    val label: String = ""
)

data class FareBreakdown(
    val baseFare: Double,
    val distanceKm: Double,
    val distanceCharge: Double,
    val timeMinutes: Int,
    val timeCharge: Double,
    val bookingFee: Double,
    val surgeMultiplier: Double,
    val surgeAmount: Double,
    val taxes: Double,
    val discountAmount: Double,
    val totalFare: Double,
    val platformCommission: Double,
    val driverEarnings: Double
)
