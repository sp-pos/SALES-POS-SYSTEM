package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.AppDestinations
import com.example.salespossystem.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(viewModel: SalesViewModel = viewModel(), onNavigate: (AppDestinations) -> Unit) {
    val isAdmin = viewModel.currentUser?.accessLevel == "1"
    val context = LocalContext.current

    val primaryMenuItems = if (isAdmin) {
        listOf(
            AppDestinations.PRODUCT_GALLERY,
            AppDestinations.MANAGEMENT,
            AppDestinations.REPORTING,
            AppDestinations.PRODUCTS,
            AppDestinations.STOCK,
            AppDestinations.PURCHASE_INVOICE,
            AppDestinations.EXPENSES,
            AppDestinations.CUSTOMERS_SUPPLIERS,
            AppDestinations.DOCUMENTS
        )
    } else {
        listOf(
            AppDestinations.SALE,
            AppDestinations.PRODUCT_GALLERY,
            AppDestinations.PRODUCTS,
            AppDestinations.STOCK,
            AppDestinations.DOCUMENTS,
            AppDestinations.CUSTOMERS_SUPPLIERS,
            AppDestinations.EXPENSES
        )
    }

    val moreMenuItems = if (isAdmin) {
        listOf(
            AppDestinations.PROMOTIONS,
            AppDestinations.USERS_SECURITY,
            AppDestinations.PAYMENT_TYPES,
            AppDestinations.MY_COMPANY,
            AppDestinations.CLOUD_BACKUP
        )
    } else {
        listOf(
            AppDestinations.PROMOTIONS,
            AppDestinations.MY_COMPANY
        )
    }

    var showMenu by remember { mutableStateOf(false) }

    // Summary Calculations
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    val today = sdf.format(Date())
    
    val todayInvoices = viewModel.allInvoices.filter { it.date.startsWith(today) && !it.isPurchase }
    val todaySalesTotal = todayInvoices.sumOf { it.totalAmount }
    val todayOrdersCount = todayInvoices.size
    val todayExpensesTotal = viewModel.expenses.filter { it.date.startsWith(today) }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBackground)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAdmin) "ADMIN PANEL" else "SALES POS SYSTEM",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Welcome, ${viewModel.currentUser?.firstName ?: "User"}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { 
                    viewModel.refreshAllData()
                    Toast.makeText(context, "Refreshing data...", Toast.LENGTH_SHORT).show()
                }) {
                    if (viewModel.isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SidebarSelected, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                }
                IconButton(onClick = { /* Notifications */ }) {
                    Icon(Icons.Default.Notifications, null, tint = Color.White)
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Today's Summary Section
            item(span = { GridItemSpan(2) }) {
                TodaySummarySection(
                    salesTotal = todaySalesTotal,
                    ordersCount = todayOrdersCount,
                    expensesTotal = todayExpensesTotal,
                    currency = viewModel.currencySymbol
                )
            }

            item(span = { GridItemSpan(2) }) {
                Text(
                    if (isAdmin) "Management & Tools" else "Quick Access",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(primaryMenuItems) { item ->
                HomeMenuItem(
                    label = item.label,
                    icon = item.icon,
                    accentColor = when {
                        item == AppDestinations.SALE -> AccentGreen
                        item == AppDestinations.MANAGEMENT -> Color(0xFF4CAF50)
                        else -> SidebarSelected
                    }
                ) { onNavigate(item) }
            }

            item(span = { GridItemSpan(2) }) {
                Box(contentAlignment = Alignment.Center) {
                    HomeMenuItem(
                        label = "Settings & More",
                        icon = Icons.Default.Settings,
                        accentColor = Color.Gray
                    ) { showMenu = true }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(CardBackground)
                    ) {
                        moreMenuItems.forEach { destination ->
                            DropdownMenuItem(
                                text = { Text(destination.label, color = Color.White) },
                                onClick = {
                                    onNavigate(destination)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label,
                                        tint = Color.White
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun TodaySummarySection(
    salesTotal: Double,
    ordersCount: Int,
    expensesTotal: Double,
    currency: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Today's Overview",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStatItem(
                    label = "Total Sales",
                    value = String.format(Locale.US, "%.2f %s", salesTotal, currency),
                    color = Color(0xFF4CAF50)
                )
                SummaryStatItem(
                    label = "Orders",
                    value = ordersCount.toString(),
                    color = Color(0xFF2196F3)
                )
                SummaryStatItem(
                    label = "Expenses",
                    value = String.format(Locale.US, "%.2f %s", expensesTotal, currency),
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun SummaryStatItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
        Text(
            text = value,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HomeMenuItem(
    label: String, 
    icon: ImageVector, 
    accentColor: Color = SidebarSelected, 
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        color = CardBackground,
        shape = RoundedCornerShape(12.dp),
        border = if (label == "Sale" || label == "Management") androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)) else null,
        shadowElevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
