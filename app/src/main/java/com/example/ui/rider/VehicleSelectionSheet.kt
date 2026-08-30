package com.example.ui.rider

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.local.entity.CityEntity
import com.example.data.local.entity.PricingRuleEntity
import com.example.data.local.entity.PromoCodeEntity
import com.example.data.model.FareBreakdown
import com.example.data.model.PaymentMethod
import com.example.data.model.VehicleCategory
import com.example.domain.PricingEngine
import com.example.domain.RouteSimulationEngine
import com.example.ui.components.CategoryVehicleIcon
import com.example.ui.theme.DarkInk
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PunchyRed
import com.example.ui.theme.SoftBlueContainer
import com.example.ui.theme.SurgeAmber
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryLight
import com.example.util.GoogleMapsLauncher

@Composable
fun VehicleSelectionSheet(
    city: CityEntity?,
    pricingRules: List<PricingRuleEntity>,
    activePromos: List<PromoCodeEntity>,
    walletBalance: Double,
    pickupName: String,
    pickupLat: Double,
    pickupLng: Double,
    destinationName: String,
    destLat: Double,
    destLng: Double,
    onConfirmBooking: (category: VehicleCategory, paymentMethod: PaymentMethod, promoCode: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(VehicleCategory.AUTO) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.WALLET) }
    var promoInput by remember { mutableStateOf("") }
    var appliedPromo by remember { mutableStateOf<PromoCodeEntity?>(null) }
    var promoError by remember { mutableStateOf<String?>(null) }
    var showPaymentPicker by remember { mutableStateOf(false) }

    BackHandler {
        onDismiss()
    }

    val distanceKm = remember(pickupLat, pickupLng, destLat, destLng) {
        RouteSimulationEngine.calculateDistanceKm(pickupLat, pickupLng, destLat, destLng)
    }
    val durationMin = remember(distanceKm) {
        RouteSimulationEngine.estimateDurationMinutes(distanceKm)
    }

    val currency = city?.currencySymbol ?: "₹"
    val surgeMultiplier = city?.baseSurge ?: 1.0

    // Compute estimate for selected category
    val currentRule = pricingRules.find { it.vehicleCategory == selectedCategory }
    val currentFare = remember(selectedCategory, appliedPromo, currentRule, distanceKm, durationMin) {
        PricingEngine.calculateFare(
            rule = currentRule,
            category = selectedCategory,
            distanceKm = distanceKm,
            durationMinutes = durationMin,
            surgeMultiplier = surgeMultiplier,
            promo = appliedPromo
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vehicle_selection_sheet"),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            // Drag Handle Pill
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RIDE OPTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Select Category",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${String.format("%.1f", distanceKm)} km • ~${durationMin} mins away",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            GoogleMapsLauncher.openRouteInGoogleMaps(
                                context = context,
                                pickupLat = pickupLat,
                                pickupLng = pickupLng,
                                destLat = destLat,
                                destLng = destLng,
                                pickupName = pickupName,
                                destName = destinationName
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_google_maps_route_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Google Maps", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Distance Fare Rate Banner (1 km = ₹25)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftBlueContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Distance: ${String.format("%.1f", distanceKm)} km • Rate: ₹25/km Auto-calc",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkInk
                        )
                    }
                    Text(
                        text = "1 km = 25₹",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = ElectricBlue
                    )
                }
            }

            // Surge Notice if applicable
            if (surgeMultiplier > 1.0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurgeAmber.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurgeAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Surge",
                            tint = SurgeAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${surgeMultiplier}X SURGE DEMAND ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = SurgeAmber,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.0.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Vehicle Category List
            VehicleCategory.values().forEach { category ->
                val rule = pricingRules.find { it.vehicleCategory == category }
                val fare = PricingEngine.calculateFare(
                    rule = rule,
                    category = category,
                    distanceKm = distanceKm,
                    durationMinutes = durationMin,
                    surgeMultiplier = surgeMultiplier,
                    promo = appliedPromo
                )
                val isSelected = category == selectedCategory

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable { selectedCategory = category }
                        .testTag("vehicle_option_${category.name.lowercase()}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) SoftBlueContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, ElectricBlue)
                    else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryVehicleIcon(category = category)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${category.capacity}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${durationMin + 3} MIN AWAY • ${category.description.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$currency${fare.totalFare}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) ElectricBlue else DarkInk
                            )
                            if (fare.discountAmount > 0) {
                                Text(
                                    text = "-$currency${fare.discountAmount} PROMO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MintGreen,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Promo Code Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = promoInput,
                    onValueChange = {
                        promoInput = it
                        promoError = null
                    },
                    placeholder = { Text("PROMO CODE (e.g. FIRST50)") },
                    leadingIcon = {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = ElectricBlue)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("promo_code_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val found = activePromos.find { it.code.equals(promoInput.trim(), ignoreCase = true) }
                        if (found != null) {
                            appliedPromo = found
                            promoError = null
                        } else {
                            promoError = "Invalid or expired promo code"
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                    modifier = Modifier.testTag("apply_promo_btn")
                ) {
                    Text(
                        text = if (appliedPromo != null) "APPLIED" else "APPLY",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.0.sp
                    )
                }
            }

            if (appliedPromo != null) {
                Text(
                    text = "✓ Promo '${appliedPromo?.code}' applied: ${appliedPromo?.title}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MintGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            } else if (promoError != null) {
                Text(
                    text = promoError ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            // Action Row with Payment Method Quick Card + Dark Ink Hero Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment Method Pill
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SoftBlueContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .size(56.dp)
                        .clickable { showPaymentPicker = !showPaymentPicker }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (selectedPaymentMethod == PaymentMethod.WALLET) Icons.Default.AccountBalanceWallet else Icons.Default.Payments,
                            contentDescription = selectedPaymentMethod.title,
                            tint = ElectricBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Hero Booking Action Button (Bold Dark Ink styling)
                Button(
                    onClick = {
                        onConfirmBooking(
                            selectedCategory,
                            selectedPaymentMethod,
                            appliedPromo?.code
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("confirm_booking_btn"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkInk,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "BOOK RIDEGO • $currency${currentFare.totalFare}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Payment methods dropdown list if opened
            AnimatedVisibility(visible = showPaymentPicker) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    PaymentMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPaymentMethod = method
                                    showPaymentPicker = false
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = method.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = method.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selectedPaymentMethod == method) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = ElectricBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}
