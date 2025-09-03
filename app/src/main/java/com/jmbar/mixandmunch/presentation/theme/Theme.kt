package com.jmbar.mixandmunch.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Filipino-inspired colors as defined in colors.xml
private val FilipinoRed = Color(0xFFD32F2F)
private val FilipinoYellow = Color(0xFFFFA000)
private val FilipinoBlue = Color(0xFF1976D2)

private val LightColorScheme = lightColorScheme(
    primary = FilipinoRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFCDD2),
    onPrimaryContainer = Color(0xFF2E0000),
    secondary = FilipinoYellow,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFF3E2723),
    tertiary = FilipinoBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBBDEFB),
    onTertiaryContainer = Color(0xFF0D47A1)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFFFE0B2),
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFF5D4037),
    onSecondaryContainer = Color(0xFFFFCCBC),
    tertiary = Color(0xFFBBDEFB),
    onTertiary = Color(0xFF0D47A1),
    tertiaryContainer = Color(0xFF1565C0),
    onTertiaryContainer = Color(0xFFE3F2FD)
)

@Composable
fun MixAndMunchTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}