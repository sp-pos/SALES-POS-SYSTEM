package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.ui.theme.*
import com.example.salespossystem.data.Promotion
import com.example.salespossystem.data.ProductItem
import java.util.*

@Composable
fun PromotionsScreen(viewModel: SalesViewModel = viewModel()) {
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // PDF Export Launcher
    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 20f
                canvas.drawText("ACTIVE PROMOTIONS REPORT", 180f, 50f, paint)
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 12f
                var y = 90f
                
                viewModel.promotions.forEach { promo ->
                    paint.isFakeBoldText = true
                    val discountText = if (promo.discountPercent > 0) "${promo.discountPercent}%" else "${promo.discountAmount}"
                    canvas.drawText("Offer: ${promo.name} ($discountText)", 50f, y, paint)
                    y += 15f
                    paint.isFakeBoldText = false
                    canvas.drawText("Description: ${promo.description}", 60f, y, paint)
                    y += 15f
                    canvas.drawText("Products: ${promo.appliedProductBarcodes.joinToString(", ")}", 60f, y, paint)
                    y += 30f
                    if (y > 800f) return@forEach
                }
                
                pdfDocument.finishPage(page)
                context.contentResolver.openOutputStream(uri)?.use { pdfDocument.writeTo(it) }
                pdfDocument.close()
                Toast.makeText(context, "PDF Exported Successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "PDF Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        PromotionsTopBar(
            onAddClick = { showAddDialog = true },
            onExportPdf = { exportPdfLauncher.launch("promotions_list.pdf") }
        )
        
        if (viewModel.promotions.isEmpty()) {
            EmptyPromotionsView { showAddDialog = true }
        } else {
            PromotionsList(
                promotions = viewModel.promotions,
                viewModel = viewModel,
                onToggle = { id, active ->
                    val index = viewModel.promotions.indexOfFirst { it.id == id }
                    if (index != -1) {
                        viewModel.promotions[index] = viewModel.promotions[index].copy(isActive = active)
                    }
                },
                onDelete = { id ->
                    viewModel.promotions.removeIf { it.id == id }
                    Toast.makeText(context, "Promotion deleted", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showAddDialog) {
        AddPromotionDialog(
            products = viewModel.products,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc, discountPercent, discountAmount, barcodes ->
                val newId = System.currentTimeMillis().toString()
                viewModel.promotions.add(Promotion(newId, name, desc, discountPercent, discountAmount, true, barcodes))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PromotionsTopBar(onAddClick: () -> Unit, onExportPdf: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).background(HeaderBackground).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Promotions", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onAddClick) { Icon(Icons.Default.Add, null, tint = Color.White) }
        Box {
            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(CardBackground)) {
                DropdownMenuItem(text = { Text("Export Promotions PDF", color = Color.White) }, onClick = { onExportPdf(); showMenu = false })
            }
        }
    }
}

@Composable
fun PromotionsList(promotions: List<Promotion>, viewModel: SalesViewModel, onToggle: (String, Boolean) -> Unit, onDelete: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(promotions) { promo ->
            PromotionItemCard(promo, viewModel, onToggle, onDelete)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PromotionItemCard(promo: Promotion, viewModel: SalesViewModel, onToggle: (String, Boolean) -> Unit, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(promo.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    val discountText = if (promo.discountPercent > 0) "Discount: ${promo.discountPercent}%" else "Discount: ${promo.discountAmount}"
                    Text(discountText, color = SidebarSelected, fontWeight = FontWeight.Bold)
                }
                Switch(checked = promo.isActive, onCheckedChange = { onToggle(promo.id, it) }, colors = SwitchDefaults.colors(checkedThumbColor = SidebarSelected))
                IconButton(onClick = { onDelete(promo.id) }) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f)) }
            }
            Text(promo.description, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
            
            if (promo.appliedProductBarcodes.isNotEmpty()) {
                Text("Applied to: ${promo.appliedProductBarcodes.size} Products", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                val productNames = promo.appliedProductBarcodes.mapNotNull { bc -> viewModel.products.find { it.barcode == bc }?.name }
                Text(productNames.joinToString(", "), color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun EmptyPromotionsView(onCreateClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocalOffer, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Text("No active promotions", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(onClick = onCreateClick, modifier = Modifier.padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected), shape = RoundedCornerShape(4.dp)) { Text("Create promotion") }
        }
    }
}

@Composable
fun AddPromotionDialog(products: List<ProductItem>, onDismiss: () -> Unit, onConfirm: (String, String, Double, Double, List<String>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("") }
    var discountAmount by remember { mutableStateOf("") }
    val selectedBarcodes = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("New Promotion", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Offer Name") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = discountPercent, onValueChange = { discountPercent = it; if (it.isNotEmpty()) discountAmount = "" }, label = { Text("Discount %") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = discountAmount, onValueChange = { discountAmount = it; if (it.isNotEmpty()) discountPercent = "" }, label = { Text("Discount Amount") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                }
                
                Text("Select Products:", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                LazyColumn(modifier = Modifier.height(150.dp).fillMaxWidth().border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp)).padding(4.dp)) {
                    items(products) { product ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { if (selectedBarcodes.contains(product.barcode)) selectedBarcodes.remove(product.barcode) else selectedBarcodes.add(product.barcode) }.padding(4.dp)) {
                            Checkbox(checked = selectedBarcodes.contains(product.barcode), onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = SidebarSelected))
                            Text(product.name, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotEmpty()) onConfirm(name, desc, discountPercent.toDoubleOrNull() ?: 0.0, discountAmount.toDoubleOrNull() ?: 0.0, selectedBarcodes.toList()) }, colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected)) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } }
    )
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
fun PromotionsScreenPreview() {
    PromotionsScreen()
}
