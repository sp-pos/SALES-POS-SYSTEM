package com.example.salespossystem.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier

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
    darkTheme: Boolean = true, // Always dark for this POS look
    content: @Composable () -> Unit
) {
    val colorScheme = if (isAdmin) AdminColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        androidx.compose.material3.Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            color = colorScheme.background,
            content = content
        )
    }
}
