package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Dish
import kotlinx.coroutines.launch
import kotlin.random.Random

// Beautiful bright modern segment colors from Vibrant Palette
val SegmentColors = listOf(
    Color(0xFFFFD8E4), // Pizza/Pinkish
    Color(0xFFD0E4FF), // Sushi/Light Blue
    Color(0xFFC4EED0), // Tacos/Light Green
    Color(0xFFFFF1AC), // Salad/Mellow Yellow
    Color(0xFFEADDFF), // Burger/Purple Lavender
    Color(0xFFFFDAD6), // Pasta/Salmon Pink
    Color(0xFFCEF2F4), // Soft Aqua
    Color(0xFFFFE0D8), // Soft Coral
    Color(0xFFE2F9E5), // Soft Lime
    Color(0xFFF4E2FF)  // Soft Orchid
)

/**
 * Calculates high-contrast text color based on background luminance.
 */
fun getContrastTextColor(backgroundColor: Color): Color {
    val luminance = 0.299f * backgroundColor.red + 0.587f * backgroundColor.green + 0.114f * backgroundColor.blue
    return if (luminance > 0.55f) Color(0xFF1E1E24) else Color.White
}

@Composable
fun DishWheel(
    dishes: List<Dish>,
    rotation: Float,
    isSpinning: Boolean = false,
    onSpinClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 300.dp
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Animate scale of the wheel slightly when spinning to add physical depth/feedback
    val scale by animateFloatAsState(
        targetValue = if (isSpinning) 0.94f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )

    // Calculate pointer deflection (flapping/tilting) based on segment crossings
    val pointerDeflection = remember(rotation, isSpinning, dishes.size) {
        if (isSpinning && dishes.isNotEmpty()) {
            val segmentCount = dishes.size
            val sweepAngle = 360f / segmentCount
            val progress = (rotation % sweepAngle) / sweepAngle
            if (progress > 0.82f) {
                val t = (progress - 0.82f) / 0.18f
                t * 24f // tilt pointer in direction of spin
            } else if (progress < 0.18f) {
                val t = progress / 0.18f
                (1f - t) * -10f // snap back and slight overshoot
            } else {
                0f
            }
        } else {
            0f
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // 1. Rotating Wheel Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    rotationZ = rotation,
                    scaleX = scale,
                    scaleY = scale
                )
        ) {
            val canvasSize = this.size
            val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            val wheelRadius = canvasSize.minDimension / 2f
            
            // Draw beautiful outer shadow/border
            drawCircle(
                color = Color.Black.copy(alpha = 0.15f),
                radius = wheelRadius,
                center = center
            )
            
            // Draw elegant outer frame rim
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.85f)),
                    center = center,
                    radius = wheelRadius
                ),
                radius = wheelRadius - with(density) { 4.dp.toPx() },
                center = center
            )

            val activeRadius = wheelRadius - with(density) { 10.dp.toPx() }
            val rectSize = Size(activeRadius * 2f, activeRadius * 2f)
            val rectTopLeft = Offset(center.x - activeRadius, center.y - activeRadius)

            if (dishes.isNotEmpty()) {
                val segmentCount = dishes.size
                val sweepAngle = 360f / segmentCount

                for (i in dishes.indices) {
                    val dish = dishes[i]
                    val color = SegmentColors[i % SegmentColors.size]
                    val startAngle = i * sweepAngle

                    // Draw segment wedge slice
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = rectTopLeft,
                        size = rectSize
                    )

                    // Draw thin white divider line between segments
                    val angleRad = Math.toRadians(startAngle.toDouble())
                    val lineEnd = Offset(
                        (center.x + activeRadius * Math.cos(angleRad)).toFloat(),
                        (center.y + activeRadius * Math.sin(angleRad)).toFloat()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = center,
                        end = lineEnd,
                        strokeWidth = with(density) { 1.5.dp.toPx() }
                    )

                    // Measure and draw label text along segment bisector
                    val bisectorAngle = startAngle + sweepAngle / 2f
                    val textRotation = bisectorAngle + 180f

                    val textStyle = TextStyle(
                        color = getContrastTextColor(color),
                        fontSize = 11.sp, // slightly smaller font size for even better fit
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Position text starting near the outer perimeter and flowing inwards
                    val padding = with(density) { 8.dp.toPx() }
                    val outerStartDistance = activeRadius - padding
                    val minDistanceToCenter = activeRadius * 0.35f
                    val maxTextWidth = (outerStartDistance - minDistanceToCenter).coerceAtLeast(0f)

                    val textLayoutResult = textMeasurer.measure(
                        text = dish.name,
                        style = textStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        constraints = Constraints(maxWidth = maxTextWidth.toInt())
                    )

                    val textWidth = textLayoutResult.size.width
                    val textHeight = textLayoutResult.size.height

                    rotate(degrees = textRotation, pivot = center) {
                        // Start exactly from the outer circle (negative relative offset) and form inwards
                        val textX = center.x - outerStartDistance
                        val textY = center.y - textHeight / 2f

                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(textX, textY)
                        )
                    }
                }
            } else {
                // Draw elegant empty state circle
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f),
                    radius = activeRadius,
                    center = center
                )
            }
        }

        // 2. Interactive Static Center SPIN Button
        Box(
            modifier = Modifier
                .size(size * 0.26f)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(primaryColor)
                .border(BorderStroke(3.dp, Color.White), shape = CircleShape)
                .clickable { onSpinClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SPIN",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }

        // 3. Dynamic Pointer Arrow at the Top (flaps as segments pass)
        Canvas(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-4).dp)
                .graphicsLayer(
                    rotationZ = pointerDeflection,
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.1f)
                )
        ) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val arrowPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(canvasWidth, 0f)
                lineTo(canvasWidth / 2f, canvasHeight)
                close()
            }
            
            // Draw pointer shadow
            drawPath(
                path = arrowPath,
                color = Color.Black.copy(alpha = 0.2f)
            )
            
            // Draw pointer face
            drawPath(
                path = arrowPath,
                color = primaryColor
            )
        }
    }
}
