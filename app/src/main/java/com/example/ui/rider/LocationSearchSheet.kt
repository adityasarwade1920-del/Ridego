package com.example.ui.rider

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.model.LatLng
import com.example.ui.theme.DarkInk
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PunchyRed
import com.example.ui.theme.SoftBlueContainer
import com.example.ui.theme.SurgeAmber

data class PlaceSuggestion(
    val title: String,
    val subtitle: String,
    val latLng: LatLng,
    val icon: ImageVector = Icons.Default.LocationOn
)

@Composable
fun LocationSearchSheet(
    initialPickup: String = "Bandra West, Mumbai",
    savedPlaces: List<SavedPlaceEntity> = emptyList(),
    onLocationsConfirmed: (pickupName: String, pickupLat: Double, pickupLng: Double, destName: String, destLat: Double, destLng: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler {
        keyboardController?.hide()
        onDismiss()
    }

    var pickupText by remember { mutableStateOf(initialPickup) }
    var destinationText by remember { mutableStateOf("") }

    var selectedPickupLatLng by remember { mutableStateOf(LatLng(19.0596, 72.8295)) }
    var selectedDestLatLng by remember { mutableStateOf<LatLng?>(null) }

    val sampleSuggestions = listOf(
        PlaceSuggestion("BKC Tech Park Tower 3", "Bandra Kurla Complex, Mumbai", LatLng(19.0657, 72.8687), Icons.Default.Work),
        PlaceSuggestion("Chhatrapati Shivaji Terminal 2", "International Airport, Sahar", LatLng(19.0896, 72.8656), Icons.Default.Flight),
        PlaceSuggestion("Phoenix Marketcity Mall", "LBS Marg, Kurla West", LatLng(19.0864, 72.8890), Icons.Default.LocationOn),
        PlaceSuggestion("Lower Parel High Street", "Senapati Bapat Marg, Mumbai", LatLng(19.0016, 72.8284), Icons.Default.LocationOn),
        PlaceSuggestion("Juhu Beach Promenade", "Juhu Tara Road, Mumbai", LatLng(19.0988, 72.8264), Icons.Default.NearMe),
        PlaceSuggestion("Powai Hiranandani Gardens", "Central Ave, Powai", LatLng(19.1197, 72.9051), Icons.Default.Work)
    )

    val filteredSuggestions = remember(destinationText) {
        if (destinationText.isBlank()) sampleSuggestions
        else sampleSuggestions.filter {
            it.title.contains(destinationText, ignoreCase = true) ||
            it.subtitle.contains(destinationText, ignoreCase = true)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("location_search_sheet"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            // Sheet Handle
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ROUTE PLANNER",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Where To?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftBlueContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable {
                        keyboardController?.hide()
                        onDismiss()
                    }
                ) {
                    Text(
                        text = "CANCEL",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.0.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pickup Input Field
            OutlinedTextField(
                value = pickupText,
                onValueChange = { pickupText = it },
                label = { Text("PICKUP LOCATION", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Pickup",
                        tint = ElectricBlue
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        pickupText = "My Current Location (Bandra)"
                        selectedPickupLatLng = LatLng(19.0596, 72.8295)
                    }) {
                        Icon(Icons.Default.NearMe, contentDescription = "Use GPS", tint = ElectricBlue)
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pickup_location_input"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Destination Input Field
            OutlinedTextField(
                value = destinationText,
                onValueChange = { destinationText = it },
                label = { Text("DESTINATION", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Destination",
                        tint = PunchyRed
                    )
                },
                placeholder = { Text("Search address, airport, landmark...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("destination_location_input"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Saved Places Chips
            if (savedPlaces.isNotEmpty()) {
                Text(
                    text = "SAVED PLACES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    savedPlaces.take(3).forEach { place ->
                        Surface(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    destinationText = place.name
                                    selectedDestLatLng = LatLng(place.lat, place.lng)
                                    onLocationsConfirmed(
                                        pickupText,
                                        selectedPickupLatLng.latitude,
                                        selectedPickupLatLng.longitude,
                                        place.name,
                                        place.lat,
                                        place.lng
                                    )
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = SoftBlueContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (place.iconType == "work") Icons.Default.Work else Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = ElectricBlue
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = place.name.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "POPULAR DESTINATIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.height(220.dp)) {
                items(filteredSuggestions) { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                destinationText = suggestion.title
                                selectedDestLatLng = suggestion.latLng
                                onLocationsConfirmed(
                                    pickupText,
                                    selectedPickupLatLng.latitude,
                                    selectedPickupLatLng.longitude,
                                    suggestion.title,
                                    suggestion.latLng.latitude,
                                    suggestion.latLng.longitude
                                )
                            }
                            .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftBlueContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = suggestion.icon,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = suggestion.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = suggestion.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }
        }
    }
}
