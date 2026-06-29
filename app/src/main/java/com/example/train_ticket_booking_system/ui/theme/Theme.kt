package com.example.train_ticket_booking_system.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = SurfaceWhite,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue700,
    secondary = Green500,
    onSecondary = SurfaceWhite,
    secondaryContainer = Green100,
    onSecondaryContainer = Green500,
    tertiary = Orange500,
    background = BackgroundLight,
    onBackground = Gray900,
    surface = SurfaceWhite,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray300,
    error = Red500,
    onError = SurfaceWhite
)

@Composable
fun Train_Ticket_Booking_SystemTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = context as? Activity ?: return@SideEffect
            activity.window.statusBarColor = Blue600.toArgb()
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
