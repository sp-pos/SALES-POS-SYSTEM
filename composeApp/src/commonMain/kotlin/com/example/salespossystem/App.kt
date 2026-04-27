package com.example.salespossystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.ui.*
import com.example.salespossystem.ui.theme.SALESPOSSYSTEMTheme
import com.example.salespossystem.viewmodel.SalesViewModel

sealed class Screen(val title: String, val icon: ImageVector) {
    object Login : Screen("Login", Icons.Default.Lock)
    object Signup : Screen("Signup", Icons.Default.PersonAdd)
    object Reporting : Screen("Reporting", Icons.Default.BarChart)
    object Promotions : Screen("Promotions", Icons.Default.LocalOffer)
    object Stock : Screen("Stock", Icons.Default.Inventory)
    object Customers : Screen("Partners", Icons.Default.People)
    object Expenses : Screen("Expenses", Icons.Default.Payments)
    object Items : Screen("Items", Icons.Default.AddShoppingCart)
}

@Composable
fun App() {
    val viewModel: SalesViewModel = viewModel()
    var isLoggedIn by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    
    SALESPOSSYSTEMTheme {
        Surface(color = Color(0xFFF8F9FA)) {
            if (!isLoggedIn) {
                AuthContent(
                    currentScreen = currentScreen,
                    onScreenChange = { currentScreen = it },
                    onLoginSuccess = { isLoggedIn = true; currentScreen = Screen.Reporting }
                )
            } else {
                BoxWithConstraints {
                    val isWideScreen = maxWidth > 900.dp
                    
                    Row(Modifier.fillMaxSize()) {
                        if (isWideScreen) {
                            Sidebar(
                                selectedScreen = currentScreen,
                                onScreenSelected = { currentScreen = it },
                                onLogout = { isLoggedIn = false; currentScreen = Screen.Login }
                            )
                        }
                        
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            Box(Modifier.weight(1f)) {
                                MainDashboardContent(currentScreen, viewModel)
                            }
                            
                            if (!isWideScreen) {
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
}

@Composable
fun AuthContent(currentScreen: Screen, onScreenChange: (Screen) -> Unit, onLoginSuccess: () -> Unit) {
    when (currentScreen) {
        is Screen.Login -> LoginScreen(
            onLoginSuccess = onLoginSuccess,
            onGoToSignup = { onScreenChange(Screen.Signup) },
            onLoginClick = { _, _, _, onSuccess -> onSuccess() }
        )
        is Screen.Signup -> AdminSignupScreen(
            onSignupSuccess = onLoginSuccess,
            onBackToLogin = { onScreenChange(Screen.Login) },
            onRegisterClick = { _, _, _, _, _, onSuccess -> onSuccess() }
        )
        else -> onScreenChange(Screen.Login)
    }
}

@Composable
fun MainDashboardContent(currentScreen: Screen, viewModel: SalesViewModel) {
    when (currentScreen) {
        is Screen.Reporting -> ReportingScreen()
        is Screen.Promotions -> PromotionsScreen(emptyList(), emptyList(), {_,_,_,_,_ ->}, {}, {_,_ ->}, {})
        is Screen.Stock -> StockScreen(viewModel)
        is Screen.Customers -> CustomerSupplierScreen()
        is Screen.Expenses -> ExpenseScreen()
        is Screen.Items -> ItemDataEntryScreen()
        else -> ReportingScreen()
    }
}

@Composable
fun Sidebar(selectedScreen: Screen, onScreenSelected: (Screen) -> Unit, onLogout: () -> Unit) {
    val navScreens = listOf(Screen.Reporting, Screen.Promotions, Screen.Stock, Screen.Customers, Screen.Expenses, Screen.Items)
    
    Column(
        Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Calculate, null, Modifier.size(32.dp), tint = Color.Black)
            Spacer(Modifier.width(12.dp))
            Text("SP POS", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(48.dp))
        
        navScreens.forEach { screen ->
            SidebarItem(
                screen = screen,
                isSelected = selectedScreen == screen,
                onClick = { onScreenSelected(screen) }
            )
            Spacer(Modifier.height(8.dp))
        }
        
        Spacer(Modifier.weight(1f))
        
        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red)
            Spacer(Modifier.width(12.dp))
            Text("Logout", color = Color.Red)
        }
    }
}

@Composable
fun SidebarItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) Color.Black else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(screen.icon, null, tint = if (isSelected) Color.White else Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Text(screen.title, color = if (isSelected) Color.White else Color.Black, fontSize = 15.sp)
        }
    }
}

@Composable
fun BottomNavBar(selectedScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    val navScreens = listOf(Screen.Reporting, Screen.Promotions, Screen.Stock, Screen.Customers, Screen.Expenses)
    
    NavigationBar(containerColor = Color.White) {
        navScreens.forEach { screen ->
            NavigationBarItem(
                selected = selectedScreen == screen,
                onClick = { onScreenSelected(screen) },
                icon = { Icon(screen.icon, null) },
                label = { Text(screen.title, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    indicatorColor = Color(0xFFEEEEEE)
                )
            )
        }
    }
}
