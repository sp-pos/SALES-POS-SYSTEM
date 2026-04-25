package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.salespossystem.data.PaymentTypeItem
import com.example.salespossystem.ui.theme.*

@Composable
fun PaymentTypesScreen(viewModel: SalesViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<PaymentTypeItem?>(null) }
    var selectedItem by remember { mutableStateOf<PaymentTypeItem?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Initial data if empty
    LaunchedEffect(Unit) {
        if (viewModel.paymentTypes.isEmpty()) {
            val defaults = listOf(
                PaymentTypeItem("Cash", "1", true, true, false, true, true, true, "C"),
                PaymentTypeItem("Credit Card", "2", true, true, false, false, true, true, "K"),
                PaymentTypeItem("Debit Card", "3", true, true, false, false, true, true, "D")
            )
            defaults.forEach { viewModel.addPaymentType(it) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        PaymentTypesContent(
            viewModel = viewModel,
            selectedItem = selectedItem,
            onItemSelect = { selectedItem = if (selectedItem == it) null else it },
            onAddClick = { showAddDialog = true },
            onEditClick = { selectedItem?.let { itemToEdit = it } },
            onDeleteClick = { if (selectedItem != null) showDeleteConfirm = true },
            onRefreshClick = { viewModel.loadDataFromDatabase() }
        )
    }

    if (showAddDialog) {
        AddEditPaymentTypeDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newItem ->
                viewModel.addPaymentType(newItem)
                showAddDialog = false
            }
        )
    }

    if (itemToEdit != null) {
        AddEditPaymentTypeDialog(
            item = itemToEdit,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                viewModel.addPaymentType(updatedItem)
                itemToEdit = null
                selectedItem = null
            }
        )
    }

    if (showDeleteConfirm && selectedItem != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = DashboardBackground,
            title = { Text("Delete Payment Type", color = Color.White) },
            text = { Text("Are you sure you want to delete ${selectedItem?.name}?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    selectedItem?.let { viewModel.deletePaymentType(it) }
                    selectedItem = null
                    showDeleteConfirm = false
                }) { Text("Yes", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("No", color = Color.White) }
            }
        )
    }
}

@Composable
fun PaymentTypesContent(
    viewModel: SalesViewModel,
    selectedItem: PaymentTypeItem?,
    onItemSelect: (PaymentTypeItem) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PaymentTypesTopBar(
            onAddClick = onAddClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onRefreshClick = onRefreshClick,
            isItemSelected = selectedItem != null
        )
        PaymentTypeListMobile(
            viewModel = viewModel, 
            selectedItem = selectedItem,
            modifier = Modifier.weight(1f),
            onItemClick = onItemSelect
        )
    }
}

@Composable
fun PaymentTypesTopBar(
    onAddClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isItemSelected: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(HeaderBackground)
            .padding(horizontal = 8.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onRefreshClick) {
            ProductAction(Icons.Default.Refresh, "Refresh")
        }
        IconButton(onClick = onAddClick) {
            ProductAction(Icons.Default.Add, "New type")
        }
        IconButton(onClick = onEditClick, enabled = isItemSelected) {
            ProductAction(Icons.Default.Edit, "Edit", tint = if(isItemSelected) Color.White else Color.Gray)
        }
        IconButton(onClick = onDeleteClick, enabled = isItemSelected) {
            ProductAction(Icons.Default.Delete, "Delete", tint = if(isItemSelected) Color.White else Color.Gray)
        }
        Spacer(modifier = Modifier.width(8.dp))
        VerticalDivider(modifier = Modifier.height(30.dp), color = Color.DarkGray)
        ProductAction(Icons.Default.Help, "Help")
    }
}

@Composable
fun PaymentTypeListMobile(
    viewModel: SalesViewModel, 
    selectedItem: PaymentTypeItem?,
    modifier: Modifier = Modifier,
    onItemClick: (PaymentTypeItem) -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxSize().background(Color.Black)) {
        items(viewModel.paymentTypes) { item ->
            val isSelected = selectedItem == item
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) SidebarSelected.copy(alpha = 0.3f) else Color.Transparent)
                    .clickable { onItemClick(item) }
            ) {
                PaymentTypeListItem(item)
            }
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
        }
    }
}

@Composable
fun PaymentTypeListItem(item: PaymentTypeItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(SidebarSelected.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                .border(1.dp, SidebarSelected, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(item.position, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                if (item.enabled) {
                    Text("Enabled", color = Color.Green, fontSize = 11.sp)
                }
                if (item.shortcutKey.isNotEmpty()) {
                    Text(" • Key: ${item.shortcutKey}", color = Color.Gray, fontSize = 11.sp)
                }
            }
            
            Row(
                modifier = Modifier.padding(top = 4.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (item.quickPayment) SettingChip("Quick")
                if (item.customerRequired) SettingChip("Cust. Req.")
                if (item.changeAllowed) SettingChip("Change OK")
                if (item.markAsPaid) SettingChip("Paid")
            }
        }
        
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.DarkGray)
    }
}

@Composable
fun SettingChip(label: String) {
    Surface(
        color = Color.DarkGray.copy(alpha = 0.5f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Gray)
    ) {
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun AddEditPaymentTypeDialog(
    item: PaymentTypeItem? = null,
    onDismiss: () -> Unit,
    onSave: (PaymentTypeItem) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var position by remember { mutableStateOf(item?.position ?: "") }
    var enabled by remember { mutableStateOf(item?.enabled ?: true) }
    var quickPayment by remember { mutableStateOf(item?.quickPayment ?: false) }
    var customerRequired by remember { mutableStateOf(item?.customerRequired ?: false) }
    var changeAllowed by remember { mutableStateOf(item?.changeAllowed ?: true) }
    var markAsPaid by remember { mutableStateOf(item?.markAsPaid ?: true) }
    var printReceipt by remember { mutableStateOf(item?.printReceipt ?: true) }
    var shortcutKey by remember { mutableStateOf(item?.shortcutKey ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DashboardBackground)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Text(if (item == null) "New Payment Type" else "Edit Payment Type", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = position,
                        onValueChange = { position = it },
                        label = { Text("Position") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = enabled, onCheckedChange = { enabled = it })
                        Text("Enabled", color = Color.White)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = quickPayment, onCheckedChange = { quickPayment = it })
                        Text("Quick payment", color = Color.White)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = customerRequired, onCheckedChange = { customerRequired = it })
                        Text("Customer required", color = Color.White)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = changeAllowed, onCheckedChange = { changeAllowed = it })
                        Text("Change allowed", color = Color.White)
                    }
                    
                    OutlinedTextField(
                        value = shortcutKey,
                        onValueChange = { shortcutKey = it },
                        label = { Text("Shortcut Key") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Button(onClick = {
                            onSave(PaymentTypeItem(name, position, enabled, quickPayment, customerRequired, changeAllowed, markAsPaid, printReceipt, shortcutKey))
                        }) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
