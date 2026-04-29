package com.example.salespossystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimpleModuleHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(32.dp), tint = Color.Black)
        Spacer(Modifier.width(16.dp))
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ModulePlaceholder(title: String, icon: ImageVector) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        SimpleModuleHeader(title, icon)
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("$title Module is Ready", color = Color.Gray)
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        SimpleModuleHeader("Dashboard Overview", Icons.Default.Dashboard)
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(300.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { DashboardStat("Today's Sales", "$0.00", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF4CAF50)) }
            item { DashboardStat("Orders", "0", Icons.Default.ShoppingCart, Color(0xFF2196F3)) }
            item { DashboardStat("Expenses", "$0.00", Icons.Default.AccountBalanceWallet, Color(0xFFF44336)) }
            item { DashboardStat("Low Stock", "5 Items", Icons.Default.Warning, Color(0xFFFF9800)) }
        }
    }
}

@Composable
fun DashboardStat(label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(label, color = Color.Gray, fontSize = 14.sp)
                Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 21 Screens implementation as requested
@Composable fun AdminStaffManagementScreen() = ModulePlaceholder("Admin & Staff Management", Icons.Default.Badge)
@Composable fun CountriesScreen() = ModulePlaceholder("Countries Settings", Icons.Default.Public)
@Composable fun CustomerSupplierScreen() = ModulePlaceholder("Customers & Suppliers", Icons.Default.People)
@Composable fun DamageProductScreen() = ModulePlaceholder("Damage Products", Icons.Default.BrokenImage)
@Composable fun DocumentScreen() = ModulePlaceholder("Documents & Invoices", Icons.Default.Description)
@Composable fun ExpenseScreen() = ModulePlaceholder("Expense Tracker", Icons.Default.Payments)
@Composable fun ItemDataEntryScreen() = ModulePlaceholder("Item Data Entry", Icons.Default.AddBox)
@Composable fun ManagementDashboard() = ModulePlaceholder("Management Dashboard", Icons.Default.Dashboard)
@Composable fun MyCompanyScreen() = ModulePlaceholder("My Company Profile", Icons.Default.Business)
@Composable fun PaymentTypesScreen() = ModulePlaceholder("Payment Methods", Icons.Default.CreditCard)
@Composable fun PriceListScreen() = ModulePlaceholder("Dynamic Price List", Icons.Default.FormatListBulleted)
@Composable fun ProductGalleryScreen() = ModulePlaceholder("Product Gallery", Icons.Default.Collections)
@Composable fun ProductScreen() = ModulePlaceholder("Product Management", Icons.Default.ShoppingBag)
@Composable fun ProfileScreen() = ModulePlaceholder("User Account", Icons.Default.Person)
@Composable fun PurchaseInvoiceScreen() = ModulePlaceholder("Purchase Invoices", Icons.Default.Inventory)
@Composable fun SaleScreen() = ModulePlaceholder("Sales Counter (POS)", Icons.Default.PointOfSale)
@Composable fun SalesReportScreen() = ModulePlaceholder("Advanced Sales Reports", Icons.Default.PieChart)
@Composable fun TaxRatesScreen() = ModulePlaceholder("Taxation Settings", Icons.Default.Percent)
@Composable fun UsersSecurityScreen() = ModulePlaceholder("Security & Permissions", Icons.Default.Security)
@Composable fun InvoiceDialogScreen() = ModulePlaceholder("Invoice Viewer", Icons.Default.ReceiptLong)
