package com.example.ubicafii.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightDefaultColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,

    primaryContainer = BlueLight,
    onPrimaryContainer = Blue900,

    secondary = BlueLight,
    onSecondary = Blue900,

    background = Background,
    onBackground = Foreground,

    surface = Surface,
    onSurface = Foreground,

    surfaceVariant = Muted,
    onSurfaceVariant = MutedForeground,

    outline = Border,

    error = Color(0xFFB00020),
    onError = Color.White
)

private val DarkDefaultColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),

    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF0D47A1),

    background = Color(0xFF121212),
    onBackground = Color.White,

    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,

    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFB0B0B0),

    outline = Color(0xFF4D4D4D),

    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun UbicaFIITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            }

            darkTheme -> DarkDefaultColors
            else -> LightDefaultColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}