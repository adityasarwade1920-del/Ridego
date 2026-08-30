package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DriverProfileEntity
import com.example.data.model.LatLng
import com.example.data.model.RideStatus
import com.example.data.model.RoutePoint
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.MapBgDark
import com.example.ui.theme.MapBgLight
import com.example.ui.theme.MapBuildingDark
import com.example.ui.theme.MapBuildingLight
import com.example.ui.theme.MapParkDark
import com.example.ui.theme.MapParkLight
import com.example.ui.theme.MapRoadBorderDark
import com.example.ui.theme.MapRoadBorderLight
import com.example.ui.theme.MapRoadDark
import com.example.ui.theme.MapRoadLight
import com.example.ui.theme.MapWaterDark
import com.example.ui.theme.MapWaterLight
import com.example.ui.theme.PunchyRed
import com.example.ui.theme.SurgeAmber
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealPrimaryLight
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RideGoMapCanvas(
    modifier: Modifier = Modifier,
    pickupLocation: LatLng? = null,
    pickupName: String? = null,
    destinationLocation: LatLng? = null,
    destinationName: String? = null,
    routePoints: List<RoutePoint> = emptyList(),
    nearbyDrivers: List<DriverProfileEntity> = emptyList(),
    activeDriverProgress: Float = 0f, // 0.0 to 1.0 along route
    rideStatus: RideStatus? = null,
    isDriverView: Boolean = false,
    onMapClicked: ((LatLng) -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val textMeasurer = rememberTextMeasurer()

    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    // Pulse animation for pickup point
    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Roaming animation for nearby drivers
    val roamOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "roam"
    )

    Box(
        modifier = modifier
            .testTag("ridego_map_canvas")
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    panOffsetX += pan.x
                    panOffsetY += pan.y
                    zoomScale = (zoomScale * zoom).coerceIn(0.6f, 2.5f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            val bgColor = if (isDark) MapBgDark else MapBgLight
            val roadColor = if (isDark) MapRoadDark else MapRoadLight
            val roadBorder = if (isDark) MapRoadBorderDark else MapRoadBorderLight
            val buildingColor = if (isDark) MapBuildingDark else MapBuildingLight
            val parkColor = if (isDark) MapParkDark else MapParkLight
            val waterColor = if (isDark) MapWaterDark else MapWaterLight

            // 1. Draw Map Canvas Background
            drawRect(color = bgColor)

            // 2. Draw Park and River Features
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(canvasW * 0.05f + panOffsetX * 0.2f, canvasH * 0.1f + panOffsetY * 0.2f),
                size = Size(canvasW * 0.35f, canvasH * 0.25f),
                cornerRadius = CornerRadius(24f, 24f)
            )
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(canvasW * 0.65f + panOffsetX * 0.2f, canvasH * 0.65f + panOffsetY * 0.2f),
                size = Size(canvasW * 0.3f, canvasH * 0.2f),
                cornerRadius = CornerRadius(20f, 20f)
            )

            // River Path
            val riverPath = Path().apply {
                moveTo(-50f + panOffsetX * 0.3f, canvasH * 0.45f + panOffsetY * 0.3f)
                cubicTo(
                    canvasW * 0.3f + panOffsetX * 0.3f, canvasH * 0.35f + panOffsetY * 0.3f,
                    canvasW * 0.6f + panOffsetX * 0.3f, canvasH * 0.55f + panOffsetY * 0.3f,
                    canvasW + 50f + panOffsetX * 0.3f, canvasH * 0.48f + panOffsetY * 0.3f
                )
            }
            drawPath(
                path = riverPath,
                color = waterColor,
                style = Stroke(width = 44f * zoomScale, cap = StrokeCap.Round)
            )

            // 3. City Road Grid & Blocks
            val gridStep = 80f * zoomScale
            var x = (panOffsetX % gridStep)
            while (x < canvasW + gridStep) {
                // Secondary Road
                drawLine(
                    color = roadBorder,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 14f * zoomScale
                )
                drawLine(
                    color = roadColor,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 10f * zoomScale
                )
                x += gridStep
            }

            var y = (panOffsetY % gridStep)
            while (y < canvasH + gridStep) {
                drawLine(
                    color = roadBorder,
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 14f * zoomScale
                )
                drawLine(
                    color = roadColor,
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 10f * zoomScale
                )
                y += gridStep
            }

            // Main Avenue Expressway (Diagonal)
            val avenuePath = Path().apply {
                moveTo(0f, canvasH * 0.75f + panOffsetY * 0.8f)
                cubicTo(
                    canvasW * 0.4f + panOffsetX * 0.8f, canvasH * 0.6f + panOffsetY * 0.8f,
                    canvasW * 0.6f + panOffsetX * 0.8f, canvasH * 0.3f + panOffsetY * 0.8f,
                    canvasW, canvasH * 0.15f + panOffsetY * 0.8f
                )
            }
            drawPath(
                path = avenuePath,
                color = roadBorder,
                style = Stroke(width = 28f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = avenuePath,
                color = roadColor,
                style = Stroke(width = 22f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 4. City Buildings / Blocks
            for (bx in 0..4) {
                for (by in 0..6) {
                    val bLeft = (bx * 130f + 20f) * zoomScale + panOffsetX
                    val bTop = (by * 110f + 30f) * zoomScale + panOffsetY
                    val bW = 60f * zoomScale
                    val bH = 50f * zoomScale
                    if (bLeft > -100 && bLeft < canvasW + 100 && bTop > -100 && bTop < canvasH + 100) {
                        drawRoundRect(
                            color = buildingColor,
                            topLeft = Offset(bLeft, bTop),
                            size = Size(bW, bH),
                            cornerRadius = CornerRadius(6f * zoomScale, 6f * zoomScale)
                        )
                    }
                }
            }

            // 5. Pickup & Destination Coordinates in Canvas Space
            val centerMapX = canvasW * 0.5f + panOffsetX
            val centerMapY = canvasH * 0.45f + panOffsetY

            val pickupX = canvasW * 0.32f + panOffsetX
            val pickupY = canvasH * 0.52f + panOffsetY

            val destX = canvasW * 0.72f + panOffsetX
            val destY = canvasH * 0.26f + panOffsetY

            // 6. Draw Trip Route Polyline if destinations exist
            if (destinationLocation != null || routePoints.isNotEmpty() || rideStatus != null) {
                val routePath = Path().apply {
                    moveTo(pickupX, pickupY)
                    // Curve through avenue
                    cubicTo(
                        pickupX + (destX - pickupX) * 0.2f, pickupY - 40f,
                        pickupX + (destX - pickupX) * 0.6f, destY + 50f,
                        destX, destY
                    )
                }

                // Route Shadow / Outer Glow
                drawPath(
                    path = routePath,
                    color = ElectricBlue.copy(alpha = 0.35f),
                    style = Stroke(width = 16f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Route Main Line
                val routeGradient = Brush.linearGradient(
                    colors = listOf(ElectricBlueLight, ElectricBlue, PunchyRed),
                    start = Offset(pickupX, pickupY),
                    end = Offset(destX, destY)
                )
                drawPath(
                    path = routePath,
                    brush = routeGradient,
                    style = Stroke(width = 8f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Dashed overlay for active path
                drawPath(
                    path = routePath,
                    color = Color.White.copy(alpha = 0.8f),
                    style = Stroke(
                        width = 3f * zoomScale,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                )

                // 7. Destination Pin (Punchy Red Marker with Shadow)
                drawCircle(
                    color = PunchyRed.copy(alpha = 0.25f),
                    radius = 20f * zoomScale,
                    center = Offset(destX, destY)
                )
                drawCircle(
                    color = PunchyRed,
                    radius = 10f * zoomScale,
                    center = Offset(destX, destY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.5f * zoomScale,
                    center = Offset(destX, destY)
                )

                if (destinationName != null) {
                    val destTextLayout = textMeasurer.measure(
                        text = destinationName.take(18).uppercase(),
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.0.sp,
                            color = if (isDark) Color.White else Color(0xFF1A1C1E)
                        )
                    )
                    drawRoundRect(
                        color = if (isDark) Color(0xDD1E293B) else Color(0xF2FFFFFF),
                        topLeft = Offset(destX - (destTextLayout.size.width / 2f) - 10f, destY - 45f * zoomScale),
                        size = Size(destTextLayout.size.width + 20f, destTextLayout.size.height + 10f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawText(
                        textLayoutResult = destTextLayout,
                        topLeft = Offset(destX - (destTextLayout.size.width / 2f), destY - 40f * zoomScale)
                    )
                }
            }

            // 8. Pickup Marker with Pulse Ring
            drawCircle(
                color = ElectricBlue.copy(alpha = pulseAlpha),
                radius = pulseRadius * zoomScale,
                center = Offset(pickupX, pickupY)
            )
            drawCircle(
                color = ElectricBlue,
                radius = 12f * zoomScale,
                center = Offset(pickupX, pickupY)
            )
            drawCircle(
                color = Color.White,
                radius = 5f * zoomScale,
                center = Offset(pickupX, pickupY)
            )

            if (pickupName != null) {
                val pickupTextLayout = textMeasurer.measure(
                    text = pickupName.take(16).uppercase(),
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.0.sp,
                        color = Color.White
                    )
                )
                drawRoundRect(
                    color = if (isDark) Color(0xEE1E3A8A) else Color(0xEE2D5CF6),
                    topLeft = Offset(pickupX - (pickupTextLayout.size.width / 2f) - 10f, pickupY + 20f * zoomScale),
                    size = Size(pickupTextLayout.size.width + 20f, pickupTextLayout.size.height + 10f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawText(
                    textLayoutResult = pickupTextLayout,
                    topLeft = Offset(pickupX - (pickupTextLayout.size.width / 2f), pickupY + 25f * zoomScale)
                )
            }

            // 9. Active Driver Moving along the route
            if (activeDriverProgress > 0f || rideStatus in listOf(
                    RideStatus.DRIVER_ASSIGNED,
                    RideStatus.DRIVER_ARRIVING,
                    RideStatus.DRIVER_ARRIVED,
                    RideStatus.RIDE_STARTED
                )
            ) {
                val t = activeDriverProgress.coerceIn(0f, 1f)
                // Interpolate car position
                val carX = pickupX + (destX - pickupX) * t
                val carY = pickupY + (destY - pickupY) * t
                val angle = if (destX != pickupX) {
                    Math.toDegrees(kotlin.math.atan2((destY - pickupY).toDouble(), (destX - pickupX).toDouble())).toFloat()
                } else 0f

                drawCarIcon(
                    center = Offset(carX, carY),
                    angle = angle + 90f,
                    scale = zoomScale,
                    carColor = TealPrimaryLight,
                    isDark = isDark
                )
            }

            // 10. Nearby Idle Drivers on Map
            val driverOffsets = listOf(
                Pair(70f, -80f),
                Pair(-90f, 60f),
                Pair(110f, 90f),
                Pair(-80f, -110f)
            )
            nearbyDrivers.take(4).forEachIndexed { index, driver ->
                val (baseDx, baseDy) = driverOffsets.getOrElse(index) { Pair(50f, 50f) }
                val dx = (baseDx + cos(roamOffset + index) * 18f) * zoomScale
                val dy = (baseDy + sin(roamOffset + index) * 18f) * zoomScale

                drawCarIcon(
                    center = Offset(pickupX + dx, pickupY + dy),
                    angle = ((roamOffset + index) * 57.29f).toFloat(),
                    scale = zoomScale * 0.9f,
                    carColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                    isDark = isDark
                )
            }
        }

        // Map Control Floating Actions (Zoom in/out, Re-center)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 70.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(4.dp)
                ) {
                    IconButton(
                        onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.5f) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.6f) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = {
                            panOffsetX = 0f
                            panOffsetY = 0f
                            zoomScale = 1.0f
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Re-center",
                            tint = TealPrimary
                        )
                    }
                }
            }
        }

        // Live GPS Status Chip
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 70.dp, start = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 4.dp
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TealPrimary)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "GPS High Accuracy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun DrawScope.drawCarIcon(
    center: Offset,
    angle: Float,
    scale: Float,
    carColor: Color,
    isDark: Boolean
) {
    rotate(degrees = angle, pivot = center) {
        val carW = 18f * scale
        val carH = 34f * scale

        // Car Shadow
        drawRoundRect(
            color = Color(0x40000000),
            topLeft = Offset(center.x - carW / 2 + 2f, center.y - carH / 2 + 4f),
            size = Size(carW, carH),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )

        // Car Body
        drawRoundRect(
            color = carColor,
            topLeft = Offset(center.x - carW / 2, center.y - carH / 2),
            size = Size(carW, carH),
            cornerRadius = CornerRadius(6f * scale, 6f * scale)
        )

        // Windshield
        drawRoundRect(
            color = if (isDark) Color(0xFF0F172A) else Color(0xFF64748B),
            topLeft = Offset(center.x - (carW * 0.35f), center.y - (carH * 0.25f)),
            size = Size(carW * 0.7f, carH * 0.22f),
            cornerRadius = CornerRadius(2f * scale, 2f * scale)
        )

        // Headlights (Yellow/Cyan glow)
        drawCircle(
            color = Color(0xFFFDE047),
            radius = 2f * scale,
            center = Offset(center.x - (carW * 0.3f), center.y - (carH * 0.42f))
        )
        drawCircle(
            color = Color(0xFFFDE047),
            radius = 2f * scale,
            center = Offset(center.x + (carW * 0.3f), center.y - (carH * 0.42f))
        )
    }
}
