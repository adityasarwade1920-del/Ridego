package com.example.ui.rider

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.SavedPlaceEntity
import com.example.ui.theme.AlertRed
import com.example.ui.theme.TealPrimary
import java.util.UUID

@Composable
fun SavedPlacesScreen(
    savedPlaces: List<SavedPlaceEntity>,
    onAddPlace: (SavedPlaceEntity) -> Unit,
    onDeletePlace: (String) -> Unit,
    onSelectPlace: (SavedPlaceEntity) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var selectedIconType by remember { mutableStateOf("home") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("saved_places_screen")
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved Places",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1-tap booking for your regular destinations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddForm = !showAddForm },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Place")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showAddForm) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Save New Destination",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Place Name (e.g. Gym, Client Office)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text("Full Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val icons = listOf("home" to Icons.Default.Home, "work" to Icons.Default.Work, "airport" to Icons.Default.Flight, "gym" to Icons.Default.FitnessCenter)
                            icons.forEach { (type, icon) ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedIconType = type },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedIconType == type) TealPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                    border = if (selectedIconType == type) androidx.compose.foundation.BorderStroke(1.dp, TealPrimary) else null
                                ) {
                                    Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                        Icon(icon, contentDescription = null, tint = if (selectedIconType == type) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (nameInput.isNotBlank() && addressInput.isNotBlank()) {
                                    onAddPlace(
                                        SavedPlaceEntity(
                                            id = "place_" + UUID.randomUUID().toString().take(6),
                                            userId = "user_rider_1",
                                            name = nameInput,
                                            address = addressInput,
                                            lat = 19.0760 + (Math.random() - 0.5) * 0.05,
                                            lng = 72.8777 + (Math.random() - 0.5) * 0.05,
                                            iconType = selectedIconType
                                        )
                                    )
                                    nameInput = ""
                                    addressInput = ""
                                    showAddForm = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text("Save Location")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        items(savedPlaces) { place ->
            val icon = when (place.iconType) {
                "work" -> Icons.Default.Work
                "airport" -> Icons.Default.Flight
                "gym" -> Icons.Default.FitnessCenter
                else -> Icons.Default.Home
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectPlace(place) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TealPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(22.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = place.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { onDeletePlace(place.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed)
                    }
                }
            }
        }
    }
}
