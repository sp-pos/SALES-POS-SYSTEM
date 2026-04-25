package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.AppDestinations
import com.example.salespossystem.data.Invoice
import com.example.salespossystem.data.Expense
import com.example.salespossystem.ui.theme.*
import com.example.salespossystem.util.PrintingService
import java.text.SimpleDateFormat
import java.util.*

data class DocumentDisplayItem(
    val id: String,
    val displayId: String,
    val date: String,
    val amount: Double,
    val type: String,
    val reference: Any
)

@Composable
fun DocumentScreen(
    viewModel: SalesViewModel = viewModel(),
    onNavigate: (AppDestinations) -> Unit
) {
    var selectedDocument by remember { mutableStateOf<DocumentDisplayItem?>(null) }
    var selectedPeriod by remember { mutableStateOf("All Time") }
    var selectedType by remember { mutableStateOf("ALL") }
    var showPasswordPrompt by remember { mutableStateOf<(() -> Unit)?>(null) }
    val context = LocalContext.current

    val filteredDocuments by remember(selectedPeriod, selectedType, viewModel.allInvoices.size, viewModel.expenses.size) {
        derivedStateOf {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val today = sdf.format(Date())
            val currentMonth = today.substring(3)

            val allDocs = mutableListOf<DocumentDisplayItem>()
            
            viewModel.allInvoices.forEach { inv ->
                val type = when {
                    inv.paymentMethod == "DAMAGE" -> "DAMAGE"
                    inv.isPurchase && inv.paymentMethod.contains("RETURN") -> "PURCHASE RETURN"
                    inv.isPurchase -> "PURCHASE"
                    inv.paymentMethod.contains("RETURN") -> "SALE RETURN"
                    else -> "SALE"
                }
                allDocs.add(DocumentDisplayItem(
                    id = inv.invoiceNumber,
                    displayId = inv.invoiceNumber.takeLast(6),
                    date = inv.date,
                    amount = inv.totalAmount,
                    type = type,
                    reference = inv
                ))
            }
            
            viewModel.expenses.forEach { exp ->
                allDocs.add(DocumentDisplayItem(
                    id = exp.id.toString(),
                    displayId = "EXP-${exp.id}",
                    date = exp.date,
                    amount = exp.amount,
                    type = "EXPENSE",
                    reference = exp
                ))
            }

            allDocs.filter { doc ->
                val periodMatch = when (selectedPeriod) {
                    "Today" -> doc.date.startsWith(today)
                    "This Month" -> doc.date.contains(currentMonth)
                    else -> true
                }
                val typeMatch = when (selectedType) {
                    "ALL" -> true
                    else -> doc.type == selectedType
                }
                periodMatch && typeMatch
            }.sortedWith { a, b ->
                try {
                    val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
                    val dateA = format.parse(a.date)
                    val dateB = format.parse(b.date)
                    dateB?.compareTo(dateA) ?: 0
                } catch (e: Exception) {
                    0
                }
            }
        }
    }

    // Password Prompt Dialog
    if (showPasswordPrompt != null) {
        SecurityPasswordDialog(
            viewModel = viewModel,
            onDismiss = { showPasswordPrompt = null },
            onVerified = {
                val action = showPasswordPrompt
                showPasswordPrompt = null
                action?.invoke()
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            DocumentFilters(
                onAdd = { onNavigate(AppDestinations.SALE) },
                onEdit = {
                    val doc = selectedDocument
                    if (doc != null) {
                        showPasswordPrompt = {
                            val obj = doc.reference
                            if (obj is Invoice) {
                                if (obj.paymentMethod == "DAMAGE") {
                                    Toast.makeText(context, "Damage invoices cannot be edited", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.startEditing(obj)
                                    val destination = if (obj.isPurchase) AppDestinations.PURCHASE_INVOICE else AppDestinations.SALE
                                    onNavigate(destination)
                                }
                            } else {
                                Toast.makeText(context, "Expenses cannot be edited yet", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please select a document to edit", Toast.LENGTH_SHORT).show()
                    }
                },
                onPrint = {
                    selectedDocument?.let { doc ->
                        when (val obj = doc.reference) {
                            is Invoice -> PrintingService.printInvoice(context, obj, viewModel.currencySymbol)
                            is Expense -> {
                                PrintingService.printExpense(
                                    context, obj, viewModel.companyName, 
                                    viewModel.companyAddress, viewModel.companyPhone, viewModel.companyTaxNumber,
                                    viewModel.currencySymbol
                                )
                            }
                        }
                    } ?: Toast.makeText(context, "Please select a document", Toast.LENGTH_SHORT).show()
                },
                onDelete = {
                    val doc = selectedDocument
                    if (doc != null) {
                        showPasswordPrompt = {
                            when (val obj = doc.reference) {
                                is Invoice -> viewModel.deleteInvoice(obj.invoiceNumber)
                                is Expense -> viewModel.deleteExpense(obj.id)
                            }
                            selectedDocument = null
                            Toast.makeText(context, "Document deleted", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Please select a document to delete", Toast.LENGTH_SHORT).show()
                    }
                },
                period = selectedPeriod,
                onPeriodChange = { selectedPeriod = it },
                type = selectedType,
                onTypeChange = { selectedType = it }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            DocumentTableMobile(
                title = "Documents (${filteredDocuments.size})",
                columns = listOf("Number", "Date", "Total", "Type"),
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    items(filteredDocuments, key = { it.id + it.type }) { doc ->
                        DocumentRow(
                            doc = doc, 
                            currencySymbol = viewModel.currencySymbol,
                            isSelected = selectedDocument?.id == doc.id && selectedDocument?.type == doc.type
                        ) {
                            selectedDocument = doc
                        }
                        HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DocumentTableMobile(
                title = "Document details",
                columns = listOf("Name", "Qty", "Price", "Total"),
                modifier = Modifier.weight(1f)
            ) {
                val currentDoc = selectedDocument
                if (currentDoc != null) {
                    LazyColumn(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        when (val obj = currentDoc.reference) {
                            is Invoice -> {
                                items(obj.items) { item ->
                                    ItemRow(item)
                                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                                }
                            }
                            is Expense -> {
                                item {
                                    ExpenseItemRow(obj)
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                        Text("Select a document to view details", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentFilters(
    onAdd: () -> Unit,
    onEdit: () -> Unit,
    onPrint: () -> Unit,
    onDelete: () -> Unit,
    period: String,
    onPeriodChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit
) {
    var showPeriodMenu by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SidebarBackground)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAdd) { Icon(Icons.Default.Add, "Add", tint = Color.Green) }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = SidebarSelected) }
        IconButton(onClick = onPrint) { Icon(Icons.Default.Print, "Print", tint = Color.White) }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }

        Spacer(modifier = Modifier.weight(1f))

        Box {
            Button(
                onClick = { showPeriodMenu = true },
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(period, fontSize = 12.sp)
                Icon(Icons.Default.ArrowDropDown, null)
            }
            DropdownMenu(
                expanded = showPeriodMenu,
                onDismissRequest = { showPeriodMenu = false },
                modifier = Modifier.background(CardBackground)
            ) {
                listOf("Today", "This Month", "All Time").forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p, color = Color.White) },
                        onClick = {
                            onPeriodChange(p)
                            showPeriodMenu = false
                        }
                    )
                }
            }
        }

        Box {
            Button(
                onClick = { showTypeMenu = true },
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(type, fontSize = 12.sp)
                Icon(Icons.Default.ArrowDropDown, null)
            }
            DropdownMenu(
                expanded = showTypeMenu,
                onDismissRequest = { showTypeMenu = false },
                modifier = Modifier.background(CardBackground)
            ) {
                listOf("ALL", "SALE", "PURCHASE", "SALE RETURN", "PURCHASE RETURN", "DAMAGE", "EXPENSE").forEach { t ->
                    DropdownMenuItem(
                        text = { Text(t, color = Color.White) },
                        onClick = {
                            onTypeChange(t)
                            showTypeMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentTableMobile(
    title: String,
    columns: List<String>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth().border(0.5.dp, Color.DarkGray)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SidebarBackground)
                .padding(8.dp)
        ) {
            Text(title, color = SidebarSelected, fontWeight = FontWeight.Bold)
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(8.dp)
        ) {
            columns.forEachIndexed { index, col ->
                Text(
                    text = col,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(if (index == 0) 1.5f else 1f)
                )
            }
        }
        
        content()
    }
}

@Composable
fun DocumentRow(
    doc: DocumentDisplayItem,
    currencySymbol: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) SidebarSelected.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(doc.displayId, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
        Text(doc.date.split(" ")[0], color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text("$currencySymbol ${String.format("%.2f", doc.amount)}", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(doc.type, color = when(doc.type) {
            "SALE" -> Color.Green
            "PURCHASE" -> Color.Cyan
            "EXPENSE" -> Color.Yellow
            else -> Color.Red
        }, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ItemRow(item: com.example.salespossystem.data.CartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(item.productName, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
        Text(String.format(java.util.Locale.US, "%.2f", item.quantity), color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(String.format(java.util.Locale.US, "%.2f", item.price), color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(String.format(java.util.Locale.US, "%.2f", item.quantity * item.price), color = SidebarSelected, fontSize = 12.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ExpenseItemRow(expense: com.example.salespossystem.data.Expense) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(expense.category, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
        Text("1", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(String.format("%.2f", expense.amount), color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(String.format("%.2f", expense.amount), color = SidebarSelected, fontSize = 12.sp, modifier = Modifier.weight(1f))
    }
}
