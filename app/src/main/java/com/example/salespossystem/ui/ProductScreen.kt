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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.salespossystem.data.ProductItem
import com.example.salespossystem.ui.theme.*
import com.example.salespossystem.util.ExcelHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProductScreen(
    viewModel: SalesViewModel = viewModel(),
    onNavigateToItemEntry: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var editingProduct by remember { mutableStateOf<ProductItem?>(null) }
    var selectedGroupFilter by remember { mutableStateOf("All") }
    var showExampleDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Excel Import Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val importedData = ExcelHelper.importProductsFromExcel(context, it)
            if (importedData.isNotEmpty()) {
                importedData.forEach { (product, balance) ->
                    viewModel.addProduct(product, initialStock = balance)
                }
                Toast.makeText(context, "Imported ${importedData.size} products", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to import products", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Excel Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
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
                    Toast.makeText(context, "Products exported successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to export products", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Designed PDF Export Launcher
    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
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
                canvas.drawText("PRODUCT LIST REPORT", 40f, 140f, titlePaint)
                
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
                canvas.drawText("NAME", 130f, currentY, paint)
                canvas.drawText("GROUP", 300f, currentY, paint)
                canvas.drawText("PRICE", 420f, currentY, paint)
                canvas.drawText("STOCK", 500f, currentY, paint)
                
                // --- 4. Table Rows ---
                paint.apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    textSize = 9f
                }
                currentY += 25f
                
                viewModel.products.forEach { product ->
                    if (currentY > 800f) return@forEach // Simple overflow check
                    
                    canvas.drawText(product.barcode, 45f, currentY, paint)
                    canvas.drawText(product.name.take(25), 130f, currentY, paint)
                    canvas.drawText(product.group.take(15), 300f, currentY, paint)
                    canvas.drawText(product.salePrice, 420f, currentY, paint)
                    val stock = viewModel.stockMap[product.barcode] ?: 0.0
                    canvas.drawText(String.format(Locale.US, "%.0f", stock), 500f, currentY, paint)
                    
                    paint.color = android.graphics.Color.LTGRAY
                    paint.strokeWidth = 0.5f
                    canvas.drawLine(40f, currentY + 5f, 555f, currentY + 5f, paint)
                    paint.color = android.graphics.Color.BLACK
                    
                    currentY += 20f
                }

                // --- 5. Footer ---
                paint.color = android.graphics.Color.GRAY
                paint.textSize = 8f
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

    val allGroups by remember {
        derivedStateOf {
            listOf("All") + viewModel.products.map { it.group }.distinct()
        }
    }

    val filteredProducts by remember(searchQuery, selectedGroupFilter, viewModel.products.size) {
        derivedStateOf {
            viewModel.products.filter { 
                (selectedGroupFilter == "All" || it.group == selectedGroupFilter) &&
                (it.name.contains(searchQuery, ignoreCase = true) || it.barcode.contains(searchQuery))
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToItemEntry,
                containerColor = SidebarSelected,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        },
        containerColor = DashboardBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ProductTopBar(
                onAddClick = onNavigateToItemEntry,
                groups = allGroups,
                selectedGroup = selectedGroupFilter,
                onGroupSelected = { selectedGroupFilter = it },
                onImportClick = { importLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                onExportClick = { exportLauncher.launch("products.xlsx") },
                onExportPdfClick = { exportPdfLauncher.launch("product_list.pdf") },
                onShowExampleClick = {
                    showExampleDialog = true
                },
                onRefreshClick = {
                    isRefreshing = true
                    viewModel.refreshAllData()
                    Toast.makeText(context, "Syncing with Cloud...", Toast.LENGTH_SHORT).show()
                }
            )

            if (isRefreshing || viewModel.isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = SidebarSelected,
                    trackColor = Color.Transparent
                )
                LaunchedEffect(isRefreshing) {
                    if (isRefreshing) {
                        kotlinx.coroutines.delay(2000)
                        isRefreshing = false
                    }
                }
            }

            ProductSearchBarMobile(searchQuery) { searchQuery = it }
            ProductListMobile(filteredProducts) { product ->
                editingProduct = product
            }
        }
    }

    if (editingProduct != null) {
        EditProductDialog(
            product = editingProduct!!,
            onDismiss = { editingProduct = null },
            onConfirm = { updatedProduct ->
                viewModel.updateProduct(editingProduct!!.barcode, updatedProduct)
                editingProduct = null
            },
            onDelete = {
                viewModel.deleteProduct(editingProduct!!.barcode)
                editingProduct = null
            }
        )
    }

    if (showExampleDialog) {
        ExampleCSVDialog(onDismiss = { showExampleDialog = false })
    }
}

@Composable
fun ProductTopBar(
    onAddClick: () -> Unit,
    groups: List<String>,
    selectedGroup: String,
    onGroupSelected: (String) -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onExportPdfClick: () -> Unit,
    onShowExampleClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBackground)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Products", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.weight(1f))
        
        IconButton(onClick = onRefreshClick) { 
            Icon(Icons.Default.Refresh, "Refresh", tint = Color.White) 
        }

        IconButton(onClick = onAddClick) { 
            Icon(Icons.Default.Add, "Add Product", tint = Color.White) 
        }
        
        Box {
            IconButton(onClick = { showFilterMenu = true }) { 
                Icon(Icons.Default.FilterList, "Filter", tint = Color.White) 
            }
            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = { showFilterMenu = false },
                modifier = Modifier.background(CardBackground)
            ) {
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                group, 
                                color = if (group == selectedGroup) SidebarSelected else Color.White,
                                fontWeight = if (group == selectedGroup) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        onClick = {
                            onGroupSelected(group)
                            showFilterMenu = false
                        }
                    )
                }
            }
        }

        Box {
            IconButton(onClick = { showOptionsMenu = true }) { 
                Icon(Icons.Default.MoreVert, "Options", tint = Color.White) 
            }
            DropdownMenu(
                expanded = showOptionsMenu,
                onDismissRequest = { showOptionsMenu = false },
                modifier = Modifier.background(CardBackground)
            ) {
                DropdownMenuItem(
                    text = { Text("Import Products", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.FileUpload, null, tint = Color.White) },
                    onClick = {
                        onImportClick()
                        showOptionsMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Export to Excel", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.FileDownload, null, tint = Color.White) },
                    onClick = {
                        onExportClick()
                        showOptionsMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Export to PDF Report", color = Color.White) },
                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, null, tint = Color.White) },
                    onClick = {
                        onExportPdfClick()
                        showOptionsMenu = false
                    }
                )
                 DropdownMenuItem(
                    text = { Text("Show Example", color = Color.White) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, null, tint = Color.White) },
                    onClick = {
                        onShowExampleClick()
                        showOptionsMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun ExampleCSVDialog(onDismiss: () -> Unit) {
    val exampleHeader = "Barcode, Name, Price, Balance"
    val exampleCsv = """
        Barcode, Name, Price, Balance
        1001, Coca-Cola, 1.50, 50.0
        1002, Pepsi, 1.45, 100.0
        1003, Lays Chips, 2.00, 30.0
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("Import Example (Excel/CSV)", color = Color.White) },
        text = {
            Column {
                Text("Ensure your Excel file has these columns in order:", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        exampleCsv,
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected)) {
                Text("Got it")
            }
        }
    )
}

@Composable
fun ProductSearchBarMobile(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        placeholder = { Text("Search by name or barcode...", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = SidebarSelected,
            focusedIndicatorColor = SidebarSelected
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun ProductListMobile(products: List<ProductItem>, onEdit: (ProductItem) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(products) { product ->
            ProductItemRowMobile(product, onEdit)
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
        }
    }
}

@Composable
fun ProductItemRowMobile(product: ProductItem, onEdit: (ProductItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(product) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Barcode: ${product.barcode} | Group: ${product.group}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${product.salePrice}",
                color = SidebarSelected,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = product.unit,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
        
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.DarkGray,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: ProductItem,
    onDismiss: () -> Unit,
    onConfirm: (ProductItem) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var price by remember { mutableStateOf(product.salePrice) }
    var cost by remember { mutableStateOf(product.cost) }
    var group by remember { mutableStateOf(product.group) }
    var unit by remember { mutableStateOf(product.unit) }
    var barcode by remember { mutableStateOf(product.barcode) }
    var code by remember { mutableStateOf(product.code) }
    var active by remember { mutableStateOf(product.active) }
    var imageUrl by remember { mutableStateOf(product.imageUrl) }
    
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUrl = it.toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("Edit Product", color = Color.White) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Image Section in Edit Dialog
                Text("Product Image", color = Color.Gray, fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                        .clickable { photoLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.DarkGray, modifier = Modifier.size(32.dp))
                    }
                }
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Sale Price") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost Price") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = group, onValueChange = { group = it }, label = { Text("Group") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (PCS, KG)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = active, onCheckedChange = { active = it }, colors = CheckboxDefaults.colors(checkedColor = SidebarSelected))
                    Text("Active Product", color = Color.White)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = Color.Red)
                }
                Button(
                    onClick = {
                        onConfirm(product.copy(name = name, salePrice = price, cost = cost, group = group, unit = unit, barcode = barcode, active = active, imageUrl = imageUrl))
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected)
                ) {
                    Text("Save Changes")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
