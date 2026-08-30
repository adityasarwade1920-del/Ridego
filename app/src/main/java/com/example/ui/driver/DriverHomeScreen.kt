package com.example.ui.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DriverProfileEntity
import com.example.data.local.entity.RideEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.LatLng
import com.example.data.model.RideStatus
import com.example.data.model.UserRole
import com.example.ui.components.RideGoMapCanvas
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MintGreen
import com.example.ui.theme.SurgeAmber
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryLight
import com.example.util.GoogleMapsLauncher

@Composable
fun DriverHomeScreen(
    driverUser: UserEntity,
    driverProfile: DriverProfileEntity?,
    activeRide: RideEntity?,
    pendingIncomingRide: RideEntity?,
    onToggleOnline: (Boolean) -> Unit,
    onAcceptIncomingRide: (String) -> Unit,
    onDeclineIncomingRide: (String) -> Unit,
    onAdvanceRideStep: (String, RideStatus) -> Unit,
    onVerifyOtpAndStart: (String, String) -> Boolean,
    onRoleSwitched: (UserRole) -> Unit,
    onNavigateToEarnings: () -> Unit,
    onNavigateToProfileDocs: () -> Unit
) {
    val context = LocalContext.current
    val isOnline = driverProfile?.isOnline ?: false
    var enteredOtp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("driver_home_screen")
    ) {
        // Background Map
        RideGoMapCanvas(
            modifier = Modifier.fillMaxSize(),
            pickupLocation = if (activeRide != null) LatLng(activeRide.pickupLat, activeRide.pickupLng) else LatLng(19.0760, 72.8777),
            pickupName = activeRide?.pickupName,
            destinationLocation = if (activeRide != null) LatLng(activeRide.destLat, activeRide.destLng) else null,
            destinationName = activeRide?.destinationName,
            activeDriverProgress = if (activeRide?.status == RideStatus.RIDE_STARTED) 0.6f else 0.1f,
            rideStatus = activeRide?.status,
            isDriverView = true
        )

        // Top Status & Online Switcher Header
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) MintGreen else MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isOnline) "YOU ARE ONLINE" else "YOU ARE OFFLINE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isOnline) MintGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isOnline) "Ready to accept nearby rides" else "Go online to receive trip requests",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Online/Offline Switch
                    Switch(
                        checked = isOnline,
                        onCheckedChange = onToggleOnline,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MintGreen
                        ),
                        modifier = Modifier.testTag("driver_online_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                // Quick Navigation & Role Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TealPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.clickable { onRoleSwitched(UserRole.RIDER) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch to Rider App", style = MaterialTheme.typography.labelSmall, color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onNavigateToEarnings() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Earnings", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onNavigateToProfileDocs() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Vehicle", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Dispatch & Active Trip Area
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Case 1: Incoming Ride Dispatch Request
            if (pendingIncomingRide != null && activeRide == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("incoming_ride_dispatch_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MintGreen)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MintGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "NEW RIDE REQUEST",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MintGreen
                                )
                            }
                            Text(
                                text = "₹${pendingIncomingRide.driverEarnings} (Net Payout)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TealPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pickup Address
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Pickup", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = pendingIncomingRide.pickupName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Destination Address
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SurgeAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Destination", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = pendingIncomingRide.destinationName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Distance: ${pendingIncomingRide.distanceKm} km", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Category: ${pendingIncomingRide.vehicleCategory.displayName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(text = "Payment: ${pendingIncomingRide.paymentMethod.title}", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onDeclineIncomingRide(pendingIncomingRide.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("decline_incoming_ride_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Decline")
                            }

                            Button(
                                onClick = { onAcceptIncomingRide(pendingIncomingRide.id) },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp)
                                    .testTag("accept_incoming_ride_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Accept Ride", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else if (activeRide != null) {
                // Case 2: Active Ongoing Ride Workflow
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_active_ride_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(status = activeRide.status)
                            Text(
                                text = "Earnings: ₹${activeRide.driverEarnings}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MintGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Passenger Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(TealPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = TealPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeRide.riderName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rider Phone: ${activeRide.riderPhone ?: "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Current Target Address (Pickup or Destination)
                        val isHeadingToDrop = activeRide.status == RideStatus.RIDE_STARTED
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isHeadingToDrop) Icons.Default.LocationOn else Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = if (isHeadingToDrop) SurgeAmber else TealPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isHeadingToDrop) "Navigate to Destination" else "Navigate to Pickup Location",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (isHeadingToDrop) activeRide.destinationName else activeRide.pickupName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        if (isHeadingToDrop) {
                                            GoogleMapsLauncher.openRouteInGoogleMaps(
                                                context = context,
                                                pickupLat = activeRide.pickupLat,
                                                pickupLng = activeRide.pickupLng,
                                                destLat = activeRide.destLat,
                                                destLng = activeRide.destLng,
                                                pickupName = activeRide.pickupName,
                                                destName = activeRide.destinationName
                                            )
                                        } else {
                                            GoogleMapsLauncher.openLocationInGoogleMaps(
                                                context = context,
                                                lat = activeRide.pickupLat,
                                                lng = activeRide.pickupLng,
                                                label = activeRide.pickupName
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary),
                                    modifier = Modifier.testTag("driver_google_maps_nav_btn")
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Maps Nav", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // OTP Entry field if driver has arrived
                        if (activeRide.status == RideStatus.DRIVER_ARRIVED) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Enter 4-Digit Passenger PIN to Start Trip",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = enteredOtp,
                                onValueChange = {
                                    enteredOtp = it.filter { ch -> ch.isDigit() }.take(4)
                                    otpError = null
                                },
                                label = { Text("Passenger PIN (e.g. ${activeRide.otp})") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("driver_otp_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (otpError != null) {
                                Text(
                                    text = otpError ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AlertRed,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Workflow Step CTA Button
                        when (activeRide.status) {
                            RideStatus.DRIVER_ASSIGNED -> {
                                Button(
                                    onClick = { onAdvanceRideStep(activeRide.id, RideStatus.DRIVER_ARRIVING) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("driver_start_pickup_nav_btn"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Head to Pickup Location", fontWeight = FontWeight.Bold)
                                }
                            }
                            RideStatus.DRIVER_ARRIVING -> {
                                Button(
                                    onClick = { onAdvanceRideStep(activeRide.id, RideStatus.DRIVER_ARRIVED) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("driver_confirm_arrived_btn"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurgeAmber)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("I Have Arrived at Pickup", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                            RideStatus.DRIVER_ARRIVED -> {
                                Button(
                                    onClick = {
                                        if (enteredOtp.length == 4) {
                                            val success = onVerifyOtpAndStart(activeRide.id, enteredOtp)
                                            if (!success) {
                                                otpError = "Incorrect PIN! Passenger PIN is ${activeRide.otp}"
                                            }
                                        } else {
                                            otpError = "Please enter complete 4-digit PIN"
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("driver_verify_otp_start_btn"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verify PIN & Start Trip", fontWeight = FontWeight.Bold)
                                }
                            }
                            RideStatus.RIDE_STARTED -> {
                                Button(
                                    onClick = { onAdvanceRideStep(activeRide.id, RideStatus.COMPLETED) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("driver_complete_trip_btn"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Complete Trip & Collect Payment", fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            } else {
                // Case 3: Driver Idle Dashboard Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_idle_dashboard_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Today's Payouts",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${driverProfile?.todayEarnings ?: 0.0}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MintGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = SurgeAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${driverProfile?.rating ?: 4.9}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${driverProfile?.totalRides ?: 0} Total Trips",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // High Demand Hotspots Notice
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SurgeAmber.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SurgeAmber)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "High Surge in BKC & Airport T2 (1.5x)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SurgeAmber
                                    )
                                    Text(
                                        text = "Head towards airport zone for higher earnings",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
