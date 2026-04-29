package com.example.salespossystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    // Auth Screens
    object Login : Screen("Login", Icons.Default.Lock)
    object Signup : Screen("Signup", Icons.Default.PersonAdd)
    
    // Dashboard Screens
    object Home : Screen("Home Dashboard", Icons.Default.Home)
    object Sale : Screen("Point of Sale", Icons.Default.PointOfSale)
    object Products : Screen("Product Management", Icons.Default.ShoppingBag)
    object Stock : Screen("Stock Inventory", Icons.Default.Inventory)
    object Customers : Screen("Partners", Icons.Default.People)
    object Expenses : Screen("Expenses", Icons.Default.Payments)
    object Promotions : Screen("Promotions", Icons.Default.LocalOffer)
    object Reporting : Screen("Reporting", Icons.Default.BarChart)
    object ItemsEntry : Screen("Data Entry", Icons.Default.AddBox)
    object Management : Screen("Management Dashboard", Icons.Default.Dashboard)
    object Company : Screen("My Company", Icons.Default.Business)
    object PriceList : Screen("Price List", Icons.Default.FormatListBulleted)
    object TaxRates : Screen("Tax Rates", Icons.Default.Percent)
    object PaymentTypes : Screen("Payment Types", Icons.Default.CreditCard)
    object UsersSecurity : Screen("Security Settings", Icons.Default.Security)
    object AdminStaff : Screen("Staff Management", Icons.Default.Badge)
    object DamageProducts : Screen("Damage Products", Icons.Default.BrokenImage)
    object Documents : Screen("Documents", Icons.Default.Description)
}

@Composable
fun App() {
    val viewModel: SalesViewModel = viewModel()
    var isLoggedIn by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    
    SALESPOSSYSTEMTheme {
        Surface(color = Color(0xFFF8F9FA)) {
            if (!isLoggedIn) {
                AuthFlow(
                    currentScreen = currentScreen,
                    onScreenChange = { currentScreen = it },
                    onLoginSuccess = { isLoggedIn = true; currentScreen = Screen.Home }
                )
            } else {
                DashboardFlow(
                    currentScreen = currentScreen,
                    onScreenChange = { currentScreen = it },
                    onLogout = { isLoggedIn = false; currentScreen = Screen.Login },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun AuthFlow(currentScreen: Screen, onScreenChange: (Screen) -> Unit, onLoginSuccess: () -> Unit) {
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
fun DashboardFlow(currentScreen: Screen, onScreenChange: (Screen) -> Unit, onLogout: () -> Unit, viewModel: SalesViewModel) {
    BoxWithConstraints {
        val isWideScreen = maxWidth > 1100.dp
        
        Row(Modifier.fillMaxSize()) {
            Sidebar(
                selectedScreen = currentScreen,
                onScreenSelected = onScreenChange,
                onLogout = onLogout,
                isCollapsed = !isWideScreen
            )
            
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Box(Modifier.weight(1f)) {
                    DashboardRouter(currentScreen, viewModel)
                }
            }
        }
    }
}

@Composable
fun DashboardRouter(screen: Screen, viewModel: SalesViewModel) {
    when (screen) {
        is Screen.Home -> HomeScreen()
        is Screen.Sale -> SaleScreen()
        is Screen.Products -> ProductScreen()
        is Screen.Stock -> StockScreen(viewModel)
        is Screen.Customers -> CustomerSupplierScreen()
        is Screen.Expenses -> ExpenseScreen()
        is Screen.Promotions -> PromotionsScreen(emptyList(), emptyList(), {_,_,_,_,_ ->}, {}, {_,_ ->}, {})
        is Screen.Reporting -> ReportingScreen()
        is Screen.ItemsEntry -> ItemDataEntryScreen()
        is Screen.Management -> ManagementDashboard()
        is Screen.Company -> MyCompanyScreen()
        is Screen.PriceList -> PriceListScreen()
        is Screen.TaxRates -> TaxRatesScreen()
        is Screen.PaymentTypes -> PaymentTypesScreen()
        is Screen.UsersSecurity -> UsersSecurityScreen()
        is Screen.AdminStaff -> AdminStaffManagementScreen()
        is Screen.DamageProducts -> DamageProductScreen()
        is Screen.Documents -> DocumentScreen()
        else -> HomeScreen()
    }
}

@Composable
fun Sidebar(selectedScreen: Screen, onScreenSelected: (Screen) -> Unit, onLogout: () -> Unit, isCollapsed: Boolean) {
    val menuItems = listOf(
        Screen.Home, Screen.Sale, Screen.Products, Screen.Stock, 
        Screen.DamageProducts, Screen.Documents, Screen.Customers, 
        Screen.Expenses, Screen.Promotions, Screen.Reporting,
        Screen.ItemsEntry, Screen.Management, Screen.Company, Screen.PriceList,
        Screen.TaxRates, Screen.PaymentTypes, Screen.UsersSecurity, Screen.AdminStaff
    )
    
    val sidebarWidth = if (isCollapsed) 80.dp else 280.dp
    
    Column(
        Modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(Color.White)
            .padding(vertical = 24.dp, horizontal = if (isCollapsed) 8.dp else 16.dp)
    ) {
        // App Logo/Name
        if (!isCollapsed) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(Icons.Default.Calculate, null, Modifier.size(32.dp), tint = Color.Black)
                Spacer(Modifier.width(12.dp))
                Text("SP POS", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Icon(Icons.Default.Calculate, null, Modifier.size(32.dp).align(Alignment.CenterHorizontally), tint = Color.Black)
        }
        
        Spacer(Modifier.height(32.dp))
        
        // Navigation Items
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            menuItems.forEach { screen ->
                SidebarItem(
                    screen = screen,
                    isSelected = selectedScreen == screen,
                    onClick = { onScreenSelected(screen) },
                    isCollapsed = isCollapsed
                )
                Spacer(Modifier.height(4.dp))
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Logout Button
        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red)
            if (!isCollapsed) {
                Spacer(Modifier.width(12.dp))
                Text("Logout", color = Color.Red)
            }
        }
    }
}

@Composable
fun SidebarItem(screen: Screen, isSelected: Boolean, onClick: () -> Unit, isCollapsed: Boolean) {
    Surface(
        color = if (isSelected) Color.Black else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp), 
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start
        ) {
            Icon(
                screen.icon, 
                null, 
                tint = if (isSelected) Color.White else Color.Gray, 
                modifier = Modifier.size(22.dp)
            )
            if (!isCollapsed) {
                Spacer(Modifier.width(16.dp))
                Text(
                    screen.title, 
                    color = if (isSelected) Color.White else Color.Black, 
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}
