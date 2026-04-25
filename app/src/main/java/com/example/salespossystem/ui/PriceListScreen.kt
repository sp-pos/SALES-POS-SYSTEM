package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.data.ProductItem
import com.example.salespossystem.ui.theme.*

@Composable
fun PriceListScreen(
    viewModel: SalesViewModel = viewModel(),
    onNavigateToItemEntry: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredProducts by remember(searchQuery) {
        derivedStateOf {
            if (searchQuery.isEmpty()) {
                viewModel.products
            } else {
                viewModel.products.filter { 
                    it.name.contains(searchQuery, ignoreCase = true) || 
                    it.barcode.contains(searchQuery) 
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        PriceListTopBar(onAddClick = onNavigateToItemEntry)
        PriceListSearchBarMobile(searchQuery) { searchQuery = it }
        PriceListMobile(filteredProducts)
    }
}

@Composable
fun PriceListTopBar(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBackground)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Price Lists", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onAddClick) { Icon(Icons.Default.Add, null, tint = Color.White) }
        IconButton(onClick = {}) { Icon(Icons.Default.Percent, null, tint = Color.White) }
        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
    }
}

@Composable
fun PriceListSearchBarMobile(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        placeholder = { Text("Search products...", color = Color.Gray, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
        trailingIcon = if (query.isNotEmpty()) {
            { IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Close, null, tint = Color.Gray) } }
        } else null,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Black,
            unfocusedContainerColor = Color.Black,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}

@Composable
fun PriceListMobile(products: List<ProductItem>) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No products found", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(products) { item ->
                PriceListItemMobile(item)
                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun PriceListItemMobile(item: ProductItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Code: ${item.barcode} | Cost: ${item.cost}",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                item.salePrice,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            // Markup calculation (Sale - Cost) / Cost * 100
            val cost = item.cost.toDoubleOrNull() ?: 0.0
            val sale = item.salePrice.toDoubleOrNull() ?: 0.0
            val markup = if (cost > 0) ((sale - cost) / cost * 100).toInt() else 0
            
            Text(
                "Markup: $markup%",
                color = if (markup > 0) Color.Green else Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
fun PriceListScreenMobilePreview() {
    PriceListScreen()
}
