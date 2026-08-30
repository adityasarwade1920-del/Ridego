package com.example.domain

import com.example.data.local.entity.DriverProfileEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.DriverDocStatus
import com.example.data.model.VehicleCategory
import kotlin.math.roundToInt

data class MatchedDriverCandidate(
    val driverProfile: DriverProfileEntity,
    val user: UserEntity?,
    val distanceKm: Double,
    val etaMinutes: Int,
    val matchScore: Double
)

object DriverMatchingEngine {

    fun rankCandidates(
        onlineDrivers: List<Pair<DriverProfileEntity, UserEntity?>>,
        pickupLat: Double,
        pickupLng: Double,
        requestedCategory: VehicleCategory
    ): List<MatchedDriverCandidate> {
        return onlineDrivers
            .filter { (driver, _) ->
                driver.isOnline &&
                driver.docStatus == DriverDocStatus.APPROVED &&
                driver.vehicleCategory == requestedCategory
            }
            .map { (driver, user) ->
                val distance = RouteSimulationEngine.calculateDistanceKm(
                    pickupLat, pickupLng,
                    driver.currentLat, driver.currentLng
                )
                val eta = RouteSimulationEngine.estimateDurationMinutes(distance)
                // Score based on proximity and driver rating
                val proximityScore = (10.0 - distance).coerceAtLeast(0.0) * 10
                val ratingScore = driver.rating * 15
                val score = proximityScore + ratingScore

                MatchedDriverCandidate(
                    driverProfile = driver,
                    user = user,
                    distanceKm = (distance * 10).roundToInt() / 10.0,
                    etaMinutes = eta.coerceAtLeast(2),
                    matchScore = score
                )
            }
            .sortedByDescending { it.matchScore }
    }
}
