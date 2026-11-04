package com.nagarlens.app.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NagarLightBlue,
    onPrimary = FieldTextLight,
    primaryContainer = NagarBlue,
    onPrimaryContainer = FieldTextLight,
    secondary = SignalAmber,
    onSecondary = FieldTextDark,
    tertiary = SafetyGreen,
    error = AlertRed,
    background = FieldSurfaceDark,
    onBackground = FieldTextLight,
    surface = FieldSurfaceDark,
    onSurface = FieldTextLight,
    surfaceVariant = NagarBlue,
    onSurfaceVariant = FieldTextLight
)

private val LightColorScheme = lightColorScheme(
    primary = NagarNavy,
    onPrimary = FieldTextLight,
    primaryContainer = NagarBlue,
    onPrimaryContainer = FieldTextLight,
    secondary = SignalAmber,
    onSecondary = FieldTextLight,
    tertiary = SafetyGreen,
    error = AlertRed,
    background = FieldSurfaceLight,
    onBackground = FieldTextDark,
    surface = FieldSurfaceLight,
    onSurface = FieldTextDark,
    surfaceVariant = FieldSurfaceLight,
    onSurfaceVariant = FieldTextDark
)

@Composable
fun NagarlensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default for high-contrast field consistency
    content: @Composable () -> Unit
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
        typography = NagarlensTypography,
        shapes = NagarlensShapes,
        content = content
    )
}
