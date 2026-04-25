package com.example.salespossystem

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.data.UserItem
import com.example.salespossystem.ui.*
import com.example.salespossystem.viewmodel.SaleViewModel
import com.example.salespossystem.ui.theme.SALESPOSSYSTEMTheme
import com.example.salespossystem.viewmodel.SalesViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val salesViewModel: SalesViewModel = viewModel()
            val saleViewModel: SaleViewModel = viewModel()
            
            val isAdmin = salesViewModel.currentUser?.accessLevel == "1"
            
            SALESPOSSYSTEMTheme(isAdmin = isAdmin) {
                val context = LocalContext.current
                val sharedPreferences = remember { 
                    context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) 
                }
                
                var isLoggedIn by remember { 
                    mutableStateOf(sharedPreferences.getBoolean("is_logged_in", false)) 
                }
                
                var showAdminSignup by remember { mutableStateOf(false) }

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn && salesViewModel.currentUser == null) {
                        val lastUser = sharedPreferences.getString("last_username", null)
                        val lastUid = sharedPreferences.getString("last_uid", "") ?: ""
                        val isLastAdmin = sharedPreferences.getBoolean("is_last_admin", false)
                        val lastAdminId = sharedPreferences.getString("last_admin_id", "") ?: ""
                        
                        if (lastUser != null) {
                            if (isLastAdmin) {
                                salesViewModel.currentUser = UserItem(
                                    firstName = lastUser,
                                    lastName = "****",
                                    email = "Admin",
                                    accessLevel = "1",
                                    active = true,
                                    uid = lastUid,
                                    adminId = lastUid
                                )
                            } else {
                                val lastEmail = sharedPreferences.getString("last_email", "") ?: ""
                                salesViewModel.currentUser = UserItem(
                                    firstName = lastUser,
                                    lastName = "****",
                                    email = lastEmail,
                                    accessLevel = "9",
                                    active = true,
                                    uid = lastUid,
                                    adminId = lastAdminId
                                )
                            }
                        }
                    }
                }

                if (isLoggedIn) {
                    SALESPOSSYSTEMApp(
                        viewModel = salesViewModel, 
                        onLogout = { 
                            salesViewModel.clearAllData()
                            sharedPreferences.edit()
                                .putBoolean("is_logged_in", false)
                                .remove("last_username")
                                .remove("last_uid")
                                .remove("last_email")
                                .remove("is_last_admin")
                                .remove("last_admin_id")
                                .apply()
                            isLoggedIn = false 
                        }
                    )
                } else if (showAdminSignup) {
                    AdminSignupScreen(
                        onSignupSuccess = { showAdminSignup = false },
                        onBackToLogin = { showAdminSignup = false },
                        viewModel = saleViewModel
                    )
                } else {
                    LoginScreen(
                        viewModel = salesViewModel,
                        saleViewModel = saleViewModel,
                        onLogin = { user ->
                            sharedPreferences.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("last_username", user.firstName)
                                .putString("last_uid", user.uid)
                                .putString("last_email", user.email)
                                .putBoolean("is_last_admin", false)
                                .putString("last_admin_id", user.adminId)
                                .apply()
                            salesViewModel.currentUser = user
                            isLoggedIn = true 
                        },
                        onAdminLogin = { adminData ->
                            val adminName = adminData["name"] as? String ?: "Admin"
                            val adminUid = adminData["uid"] as? String ?: ""
                            val adminEmail = adminData["email"] as? String ?: ""
                            sharedPreferences.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("last_username", adminName)
                                .putString("last_uid", adminUid)
                                .putString("last_email", adminEmail)
                                .putBoolean("is_last_admin", true)
                                .putString("last_admin_id", adminUid)
                                .apply()
                            salesViewModel.currentUser = UserItem(
                                firstName = adminName,
                                lastName = "****",
                                email = adminEmail,
                                accessLevel = "1",
                                active = true,
                                uid = adminUid,
                                adminId = adminUid
                            )
                            isLoggedIn = true
                        },
                        onNavigateToAdminSignup = { showAdminSignup = true }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SALESPOSSYSTEMApp(viewModel: SalesViewModel, onLogout: () -> Unit) {
    val isAdmin = viewModel.currentUser?.accessLevel == "1"
    val initialDestination = if (isAdmin) AppDestinations.DASHBOARD else AppDestinations.HOME
    
    // Navigation History Stack
    val navigationHistory = remember { mutableStateListOf<AppDestinations>() }
    var currentDestination by rememberSaveable { mutableStateOf(initialDestination) }
    
    // Helper function for navigation that keeps track of history
    val navigateTo: (AppDestinations) -> Unit = { destination ->
        if (currentDestination != destination) {
            navigationHistory.add(currentDestination)
            currentDestination = destination
        }
    }

    // Helper for back navigation
    val goBack: () -> Unit = {
        if (navigationHistory.isNotEmpty()) {
            currentDestination = navigationHistory.removeAt(navigationHistory.size - 1)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Handle system back button
    BackHandler(enabled = navigationHistory.isNotEmpty()) {
        goBack()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.width(300.dp).verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    if (isAdmin) "ADMIN PANEL" else "SALES POS",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                viewModel.currentUser?.let { user ->
                    Text(
                        "User: ${user.firstName} (${if(isAdmin) "Admin" else "Staff"})",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
                
                HorizontalDivider(color = Color.DarkGray)
                
                NavigationDrawerItem(
                    label = { Text("Profile", color = Color.White) },
                    selected = currentDestination == AppDestinations.PROFILE,
                    onClick = { navigateTo(AppDestinations.PROFILE); scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.AccountCircle, "Profile", tint = Color.White) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )

                NavigationDrawerItem(
                    label = { Text("Logout", color = Color.White) },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close()
                            onLogout() 
                        } 
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color.White) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                val filteredDestinations = AppDestinations.entries.filter { destination ->
                    if (isAdmin) {
                        true
                    } else {
                        !destination.isAdminOnly
                    }
                }

                for (destination in filteredDestinations) {
                    if (destination == AppDestinations.PROFILE) continue
                    NavigationDrawerItem(
                        label = { Text(destination.label, color = Color.White) },
                        selected = currentDestination == destination,
                        onClick = { navigateTo(destination); scope.launch { drawerState.close() } },
                        icon = { Icon(destination.icon, destination.label, tint = if(currentDestination == destination) Color.White else Color.Gray) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            unselectedContainerColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentDestination.label) },
                    navigationIcon = {
                        val isAtRoot = navigationHistory.isEmpty()
                        if (isAtRoot) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        } else {
                            IconButton(onClick = { goBack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    AppDestinations.DASHBOARD -> ManagementDashboard(viewModel = viewModel, onNavigate = { navigateTo(it) })
                    AppDestinations.HOME -> HomeScreen(onNavigate = { navigateTo(it) })
                    AppDestinations.SALE -> SaleScreen(viewModel = viewModel, onNavigate = { navigateTo(it) })
                    AppDestinations.EXPENSES -> ExpenseScreen(viewModel = viewModel, onBack = { goBack() })
                    AppDestinations.MANAGEMENT -> {
                        val authViewModel: com.example.salespossystem.viewmodel.AuthViewModel = viewModel()
                        val adminId = viewModel.currentUser?.adminId ?: ""
                        AdminStaffManagementScreen(authViewModel = authViewModel, adminId = adminId, salesViewModel = viewModel)
                    }
                    AppDestinations.DOCUMENTS -> DocumentScreen(viewModel = viewModel, onNavigate = { navigateTo(it) })
                    AppDestinations.PRODUCTS -> ProductScreen(viewModel = viewModel, onNavigateToItemEntry = { navigateTo(AppDestinations.ITEM_DATA_ENTRY) })
                    AppDestinations.STOCK -> StockScreen(viewModel = viewModel)
                    AppDestinations.REPORTING -> ReportingScreen()
                    AppDestinations.CUSTOMERS_SUPPLIERS -> CustomerSupplierScreen()
                    AppDestinations.PROMOTIONS -> PromotionsScreen()
                    AppDestinations.USERS_SECURITY -> UsersSecurityScreen(viewModel = viewModel)
                    AppDestinations.PAYMENT_TYPES -> PaymentTypesScreen(viewModel = viewModel)
                    AppDestinations.MY_COMPANY -> MyCompanyScreen()
                    AppDestinations.PURCHASE_INVOICE -> PurchaseInvoiceScreen(viewModel = viewModel, onNavigate = { navigateTo(it) })
                    AppDestinations.ITEM_DATA_ENTRY -> ItemDataEntryScreen(viewModel = viewModel, onBack = { goBack() })
                    AppDestinations.CLOUD_BACKUP -> CloudBackupScreen(viewModel = viewModel)
                    AppDestinations.PROFILE -> ProfileScreen(viewModel = viewModel, onLogout = onLogout)
                    AppDestinations.COUNTRIES -> CountriesScreen(viewModel = viewModel)
                    AppDestinations.PRODUCT_GALLERY -> ProductGalleryScreen(
                        viewModel = viewModel, 
                        onBack = { goBack() }, 
                        onNavigateToSale = { navigateTo(AppDestinations.SALE) }
                    )
                    AppDestinations.PRINT_STATIONS -> PrintStationsScreen(viewModel = viewModel)
                    AppDestinations.SALES_REPORT -> {
                        val saleViewModel: com.example.salespossystem.viewmodel.SaleViewModel = viewModel()
                        val adminId = viewModel.currentUser?.adminId ?: ""
                        SalesReportScreen(saleViewModel = saleViewModel, salesViewModel = viewModel, adminId = adminId)
                    }
                    AppDestinations.TAX_RATES -> TaxRatesScreen(viewModel = viewModel)
                    AppDestinations.DAMAGE_PRODUCT -> DamageProductScreen(viewModel = viewModel)
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val isAdminOnly: Boolean = false
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, isAdminOnly = true),
    HOME("Home", Icons.Default.Home),
    PRODUCT_GALLERY("Gallery", Icons.Default.GridView),
    SALE("Sale", Icons.Default.ShoppingCart),
    EXPENSES("Expenses", Icons.Default.AccountBalanceWallet),
    MANAGEMENT("Staff Management", Icons.Default.ManageAccounts, isAdminOnly = true),
    DOCUMENTS("Documents", Icons.Default.Description),
    PRODUCTS("Products", Icons.Default.Inventory),
    PURCHASE_INVOICE("Purchase Invoice", Icons.Default.AddShoppingCart),
    ITEM_DATA_ENTRY("Item Data Entry", Icons.Default.AddBox),
    STOCK("Stock", Icons.Default.Storage),
    DAMAGE_PRODUCT("Damage Products", Icons.Default.BrokenImage),
    REPORTING("Reporting", Icons.Default.BarChart, isAdminOnly = false),
    SALES_REPORT("Sales Report", Icons.Default.Assessment, isAdminOnly = true),
    CUSTOMERS_SUPPLIERS("Customers", Icons.Default.People),
    PROMOTIONS("Promotions", Icons.Default.Favorite),
    USERS_SECURITY("System Users", Icons.Default.Security, isAdminOnly = true),
    PAYMENT_TYPES("Payment types", Icons.Default.Payment, isAdminOnly = true),
    TAX_RATES("Tax Rates", Icons.Default.Calculate, isAdminOnly = false),
    MY_COMPANY("My company", Icons.Default.Business),
    PRINT_STATIONS("Print Stations", Icons.Default.Print),
    COUNTRIES("Countries", Icons.Default.Public),
    CLOUD_BACKUP("Backup to Cloud", Icons.Default.CloudUpload, isAdminOnly = true),
    PROFILE("Profile", Icons.Default.Person),
}
