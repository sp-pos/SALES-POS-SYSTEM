package com.example.salespossystem.ui

import androidx.compose.foundation.clickable
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
import com.example.salespossystem.data.ProductItem
import com.example.salespossystem.viewmodel.SalesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(viewModel: SalesViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductItem?>(null) }

    val filteredProducts = if (searchQuery.isEmpty()) {
        viewModel.products
    } else {
        viewModel.products.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || it.barcode.contains(searchQuery) 
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Inventory", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.loadDataFromDatabase() }) {
                        Icon(Icons.Default.Refresh, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            StockFooter(viewModel)
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp)
            )

            // Stock List
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                items(filteredProducts) { item ->
                    val qty = viewModel.stockMap[item.barcode] ?: 0.0
                    StockListItem(
                        item = item,
                        quantity = qty,
                        isSelected = selectedProduct?.barcode == item.barcode,
                        onClick = { selectedProduct = item }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun StockListItem(item: ProductItem, quantity: Double, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color.Black) else null,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Barcode: ${item.barcode}", color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = quantity.toString(),
                    color = if (quantity < 0) Color.Red else Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text("In Stock", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun StockFooter(viewModel: SalesViewModel) {
    val totalValue = viewModel.products.sumOf { 
        (it.salePrice.toDoubleOrNull() ?: 0.0) * (viewModel.stockMap[it.barcode] ?: 0.0) 
    }

    Surface(
        color = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total Stock Value:", fontWeight = FontWeight.Medium)
            Text(
                "${viewModel.currencySymbol}${totalValue}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color.Black
            )
        }
    }
}
