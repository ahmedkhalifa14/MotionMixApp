package com.ahmedkhalifa.motionmix.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppMainColor,           // Main brand color for buttons, tabs, and highlights
    onPrimary = Color.Black,      // Text and icons on primary color (e.g., button text)
    secondary = PurpleGrey80,     // Accent color for secondary actions (e.g., progress bars)
    onSecondary = Color.Black,    // Text and icons on secondary color (e.g., secondary button text)
    tertiary = Pink80,            // Additional accent for charts or decorative elements
    onTertiary = Color.Black,     // Text and icons on tertiary color (e.g., chart labels)
    background = Color(0xFF121212), // App's overall background color (root canvas)
    onBackground = Color.White,   // Text and icons on background (e.g., app title)
    surface = Color(0xFF121212),  // Background for cards, dialogs, and elevated surfaces
    onSurface = Color.White,      // Text and icons on surface (e.g., body text in cards)
    surfaceVariant = Color(0xFF2C2C2C), // Subtle background for dividers or secondary surfaces
    onSurfaceVariant = Color(0xFFB0B0B0), // Secondary text on surfaceVariant (e.g., hints, labels)
    error = Color(0xFFCF6679),    // Color for error states (e.g., error messages)
    onError = Color.Black         // Text and icons on error color (e.g., error message text)
)

private val LightColorScheme = lightColorScheme(
    primary = AppMainColor,           // Main brand color for buttons, tabs, and highlights
    onPrimary = Color.White,      // Text and icons on primary color (e.g., button text)
    secondary = PurpleGrey40,     // Accent color for secondary actions (e.g., progress bars)
    onSecondary = Color.White,    // Text and icons on secondary color (e.g., secondary button text)
    tertiary = Pink40,            // Additional accent for charts or decorative elements
    onTertiary = Color.White,     // Text and icons on tertiary color (e.g., chart labels)
    background = Color(0xFFFFFBFE), // App's overall background color (root canvas)
    onBackground = Color(0xFF1C1B1F), // Text and icons on background (e.g., app title)
    surface = Color(0xFFFFFBFE),  // Background for cards, dialogs, and elevated surfaces
    onSurface = Color(0xFF1C1B1F), // Text and icons on surface (e.g., body text in cards)
    surfaceVariant = Color(0xFFE0E0E0), // Subtle background for dividers or secondary surfaces
    onSurfaceVariant = Color(0xFF444444), // Secondary text on surfaceVariant (e.g., hints, labels)
    error = Color(0xFFB00020),    // Color for error states (e.g., error messages)
    onError = Color.White         // Text and icons on error color (e.g., error message text)
)

@Composable
fun MotionMixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}