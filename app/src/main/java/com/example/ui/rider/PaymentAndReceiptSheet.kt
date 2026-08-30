package com.example.ui.rider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.RideEntity
import com.example.ui.components.DigitalReceiptCard
import com.example.ui.components.StarRatingBar
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentAndReceiptSheet(
    ride: RideEntity,
    onSubmitRating: (rating: Int, review: String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    val selectedCompliments = remember { mutableStateListOf<String>() }

    val complimentChips = listOf(
        "✨ Clean Car",
        "🛡️ Smooth & Safe Driving",
        "😊 Polite & Courteous",
        "⏰ Punctual Arrival",
        "🎵 Great Music",
        "❄️ Perfect AC"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payment_receipt_sheet"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Success Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MintGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Ride Completed & Paid!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MintGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Digital Receipt Card
            DigitalReceiptCard(ride = ride)

            Spacer(modifier = Modifier.height(16.dp))

            // Rating Section
            Text(
                text = "How was your ride with ${ride.driverName ?: "the driver"}?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            StarRatingBar(
                rating = rating,
                onRatingChanged = { rating = it },
                modifier = Modifier.testTag("rider_rating_bar")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Compliment Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                complimentChips.forEach { chip ->
                    val isSelected = selectedCompliments.contains(chip)
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                if (isSelected) selectedCompliments.remove(chip)
                                else selectedCompliments.add(chip)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) TealPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, TealPrimary) else null
                    ) {
                        Text(
                            text = chip,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                placeholder = { Text("Write a comment or tip for driver (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rider_review_input"),
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val fullReview = if (selectedCompliments.isNotEmpty()) {
                        "${selectedCompliments.joinToString(", ")}. $reviewText".trim()
                    } else reviewText
                    onSubmitRating(rating, fullReview)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_rating_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text(
                    text = "Submit Rating & Done",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
