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
import com.example.salespossystem.viewmodel.SalesViewModel

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
                Text("$title Module - Integration Active", color = Color.Gray)
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
            item { StatCard("Total Sales", "0.00", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF4CAF50)) }
            item { StatCard("Total Orders", "0", Icons.Default.ShoppingCart, Color(0xFF2196F3)) }
            item { StatCard("Today's Expenses", "0.00", Icons.Default.AccountBalanceWallet, Color(0xFFF44336)) }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, color = Color.Gray, fontSize = 14.sp)
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable fun AdminStaffManagementScreen() = ModulePlaceholder("Staff Management", Icons.Default.Badge)
@Composable fun CountriesScreen() = ModulePlaceholder("Countries Settings", Icons.Default.Public)
@Composable fun CustomerSupplierScreen() = ModulePlaceholder("Partners", Icons.Default.Group)
@Composable fun DamageProductScreen() = ModulePlaceholder("Damage Products", Icons.Default.BrokenImage)
@Composable fun DocumentScreen() = ModulePlaceholder("Documents", Icons.Default.Description)
@Composable fun ExpenseScreen() = ModulePlaceholder("Expenses", Icons.Default.Payments)
@Composable fun InvoiceDialogScreen() = ModulePlaceholder("Invoice Detail", Icons.Default.Receipt)
@Composable fun ItemDataEntryScreen() = ModulePlaceholder("Data Entry", Icons.Default.AddBox)
@Composable fun ManagementDashboard() = ModulePlaceholder("Management", Icons.Default.AdminPanelSettings)
@Composable fun MyCompanyScreen() = ModulePlaceholder("Company Profile", Icons.Default.Business)
@Composable fun PaymentTypesScreen() = ModulePlaceholder("Payment Methods", Icons.Default.CreditCard)
@Composable fun PriceListScreen() = ModulePlaceholder("Price List", Icons.Default.FormatListBulleted)
@Composable fun ProductGalleryScreen() = ModulePlaceholder("Product Gallery", Icons.Default.Collections)
@Composable fun ProductScreen() = ModulePlaceholder("Product List", Icons.Default.ShoppingBag)
@Composable fun ProfileScreen() = ModulePlaceholder("My Profile", Icons.Default.Person)
@Composable fun PurchaseInvoiceScreen() = ModulePlaceholder("Purchases", Icons.Default.Inventory)
@Composable fun SaleScreen() = ModulePlaceholder("Point of Sale", Icons.Default.PointOfSale)
@Composable fun SalesReportScreen() = ModulePlaceholder("Detailed Reports", Icons.Default.PieChart)
@Composable fun TaxRatesScreen() = ModulePlaceholder("Tax Rates", Icons.Default.Percent)
@Composable fun UsersSecurityScreen() = ModulePlaceholder("Security", Icons.Default.Security)
