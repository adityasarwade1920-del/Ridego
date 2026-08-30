package com.example.domain

import com.example.data.model.LatLng
import com.example.data.model.RoutePoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object RouteSimulationEngine {

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c
        return if (distance < 0.2) 1.5 else distance
    }

    fun estimateDurationMinutes(distanceKm: Double): Int {
        // Average city traffic speed ~ 22 km/h
        val hours = distanceKm / 22.0
        val mins = (hours * 60).toInt()
        return if (mins < 4) 5 else mins
    }

    fun generateRoutePoints(start: LatLng, end: LatLng, stepsCount: Int = 20): List<RoutePoint> {
        val points = mutableListOf<RoutePoint>()
        // Generate realistic street polyline with slight zig-zag curve simulating city roads
        val midLat = (start.latitude + end.latitude) / 2.0
        val midLng = (start.longitude + end.longitude) / 2.0
        val perpLat = -(end.longitude - start.longitude) * 0.15
        val perpLng = (end.latitude - start.latitude) * 0.15

        for (i in 0..stepsCount) {
            val t = i.toDouble() / stepsCount.toDouble()
            // Quadratic Bezier interpolation with slight road-like jitter
            val lat = (1 - t) * (1 - t) * start.latitude + 2 * (1 - t) * t * (midLat + perpLat) + t * t * end.latitude
            val lng = (1 - t) * (1 - t) * start.longitude + 2 * (1 - t) * t * (midLng + perpLng) + t * t * end.longitude
            points.add(RoutePoint(lat, lng))
        }
        return points
    }

    fun calculateBearing(start: LatLng, end: LatLng): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val bearing = Math.toDegrees(atan2(y, x))
        return ((bearing + 360) % 360).toFloat()
    }
}
