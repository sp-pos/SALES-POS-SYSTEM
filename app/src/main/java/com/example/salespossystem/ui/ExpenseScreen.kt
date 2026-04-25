package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.ui.theme.DashboardBackground
import com.example.salespossystem.ui.theme.HeaderBackground
import com.example.salespossystem.ui.theme.SidebarSelected
import com.example.salespossystem.util.PrintingService
import com.example.salespossystem.data.Expense
import java.util.Locale

@Composable
fun ExpenseScreen(viewModel: SalesViewModel = viewModel(), onBack: () -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }
    val expenses = viewModel.expenses
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (expenses.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No expenses recorded", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(expenses) { expense ->
                        ExpenseItem(
                            expense = expense,
                            currencySymbol = viewModel.currencySymbol,
                            onDelete = { viewModel.deleteExpense(expense.id) },
                            onPrint = { 
                                PrintingService.printExpense(
                                    context, expense, viewModel.companyName, 
                                    viewModel.companyAddress, viewModel.companyPhone, viewModel.companyTaxNumber,
                                    viewModel.currencySymbol
                                ) 
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = SidebarSelected,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(Icons.Default.Add, "Add Expense")
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            currencySymbol = viewModel.currencySymbol,
            onDismiss = { showAddDialog = false },
            onConfirm = { category, amount, description, paymentMethod ->
                viewModel.addExpense(category, amount, description, paymentMethod)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExpenseItem(expense: Expense, currencySymbol: String, onDelete: () -> Unit, onPrint: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(expense.category, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = SidebarSelected.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = expense.paymentMethod,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = SidebarSelected, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(expense.date, color = Color.Gray, fontSize = 12.sp)
                if (expense.description.isNotEmpty()) {
                    Text(expense.description, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                }
                Text("Spent by: ${expense.userName}", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    String.format(Locale.US, "%.2f %s", expense.amount, currencySymbol),
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    IconButton(onClick = onPrint, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Print, "Print", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(currencySymbol: String, onDismiss: () -> Unit, onConfirm: (String, Double, String, String) -> Unit) {
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var expanded by remember { mutableStateOf(false) }
    
    val paymentOptions = listOf("CASH", "CARD", "BANK TRANSFER", "MOBILE MONEY", "OTHER")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2D2D2D),
        title = { Text("Add New Expense", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Category Field
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Rent, Electricity)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = SidebarSelected,
                        unfocusedLabelColor = Color.Gray
                    )
                )
                
                // Payment Method Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = SidebarSelected,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    // Transparent clickable layer for the dropdown anchor
                    Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f).background(Color(0xFF333333))
                    ) {
                        paymentOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color.White) },
                                onClick = {
                                    paymentMethod = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = SidebarSelected,
                        unfocusedLabelColor = Color.Gray
                    )
                )
                
                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = SidebarSelected,
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    if (category.isNotEmpty() && amountVal > 0) {
                        onConfirm(category, amountVal, description, paymentMethod)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text("Add Expense", modifier = Modifier.padding(horizontal = 8.dp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
