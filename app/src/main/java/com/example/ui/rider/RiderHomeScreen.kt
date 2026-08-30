package com.example.ui.rider

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CityEntity
import com.example.data.local.entity.DriverProfileEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PricingRuleEntity
import com.example.data.local.entity.PromoCodeEntity
import com.example.data.local.entity.RideEntity
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.LatLng
import com.example.data.model.PaymentMethod
import com.example.data.model.RideStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.domain.RouteSimulationEngine
import com.example.ui.components.RideGoMapCanvas
import com.example.ui.components.SosEmergencyDialog
import com.example.ui.theme.DarkInk
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PunchyRed
import com.example.ui.theme.SoftBlueContainer
import com.example.ui.theme.SurgeAmber
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryLight

enum class RiderUiState {
    IDLE,
    SEARCHING_LOCATIONS,
    SELECTING_VEHICLE,
    MATCHING_DRIVER,
    ACTIVE_RIDE,
    PAYMENT_RATING
}

@Composable
fun RiderHomeScreen(
    currentUser: UserEntity,
    activeRide: RideEntity?,
    cities: List<CityEntity>,
    selectedCity: CityEntity?,
    pricingRules: List<PricingRuleEntity>,
    activePromos: List<PromoCodeEntity>,
    savedPlaces: List<SavedPlaceEntity>,
    nearbyDrivers: List<DriverProfileEntity>,
    notifications: List<NotificationEntity>,
    onSelectCity: (CityEntity) -> Unit,
    onRoleSwitched: (UserRole) -> Unit,
    onBookRide: (pickupName: String, pickupLat: Double, pickupLng: Double, destName: String, destLat: Double, destLng: Double, category: VehicleCategory, payment: PaymentMethod, promo: String?) -> Unit,
    onMatchDriver: (String) -> Unit,
    onAdvanceRideStatus: (String, RideStatus) -> Unit,
    onCancelRide: (String) -> Unit,
    onSubmitRating: (String, Int, String) -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSafety: () -> Unit,
    onNavigateToSavedPlaces: () -> Unit,
    onNavigateToSupport: () -> Unit
) {
    var uiState by remember { mutableStateOf(RiderUiState.IDLE) }
    var showCityDropdown by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }

    // Draft Booking Inputs
    var draftPickupName by remember { mutableStateOf("Bandra West Promenade, Mumbai") }
    var draftPickupLat by remember { mutableFloatStateOf(19.0596f) }
    var draftPickupLng by remember { mutableFloatStateOf(72.8295f) }

    var draftDestName by remember { mutableStateOf("BKC Tech Park Tower 3") }
    var draftDestLat by remember { mutableFloatStateOf(19.0657f) }
    var draftDestLng by remember { mutableFloatStateOf(72.8687f) }

    var activeDriverProgress by remember { mutableFloatStateOf(0.1f) }

    // Sync UI State with active ride from repository
    val effectiveUiState = when {
        activeRide == null -> if (uiState == RiderUiState.MATCHING_DRIVER || uiState == RiderUiState.ACTIVE_RIDE || uiState == RiderUiState.PAYMENT_RATING) RiderUiState.IDLE else uiState
        activeRide.status == RideStatus.SEARCHING_DRIVER -> RiderUiState.MATCHING_DRIVER
        activeRide.status in listOf(RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_ARRIVING, RideStatus.DRIVER_ARRIVED, RideStatus.RIDE_STARTED) -> RiderUiState.ACTIVE_RIDE
        activeRide.status in listOf(RideStatus.RIDE_COMPLETED, RideStatus.COMPLETED, RideStatus.PAYMENT_PENDING) -> RiderUiState.PAYMENT_RATING
        else -> RiderUiState.IDLE
    }

    // Compute active progress along route for map animation
    activeDriverProgress = when (activeRide?.status) {
        RideStatus.DRIVER_ASSIGNED -> 0.05f
        RideStatus.DRIVER_ARRIVING -> 0.35f
        RideStatus.DRIVER_ARRIVED -> 0.0f // at pickup
        RideStatus.RIDE_STARTED -> 0.65f // mid trip
        RideStatus.COMPLETED, RideStatus.RIDE_COMPLETED -> 1.0f
        else -> 0.0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("rider_home_screen")
    ) {
        // 1. Vector Map Canvas as Interactive Background
        RideGoMapCanvas(
            modifier = Modifier.fillMaxSize(),
            pickupLocation = LatLng(draftPickupLat.toDouble(), draftPickupLng.toDouble()),
            pickupName = if (effectiveUiState != RiderUiState.IDLE) draftPickupName else null,
            destinationLocation = if (effectiveUiState != RiderUiState.IDLE && effectiveUiState != RiderUiState.SEARCHING_LOCATIONS)
                LatLng(draftDestLat.toDouble(), draftDestLng.toDouble()) else null,
            destinationName = if (effectiveUiState != RiderUiState.IDLE && effectiveUiState != RiderUiState.SEARCHING_LOCATIONS) draftDestName else null,
            nearbyDrivers = nearbyDrivers,
            activeDriverProgress = activeDriverProgress,
            rideStatus = activeRide?.status
        )

        // 2. Top Header Bar (City Switcher, Role Badge Switcher, Quick Access)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand and City Selector
                Column {
                    Text(
                        text = "RIDEGO",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.0.sp
                    )
                    Box {
                        Row(
                            modifier = Modifier
                                .clickable { showCityDropdown = true }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (selectedCity?.name ?: "Mumbai").uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.LocationCity, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showCityDropdown,
                            onDismissRequest = { showCityDropdown = false }
                        ) {
                            cities.forEach { city ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${city.name} (${city.country})",
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    onClick = {
                                        onSelectCity(city)
                                        showCityDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Center Role Switcher Quick Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftBlueContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable { onRoleSwitched(UserRole.DRIVER) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "DRIVER MODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.0.sp
                        )
                    }
                }

                // Right Quick Navigation Icons (Wallet & History)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToWallet,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 3. Bottom Interface Overlay based on effective State
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            when (effectiveUiState) {
                RiderUiState.IDLE -> {
                    // Home Idle Search & Quick Actions Card with Bold Typography
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("rider_idle_card"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // "Where to?" Search Pill Button
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { uiState = RiderUiState.SEARCHING_LOCATIONS }
                                    .testTag("where_to_search_bar"),
                                shape = RoundedCornerShape(18.dp),
                                color = SoftBlueContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Where are you going?",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkInk
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Quick Shortcuts Row (Saved Places, Safety, Support)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                QuickActionIcon(
                                    icon = Icons.Default.Home,
                                    label = "SAVED",
                                    onClick = onNavigateToSavedPlaces
                                )
                                QuickActionIcon(
                                    icon = Icons.Default.AccountBalanceWallet,
                                    label = "₹${currentUser.walletBalance.toInt()}",
                                    onClick = onNavigateToWallet
                                )
                                QuickActionIcon(
                                    icon = Icons.Default.Security,
                                    label = "SAFETY SOS",
                                    tint = PunchyRed,
                                    onClick = onNavigateToSafety
                                )
                                QuickActionIcon(
                                    icon = Icons.Default.SupportAgent,
                                    label = "HELP",
                                    onClick = onNavigateToSupport
                                )
                            }
                        }
                    }
                }

                RiderUiState.SEARCHING_LOCATIONS -> {
                    LocationSearchSheet(
                        initialPickup = draftPickupName,
                        savedPlaces = savedPlaces,
                        onLocationsConfirmed = { pName, pLat, pLng, dName, dLat, dLng ->
                            draftPickupName = pName
                            draftPickupLat = pLat.toFloat()
                            draftPickupLng = pLng.toFloat()
                            draftDestName = dName
                            draftDestLat = dLat.toFloat()
                            draftDestLng = dLng.toFloat()
                            uiState = RiderUiState.SELECTING_VEHICLE
                        },
                        onDismiss = { uiState = RiderUiState.IDLE }
                    )
                }

                RiderUiState.SELECTING_VEHICLE -> {
                    VehicleSelectionSheet(
                        city = selectedCity,
                        pricingRules = pricingRules,
                        activePromos = activePromos,
                        walletBalance = currentUser.walletBalance,
                        pickupName = draftPickupName,
                        pickupLat = draftPickupLat.toDouble(),
                        pickupLng = draftPickupLng.toDouble(),
                        destinationName = draftDestName,
                        destLat = draftDestLat.toDouble(),
                        destLng = draftDestLng.toDouble(),
                        onConfirmBooking = { category, paymentMethod, promoCode ->
                            onBookRide(
                                draftPickupName,
                                draftPickupLat.toDouble(),
                                draftPickupLng.toDouble(),
                                draftDestName,
                                draftDestLat.toDouble(),
                                draftDestLng.toDouble(),
                                category,
                                paymentMethod,
                                promoCode
                            )
                        },
                        onDismiss = { uiState = RiderUiState.IDLE }
                    )
                }

                RiderUiState.MATCHING_DRIVER -> {
                    if (activeRide != null) {
                        DriverSearchMatchingView(
                            ride = activeRide,
                            onCancelRide = { onCancelRide(activeRide.id) },
                            onSimulateDriverAccept = { onMatchDriver(activeRide.id) }
                        )
                    }
                }

                RiderUiState.ACTIVE_RIDE -> {
                    if (activeRide != null) {
                        ActiveRideView(
                            ride = activeRide,
                            onAdvanceSimulation = {
                                val nextStatus = when (activeRide.status) {
                                    RideStatus.DRIVER_ASSIGNED -> RideStatus.DRIVER_ARRIVING
                                    RideStatus.DRIVER_ARRIVING -> RideStatus.DRIVER_ARRIVED
                                    RideStatus.DRIVER_ARRIVED -> RideStatus.RIDE_STARTED
                                    RideStatus.RIDE_STARTED -> RideStatus.COMPLETED
                                    else -> RideStatus.COMPLETED
                                }
                                onAdvanceRideStatus(activeRide.id, nextStatus)
                            },
                            onCancelRide = { onCancelRide(activeRide.id) },
                            onOpenSos = { showSosDialog = true },
                            onCallDriver = { /* call action */ }
                        )
                    }
                }

                RiderUiState.PAYMENT_RATING -> {
                    if (activeRide != null) {
                        PaymentAndReceiptSheet(
                            ride = activeRide,
                            onSubmitRating = { rating, review ->
                                onSubmitRating(activeRide.id, rating, review)
                                uiState = RiderUiState.IDLE
                            },
                            onDismiss = { uiState = RiderUiState.IDLE }
                        )
                    }
                }
            }
        }

        // SOS Dialog
        if (showSosDialog) {
            SosEmergencyDialog(
                rideId = activeRide?.id ?: "RG-LIVE",
                emergencyContactPhone = currentUser.emergencyContactPhone ?: "9876500000",
                onDismiss = { showSosDialog = false },
                onShareTrip = { showSosDialog = false },
                onDialEmergency = { showSosDialog = false }
            )
        }
    }
}

@Composable
private fun QuickActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = TealPrimary,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = tint.copy(alpha = 0.12f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
