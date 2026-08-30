package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RideEntity
import com.example.data.model.RideStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleCategory
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedLight
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkInk
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PunchyRed
import com.example.ui.theme.SoftBlueContainer
import com.example.ui.theme.SurgeAmber
import com.example.ui.theme.SurgeAmberLight

@Composable
fun StatusBadge(status: RideStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        RideStatus.REQUESTED, RideStatus.SEARCHING_DRIVER -> Pair(ElectricBlue.copy(alpha = 0.12f), ElectricBlue)
        RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_ARRIVING -> Pair(ElectricBlue.copy(alpha = 0.15f), ElectricBlue)
        RideStatus.DRIVER_ARRIVED -> Pair(SurgeAmber.copy(alpha = 0.15f), SurgeAmber)
        RideStatus.RIDE_STARTED -> Pair(MintGreen.copy(alpha = 0.15f), MintGreen)
        RideStatus.RIDE_COMPLETED, RideStatus.COMPLETED -> Pair(MintGreen.copy(alpha = 0.18f), MintGreen)
        RideStatus.PAYMENT_PENDING -> Pair(SurgeAmber.copy(alpha = 0.18f), SurgeAmber)
        else -> Pair(PunchyRed.copy(alpha = 0.15f), PunchyRed)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.label.uppercase(),
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.0.sp
            )
        }
    }
}

@Composable
fun RoleBadge(role: UserRole, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (role) {
        UserRole.RIDER -> Triple("RIDER", SoftBlueContainer, ElectricBlue)
        UserRole.DRIVER -> Triple("DRIVER", MintGreen.copy(alpha = 0.15f), MintGreen)
        UserRole.ADMIN -> Triple("ADMIN", SurgeAmber.copy(alpha = 0.15f), SurgeAmber)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, fg.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
fun StarRatingBar(
    rating: Int,
    maxStars: Int = 5,
    onRatingChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        for (i in 1..maxStars) {
            val isSelected = i <= rating
            Icon(
                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "$i Stars",
                tint = if (isSelected) SurgeAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(32.dp)
                    .padding(2.dp)
                    .then(
                        if (onRatingChanged != null) {
                            Modifier.clickable { onRatingChanged(i) }
                        } else Modifier
                    )
            )
        }
    }
}

@Composable
fun CategoryVehicleIcon(category: VehicleCategory, modifier: Modifier = Modifier) {
    val icon = when (category) {
        VehicleCategory.AUTO -> Icons.Default.ElectricRickshaw
        VehicleCategory.ECONOMY -> Icons.Default.DirectionsCar
        VehicleCategory.COMFORT -> Icons.Default.LocalTaxi
        VehicleCategory.PREMIUM -> Icons.Default.DirectionsCar
        VehicleCategory.XL -> Icons.Default.DirectionsCar
    }
    val containerBg = if (category == VehicleCategory.AUTO) Color(0xFFE8F5E9) else SoftBlueContainer
    val iconTint = if (category == VehicleCategory.AUTO) Color(0xFF2E7D32) else ElectricBlue

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.displayName,
            tint = iconTint,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
fun DigitalReceiptCard(
    ride: RideEntity,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("digital_receipt_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TRIP RECEIPT",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "#${ride.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MintGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = ride.paymentStatus.name.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MintGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.0.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // Fare breakdown line items
            ReceiptLineItem("Base Fare", "$currencySymbol${ride.baseFare}")
            ReceiptLineItem("Distance Charge (${ride.distanceKm} km)", "$currencySymbol${ride.distanceFare}")
            ReceiptLineItem("Time Charge (${ride.durationMinutes} mins)", "$currencySymbol${ride.timeFare}")
            ReceiptLineItem("Booking & Safety Fee", "$currencySymbol${ride.bookingFee}")

            if (ride.surgeMultiplier > 1.0) {
                ReceiptLineItem("Surge Multiplier (${ride.surgeMultiplier}x)", "Applied", SurgeAmber)
            }
            if (ride.discountAmount > 0.0) {
                ReceiptLineItem("Promo Discount (${ride.promoCode ?: ""})", "-$currencySymbol${ride.discountAmount}", MintGreen)
            }
            ReceiptLineItem("Taxes & GST", "$currencySymbol${ride.taxAmount}")

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL PAID (${ride.paymentMethod.title.uppercase()})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$currencySymbol${ride.totalFare}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = DarkInk
                )
            }
        }
    }
}

@Composable
private fun ReceiptLineItem(label: String, value: String, highlightColor: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = highlightColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SosEmergencyDialog(
    rideId: String,
    emergencyContactPhone: String,
    onDismiss: () -> Unit,
    onShareTrip: () -> Unit,
    onDialEmergency: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AlertRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "SOS",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SAFETY SOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = AlertRed,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Emergency Assistance",
                        style = MaterialTheme.typography.titleLarge,
                        color = AlertRed,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = "If you are in immediate danger, your live GPS location and trip details will be broadcast to emergency services and your trusted contacts.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onDialEmergency("112") },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dial_police_btn")
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CALL EMERGENCY (112)",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.0.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        val phone = if (emergencyContactPhone.isNotBlank()) emergencyContactPhone else "9876500000"
                        onDialEmergency(phone)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dial_contact_btn")
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CALL TRUSTED CONTACT",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onShareTrip,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("share_live_trip_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SHARE LIVE TRIP TRACKING",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "DISMISS",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.0.sp
                )
            }
        }
    )
}

