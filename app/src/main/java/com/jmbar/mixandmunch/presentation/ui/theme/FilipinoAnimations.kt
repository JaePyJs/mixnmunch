package com.jmbar.mixandmunch.presentation.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Filipino-inspired animations and visual effects
 */

@Composable
fun PulseAnimation(
    targetValue: Float = 1.2f,
    duration: Int = 1000,
    content: @Composable (scale: Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(duration),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    content(scale)
}

@Composable
fun FadeInAnimation(
    durationMillis: Int = 800,
    content: @Composable (alpha: Float) -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis),
        label = "fadeIn"
    )
    
    content(alpha)
}

@Composable
fun SlideInFromBottomAnimation(
    durationMillis: Int = 600,
    content: @Composable (offsetY: Float) -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = EaseOutCubic
        ),
        label = "slideIn"
    )
    
    content(offsetY)
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )
    
    Box(
        modifier = modifier.drawBehind {
            val shimmerColors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.3f),
                Color.Transparent
            )
            
            val brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(
                    x = size.width * offset,
                    y = 0f
                ),
                end = Offset(
                    x = size.width * offset + size.width,
                    y = size.height
                )
            )
            
            drawRect(brush = brush)
        }
    ) {
        content()
    }
}

/**
 * Filipino flag-inspired gradient background
 */
fun Modifier.filipinoGradientBackground(): Modifier = this.drawBehind {
    val colors = listOf(
        Color(0xFF0038A8), // Filipino Blue
        Color(0xFFCE1126), // Filipino Red
        Color(0xFFFCD116)  // Filipino Yellow
    )
    
    val brush = Brush.verticalGradient(
        colors = colors,
        startY = 0f,
        endY = size.height
    )
    
    drawRect(brush = brush, alpha = 0.1f)
}

/**
 * Subtle pattern overlay inspired by traditional Filipino textiles
 */
fun Modifier.filipinoPattern(): Modifier = this.drawBehind {
    drawFilipinoPattern(this, MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
}

private fun drawFilipinoPattern(
    drawScope: DrawScope,
    color: Color,
    strokeWidth: Dp = 1.dp
) {
    with(drawScope) {
        val spacing = 40.dp.toPx()
        val strokeWidthPx = strokeWidth.toPx()
        
        // Draw diamond pattern
        for (x in 0 until (size.width / spacing).toInt() + 2) {
            for (y in 0 until (size.height / spacing).toInt() + 2) {
                val centerX = x * spacing
                val centerY = y * spacing
                val halfSize = spacing / 4
                
                // Draw diamond
                val path = Path().apply {
                    moveTo(centerX, centerY - halfSize)
                    lineTo(centerX + halfSize, centerY)
                    lineTo(centerX, centerY + halfSize)
                    lineTo(centerX - halfSize, centerY)
                    close()
                }
                
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = strokeWidthPx)
                )
            }
        }
    }
}

/**
 * Rotating sun rays animation (inspired by the Philippine sun)
 */
@Composable
fun SunRaysAnimation(
    modifier: Modifier = Modifier,
    rayColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sunRays")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier.drawBehind {
            rotate(rotation) {
                drawSunRays(this, rayColor)
            }
        }
    )
}

private fun drawSunRays(
    drawScope: DrawScope,
    color: Color
) {
    with(drawScope) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(size.width, size.height) / 3
        
        repeat(8) { i ->
            val angle = (i * 45f) * (Math.PI / 180f)
            val startX = centerX + cos(angle).toFloat() * (radius * 0.7f)
            val startY = centerY + sin(angle).toFloat() * (radius * 0.7f)
            val endX = centerX + cos(angle).toFloat() * radius
            val endY = centerY + sin(angle).toFloat() * radius
            
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}