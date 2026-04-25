package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salespossystem.data.Sale
import com.example.salespossystem.ui.theme.DashboardBackground
import com.example.salespossystem.viewmodel.SaleViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SalesReportScreen(saleViewModel: SaleViewModel, salesViewModel: SalesViewModel, adminId: String) {
    var saleToDelete by remember { mutableStateOf<Sale?>(null) }
    var showPasswordPrompt by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        saleViewModel.fetchSalesReport(adminId)
    }

    val filteredSales = saleViewModel.salesList.filter { sale ->
        sale.saleId.contains(searchQuery, ignoreCase = true) ||
        sale.staffName.contains(searchQuery, ignoreCase = true) ||
        sale.customerName.contains(searchQuery, ignoreCase = true) ||
        sale.payableAmount.toString().contains(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Sales Report",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "${filteredSales.size} Transactions",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Income Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00C853)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Total Income", color = Color.White, fontSize = 14.sp)
                }
                Text(
                    "${salesViewModel.currencySymbol} ${String.format("%.2f", filteredSales.sumOf { it.payableAmount })}",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by ID, Staff, or Customer", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00C853),
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = Color(0xFF00C853)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Transaction History", color = Color.LightGray, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        
        Spacer(modifier = Modifier.height(8.dp))

        if (filteredSales.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, null, tint = Color.DarkGray, modifier = Modifier.size(64.dp))
                    Text("No transactions found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(filteredSales) { sale ->
                    SaleReportItem(
                        sale = sale,
                        currency = salesViewModel.currencySymbol,
                        onDeleteClick = {
                            saleToDelete = sale
                            showPasswordPrompt = true
                        }
                    )
                }
            }
        }
    }

    if (showPasswordPrompt) {
        SecurityPasswordDialog(
            viewModel = salesViewModel,
            onDismiss = { showPasswordPrompt = false },
            onVerified = {
                showPasswordPrompt = false
                saleToDelete?.let { sale ->
                    saleViewModel.deleteSale(sale.saleId, {}, {})
                }
            }
        )
    }
}

@Composable
fun SaleReportItem(sale: Sale, currency: String, onDeleteClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateString = if (sale.timestamp != null) sdf.format(sale.timestamp.toDate()) else "N/A"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF0091EA).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Receipt, 
                            contentDescription = null, 
                            tint = Color(0xFF0091EA), 
                            modifier = Modifier.padding(8.dp).size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "ID: ${sale.saleId.takeLast(6).uppercase()}", 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(dateString, color = Color.Gray, fontSize = 11.sp)
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("$currency ${String.format("%.2f", sale.payableAmount)}", color = Color(0xFF00C853), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Staff: ${sale.staffName}", color = Color.LightGray, fontSize = 12.sp)
                }
                
                if (sale.customerName.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sale.customerName, color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
