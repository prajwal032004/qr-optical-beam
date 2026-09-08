package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AirQrLogo(
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(size * 0.28f))
            .clip(RoundedCornerShape(size * 0.28f))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF091220),
                        Color(0xFF0D1E34),
                        Color(0xFF071524)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00E5FF),
                        Color(0xFF64FFDA),
                        Color(0xFF00B0FF).copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(size * 0.28f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.76f)) {
            val w = this.size.width
            val h = this.size.height
            val cyan = Color(0xFF00E5FF)
            val mint = Color(0xFF64FFDA)

            val finderSize = w * 0.30f
            val strokeW = w * 0.07f

            // Top-Left Finder
            drawRoundRect(
                color = cyan,
                topLeft = Offset(0f, 0f),
                size = Size(finderSize, finderSize),
                cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
                style = Stroke(width = strokeW)
            )
            drawRect(
                color = mint,
                topLeft = Offset(finderSize * 0.32f, finderSize * 0.32f),
                size = Size(finderSize * 0.36f, finderSize * 0.36f)
            )

            // Top-Right Finder
            drawRoundRect(
                color = cyan,
                topLeft = Offset(w - finderSize, 0f),
                size = Size(finderSize, finderSize),
                cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
                style = Stroke(width = strokeW)
            )
            drawRect(
                color = mint,
                topLeft = Offset(w - finderSize + finderSize * 0.32f, finderSize * 0.32f),
                size = Size(finderSize * 0.36f, finderSize * 0.36f)
            )

            // Bottom-Left Finder
            drawRoundRect(
                color = cyan,
                topLeft = Offset(0f, h - finderSize),
                size = Size(finderSize, finderSize),
                cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
                style = Stroke(width = strokeW)
            )
            drawRect(
                color = mint,
                topLeft = Offset(finderSize * 0.32f, h - finderSize + finderSize * 0.32f),
                size = Size(finderSize * 0.36f, finderSize * 0.36f)
            )

            // Central Photon Diamond
            val cx = w / 2f
            val cy = h / 2f
            val dRadius = w * 0.22f

            val diamondPath = Path().apply {
                moveTo(cx, cy - dRadius)
                lineTo(cx + dRadius, cy)
                lineTo(cx, cy + dRadius)
                lineTo(cx - dRadius, cy)
                close()
            }
            drawPath(diamondPath, color = cyan)

            val innerPath = Path().apply {
                val innerR = dRadius * 0.6f
                moveTo(cx, cy - innerR)
                lineTo(cx + innerR, cy)
                lineTo(cx, cy + innerR)
                lineTo(cx - innerR, cy)
                close()
            }
            drawPath(innerPath, color = Color(0xFF091220))

            // White central optical emitter
            drawCircle(
                color = Color.White,
                radius = dRadius * 0.25f,
                center = Offset(cx, cy)
            )

            // Bottom-Right high tech micro matrix dots
            val dotSize = w * 0.07f
            val gap = w * 0.10f
            val brStartX = w * 0.68f
            val brStartY = h * 0.68f

            drawRect(color = mint, topLeft = Offset(brStartX, brStartY), size = Size(dotSize, dotSize))
            drawRect(color = cyan, topLeft = Offset(brStartX + gap, brStartY), size = Size(dotSize, dotSize))
            drawRect(color = cyan, topLeft = Offset(brStartX, brStartY + gap), size = Size(dotSize, dotSize))
            drawRect(color = mint, topLeft = Offset(brStartX + gap, brStartY + gap), size = Size(dotSize, dotSize))
        }
    }
}
