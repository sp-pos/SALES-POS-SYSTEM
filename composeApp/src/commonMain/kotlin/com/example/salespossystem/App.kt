package com.example.salespossystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salespossystem.ui.AdminSignupScreen
import com.example.salespossystem.ui.PromotionsScreen
import com.example.salespossystem.ui.ReportingScreen
import com.example.salespossystem.ui.theme.SALESPOSSYSTEMTheme

sealed class Screen(val title: String, val icon: ImageVector) {
    object AdminSignup : Screen("Signup", Icons.Default.PersonAdd)
    object Promotions : Screen("Promotions", Icons.Default.LocalOffer)
    object Reporting : Screen("Reporting", Icons.Default.BarChart)
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.AdminSignup) }
    
    SALESPOSSYSTEMTheme {
        Surface(color = Color.White) {
            BoxWithConstraints {
                val isWideScreen = maxWidth > 600.dp
                
                Row(Modifier.fillMaxSize()) {
                    if (isWideScreen) {
                        // Desktop Sidebar
                        Sidebar(
                            selectedScreen = currentScreen,
                            onScreenSelected = { currentScreen = it }
                        )
                    }
                    
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Box(Modifier.weight(1f)) {
                            when (currentScreen) {
                                is Screen.AdminSignup -> AdminSignupScreen(
                                    onSignupSuccess = { currentScreen = Screen.Reporting },
                                    onBackToLogin = { },
                                    onRegisterClick = { _, _, _, _, _, _ -> }
                                )
                                is Screen.Promotions -> PromotionsScreen(
                                    promotions = emptyList(),
                                    products = emptyList(),
                                    onAddPromotion = { _, _, _, _, _ -> },
                                    onDeletePromotion = { },
                                    onTogglePromotion = { _, _ -> },
                                    onExportPdf = { }
                                )
                                is Screen.Reporting -> ReportingScreen()
                            }
                        }
                        
                        if (!isWideScreen) {
                            // Mobile Bottom Navigation
                            BottomNavBar(
                                selectedScreen = currentScreen,
                                onScreenSelected = { currentScreen = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Sidebar(selectedScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    Column(
        Modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text("POS WEB", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(32.dp))
        
        val screens = listOf(Screen.AdminSignup, Screen.Promotions, Screen.Reporting)
        screens.forEach { screen ->
            SidebarItem(
                screen = screen,
                isSelected = selectedScreen == screen,
                onClick = { onScreenSelected(screen) }
            )
        }
    }
}

@Composable
fun SidebarItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isSelected) Color.Black else Color.Transparent, MaterialTheme.shapes.small)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(screen.icon, null, tint = if (isSelected) Color.White else Color.Black)
        Spacer(Modifier.width(12.dp))
        Text(screen.title, color = if (isSelected) Color.White else Color.Black, fontSize = 14.sp)
    }
}

@Composable
fun BottomNavBar(selectedScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    NavigationBar(containerColor = Color(0xFFF5F5F5)) {
        val screens = listOf(Screen.AdminSignup, Screen.Promotions, Screen.Reporting)
        screens.forEach { screen ->
            NavigationBarItem(
                selected = selectedScreen == screen,
                onClick = { onScreenSelected(screen) },
                icon = { Icon(screen.icon, null) },
                label = { Text(screen.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.Black,
                    indicatorColor = Color.Black,
                    unselectedIconColor = Color.DarkGray,
                    unselectedTextColor = Color.DarkGray
                )
            )
        }
    }
}
