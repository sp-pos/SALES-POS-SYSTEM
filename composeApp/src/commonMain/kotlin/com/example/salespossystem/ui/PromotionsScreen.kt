package com.example.salespossystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.salespossystem.data.Promotion
import kotlinx.coroutines.launch

@Composable
fun PromotionsScreen(
    promotions: List<Promotion>,
    products: List<ProductItem>,
    onAddPromotion: (String, String, Double, Double, List<String>) -> Unit,
    onDeletePromotion: (String) -> Unit,
    onTogglePromotion: (String, Boolean) -> Unit,
    onExportPdf: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    snackbarData = data
                )
            }
        },
        topBar = {
            PromotionsTopBar(
                onAddClick = { showAddDialog = true },
                onExportPdf = onExportPdf
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(8.dp)) {
            if (promotions.isEmpty()) {
                EmptyPromotionsView { showAddDialog = true }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(promotions) { promo ->
                        PromotionItemCard(
                            promo = promo,
                            products = products,
                            onToggle = onTogglePromotion,
                            onDelete = { id ->
                                onDeletePromotion(id)
                                scope.launch { snackbarHostState.showSnackbar("Promotion deleted") }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPromotionDialog(
            products = products,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc, dPercent, dAmount, barcodes ->
                onAddPromotion(name, desc, dPercent, dAmount, barcodes)
                showAddDialog = false
                scope.launch { snackbarHostState.showSnackbar("Promotion added successfully") }
            }
        )
    }
}

@Composable
fun PromotionsTopBar(onAddClick: () -> Unit, onExportPdf: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Promotions", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onAddClick) { Icon(Icons.Default.Add, null, tint = Color.Black) }
        Box {
            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.Black) }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Export Promotions PDF") },
                    onClick = { onExportPdf(); showMenu = false }
                )
            }
        }
    }
}

@Composable
fun PromotionItemCard(
    promo: Promotion,
    products: List<ProductItem>,
    onToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(promo.name, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    val discountText = if (promo.discountPercent > 0) "Discount: ${promo.discountPercent}%" else "Discount: ${promo.discountAmount}"
                    Text(discountText, color = Color(0xFF0091EA), fontWeight = FontWeight.Bold)
                }
                Switch(
                    checked = promo.isActive,
                    onCheckedChange = { onToggle(promo.id, it) }
                )
                IconButton(onClick = { onDelete(promo.id) }) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                }
            }
            Text(promo.description, color = Color.Black, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))

            if (promo.appliedProductBarcodes.isNotEmpty()) {
                val productNames = promo.appliedProductBarcodes.mapNotNull { bc -> products.find { it.barcode == bc }?.name }
                Text("Applied to: ${productNames.joinToString(", ")}", color = Color.DarkGray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun EmptyPromotionsView(onCreateClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocalOffer, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Text("No active promotions", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = onCreateClick,
                modifier = Modifier.padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Create promotion", color = Color.White)
            }
        }
    }
}

@Composable
fun AddPromotionDialog(
    products: List<ProductItem>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("") }
    var discountAmount by remember { mutableStateOf("") }
    val selectedBarcodes = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Promotion", color = Color.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Offer Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discountPercent,
                        onValueChange = { discountPercent = it; if (it.isNotEmpty()) discountAmount = "" },
                        label = { Text("Discount %") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                    )
                    OutlinedTextField(
                        value = discountAmount,
                        onValueChange = { discountAmount = it; if (it.isNotEmpty()) discountPercent = "" },
                        label = { Text("Amount") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                    )
                }
                Text("Select Products:", color = Color.Black, fontSize = 12.sp)
                LazyColumn(modifier = Modifier.height(150.dp).fillMaxWidth().border(1.dp, Color.Gray, RoundedCornerShape(4.dp)).padding(4.dp)) {
                    items(products) { product ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (selectedBarcodes.contains(product.barcode)) selectedBarcodes.remove(product.barcode)
                                else selectedBarcodes.add(product.barcode)
                            }.padding(4.dp)
                        ) {
                            Checkbox(checked = selectedBarcodes.contains(product.barcode), onCheckedChange = null)
                            Text(product.name, color = Color.Black, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotEmpty()) onConfirm(name, desc, discountPercent.toDoubleOrNull() ?: 0.0, discountAmount.toDoubleOrNull() ?: 0.0, selectedBarcodes.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Black) } }
    )
}
