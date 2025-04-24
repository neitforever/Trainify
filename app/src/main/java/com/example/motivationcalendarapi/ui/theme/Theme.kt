package com.example.motivationcalendarapi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.motivationcalendarapi.viewmodel.MainViewModel

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFD700),
    secondary = Color(0xFFB0B0B0),
    tertiary = Color(0xFF808080),
    background = Color(0xFF1C1C1C),
    surface = Color(0xFF2D2D2D),
    onPrimary = Color(0xFF1C1C1C),
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF404040),
    errorContainer = Color(0xFFB00020),
    inverseSurface = Color(0xFFE0E0E0),
    error = Color(0xFFFF0000),
    surfaceDim = Color(0xFF1C1C1C),
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006A60),
    secondary = Color(0xFF4A635E),
    tertiary = Color(0xFF3D8479),
    background = Color(0xFFE0E0E0),
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1C1C),
    onSurface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFFEEEEEE),
    inverseSurface = Color(0xFF2D2D2D),
    errorContainer = Color(0xFFFCD8DF),
    error = Color(0xFFFF0000),
    onErrorContainer = Color(0xFFFF0000),
    surfaceDim = Color(0xFF006A60),
)


@Composable
fun MotivationCalendarAPITheme(
    mainViewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    val isDarkTheme = mainViewModel.isDarkTheme
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
