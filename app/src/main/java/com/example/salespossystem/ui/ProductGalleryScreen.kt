package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.salespossystem.AppDestinations
import java.util.Locale

@Composable
fun ProductGalleryScreen(viewModel: SalesViewModel, onBack: () -> Unit, onNavigateToSale: () -> Unit) {
    val products = viewModel.products
    val isAdmin = viewModel.currentUser?.accessLevel == "1"
    
    // Redirect Admin if they shouldn't be here, but usually navigation handles this.
    // For now, we just implement the UI for Staff.

    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val categories = remember(products) {
        listOf("All") + products.map { it.group }.distinct().filter { it.isNotEmpty() }
    }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val filteredProducts = remember(selectedCategory, products, searchQuery) {
        val baseList = if (selectedCategory == "All") {
            products
        } else {
            products.filter { it.group == selectedCategory }
        }
        
        if (searchQuery.isEmpty()) {
            baseList
        } else {
            baseList.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.barcode.contains(searchQuery) 
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        // Top Bar
        GalleryTopBar(
            viewModel = viewModel,
            onBack = onBack,
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            isSearchExpanded = isSearchExpanded,
            onSearchToggle = { isSearchExpanded = !isSearchExpanded },
            onCartClick = onNavigateToSale
        )

        // Category Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategoryTab(
                    title = category,
                    isSelected = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }

        // Product Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredProducts) { product ->
                GalleryProductCard(
                    product = product,
                    onAddToCart = { qty ->
                        val existing = viewModel.cartItems.find { it.productId == product.barcode }
                        if (existing != null) {
                            viewModel.updateCartItemQuantity(product.barcode, existing.quantity + qty)
                        } else {
                            viewModel.cartItems.add(com.example.salespossystem.data.CartItem(product.barcode, product.name, qty, product.salePrice.toDoubleOrNull() ?: 0.0))
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryTopBar(
    viewModel: SalesViewModel,
    onBack: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isSearchExpanded: Boolean,
    onSearchToggle: () -> Unit,
    onCartClick: () -> Unit
) {
    val cartCount = viewModel.cartItems.sumOf { it.quantity }

    Column(modifier = Modifier.background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF0F0F0), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }

            if (!isSearchExpanded) {
                Text(
                    "Product Gallery",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            } else {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(50.dp),
                    placeholder = { Text("Search name or barcode...", fontSize = 14.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF0F0F0),
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(25.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onSearchToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .background(if(isSearchExpanded) Color(0xFFFFD54F) else Color(0xFFF0F0F0), CircleShape)
                ) {
                    Icon(
                        if(isSearchExpanded) Icons.Default.Close else Icons.Default.Search, 
                        contentDescription = "Search", 
                        tint = Color.Black
                    )
                }
                
                // Cart Icon with Badge
                Box {
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF0F0F0), CircleShape)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.Black)
                    }
                    if (cartCount > 0) {
                        Badge(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = (4).dp),
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) {
                            Text(cartCount.toString(), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryProductCard(
    product: com.example.salespossystem.data.ProductItem,
    onAddToCart: (Double) -> Unit
) {
    val price = product.salePrice.toDoubleOrNull() ?: 0.0
    var quantity by remember { mutableStateOf(1.0) }
    var showQtyDialog by remember { mutableStateOf(false) }

    if (showQtyDialog) {
        com.example.salespossystem.ui.QuantityEditDialog(
            item = com.example.salespossystem.data.CartItem(product.barcode, product.name, quantity, price),
            onDismiss = { showQtyDialog = false },
            onConfirm = { newQty ->
                quantity = newQty
                showQtyDialog = false
            }
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            
            Text(
                text = "Barcode: ${product.barcode}",
                fontSize = 10.sp,
                color = Color.Gray
            )

            Text(
                text = "৳ ${String.format(Locale.US, "%.2f", price)}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Quantity Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { if (quantity > 1.0) quantity -= 1.0 },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                }
                
                Text(
                    text = quantity.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.clickable { showQtyDialog = true }
                )
                
                IconButton(
                    onClick = { quantity += 1.0 },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { 
                    onAddToCart(quantity)
                    quantity = 1.0 // Reset after adding
                },
                modifier = Modifier.fillMaxWidth().height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.AddShoppingCart, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add to Cart", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CategoryTab(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(25.dp),
        color = if (isSelected) Color(0xFFFFD54F) else Color.White,
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = if (isSelected) Color.Black else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}
