package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object GoogleMapsLauncher {

    /**
     * Opens Google Maps with turn-by-turn driving directions between origin and destination.
     */
    fun openRouteInGoogleMaps(
        context: Context,
        pickupLat: Double,
        pickupLng: Double,
        destLat: Double,
        destLng: Double,
        pickupName: String = "Pickup",
        destName: String = "Destination"
    ) {
        try {
            // Google Maps Directions intent format
            val uriString = "https://www.google.com/maps/dir/?api=1&origin=$pickupLat,$pickupLng&destination=$destLat,$destLng&travelmode=driving"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                // Attempt to target Google Maps app first
                setPackage("com.google.android.apps.maps")
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback to web browser or any map provider
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            }
        } catch (e: Exception) {
            // Fallback to generic geo intent
            try {
                val geoUri = Uri.parse("geo:$destLat,$destLng?q=$destLat,$destLng(${Uri.encode(destName)})")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (err: Exception) {
                Toast.makeText(context, "Could not open Google Maps", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Opens Google Maps pinned at a specific location.
     */
    fun openLocationInGoogleMaps(
        context: Context,
        lat: Double,
        lng: Double,
        label: String = "Location"
    ) {
        try {
            val uriString = "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.google.android.apps.maps")
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Google Maps", Toast.LENGTH_SHORT).show()
        }
    }
}
