package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.UserRole
import com.example.ui.admin.AdminAuthDialog
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.admin.AdminSecurityManager
import com.example.ui.components.RoleBadge
import com.example.ui.driver.DriverEarningsScreen
import com.example.ui.driver.DriverHomeScreen
import com.example.ui.driver.DriverProfileVerificationScreen
import com.example.ui.rider.RiderHomeScreen
import com.example.ui.rider.RiderRideHistoryScreen
import com.example.ui.rider.RiderWalletScreen
import com.example.ui.rider.SafetyCenterScreen
import com.example.ui.rider.SavedPlacesScreen
import com.example.ui.rider.SupportScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.MintGreen
import com.example.ui.theme.SurgeAmber
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.RideGoViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object RiderHome : Screen("rider_home", "Book Ride", Icons.Default.DirectionsCar)
    object RiderWallet : Screen("rider_wallet", "Wallet", Icons.Default.AccountBalanceWallet)
    object RiderHistory : Screen("rider_history", "Trips", Icons.Default.History)
    object RiderSafety : Screen("rider_safety", "Safety SOS", Icons.Default.Security)
    object RiderSavedPlaces : Screen("rider_saved_places", "Saved Places", Icons.Default.Home)
    object RiderSupport : Screen("rider_support", "Support", Icons.Default.SupportAgent)

    object DriverHome : Screen("driver_home", "Dispatch", Icons.Default.DirectionsCar)
    object DriverEarnings : Screen("driver_earnings", "Earnings", Icons.Default.AccountBalanceWallet)
    object DriverProfileDocs : Screen("driver_profile_docs", "Vehicle & KYC", Icons.Default.Person)

    object AdminConsole : Screen("admin_console", "Admin Console", Icons.Default.AdminPanelSettings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideGoApp(viewModel: RideGoViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val navController = rememberNavController()

    var showRoleSwitchDialog by remember { mutableStateOf(false) }
    var showAdminAuthDialog by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf(Screen.RiderHome.route) }

    fun navigateToAdminDirectly() {
        viewModel.switchRole(UserRole.ADMIN)
        currentRoute = Screen.AdminConsole.route
        navController.navigate(Screen.AdminConsole.route)
    }

    fun requestAdminAccess() {
        if (AdminSecurityManager.isAuthenticated(context)) {
            navigateToAdminDirectly()
        } else {
            showAdminAuthDialog = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Contextual Navigation Bar based on active role
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                when (currentRole) {
                    UserRole.RIDER -> {
                        val riderTabs = listOf(
                            Screen.RiderHome,
                            Screen.RiderWallet,
                            Screen.RiderHistory,
                            Screen.RiderSafety,
                            Screen.RiderSupport
                        )
                        riderTabs.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    currentRoute = screen.route
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.RiderHome.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TealPrimary,
                                    selectedTextColor = TealPrimary,
                                    indicatorColor = TealPrimary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }

                    UserRole.DRIVER -> {
                        val driverTabs = listOf(
                            Screen.DriverHome,
                            Screen.DriverEarnings,
                            Screen.DriverProfileDocs
                        )
                        driverTabs.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    currentRoute = screen.route
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.DriverHome.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MintGreen,
                                    selectedTextColor = MintGreen,
                                    indicatorColor = MintGreen.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }

                    UserRole.ADMIN -> {
                        NavigationBarItem(
                            selected = currentRoute == Screen.AdminConsole.route,
                            onClick = {
                                requestAdminAccess()
                            },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Console") },
                            label = { Text("Master Console", style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SurgeAmber,
                                selectedTextColor = SurgeAmber,
                                indicatorColor = SurgeAmber.copy(alpha = 0.15f)
                            )
                        )
                    }
                }

                // Global Role Switcher Navigation Item
                NavigationBarItem(
                    selected = false,
                    onClick = { showRoleSwitchDialog = true },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Mode", tint = TealPrimary) },
                    label = { Text("Switch", style = MaterialTheme.typography.labelSmall, color = TealPrimary) }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = when (currentRole) {
                    UserRole.RIDER -> Screen.RiderHome.route
                    UserRole.DRIVER -> Screen.DriverHome.route
                    UserRole.ADMIN -> Screen.AdminConsole.route
                }
            ) {
                // --- Rider Routes ---
                composable(Screen.RiderHome.route) {
                    if (uiState.currentRider != null) {
                        RiderHomeScreen(
                            currentUser = uiState.currentRider!!,
                            activeRide = uiState.activeRide,
                            cities = uiState.cities,
                            selectedCity = uiState.selectedCity,
                            pricingRules = uiState.pricingRules,
                            activePromos = uiState.activePromos,
                            savedPlaces = uiState.savedPlaces,
                            nearbyDrivers = uiState.nearbyDrivers,
                            notifications = uiState.notifications,
                            onSelectCity = { viewModel.selectCity(it) },
                            onRoleSwitched = { role ->
                                if (role == UserRole.ADMIN) {
                                    requestAdminAccess()
                                } else {
                                    viewModel.switchRole(role)
                                    navController.navigate(Screen.DriverHome.route)
                                }
                            },
                            onBookRide = { pName, pLat, pLng, dName, dLat, dLng, cat, pay, promo ->
                                viewModel.bookRide(pName, pLat, pLng, dName, dLat, dLng, cat, pay, promo)
                            },
                            onMatchDriver = { viewModel.autoMatchDriver(it) },
                            onAdvanceRideStatus = { id, next -> viewModel.advanceRideStatus(id, next) },
                            onCancelRide = { viewModel.cancelRide(it) },
                            onSubmitRating = { id, rating, review -> viewModel.submitRating(id, rating, review) },
                            onNavigateToWallet = {
                                currentRoute = Screen.RiderWallet.route
                                navController.navigate(Screen.RiderWallet.route)
                            },
                            onNavigateToHistory = {
                                currentRoute = Screen.RiderHistory.route
                                navController.navigate(Screen.RiderHistory.route)
                            },
                            onNavigateToSafety = {
                                currentRoute = Screen.RiderSafety.route
                                navController.navigate(Screen.RiderSafety.route)
                            },
                            onNavigateToSavedPlaces = {
                                currentRoute = Screen.RiderSavedPlaces.route
                                navController.navigate(Screen.RiderSavedPlaces.route)
                            },
                            onNavigateToSupport = {
                                currentRoute = Screen.RiderSupport.route
                                navController.navigate(Screen.RiderSupport.route)
                            }
                        )
                    }
                }

                composable(Screen.RiderWallet.route) {
                    RiderWalletScreen(
                        walletBalance = uiState.currentRider?.walletBalance ?: 0.0,
                        transactions = uiState.walletTransactions,
                        onAddMoney = { amount, method -> viewModel.addMoneyToWallet(amount, method) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.RiderHistory.route) {
                    RiderRideHistoryScreen(
                        rides = uiState.riderHistory,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.RiderSafety.route) {
                    SafetyCenterScreen(
                        emergencyContacts = uiState.emergencyContacts,
                        onAddContact = { viewModel.addEmergencyContact(it) },
                        onDeleteContact = { viewModel.deleteEmergencyContact(it) },
                        onDialEmergency = { /* Simulated dial action */ }
                    )
                }

                composable(Screen.RiderSavedPlaces.route) {
                    SavedPlacesScreen(
                        savedPlaces = uiState.savedPlaces,
                        onAddPlace = { viewModel.addSavedPlace(it) },
                        onDeletePlace = { viewModel.deleteSavedPlace(it) },
                        onSelectPlace = { navController.popBackStack() }
                    )
                }

                composable(Screen.RiderSupport.route) {
                    SupportScreen(
                        tickets = uiState.allTickets.filter { it.userId == (uiState.currentRider?.id ?: "user_rider_1") },
                        onCreateTicket = { subject, msg, rideId -> viewModel.createSupportTicket(subject, msg, rideId) }
                    )
                }

                // --- Driver Routes ---
                composable(Screen.DriverHome.route) {
                    if (uiState.currentDriver != null) {
                        DriverHomeScreen(
                            driverUser = uiState.currentDriver!!,
                            driverProfile = uiState.currentDriverProfile,
                            activeRide = uiState.activeRide,
                            pendingIncomingRide = uiState.pendingIncomingDriverRide,
                            onToggleOnline = { viewModel.toggleDriverOnline(it) },
                            onAcceptIncomingRide = { viewModel.acceptIncomingRide(it) },
                            onDeclineIncomingRide = { viewModel.declineIncomingRide(it) },
                            onAdvanceRideStep = { id, next -> viewModel.advanceRideStatus(id, next) },
                            onVerifyOtpAndStart = { id, otp -> viewModel.verifyOtpAndStartRide(id, otp) },
                            onRoleSwitched = { role ->
                                if (role == UserRole.ADMIN) {
                                    requestAdminAccess()
                                } else {
                                    viewModel.switchRole(role)
                                    navController.navigate(Screen.RiderHome.route)
                                }
                            },
                            onNavigateToEarnings = {
                                currentRoute = Screen.DriverEarnings.route
                                navController.navigate(Screen.DriverEarnings.route)
                            },
                            onNavigateToProfileDocs = {
                                currentRoute = Screen.DriverProfileDocs.route
                                navController.navigate(Screen.DriverProfileDocs.route)
                            }
                        )
                    }
                }

                composable(Screen.DriverEarnings.route) {
                    DriverEarningsScreen(
                        profile = uiState.currentDriverProfile,
                        pastRides = uiState.driverHistory,
                        onInstantCashout = { viewModel.instantDriverCashout() }
                    )
                }

                composable(Screen.DriverProfileDocs.route) {
                    if (uiState.currentDriver != null) {
                        DriverProfileVerificationScreen(
                            user = uiState.currentDriver!!,
                            profile = uiState.currentDriverProfile,
                            onUploadDocSim = { /* document uploaded */ }
                        )
                    }
                }

                // --- Admin Routes ---
                composable(Screen.AdminConsole.route) {
                    if (!AdminSecurityManager.isAuthenticated(context)) {
                        AdminAuthDialog(
                            onDismiss = {
                                viewModel.switchRole(UserRole.RIDER)
                                currentRoute = Screen.RiderHome.route
                                navController.navigate(Screen.RiderHome.route)
                            },
                            onSuccess = {
                                // authenticated in place
                            }
                        )
                    } else {
                        AdminDashboardScreen(
                            allUsers = uiState.allUsers,
                            allDrivers = uiState.allDrivers,
                            allRides = uiState.allRides,
                            allCities = uiState.cities,
                            allPricingRules = uiState.pricingRules,
                            allPromos = uiState.allPromos,
                            allTickets = uiState.allTickets,
                            auditLogs = uiState.auditLogs,
                            onUpdatePricingRule = { viewModel.updatePricingRule(it) },
                            onUpdateCity = { viewModel.updateCity(it) },
                            onAddCity = { viewModel.addCity(it) },
                            onAddPromo = { viewModel.addPromoCode(it) },
                            onUpdateDriverDocStatus = { id, status -> viewModel.updateDriverDocStatus(id, status) },
                            onToggleUserBlocked = { id, blocked -> viewModel.toggleUserBlocked(id, blocked) },
                            onResolveTicket = { ticket, res -> viewModel.resolveTicket(ticket, res) },
                            onRoleSwitched = { role ->
                                viewModel.switchRole(role)
                                currentRoute = if (role == UserRole.RIDER) Screen.RiderHome.route else Screen.DriverHome.route
                                navController.navigate(currentRoute)
                            },
                            onLockAdminSession = {
                                AdminSecurityManager.logout(context)
                                viewModel.switchRole(UserRole.RIDER)
                                currentRoute = Screen.RiderHome.route
                                navController.navigate(Screen.RiderHome.route)
                            }
                        )
                    }
                }
            }
        }
    }

    // Role Switcher Modal Dialog
    if (showRoleSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showRoleSwitchDialog = false },
            title = {
                Text(
                    text = "Select Application Persona",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "RideGo supports multi-role operation. Switch roles below to test different user journeys:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    RoleSelectCard(
                        role = UserRole.RIDER,
                        title = "Rider Persona (Aarav Sharma)",
                        description = "Book rides, select vehicle categories (including Auto), auto fare @ ₹25/km, live Maps nav.",
                        icon = Icons.Default.Person,
                        tint = TealPrimary,
                        isSelected = currentRole == UserRole.RIDER,
                        onClick = {
                            viewModel.switchRole(UserRole.RIDER)
                            currentRoute = Screen.RiderHome.route
                            navController.navigate(Screen.RiderHome.route)
                            showRoleSwitchDialog = false
                        }
                    )

                    RoleSelectCard(
                        role = UserRole.DRIVER,
                        title = "Driver Persona (Rajesh Kumar - Auto)",
                        description = "Go online/offline, receive dispatch requests, open Google Maps GPS nav, OTP start & instant payouts.",
                        icon = Icons.Default.DirectionsCar,
                        tint = MintGreen,
                        isSelected = currentRole == UserRole.DRIVER,
                        onClick = {
                            viewModel.switchRole(UserRole.DRIVER)
                            currentRoute = Screen.DriverHome.route
                            navController.navigate(Screen.DriverHome.route)
                            showRoleSwitchDialog = false
                        }
                    )

                    RoleSelectCard(
                        role = UserRole.ADMIN,
                        title = "Platform Admin (Aditya)",
                        description = "Password-protected console for Aditya (adityasarwade1920@gmail.com). Manage ₹25/km rates, surge & fleet.",
                        icon = Icons.Default.AdminPanelSettings,
                        tint = SurgeAmber,
                        isSelected = currentRole == UserRole.ADMIN,
                        onClick = {
                            showRoleSwitchDialog = false
                            requestAdminAccess()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleSwitchDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Global Admin Authentication Modal Dialog
    if (showAdminAuthDialog) {
        AdminAuthDialog(
            onDismiss = { showAdminAuthDialog = false },
            onSuccess = {
                showAdminAuthDialog = false
                navigateToAdminDirectly()
            }
        )
    }
}

@Composable
private fun RoleSelectCard(
    role: UserRole,
    title: String,
    description: String,
    icon: ImageVector,
    tint: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) tint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, tint) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
