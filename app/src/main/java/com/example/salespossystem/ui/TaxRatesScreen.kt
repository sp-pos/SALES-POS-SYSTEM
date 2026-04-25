package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import android.widget.Toast
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.data.TaxRateItem
import com.example.salespossystem.ui.theme.*

@Composable
fun TaxRatesScreen(viewModel: SalesViewModel = viewModel()) {
    var selectedTaxRate by remember { mutableStateOf<TaxRateItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        TaxRatesTopBar(
            onRefresh = { viewModel.loadDataFromDatabase() },
            onNew = { showAddDialog = true },
            onEdit = {
                if (selectedTaxRate != null) {
                    showEditDialog = true
                } else {
                    Toast.makeText(context, "Please select a tax rate to edit", Toast.LENGTH_SHORT).show()
                }
            },
            onDelete = {
                selectedTaxRate?.let {
                    viewModel.deleteTaxRate(it)
                    selectedTaxRate = null
                    Toast.makeText(context, "Tax rate deleted", Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(context, "Please select a tax rate to delete", Toast.LENGTH_SHORT).show()
            }
        )
        
        TaxRatesListMobile(
            taxRates = viewModel.taxRates,
            selectedTaxRate = selectedTaxRate,
            onSelect = { selectedTaxRate = it },
            modifier = Modifier.weight(1f)
        )
    }

    if (showAddDialog) {
        TaxRateDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newRate ->
                viewModel.addTaxRate(newRate)
                showAddDialog = false
                Toast.makeText(context, "Tax rate added", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showEditDialog && selectedTaxRate != null) {
        TaxRateDialog(
            taxRate = selectedTaxRate,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedRate ->
                viewModel.addTaxRate(updatedRate)
                showEditDialog = false
                selectedTaxRate = updatedRate
                Toast.makeText(context, "Tax rate updated", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun TaxRateDialog(
    taxRate: TaxRateItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (TaxRateItem) -> Unit
) {
    var code by remember { mutableStateOf(taxRate?.code ?: "") }
    var name by remember { mutableStateOf(taxRate?.name ?: "") }
    var rate by remember { mutableStateOf(taxRate?.rate ?: "") }
    var enabled by remember { mutableStateOf(taxRate?.enabled ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2D2D2D),
        title = { Text(if (taxRate == null) "New Tax Rate" else "Edit Tax Rate", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SidebarSelected,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SidebarSelected,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Rate (%)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SidebarSelected,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        colors = CheckboxDefaults.colors(checkedColor = SidebarSelected)
                    )
                    Text("Enabled", color = Color.White)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (code.isNotBlank() && name.isNotBlank() && rate.isNotBlank()) {
                        onConfirm(TaxRateItem(code, name, rate, enabled, taxRate?.fixed ?: false))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun TaxRatesTopBar(
    onRefresh: () -> Unit,
    onNew: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
        ProductActionClickable(Icons.Default.Refresh, "Refresh", onClick = onRefresh)
        ProductActionClickable(Icons.Default.Add, "New tax", onClick = onNew)
        ProductActionClickable(Icons.Default.Edit, "Edit", onClick = onEdit)
        ProductActionClickable(Icons.Default.Delete, "Delete", onClick = onDelete)
        Spacer(modifier = Modifier.width(8.dp))
        VerticalDivider(modifier = Modifier.height(30.dp), color = Color.DarkGray)
        ProductActionClickable(Icons.Default.Help, "Help", onClick = {})
    }
}

@Composable
fun ProductActionClickable(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable { onClick() }
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Text(label, color = Color.White, fontSize = 10.sp)
    }
}

@Composable
fun TaxRatesListMobile(
    taxRates: List<TaxRateItem>,
    selectedTaxRate: TaxRateItem?,
    onSelect: (TaxRateItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize().background(Color.Black)) {
        items(taxRates) { item ->
            TaxRateListItem(
                item = item,
                isSelected = selectedTaxRate?.code == item.code,
                onClick = { onSelect(item) }
            )
            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
        }
    }
}

@Composable
fun TaxRateListItem(
    item: TaxRateItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFF333333) else Color.Transparent)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tax Code
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(SidebarSelected.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .border(1.dp, SidebarSelected, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(item.code, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                if (item.enabled) {
                    Text("Enabled", color = Color.Green, fontSize = 11.sp)
                } else {
                    Text("Disabled", color = Color.Red, fontSize = 11.sp)
                }
                if (item.fixed) {
                    Text(" • Fixed", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
        
        Text("${item.rate}%", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
fun TaxRatesScreenPreview() {
    TaxRatesScreen()
}
