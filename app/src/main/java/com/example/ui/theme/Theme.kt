package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val BentoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF00373E),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF80F3FF),
    secondary = Color(0xFF64FFDA),
    onSecondary = Color(0xFF00382E),
    secondaryContainer = Color(0xFF005144),
    onSecondaryContainer = Color(0xFF82FBD9),
    tertiary = Color(0xFF00E676),
    onTertiary = Color(0xFF003915),
    background = Color(0xFF080E18),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF0F1A2A),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF18263D),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334766),
    outlineVariant = Color(0xFF1E2D44),
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    onPrimary = Color.White,
    primaryContainer = BentoLightBlue,
    onPrimaryContainer = BentoNavy,
    secondary = Color(0xFF0097A7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF006064),
    tertiary = BentoSuccess,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = BentoNavy,
    surface = Color.White,
    onSurface = BentoText,
    surfaceVariant = Color(0xFFEEF2F6),
    onSurfaceVariant = BentoMuted,
    outline = BentoBorder,
    outlineVariant = BentoBorderDark,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = BentoShapes,
        content = content
    )
}


