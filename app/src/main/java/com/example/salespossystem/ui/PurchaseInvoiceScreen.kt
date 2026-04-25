package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.AppDestinations
import com.example.salespossystem.data.CartItem
import com.example.salespossystem.data.Customer
import com.example.salespossystem.data.ProductItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseInvoiceScreen(
    viewModel: SalesViewModel = viewModel(),
    onNavigate: (AppDestinations) -> Unit
) {
    var supplierName by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }
    var invoiceDate by remember { mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())) }
    var notes by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var applyTax by remember { mutableStateOf(true) }

    val purchaseItems = remember { mutableStateListOf<CartItem>() }
    var showProductDialog by remember { mutableStateOf(false) }
    var showSupplierDialog by remember { mutableStateOf(false) }

    // Load data if editing
    LaunchedEffect(viewModel.editingInvoice) {
        viewModel.editingInvoice?.let { inv ->
            if (inv.isPurchase) {
                supplierName = inv.customerName.replace("Supplier: ", "")
                invoiceNumber = inv.invoiceNumber
                invoiceDate = inv.date
                paymentMethod = inv.paymentMethod
                purchaseItems.clear()
                purchaseItems.addAll(inv.items)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (viewModel.editingInvoice != null) "Edit Purchase Invoice" else "New Purchase Invoice",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { 
                viewModel.clearCart()
                onNavigate(AppDestinations.HOME) 
            }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("Supplier Name") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showSupplierDialog = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Select Supplier", tint = Color.White)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.LightGray,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        label = { Text("Invoice #") },
                        readOnly = viewModel.editingInvoice != null,
                        modifier = Modifier.weight(0.6f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.LightGray,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = invoiceDate,
                        onValueChange = { invoiceDate = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.LightGray,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type / Payment") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color.LightGray,
                                unfocusedLabelColor = Color.Gray
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color(0xFF2D2D2D))
                        ) {
                            val options = listOf(
                                "CASH", "CARD", "CREDIT",
                                "CASH RETURN", "CARD RETURN", "CREDIT RETURN"
                            )
                            options.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method, color = Color.White) },
                                    onClick = {
                                        paymentMethod = method
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Items", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Button(
                onClick = { showProductDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Item")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF333333))
                .padding(8.dp)
        ) {
            Text("Product", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Qty", modifier = Modifier.width(60.dp), color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 12.sp)
            Text("Cost", modifier = Modifier.width(80.dp), color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, fontSize = 12.sp)
            Text("Total", modifier = Modifier.width(80.dp), color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(40.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            if (purchaseItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items added yet", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(purchaseItems) { item ->
                        PurchaseItemRow(
                            item = item, 
                            onDelete = { purchaseItems.remove(item) },
                            onQtyChange = { newQty ->
                                val index = purchaseItems.indexOf(item)
                                if (index != -1) purchaseItems[index] = item.copy(quantity = newQty)
                            },
                            onCostChange = { newCost ->
                                val index = purchaseItems.indexOf(item)
                                if (index != -1) purchaseItems[index] = item.copy(price = newCost)
                            }
                        )
                        HorizontalDivider(color = Color.DarkGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Additional Notes") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        val subtotal = purchaseItems.sumOf { it.price * it.quantity }
        val taxRateItem = viewModel.taxRates.find { it.enabled && (it.rate.toDoubleOrNull() ?: 0.0) > 0.0 }
        val taxRate = (taxRateItem?.rate?.toDoubleOrNull() ?: 0.0) / 100.0
        val tax = if (applyTax) subtotal * taxRate else 0.0
        val total = subtotal + tax
        
        val isReturn = paymentMethod.contains("RETURN")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                SummaryRow("Subtotal", subtotal)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = applyTax,
                        onCheckedChange = { applyTax = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50))
                    )
                    Text("Apply Tax (${taxRateItem?.name ?: "Not Set"})")
                    Spacer(modifier = Modifier.weight(1f))
                    Text(String.format(Locale.US, "%.2f", tax), color = Color.White, fontSize = 14.sp)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.DarkGray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isReturn) "Return Total:" else "Grand Total:", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        String.format(Locale.US, "%.2f", total),
                        color = if (isReturn) Color.Red else Color(0xFF4CAF50),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { 
                    viewModel.clearCart()
                    onNavigate(AppDestinations.DOCUMENTS) 
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    viewModel.completePurchase(purchaseItems, supplierName, invoiceNumber, paymentMethod, applyTax)
                    onNavigate(AppDestinations.DOCUMENTS)
                },
                modifier = Modifier.weight(1f),
                enabled = supplierName.isNotEmpty() && purchaseItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = if (isReturn) Color.Red else Color(0xFF2196F3)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(if (isReturn) Icons.AutoMirrored.Filled.AssignmentReturn else Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        viewModel.editingInvoice != null -> "Update Invoice"
                        isReturn -> "Process Return"
                        else -> "Save Invoice"
                    }
                )
            }
        }
    }

    if (showProductDialog) {
        PurchaseProductSearchDialog(
            products = viewModel.products,
            onDismiss = { showProductDialog = false },
            onProductSelect = { product: ProductItem ->
                val cost = product.cost.toDoubleOrNull() ?: 0.0
                purchaseItems.add(CartItem(product.barcode, product.name, 1.0, cost))
                showProductDialog = false
            }
        )
    }

    if (showSupplierDialog) {
        SupplierSelectionDialog(
            suppliers = viewModel.suppliers,
            onDismiss = { showSupplierDialog = false },
            onSupplierSelect = { supplier ->
                supplierName = supplier.name
                showSupplierDialog = false
            }
        )
    }
}

@Composable
fun SupplierSelectionDialog(
    suppliers: List<Customer>,
    onDismiss: () -> Unit,
    onSupplierSelect: (Customer) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = suppliers.filter { it.name.contains(query, ignoreCase = true) || it.phone.contains(query) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2D2D2D),
        title = { Text("Select Supplier", color = Color.White) },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search by name or phone") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (suppliers.isEmpty()) {
                        item {
                            Text("No suppliers found. Add them in Customers & Suppliers screen.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(filtered) { supplier ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSupplierSelect(supplier) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(supplier.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(supplier.phone, color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                            HorizontalDivider(color = Color.DarkGray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun SummaryRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.LightGray, fontSize = 14.sp)
        Text(String.format(Locale.US, "%.2f", amount), color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun PurchaseItemRow(
    item: CartItem, 
    onDelete: () -> Unit, 
    onQtyChange: (Double) -> Unit,
    onCostChange: (Double) -> Unit
) {
    var qtyText by remember(item.productId) { mutableStateOf(item.quantity.toString()) }
    var costText by remember(item.productId) { mutableStateOf(String.format(Locale.US, "%.2f", item.price)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(item.productId, color = Color.Gray, fontSize = 11.sp)
        }
        
        // Editable Qty
        BasicTextField(
            value = qtyText,
            onValueChange = {
                qtyText = it
                it.toDoubleOrNull()?.let { qty -> onQtyChange(qty) }
            },
            modifier = Modifier
                .width(60.dp)
                .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                .padding(4.dp),
            textStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center, fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = SolidColor(Color.White)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Editable Cost
        BasicTextField(
            value = costText,
            onValueChange = {
                costText = it
                it.toDoubleOrNull()?.let { cost -> onCostChange(cost) }
            },
            modifier = Modifier
                .width(80.dp)
                .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                .padding(4.dp),
            textStyle = TextStyle(color = Color.White, textAlign = TextAlign.End, fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = SolidColor(Color.White)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            String.format(Locale.US, "%.2f", item.price * item.quantity),
            modifier = Modifier.width(80.dp),
            color = Color.White,
            textAlign = TextAlign.End,
            fontSize = 14.sp
        )

        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun PurchaseProductSearchDialog(
    products: List<ProductItem>,
    onDismiss: () -> Unit,
    onProductSelect: (ProductItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = products.filter { it.name.contains(query, ignoreCase = true) || it.barcode.contains(query) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2D2D2D),
        title = { Text("Select Product", color = Color.White) },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search by name or barcode") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered) { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProductSelect(product) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Barcode: ${product.barcode}", color = Color.Gray, fontSize = 11.sp)
                                Text("Sale Price: ${product.salePrice}", color = Color.LightGray, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Cost:", color = Color.Gray, fontSize = 10.sp)
                                Text(product.cost, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(color = Color.DarkGray)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
