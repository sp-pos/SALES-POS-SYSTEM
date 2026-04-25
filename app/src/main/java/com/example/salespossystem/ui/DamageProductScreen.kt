package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.ui.theme.*
import com.example.salespossystem.util.ExcelHelper
import com.example.salespossystem.util.PrintingService
import com.example.salespossystem.data.CartItem
import com.example.salespossystem.data.Invoice
import com.example.salespossystem.data.ProductItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DamageProductScreen(viewModel: SalesViewModel = viewModel()) {
    var isCreatingInvoice by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val damageInvoices = remember(viewModel.allInvoices.size) {
        viewModel.allInvoices.filter { it.paymentMethod == "DAMAGE" }.reversed()
    }

    if (isCreatingInvoice) {
        CreateDamageInvoiceView(
            products = viewModel.products,
            onDismiss = { isCreatingInvoice = false },
            onConfirm = { items, reason ->
                viewModel.completeDamageInvoice(items, reason)
                Toast.makeText(context, "Damage Invoice Created!", Toast.LENGTH_SHORT).show()
                isCreatingInvoice = false
            }
        )
    } else {
        DamageInvoiceListView(
            viewModel = viewModel,
            damageInvoices = damageInvoices,
            onAddNew = { isCreatingInvoice = true }
        )
    }
}

@Composable
fun DamageInvoiceListView(
    viewModel: SalesViewModel,
    damageInvoices: List<Invoice>,
    onAddNew: () -> Unit
) {
    val context = LocalContext.current
    var selectedInvoice by remember { mutableStateOf<Invoice?>(null) }

    // Excel Export Launcher
    val excelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let {
            val outputStream = context.contentResolver.openOutputStream(it)
            if (outputStream != null) {
                val success = ExcelHelper.exportInvoicesToExcel(damageInvoices, outputStream)
                if (success) Toast.makeText(context, "Damage report exported!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        DamageTopBar(
            onAddClick = onAddNew,
            onExportExcel = { excelLauncher.launch("damage_history.xlsx") }
        )
        
        Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            // Left Side: Invoice List
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text("Damage Invoices", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                LazyColumn(modifier = Modifier.fillMaxSize().border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))) {
                    items(damageInvoices) { inv ->
                        DamageInvoiceRow(
                            inv = inv,
                            isSelected = selectedInvoice?.invoiceNumber == inv.invoiceNumber,
                            onClick = { selectedInvoice = inv }
                        )
                        HorizontalDivider(color = Color.DarkGray)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Right Side: Invoice Details
            Column(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                Text("Invoice Details", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
                ) {
                    if (selectedInvoice != null) {
                        DamageInvoiceDetails(selectedInvoice!!, context, viewModel)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select an invoice", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DamageInvoiceRow(inv: Invoice, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) SidebarSelected.copy(alpha = 0.3f) else Color.Transparent)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(inv.invoiceNumber, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(inv.date, color = Color.Gray, fontSize = 10.sp)
        }
        Text(String.format(Locale.US, "%.2f", inv.totalAmount), color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DamageInvoiceDetails(inv: Invoice, context: android.content.Context, viewModel: SalesViewModel) {
    Column(modifier = Modifier.padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Invoice: ${inv.invoiceNumber}", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Date: ${inv.date}", color = Color.Gray, fontSize = 11.sp)
            }
            IconButton(onClick = { PrintingService.printInvoice(context, inv, viewModel.currencySymbol) }) {
                Icon(Icons.Default.Print, null, tint = SidebarSelected)
            }
        }
        Text(inv.customerName, color = SidebarSelected, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(inv.items) { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(item.productName, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("x${item.quantity}", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.width(40.dp))
                    Text(String.format(Locale.US, "%.2f", item.price * item.quantity), color = Color.White, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.width(70.dp))
                }
            }
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TOTAL LOSS", color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(String.format(Locale.US, "%.2f", inv.totalAmount), color = Color.Red, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun CreateDamageInvoiceView(
    products: List<ProductItem>,
    onDismiss: () -> Unit,
    onConfirm: (List<CartItem>, String) -> Unit
) {
    val damageCart = remember { mutableStateListOf<CartItem>() }
    var reason by remember { mutableStateOf("Damaged/Broken") }
    var showProductSearch by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("New Damage Invoice", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("General Reason") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { showProductSearch = true }, colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected)) {
                Icon(Icons.Default.Add, null)
                Text("Add Items")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cart Table Header
        Row(modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp)) {
            Text("Product", color = Color.White, modifier = Modifier.weight(1f), fontSize = 12.sp)
            Text("Qty", color = Color.White, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center, fontSize = 12.sp)
            Text("Remove", color = Color.White, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center, fontSize = 12.sp)
        }

        LazyColumn(modifier = Modifier.weight(1f).border(1.dp, Color.DarkGray)) {
            items(damageCart) { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.productName, color = Color.White, modifier = Modifier.weight(1f), fontSize = 13.sp)
                    
                    // Simple Qty Edit
                    var qtyText by remember { mutableStateOf(item.quantity.toString()) }
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { 
                            qtyText = it
                            it.toDoubleOrNull()?.let { q -> 
                                val idx = damageCart.indexOf(item)
                                if (idx != -1) damageCart[idx] = item.copy(quantity = q)
                            }
                        },
                        modifier = Modifier.width(70.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    
                    IconButton(onClick = { damageCart.remove(item) }, modifier = Modifier.width(60.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }
                HorizontalDivider(color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { if (damageCart.isNotEmpty()) onConfirm(damageCart.toList(), reason) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = damageCart.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("SAVE DAMAGE INVOICE & REDUCE STOCK", fontWeight = FontWeight.Bold)
        }
    }

    if (showProductSearch) {
        DamageProductSearchDialog(
            products = products,
            onDismiss = { showProductSearch = false },
            onProductSelect = { product ->
                val existing = damageCart.find { it.productId == product.barcode }
                if (existing == null) {
                    damageCart.add(CartItem(product.barcode, product.name, 1.0, product.salePrice.toDoubleOrNull() ?: 0.0))
                }
                showProductSearch = false
            }
        )
    }
}

@Composable
fun DamageProductSearchDialog(
    products: List<ProductItem>,
    onDismiss: () -> Unit,
    onProductSelect: (ProductItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = products.filter { it.name.contains(query, ignoreCase = true) || it.barcode.contains(query) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("Select Product to Add", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search product...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(filtered) { product ->
                        ListItem(
                            headlineContent = { Text(product.name, color = Color.White) },
                            supportingContent = { Text(product.barcode, color = Color.Gray) },
                            modifier = Modifier.clickable { onProductSelect(product) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(color = Color.DarkGray)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DamageTopBar(onAddClick: () -> Unit, onExportExcel: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).background(HeaderBackground).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Damage System", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onAddClick) { Icon(Icons.Default.Add, null, tint = Color.White) }
        Box {
            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(CardBackground)) {
                DropdownMenuItem(
                    text = { Text("Export All to Excel", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.TableChart, null, tint = Color.White) },
                    onClick = { onExportExcel(); showMenu = false }
                )
            }
        }
    }
}
