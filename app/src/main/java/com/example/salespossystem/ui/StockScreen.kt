package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.data.ProductItem
import com.example.salespossystem.ui.theme.*
import com.example.salespossystem.util.ExcelHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StockScreen(viewModel: SalesViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductItem?>(null) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Excel Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            val outputStream = context.contentResolver.openOutputStream(it)
            if (outputStream != null) {
                val success = ExcelHelper.exportProductsToExcel(
                    context, 
                    viewModel.products, 
                    viewModel.stockMap,
                    outputStream
                )
                if (success) {
                    Toast.makeText(context, "Stock exported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to export stock", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Designed PDF Export Launcher
    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()
                val titlePaint = Paint()
                
                // --- 1. Header Design ---
                titlePaint.apply {
                    color = android.graphics.Color.BLACK
                    textSize = 24f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(viewModel.companyName, 40f, 60f, titlePaint)
                
                paint.apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 10f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                canvas.drawText(viewModel.companyAddress, 40f, 80f, paint)
                canvas.drawText("Phone: ${viewModel.companyPhone} | TRN: ${viewModel.companyTaxNumber}", 40f, 95f, paint)
                
                paint.color = android.graphics.Color.BLACK
                paint.strokeWidth = 2f
                canvas.drawLine(40f, 110f, 555f, 110f, paint)
                
                // --- 2. Report Title ---
                titlePaint.textSize = 16f
                canvas.drawText("STOCK LEVEL REPORT", 40f, 140f, titlePaint)
                
                val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(Date())
                paint.textSize = 10f
                canvas.drawText("Generated on: $dateStr", 40f, 155f, paint)

                // --- 3. Table Headers ---
                var currentY = 190f
                paint.apply {
                    color = android.graphics.Color.LTGRAY
                    style = Paint.Style.FILL
                }
                canvas.drawRect(40f, currentY - 20f, 555f, currentY + 5f, paint)
                
                paint.apply {
                    color = android.graphics.Color.BLACK
                    textSize = 10f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("BARCODE", 45f, currentY, paint)
                canvas.drawText("PRODUCT NAME", 130f, currentY, paint)
                canvas.drawText("COST", 320f, currentY, paint)
                canvas.drawText("PRICE", 390f, currentY, paint)
                canvas.drawText("STOCK", 460f, currentY, paint)
                canvas.drawText("VALUE", 510f, currentY, paint)
                
                // --- 4. Table Rows ---
                paint.apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    textSize = 9f
                }
                currentY += 25f
                
                var totalStockValue = 0.0
                
                viewModel.products.forEach { product ->
                    if (currentY > 780f) return@forEach // Simple overflow check
                    
                    val stock = viewModel.stockMap[product.barcode] ?: 0.0
                    val salePrice = product.salePrice.toDoubleOrNull() ?: 0.0
                    val value = stock * salePrice
                    totalStockValue += value
                    
                    canvas.drawText(product.barcode, 45f, currentY, paint)
                    canvas.drawText(product.name.take(25), 130f, currentY, paint)
                    canvas.drawText(product.cost, 320f, currentY, paint)
                    canvas.drawText(product.salePrice, 390f, currentY, paint)
                    canvas.drawText(String.format(Locale.US, "%.0f", stock), 460f, currentY, paint)
                    canvas.drawText(String.format(Locale.US, "%.2f", value), 510f, currentY, paint)
                    
                    paint.color = android.graphics.Color.LTGRAY
                    paint.strokeWidth = 0.5f
                    canvas.drawLine(40f, currentY + 5f, 555f, currentY + 5f, paint)
                    paint.color = android.graphics.Color.BLACK
                    
                    currentY += 20f
                }

                // --- Summary Line ---
                currentY += 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("TOTAL INVENTORY VALUE (SALE PRICE):", 300f, currentY, paint)
                canvas.drawText(String.format(Locale.US, "%.2f", totalStockValue), 510f, currentY, paint)

                // --- 5. Footer ---
                paint.color = android.graphics.Color.GRAY
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("Software Powered by SALES POS SYSTEM", 40f, 820f, paint)

                pdfDocument.finishPage(page)
                context.contentResolver.openOutputStream(uri)?.use { 
                    pdfDocument.writeTo(it)
                }
                pdfDocument.close()
                Toast.makeText(context, "Professional PDF Generated!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "PDF Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
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

    val stockCounts by remember {
        derivedStateOf {
            val values = viewModel.stockMap.values
            Triple(
                values.count { it < 0 },
                values.count { it > 0 },
                values.count { it == 0.0 }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        StockTopBar(
            onHistoryClick = {
                if (selectedProduct != null) {
                    showHistoryDialog = true
                } else {
                    Toast.makeText(context, "Select a product first", Toast.LENGTH_SHORT).show()
                }
            },
            onRefresh = { viewModel.loadDataFromDatabase() },
            onExportExcelClick = { exportLauncher.launch("stock_report.xlsx") },
            onExportPdfClick = { exportPdfLauncher.launch("stock_report.pdf") }
        )
        StockFilterBarMobile(stockCounts.first, stockCounts.second, stockCounts.third)
        StockSearchBarMobile(searchQuery) { searchQuery = it }
        StockListMobile(
            products = filteredProducts, 
            stockMap = viewModel.stockMap,
            selectedProduct = selectedProduct,
            onProductSelect = { selectedProduct = it }
        )
        StockFooterMobile(viewModel.products, viewModel.stockMap)
    }

    if (showHistoryDialog && selectedProduct != null) {
        ProductHistoryDialog(
            product = selectedProduct!!,
            viewModel = viewModel,
            onDismiss = { showHistoryDialog = false }
        )
    }
}

@Composable
fun StockTopBar(
    onHistoryClick: () -> Unit,
    onRefresh: () -> Unit, 
    onExportExcelClick: () -> Unit,
    onExportPdfClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBackground)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Stock", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, null, tint = Color.White) }
        IconButton(onClick = onHistoryClick) { Icon(Icons.Default.History, null, tint = Color.White) }
        
        Box {
            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(CardBackground)
            ) {
                DropdownMenuItem(
                    text = { Text("Export to Excel", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.FileDownload, null, tint = Color.White) },
                    onClick = {
                        onExportExcelClick()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Export to PDF Report", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, null, tint = Color.White) },
                    onClick = {
                        onExportPdfClick()
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun StockFilterBarMobile(neg: Int, pos: Int, zero: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF252525))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StockBadgeMobile("Neg: $neg", Color.Red)
        StockBadgeMobile("Pos: $pos", Color(0xFF2196F3))
        StockBadgeMobile("Zero: $zero", Color.Gray)
    }
}

@Composable
fun StockBadgeMobile(text: String, bgColor: Color) {
    Surface(
        color = bgColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, bgColor),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StockSearchBarMobile(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        placeholder = { Text("Search stock...", color = Color.Gray, fontSize = 14.sp) },
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
fun ColumnScope.StockListMobile(
    products: List<ProductItem>, 
    stockMap: Map<String, Double>,
    selectedProduct: ProductItem?,
    onProductSelect: (ProductItem) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
        items(products) { item ->
            val qty = stockMap[item.barcode] ?: 0.0
            StockListItemMobile(
                item = item, 
                quantity = qty,
                isSelected = selectedProduct?.barcode == item.barcode,
                onClick = { onProductSelect(item) }
            )
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
        }
    }
}

@Composable
fun StockListItemMobile(item: ProductItem, quantity: Double, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) SidebarSelected.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(if (quantity < 0) Color.Red else if (quantity > 0) Color(0xFF2196F3) else Color.Gray, RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Code: ${item.barcode} | Unit: ${item.unit}",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                String.format(Locale.US, "%.2f", quantity),
                color = if (quantity < 0) Color.Red else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            val value = (item.salePrice.toDoubleOrNull() ?: 0.0) * quantity
            Text(
                "Value: ${String.format(Locale.US, "%.2f", value)}",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun StockFooterMobile(products: List<ProductItem>, stockMap: Map<String, Double>) {
    val totalCost = products.sumOf { 
        (it.cost.toDoubleOrNull() ?: 0.0) * (stockMap[it.barcode] ?: 0.0) 
    }
    val totalValue = products.sumOf { 
        (it.salePrice.toDoubleOrNull() ?: 0.0) * (stockMap[it.barcode] ?: 0.0) 
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Cost:", color = Color.Gray, fontSize = 12.sp)
            Text(String.format(Locale.US, "%.2f", totalCost), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Value:", color = Color.Gray, fontSize = 12.sp)
            Text(String.format(Locale.US, "%.2f", totalValue), color = if (totalValue < 0) AccentRed else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProductHistoryDialog(
    product: ProductItem,
    viewModel: SalesViewModel,
    onDismiss: () -> Unit
) {
    val history = remember(product.barcode, viewModel.allInvoices.size) {
        viewModel.allInvoices.filter { inv ->
            inv.items.any { it.productId == product.barcode }
        }.map { inv ->
            val item = inv.items.find { it.productId == product.barcode }!!
            val isReturn = inv.paymentMethod.contains("RETURN", ignoreCase = true)
            val type = when {
                inv.paymentMethod.equals("DAMAGE", ignoreCase = true) -> "Damage"
                inv.isPurchase && isReturn -> "Purchase Return"
                inv.isPurchase -> "Purchase"
                !inv.isPurchase && isReturn -> "Sale Return"
                else -> "Sale"
            }
            HistoryEntry(inv.date, type, item.quantity)
        }.reversed()
    }

    val totalPurchased = history.filter { it.type == "Purchase" }.sumOf { it.qty }
    val totalSold = history.filter { it.type == "Sale" }.sumOf { it.qty }
    val saleReturns = history.filter { it.type == "Sale Return" }.sumOf { it.qty }
    val purchaseReturns = history.filter { it.type == "Purchase Return" }.sumOf { it.qty }
    val damages = history.filter { it.type == "Damage" }.sumOf { it.qty }
    
    // Correct Available stock calculation
    val availableStock = totalPurchased + saleReturns - totalSold - purchaseReturns - damages

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2D2D2D),
        title = {
            Column {
                Text("Stock History: ${product.name}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Barcode: ${product.barcode}", color = Color.Gray, fontSize = 12.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                // Summary Cards
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SummaryItem("Bought", String.format(Locale.US, "%.2f", totalPurchased), Color(0xFF2196F3))
                            SummaryItem("Sold", String.format(Locale.US, "%.2f", totalSold), Color.Green)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SummaryItem("Returns", String.format(Locale.US, "%.2f", saleReturns + purchaseReturns), Color.Yellow)
                            SummaryItem("Damage", String.format(Locale.US, "%.2f", damages), Color.Red)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            SummaryItem("Available", String.format(Locale.US, "%.2f", availableStock), if (availableStock > 0) Color.Green else Color.Red)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF333333)).padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Date", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                    Text("Type", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                    Text("Qty", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
                }
                
                if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transaction history", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(history) { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(entry.date.split(" ")[0], color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1.5f))
                                Text(
                                    entry.type, 
                                    color = when(entry.type) {
                                        "Purchase" -> Color(0xFF2196F3)
                                        "Sale" -> Color.Green
                                        "Sale Return" -> Color.Yellow
                                        "Purchase Return" -> Color.Red
                                        "Damage" -> Color.Red
                                        else -> Color.White
                                    }, 
                                    fontSize = 11.sp, 
                                    modifier = Modifier.weight(1.2f)
                                )
                                Text(
                                    entry.qty.toString(), 
                                    color = Color.White, 
                                    fontSize = 12.sp, 
                                    modifier = Modifier.weight(0.5f), 
                                    textAlign = TextAlign.End,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(color = Color.DarkGray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = Color.White) }
        }
    )
}

@Composable
fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 10.sp)
        Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

data class HistoryEntry(val date: String, val type: String, val qty: Double)

@Preview(widthDp = 360, heightDp = 640)
@Composable
fun StockScreenMobilePreview() {
    StockScreen()
}
