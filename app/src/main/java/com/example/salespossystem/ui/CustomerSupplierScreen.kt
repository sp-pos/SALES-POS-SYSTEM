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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.ui.theme.*
import com.example.salespossystem.data.Customer
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerSupplierScreen(viewModel: SalesViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf("Customers") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Customer?>(null) }
    var selectedItemForAction by remember { mutableStateOf<Customer?>(null) }
    val context = LocalContext.current

    val currentList = if (selectedTab == "Customers") viewModel.customers else viewModel.suppliers

    // Professional PDF Export
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
                    color = AndroidColor.BLACK
                    textSize = 24f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(viewModel.companyName.ifEmpty { "SALES POS SYSTEM" }, 40f, 60f, titlePaint)
                
                paint.apply {
                    color = AndroidColor.DKGRAY
                    textSize = 10f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                canvas.drawText(viewModel.companyAddress, 40f, 80f, paint)
                canvas.drawText("Phone: ${viewModel.companyPhone} | TRN: ${viewModel.companyTaxNumber}", 40f, 95f, paint)
                
                paint.color = AndroidColor.BLACK
                paint.strokeWidth = 2f
                canvas.drawLine(40f, 110f, 555f, 110f, paint)
                
                // --- 2. Report Title ---
                titlePaint.textSize = 18f
                val reportTitle = if (selectedTab == "Customers") "CUSTOMER LIST REPORT" else "SUPPLIER LIST REPORT"
                canvas.drawText(reportTitle, 40f, 140f, titlePaint)
                
                val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(Date())
                paint.textSize = 10f
                canvas.drawText("Generated on: $dateStr", 40f, 155f, paint)

                // --- 3. Table Headers ---
                var currentY = 190f
                paint.apply {
                    color = AndroidColor.LTGRAY
                    style = Paint.Style.FILL
                }
                canvas.drawRect(40f, currentY - 20f, 555f, currentY + 5f, paint)
                
                paint.apply {
                    color = AndroidColor.BLACK
                    textSize = 10f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("CODE", 45f, currentY, paint)
                canvas.drawText("NAME", 100f, currentY, paint)
                canvas.drawText("PHONE", 250f, currentY, paint)
                canvas.drawText("TAX NUMBER", 360f, currentY, paint)
                canvas.drawText("ADDRESS", 470f, currentY, paint)
                
                // --- 4. Table Rows ---
                paint.apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    textSize = 9f
                }
                currentY += 25f
                
                currentList.forEach { item ->
                    if (currentY > 800f) return@forEach 
                    
                    canvas.drawText(item.code, 45f, currentY, paint)
                    canvas.drawText(item.name.take(20), 100f, currentY, paint)
                    canvas.drawText(item.phone, 250f, currentY, paint)
                    canvas.drawText(item.taxNumber, 360f, currentY, paint)
                    canvas.drawText(item.address.take(15), 470f, currentY, paint)
                    
                    paint.color = AndroidColor.LTGRAY
                    paint.strokeWidth = 0.5f
                    canvas.drawLine(40f, currentY + 5f, 555f, currentY + 5f, paint)
                    paint.color = AndroidColor.BLACK
                    
                    currentY += 20f
                }

                // --- 5. Footer ---
                paint.color = AndroidColor.GRAY
                paint.textSize = 8f
                canvas.drawText("Software Powered by SALES POS SYSTEM", 40f, 820f, paint)

                pdfDocument.finishPage(page)
                context.contentResolver.openOutputStream(uri)?.use { pdfDocument.writeTo(it) }
                pdfDocument.close()
                Toast.makeText(context, "Professional PDF Generated Successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "PDF Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val filteredList by remember(searchQuery, selectedTab, currentList.size) {
        derivedStateOf {
            if (searchQuery.isEmpty()) currentList
            else currentList.filter { it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        CustomerSupplierTopBar(
            onAddClick = { showAddDialog = true },
            onRefreshClick = { searchQuery = ""; selectedItemForAction = null },
            onEditClick = {
                selectedItemForAction?.let { editingItem = it }
                    ?: Toast.makeText(context, "Select an item first", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = {
                selectedItemForAction?.let { item ->
                    viewModel.deleteCustomer(item.id, selectedTab == "Suppliers")
                    selectedItemForAction = null
                } ?: Toast.makeText(context, "Select an item first", Toast.LENGTH_SHORT).show()
            },
            onExportPdf = { exportPdfLauncher.launch("${selectedTab.lowercase()}_report.pdf") },
            onHelpClick = { showHelpDialog = true }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBackground)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TabItem("Customers", selectedTab == "Customers") { selectedTab = "Customers"; selectedItemForAction = null }
            Spacer(Modifier.width(16.dp))
            TabItem("Suppliers", selectedTab == "Suppliers") { selectedTab = "Suppliers"; selectedItemForAction = null }
        }
        
        CustomerSupplierSearchBar(searchQuery, if(selectedTab == "Customers") "customers" else "suppliers") { searchQuery = it }
        
        CustomerListMobile(
            items = filteredList,
            selectedItemId = selectedItemForAction?.id,
            onItemClick = { selectedItemForAction = it },
            type = if (selectedTab == "Customers") "Customer" else "Supplier",
            modifier = Modifier.weight(1f)
        )
    }

    if (showAddDialog) {
        AddEditCustomerDialog(
            isSupplier = selectedTab == "Suppliers",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, address, taxNumber, code ->
                viewModel.insertCustomer(name, phone, address, taxNumber, code, selectedTab == "Suppliers")
                showAddDialog = false
            }
        )
    }

    if (editingItem != null) {
        AddEditCustomerDialog(
            customer = editingItem,
            isSupplier = selectedTab == "Suppliers",
            onDismiss = { editingItem = null },
            onConfirm = { name, phone, address, taxNumber, code ->
                viewModel.updateCustomer(editingItem!!.copy(name = name, phone = phone, address = address, taxNumber = taxNumber, code = code), selectedTab == "Suppliers")
                editingItem = null
                selectedItemForAction = null
            }
        )
    }
}

@Composable
fun CustomerSupplierTopBar(
    onAddClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onExportPdf: () -> Unit,
    onHelpClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(HeaderBackground).padding(8.dp).horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProductAction(Icons.Default.Refresh, "Refresh", onClick = onRefreshClick)
        ProductAction(Icons.Default.Add, "Add", onClick = onAddClick)
        ProductAction(Icons.Default.Edit, "Edit", onClick = onEditClick)
        ProductAction(Icons.Default.Delete, "Delete", onClick = onDeleteClick)
        VerticalDivider(modifier = Modifier.height(32.dp).padding(horizontal = 8.dp), color = Color.DarkGray)
        ProductAction(Icons.Default.PictureAsPdf, "Export PDF", onClick = onExportPdf)
        ProductAction(Icons.Default.Help, "Help", onClick = onHelpClick)
    }
}

@Composable
fun CustomerSupplierSearchBar(query: String, type: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        placeholder = { Text("Search $type...", color = Color.Gray, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Black, unfocusedContainerColor = Color.Black,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}

@Composable
fun CustomerListMobile(items: List<Customer>, selectedItemId: String?, onItemClick: (Customer) -> Unit, type: String, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        items(items) { item ->
            val isSelected = item.id == selectedItemId
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onItemClick(item) }.border(1.dp, if (isSelected) SidebarSelected else Color.Transparent, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) SidebarSelected.copy(alpha = 0.2f) else CardBackground),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(if (isSelected) SidebarSelected else Color.DarkGray, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                        Text(item.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Phone: ${item.phone}", color = Color.Gray, fontSize = 14.sp)
                    }
                    if (item.code.isNotEmpty()) {
                        Text(item.code, color = SidebarSelected, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(SidebarSelected.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerDialog(
    customer: Customer? = null,
    isSupplier: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var taxNumber by remember { mutableStateOf(customer?.taxNumber ?: "") }
    var code by remember { mutableStateOf(customer?.code ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer == null) "Add ${if (isSupplier) "Supplier" else "Customer"}" else "Edit ${if (isSupplier) "Supplier" else "Customer"}", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = taxNumber, onValueChange = { taxNumber = it }, label = { Text("Tax Number / TRN") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), maxLines = 3)
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, phone, address, taxNumber, code) }, colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected)) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } },
        containerColor = DashboardBackground
    )
}
