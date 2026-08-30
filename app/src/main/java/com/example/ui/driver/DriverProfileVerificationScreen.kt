package com.example.ui.driver

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.local.entity.DriverProfileEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.DriverDocStatus
import com.example.ui.theme.MintGreen
import com.example.ui.theme.SurgeAmber
import com.example.ui.theme.TealPrimary

@Composable
fun DriverProfileVerificationScreen(
    user: UserEntity,
    profile: DriverProfileEntity?,
    onUploadDocSim: (String) -> Unit
) {
    var uploadedCount by remember { mutableStateOf(4) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("driver_profile_docs_screen")
    ) {
        item {
            // Status Header
            val isApproved = profile?.docStatus == DriverDocStatus.APPROVED
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isApproved) MintGreen.copy(alpha = 0.15f) else SurgeAmber.copy(alpha = 0.15f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isApproved) MintGreen else SurgeAmber)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isApproved) MintGreen else SurgeAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isApproved) Icons.Default.VerifiedUser else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isApproved) "Driver Account Verified" else "Verification Under Review",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isApproved) MintGreen else SurgeAmber
                        )
                        Text(
                            text = if (isApproved) "All background checks passed. Active for rides." else "Admin reviewing your uploaded documentation.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Registered Vehicle Specifications
            Text(
                text = "Vehicle Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    VehicleSpecRow("Vehicle Model", profile?.vehicleModel ?: "Maruti Suzuki Swift Dzire")
                    VehicleSpecRow("License Plate", profile?.vehicleNumber ?: "MH 02 ER 8421")
                    VehicleSpecRow("Vehicle Category", profile?.vehicleCategory?.displayName ?: "Economy")
                    VehicleSpecRow("Exterior Color", profile?.vehicleColor ?: "Silver Metallic")
                    VehicleSpecRow("Driver Commercial DL", profile?.licenseNumber ?: "MH02-2018-0091244")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // KYC Documents Checklist
            Text(
                text = "Compliance Documents",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            DocItemRow("Commercial Driving License (DL)", "Verified on 12 Jan 2026", isVerified = true)
            DocItemRow("Vehicle Registration Certificate (RC)", "Verified for MH 02 ER 8421", isVerified = true)
            DocItemRow("Commercial Vehicle Insurance", "Valid till Nov 2026", isVerified = true)
            DocItemRow("Police Clearance & Background Check", "Clean Record Verified", isVerified = true)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onUploadDocSim("Updated Insurance Policy") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload Renewed Document")
            }
        }
    }
}

@Composable
private fun VehicleSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DocItemRow(title: String, subtitle: String, isVerified: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isVerified) MintGreen.copy(alpha = 0.15f) else SurgeAmber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = if (isVerified) MintGreen else SurgeAmber,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Verified",
                tint = MintGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
