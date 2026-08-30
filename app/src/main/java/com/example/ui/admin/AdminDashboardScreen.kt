package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CityEntity
import com.example.data.local.entity.DriverProfileEntity
import com.example.data.local.entity.PricingRuleEntity
import com.example.data.local.entity.PromoCodeEntity
import com.example.data.local.entity.RideEntity
import com.example.data.local.entity.SupportTicketEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.DriverDocStatus
import com.example.data.model.RideStatus
import com.example.data.model.TicketStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MintGreen
import com.example.ui.theme.SurgeAmber
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AdminTab(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    RIDES("Live Rides", Icons.Default.DirectionsCar),
    DRIVERS("Drivers & KYC", Icons.Default.People),
    PRICING("Pricing & Surge", Icons.Default.PriceChange),
    CITIES("Cities", Icons.Default.LocationCity),
    PROMOS("Promos", Icons.Default.LocalOffer),
    SUPPORT("Support Tickets", Icons.Default.SupportAgent),
    AUDIT("Audit Logs", Icons.Default.Security),
    SECURITY("Owner Security", Icons.Default.Lock)
}

@Composable
fun AdminDashboardScreen(
    allUsers: List<UserEntity>,
    allDrivers: List<DriverProfileEntity>,
    allRides: List<RideEntity>,
    allCities: List<CityEntity>,
    allPricingRules: List<PricingRuleEntity>,
    allPromos: List<PromoCodeEntity>,
    allTickets: List<SupportTicketEntity>,
    auditLogs: List<AuditLogEntity>,
    onUpdatePricingRule: (PricingRuleEntity) -> Unit,
    onUpdateCity: (CityEntity) -> Unit,
    onAddCity: (CityEntity) -> Unit,
    onAddPromo: (PromoCodeEntity) -> Unit,
    onUpdateDriverDocStatus: (String, DriverDocStatus) -> Unit,
    onToggleUserBlocked: (String, Boolean) -> Unit,
    onResolveTicket: (SupportTicketEntity, String) -> Unit,
    onRoleSwitched: (UserRole) -> Unit,
    onLockAdminSession: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }

    // Dialog state for editing pricing rule
    var editingRule by remember { mutableStateOf<PricingRuleEntity?>(null) }
    var editBaseFare by remember { mutableStateOf("") }
    var editPerKmRate by remember { mutableStateOf("") }
    var editPerMinRate by remember { mutableStateOf("") }
    var editMinFare by remember { mutableStateOf("") }

    // Dialog state for adding city
    var showAddCityDialog by remember { mutableStateOf(false) }
    var newCityName by remember { mutableStateOf("") }
    var newCityState by remember { mutableStateOf("") }
    var newCityCountry by remember { mutableStateOf("India") }
    var newCityCurrency by remember { mutableStateOf("₹") }
    var newCitySurge by remember { mutableFloatStateOf(1.0f) }

    // Dialog state for adding promo
    var showAddPromoDialog by remember { mutableStateOf(false) }
    var newPromoCode by remember { mutableStateOf("") }
    var newPromoTitle by remember { mutableStateOf("") }
    var newPromoDiscount by remember { mutableStateOf("20") }
    var newPromoMaxDiscount by remember { mutableStateOf("100") }

    // Dialog state for replying to ticket
    var selectedTicketForReply by remember { mutableStateOf<SupportTicketEntity?>(null) }
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen")
    ) {
        // Admin Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "RideGo Master Console",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            RoleBadge(role = UserRole.ADMIN)
                        }
                        Text(
                            text = "Admin: adityasarwade1920@gmail.com (Owner Access)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Lock Admin Console button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AlertRed.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clickable { onLockAdminSession() }
                                .testTag("admin_lock_session_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Lock", tint = AlertRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock", style = MaterialTheme.typography.labelSmall, color = AlertRed, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Role switch to Rider/Driver
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
                                Text("Switch App", style = MaterialTheme.typography.labelSmall, color = TealPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Horizontal Tab Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedTab = tab }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    tab.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tab Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                AdminTab.OVERVIEW -> {
                    AdminOverviewTab(
                        allUsers = allUsers,
                        allDrivers = allDrivers,
                        allRides = allRides,
                        allCities = allCities
                    )
                }
                AdminTab.RIDES -> {
                    AdminLiveRidesTab(allRides = allRides)
                }
                AdminTab.DRIVERS -> {
                    AdminDriversKycTab(
                        drivers = allDrivers,
                        users = allUsers,
                        onUpdateDocStatus = onUpdateDriverDocStatus,
                        onToggleUserBlocked = onToggleUserBlocked
                    )
                }
                AdminTab.PRICING -> {
                    AdminPricingTab(
                        cities = allCities,
                        pricingRules = allPricingRules,
                        onUpdateRule = onUpdatePricingRule,
                        onUpdateCity = onUpdateCity,
                        onEditRule = { rule ->
                            editingRule = rule
                            editBaseFare = rule.baseFare.toString()
                            editPerKmRate = rule.perKmRate.toString()
                            editPerMinRate = rule.perMinuteRate.toString()
                            editMinFare = rule.minFare.toString()
                        }
                    )
                }
                AdminTab.CITIES -> {
                    AdminCitiesTab(
                        cities = allCities,
                        onAddCityClick = { showAddCityDialog = true },
                        onToggleCityActive = { city ->
                            onUpdateCity(city.copy(isActive = !city.isActive))
                        }
                    )
                }
                AdminTab.PROMOS -> {
                    AdminPromosTab(
                        promos = allPromos,
                        onAddPromoClick = { showAddPromoDialog = true },
                        onTogglePromo = { promo ->
                            onAddPromo(promo.copy(isActive = !promo.isActive))
                        }
                    )
                }
                AdminTab.SUPPORT -> {
                    AdminSupportTab(
                        tickets = allTickets,
                        onOpenReply = { ticket ->
                            selectedTicketForReply = ticket
                            replyText = ""
                        }
                    )
                }
                AdminTab.AUDIT -> {
                    AdminAuditTab(auditLogs = auditLogs)
                }
                AdminTab.SECURITY -> {
                    AdminSecurityTab(onLockSession = onLockAdminSession)
                }
            }
        }
    }

    // Edit Pricing Rule Dialog (Per Km Rate ₹25 default, Base Fare, etc.)
    if (editingRule != null) {
        val current = editingRule!!
        AlertDialog(
            onDismissRequest = { editingRule = null },
            title = { Text("Edit Rates: ${current.vehicleCategory.displayName} (${current.cityId})", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Standard per-km rate default is ₹25/km as configured by owner.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editBaseFare,
                        onValueChange = { editBaseFare = it },
                        label = { Text("Base Fare (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPerKmRate,
                        onValueChange = { editPerKmRate = it },
                        label = { Text("Per KM Rate (₹ / km) [e.g. 25.0]") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPerMinRate,
                        onValueChange = { editPerMinRate = it },
                        label = { Text("Per Minute Rate (₹ / min)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editMinFare,
                        onValueChange = { editMinFare = it },
                        label = { Text("Minimum Ride Fare (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val base = editBaseFare.toDoubleOrNull() ?: current.baseFare
                        val perKm = editPerKmRate.toDoubleOrNull() ?: current.perKmRate
                        val perMin = editPerMinRate.toDoubleOrNull() ?: current.perMinuteRate
                        val minF = editMinFare.toDoubleOrNull() ?: current.minFare
                        onUpdatePricingRule(
                            current.copy(
                                baseFare = base,
                                perKmRate = perKm,
                                perMinuteRate = perMin,
                                minFare = minF
                            )
                        )
                        editingRule = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Save Rates")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRule = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add City Dialog
    if (showAddCityDialog) {
        AlertDialog(
            onDismissRequest = { showAddCityDialog = false },
            title = { Text("Launch New Operational City", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = newCityName, onValueChange = { newCityName = it }, label = { Text("City Name (e.g. Hyderabad)") }, singleLine = true)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = newCityState, onValueChange = { newCityState = it }, label = { Text("State / Province") }, singleLine = true)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = newCityCountry, onValueChange = { newCityCountry = it }, label = { Text("Country") }, singleLine = true)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = newCityCurrency, onValueChange = { newCityCurrency = it }, label = { Text("Currency Symbol (₹, $, £)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCityName.isNotBlank()) {
                            onAddCity(
                                CityEntity(
                                    id = "city_" + newCityName.lowercase().replace(" ", "_"),
                                    name = newCityName,
                                    state = newCityState,
                                    country = newCityCountry,
                                    currencySymbol = newCityCurrency,
                                    currencyCode = "INR",
                                    isActive = true,
                                    baseSurge = 1.0,
                                    centerLat = 19.0760,
                                    centerLng = 72.8777
                                )
                            )
                            showAddCityDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) { Text("Launch City") }
            },
            dismissButton = {
                TextButton(onClick = { showAddCityDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Promo Dialog
    if (showAddPromoDialog) {
        AlertDialog(
            onDismissRequest = { showAddPromoDialog = false },
            title = { Text("Create Discount Promo Code", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = newPromoCode, onValueChange = { newPromoCode = it.uppercase() }, label = { Text("Promo Code (e.g. FESTIVE30)") }, singleLine = true)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = newPromoTitle, onValueChange = { newPromoTitle = it }, label = { Text("Title / Marketing Label") }, singleLine = true)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = newPromoDiscount, onValueChange = { newPromoDiscount = it }, label = { Text("Discount Percent (%)") }, singleLine = true)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = newPromoMaxDiscount, onValueChange = { newPromoMaxDiscount = it }, label = { Text("Max Discount Amount (₹)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPromoCode.isNotBlank()) {
                            onAddPromo(
                                PromoCodeEntity(
                                    code = newPromoCode.trim().uppercase(),
                                    title = newPromoTitle,
                                    description = "Save up to ₹$newPromoMaxDiscount on rides",
                                    discountPercent = newPromoDiscount.toDoubleOrNull() ?: 20.0,
                                    maxDiscount = newPromoMaxDiscount.toDoubleOrNull() ?: 100.0,
                                    minRideValue = 100.0,
                                    cityId = "all"
                                )
                            )
                            showAddPromoDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) { Text("Activate Promo") }
            },
            dismissButton = {
                TextButton(onClick = { showAddPromoDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Reply Ticket Dialog
    if (selectedTicketForReply != null) {
        AlertDialog(
            onDismissRequest = { selectedTicketForReply = null },
            title = { Text("Respond to Ticket #${selectedTicketForReply?.id}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "Subject: ${selectedTicketForReply?.subject}", fontWeight = FontWeight.SemiBold)
                    Text(text = "User Message: ${selectedTicketForReply?.message}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Admin Official Response") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onResolveTicket(selectedTicketForReply!!, replyText)
                            selectedTicketForReply = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) { Text("Resolve & Send") }
            },
            dismissButton = {
                TextButton(onClick = { selectedTicketForReply = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminOverviewTab(
    allUsers: List<UserEntity>,
    allDrivers: List<DriverProfileEntity>,
    allRides: List<RideEntity>,
    allCities: List<CityEntity>
) {
    val totalRevenue = allRides.sumOf { it.totalFare }
    val totalCommission = allRides.sumOf { it.platformCommission }
    val activeRidesCount = allRides.count { it.status in listOf(RideStatus.SEARCHING_DRIVER, RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_ARRIVING, RideStatus.DRIVER_ARRIVED, RideStatus.RIDE_STARTED) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            // KPI Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Platform Gross Volume",
                    value = "₹${totalRevenue.toInt()}",
                    subtitle = "₹${totalCommission.toInt()} net commission",
                    icon = Icons.Default.Payments,
                    tint = MintGreen,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Active Live Trips",
                    value = "$activeRidesCount",
                    subtitle = "${allRides.size} total trips booked",
                    icon = Icons.Default.DirectionsCar,
                    tint = TealPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Registered Fleet",
                    value = "${allDrivers.size}",
                    subtitle = "${allDrivers.count { it.isOnline }} drivers currently online",
                    icon = Icons.Default.People,
                    tint = SurgeAmber,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Operational Cities",
                    value = "${allCities.size}",
                    subtitle = "${allCities.count { it.isActive }} active zones",
                    icon = Icons.Default.LocationCity,
                    tint = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Live Active Fleet Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(allDrivers) { driver ->
            val user = allUsers.find { it.id == driver.driverId }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (driver.isOnline) MintGreen else MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${user?.name ?: "Driver"} • ${driver.vehicleModel}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${driver.vehicleNumber} • ${driver.vehicleCategory.displayName} • Rating: ${driver.rating}★",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (driver.isOnline) MintGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (driver.isOnline) "ONLINE" else "OFFLINE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (driver.isOnline) MintGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdminLiveRidesTab(allRides: List<RideEntity>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(allRides) { ride ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trip #${ride.id} (${ride.vehicleCategory.displayName})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(status = ride.status)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rider: ${ride.riderName} → Driver: ${ride.driverName ?: "Awaiting match"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${ride.pickupName} → ${ride.destinationName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Fare: ₹${ride.totalFare}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TealPrimary)
                        Text(text = "Commission: ₹${ride.platformCommission}", style = MaterialTheme.typography.bodySmall, color = MintGreen)
                        Text(text = "Method: ${ride.paymentMethod.title}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDriversKycTab(
    drivers: List<DriverProfileEntity>,
    users: List<UserEntity>,
    onUpdateDocStatus: (String, DriverDocStatus) -> Unit,
    onToggleUserBlocked: (String, Boolean) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(drivers) { driver ->
            val user = users.find { it.id == driver.driverId }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${user?.name ?: "Driver"} (${driver.vehicleModel})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Plate: ${driver.vehicleNumber} | License: ${driver.licenseNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (driver.docStatus) {
                                DriverDocStatus.APPROVED -> MintGreen.copy(alpha = 0.2f)
                                DriverDocStatus.PENDING -> SurgeAmber.copy(alpha = 0.2f)
                                else -> AlertRed.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = driver.docStatus.name,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (driver.docStatus) {
                                    DriverDocStatus.APPROVED -> MintGreen
                                    DriverDocStatus.PENDING -> SurgeAmber
                                    else -> AlertRed
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (driver.docStatus != DriverDocStatus.APPROVED) {
                            Button(
                                onClick = { onUpdateDocStatus(driver.driverId, DriverDocStatus.APPROVED) },
                                colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve KYC")
                            }
                        }

                        if (driver.docStatus != DriverDocStatus.SUSPENDED) {
                            OutlinedButton(
                                onClick = { onUpdateDocStatus(driver.driverId, DriverDocStatus.SUSPENDED) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Suspend")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPricingTab(
    cities: List<CityEntity>,
    pricingRules: List<PricingRuleEntity>,
    onUpdateRule: (PricingRuleEntity) -> Unit,
    onUpdateCity: (CityEntity) -> Unit,
    onEditRule: (PricingRuleEntity) -> Unit = {}
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(
                text = "City Surge Multipliers (Real-time)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(cities) { city ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${city.name}, ${city.country}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${city.baseSurge}x Surge",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (city.baseSurge > 1.0) SurgeAmber else MintGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = city.baseSurge.toFloat(),
                        onValueChange = { newSurge ->
                            val rounded = (Math.round(newSurge * 10) / 10.0)
                            onUpdateCity(city.copy(baseSurge = rounded))
                        },
                        valueRange = 1.0f..3.0f,
                        steps = 19
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Base Fares & Per-Km Rates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Default: 1 km = 25₹",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        items(pricingRules) { rule ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${rule.vehicleCategory.displayName} (${rule.cityId})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { onEditRule(rule) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Rates", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Base: ₹${rule.baseFare} • Per Km: ₹${rule.perKmRate} • Per Min: ₹${rule.perMinuteRate} • Min Fare: ₹${rule.minFare}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminSecurityTab(
    onLockSession: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MintGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MintGreen, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Authorized Master Admin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Access is restricted to authorized credentials", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Authorized Username / Email:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "adityasarwade1920@gmail.com",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TealPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Master Authentication Policy:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "• Protected with secure SHA-256 password challenge\n• Inactivity auto-lock session after 4 hours\n• Complete access to Fare Rates (₹25/km standard), Auto Services, Multi-City Fleet, Driver KYC, and Promos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onLockSession,
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock Admin Session Immediately", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCitiesTab(
    cities: List<CityEntity>,
    onAddCityClick: () -> Unit,
    onToggleCityActive: (CityEntity) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Button(
                onClick = onAddCityClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Launch New City Service Zone")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(cities) { city ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${city.name} (${city.country})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Currency: ${city.currencySymbol} (${city.currencyCode}) • Base Surge: ${city.baseSurge}x",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { onToggleCityActive(city) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (city.isActive) MintGreen else AlertRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (city.isActive) "Active" else "Disabled")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPromosTab(
    promos: List<PromoCodeEntity>,
    onAddPromoClick: () -> Unit,
    onTogglePromo: (PromoCodeEntity) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Button(
                onClick = onAddPromoClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create New Promotional Code")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(promos) { promo ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = promo.code,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TealPrimary
                        )
                        Text(
                            text = promo.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Discount: ${promo.discountPercent}% (Max ₹${promo.maxDiscount})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { onTogglePromo(promo) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (promo.isActive) MintGreen else AlertRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (promo.isActive) "Active" else "Paused")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSupportTab(
    tickets: List<SupportTicketEntity>,
    onOpenReply: (SupportTicketEntity) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(tickets) { ticket ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${ticket.id} • ${ticket.userName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (ticket.status == TicketStatus.RESOLVED) MintGreen.copy(alpha = 0.2f) else SurgeAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = ticket.status.name,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = if (ticket.status == TicketStatus.RESOLVED) MintGreen else SurgeAmber,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Subject: ${ticket.subject}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = ticket.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!ticket.adminResponse.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Admin Reply: ${ticket.adminResponse}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MintGreen,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onOpenReply(ticket) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text("Reply & Resolve Ticket")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAuditTab(auditLogs: List<AuditLogEntity>) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault()) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(auditLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(TealPrimary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${log.action} • by ${log.performedBy}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormatter.format(Date(log.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
