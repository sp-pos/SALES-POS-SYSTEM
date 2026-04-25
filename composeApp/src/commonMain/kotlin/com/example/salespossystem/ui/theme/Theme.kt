package com.example.salespossystem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SidebarSelected,
    secondary = AccentBlue,
    tertiary = AccentGreen,
    background = DashboardBackground,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val AdminColorScheme = darkColorScheme(
    primary = AdminPrimary,
    secondary = AdminSecondary,
    tertiary = AdminSidebarSelected,
    background = AdminBackground,
    surface = AdminCardBackground,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun SALESPOSSYSTEMTheme(
    isAdmin: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isAdmin) AdminColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
