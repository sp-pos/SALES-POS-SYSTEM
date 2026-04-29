package com.example.salespossystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salespossystem.viewmodel.SalesViewModel
import com.example.salespossystem.data.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SimpleModuleHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            title, 
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ModulePlaceholder(title: String, icon: ImageVector) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        SimpleModuleHeader(title, icon)
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("$title Module is Ready", color = Color.Gray)
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        SimpleModuleHeader("Dashboard Overview", Icons.Default.Dashboard)
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(300.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { DashboardStat("Today's Sales", "$0.00", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF10B981)) }
            item { DashboardStat("Orders", "0", Icons.Default.ShoppingCart, Color(0xFF3B82F6)) }
            item { DashboardStat("Expenses", "$0.00", Icons.Default.AccountBalanceWallet, Color(0xFFEF4444)) }
            item { DashboardStat("Low Stock", "5 Items", Icons.Default.Warning, Color(0xFFF59E0B)) }
        }
    }
}

@Composable
fun DashboardStat(label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(60.dp),
                color = color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(30.dp))
                }
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(value, style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun AdminStaffManagementScreen(viewModel: SalesViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredStaff = viewModel.users.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimpleModuleHeader("Staff Management", Icons.Default.Badge)
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
            ) {
                Icon(Icons.Default.PersonAdd, null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Staff")
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search staff by name or email...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (filteredStaff.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No staff members found.", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredStaff) { staff ->
                        StaffItemRow(staff, onDelete = { viewModel.deleteUser(staff.uid) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddStaffDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { user ->
                viewModel.addUser(user)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun StaffItemRow(user: User, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(Color(0xFFEDE7F6), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, tint = Color(0xFF673AB7))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold)
                Text(user.email, fontSize = 12.sp, color = Color.Gray)
            }
            Surface(
                color = if (user.role == "ADMIN") Color(0xFF673AB7) else Color(0xFF9575CD),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    user.role, 
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun AddStaffDialog(onDismiss: () -> Unit, onConfirm: (User) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STAFF") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Staff Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                
                Text("Access Role:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = role == "STAFF", onClick = { role = "STAFF" })
                    Text("Staff (POS Access)")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = role == "ADMIN", onClick = { role = "ADMIN" })
                    Text("Admin (Full Access)")
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (name.isNotEmpty() && email.isNotEmpty()) {
                    onConfirm(User(
                        uid = "U-${(1000..9999).random()}",
                        name = name,
                        email = email,
                        role = role
                    ))
                }
            }) {
                Text("Create Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
@Composable fun CountriesScreen() = ModulePlaceholder("Countries Settings", Icons.Default.Public)
@Composable fun DamageProductScreen() = ModulePlaceholder("Damage Products", Icons.Default.BrokenImage)
@Composable fun DocumentScreen() = ModulePlaceholder("Documents & Invoices", Icons.Default.Description)
@Composable
fun ExpenseScreen(viewModel: SalesViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var categorySearch by remember { mutableStateOf("") }

    val filteredExpenses = viewModel.expenses.filter { 
        it.category.contains(categorySearch, ignoreCase = true) || it.description.contains(categorySearch, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimpleModuleHeader("Expense Tracker", Icons.Default.Payments)
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Expense")
            }
        }

        OutlinedTextField(
            value = categorySearch,
            onValueChange = { categorySearch = it },
            label = { Text("Filter expenses...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            leadingIcon = { Icon(Icons.Default.FilterList, null) },
            shape = RoundedCornerShape(12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (filteredExpenses.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No expenses recorded.", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredExpenses) { expense ->
                        ExpenseItemRow(
                            expense = expense,
                            onDelete = { viewModel.deleteExpense(expense.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { category, amount, description ->
                viewModel.addExpense(Expense(
                    id = category.hashCode().toLong() + amount.toLong(),
                    date = "Today",
                    category = category,
                    amount = amount,
                    description = description,
                    userName = viewModel.currentUser?.name ?: "Admin"
                ))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExpenseItemRow(expense: Expense, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.category, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                Text(expense.description, fontSize = 12.sp, color = Color.Gray)
            }
            Text("- $${expense.amount}", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 16.sp)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Rent, Food)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (category.isNotEmpty() && amt > 0) onConfirm(category, amt, description) 
            }) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
@Composable
fun ItemDataEntryScreen(viewModel: SalesViewModel) {
    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("PCS") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SimpleModuleHeader("Item Data Entry", Icons.Default.AddBox)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Product Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") }, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(value = costPrice, onValueChange = { costPrice = it }, label = { Text("Cost Price") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = salePrice, onValueChange = { salePrice = it }, label = { Text("Sale Price") }, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Initial Stock") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (PCS, KG, etc.)") }, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (name.isNotEmpty() && barcode.isNotEmpty()) {
                            val product = ProductItem(
                                name = name,
                                barcode = barcode,
                                cost = costPrice,
                                salePrice = salePrice,
                                unit = unit
                            )
                            viewModel.addProduct(product)
                            viewModel.updateStock(barcode, stock.toDoubleOrNull() ?: 0.0)
                            
                            // Clear fields
                            name = ""; barcode = ""; costPrice = ""; salePrice = ""; stock = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Product to Inventory")
                }
            }
        }
    }
}

@Composable
fun ManagementDashboard(viewModel: SalesViewModel, onNavigate: (com.example.salespossystem.Screen) -> Unit) {
    val totalProducts = viewModel.products.size
    val totalCustomers = viewModel.customers.size
    val totalSuppliers = viewModel.suppliers.size

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SimpleModuleHeader("Management Overview", Icons.Default.Dashboard)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(200.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { DashboardStat("Total Products", totalProducts.toString(), Icons.Default.ShoppingBag, Color(0xFF673AB7)) }
            item { DashboardStat("Customers", totalCustomers.toString(), Icons.Default.People, Color(0xFF2196F3)) }
            item { DashboardStat("Suppliers", totalSuppliers.toString(), Icons.Default.Person, Color(0xFF009688)) }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Quick Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickSettingItem("Tax Settings", Icons.Default.Percent, Modifier.weight(1f)) {
                onNavigate(com.example.salespossystem.Screen.TaxRates)
            }
            QuickSettingItem("Payment Methods", Icons.Default.CreditCard, Modifier.weight(1f)) {
                onNavigate(com.example.salespossystem.Screen.PaymentTypes)
            }
            QuickSettingItem("Company Profile", Icons.Default.Business, Modifier.weight(1f)) {
                onNavigate(com.example.salespossystem.Screen.Company)
            }
        }
    }
}

@Composable
fun QuickSettingItem(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
@Composable
fun MyCompanyScreen(viewModel: SalesViewModel) {
    var name by remember { mutableStateOf(viewModel.companyName) }
    var address by remember { mutableStateOf(viewModel.companyAddress) }
    var phone by remember { mutableStateOf(viewModel.companyPhone) }
    var taxNo by remember { mutableStateOf(viewModel.companyTaxNumber) }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        SimpleModuleHeader("My Company Profile", Icons.Default.Business)
        
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Business Details", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Business Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = taxNo, onValueChange = { taxNo = it }, label = { Text("Tax Identification Number") }, modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { viewModel.updateCompanyInfo(name, address, phone, taxNo) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Update Profile", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun PaymentTypesScreen(viewModel: SalesViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newPaymentName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimpleModuleHeader("Payment Methods", Icons.Default.CreditCard)
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Method")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (viewModel.paymentTypes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No custom payment methods. Default: Cash, Card", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(viewModel.paymentTypes) { method ->
                        PaymentMethodRow(method.name) { viewModel.deletePaymentType(method.name) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Payment Method") },
            text = {
                OutlinedTextField(
                    value = newPaymentName,
                    onValueChange = { newPaymentName = it },
                    label = { Text("Method Name (e.g. Bkash, Nagad)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPaymentName.isNotEmpty()) {
                        viewModel.addPaymentType(PaymentTypeItem(name = newPaymentName))
                        newPaymentName = ""
                        showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun PaymentMethodRow(name: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Payments, null, tint = Color(0xFF673AB7))
            Spacer(Modifier.width(16.dp))
            Text(name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun PriceListScreen(viewModel: SalesViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredProducts = viewModel.products.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.barcode.contains(searchQuery)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SimpleModuleHeader("Product Price List", Icons.Default.FormatListBulleted)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search products...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Product Name", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                    Text("Barcode", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Unit", Modifier.weight(0.5f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("Sale Price", Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                }
                
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

                if (filteredProducts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No products found.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredProducts) { product ->
                            PriceListItemRow(product, viewModel.currencySymbol)
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriceListItemRow(product: ProductItem, currency: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(product.name, fontWeight = FontWeight.SemiBold)
            Text(product.group, fontSize = 11.sp, color = Color.Gray)
        }
        Text(product.barcode, Modifier.weight(1f), fontSize = 13.sp, color = Color.Gray)
        Text(product.unit, Modifier.weight(0.5f), fontSize = 13.sp, textAlign = TextAlign.Center)
        Text(
            "$currency${product.salePrice}", 
            Modifier.weight(1f), 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}
@Composable fun ProductGalleryScreen() = ModulePlaceholder("Product Gallery", Icons.Default.Collections)

@Composable
fun CustomerSupplierScreen(viewModel: SalesViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val list = if (selectedTab == 0) viewModel.customers else viewModel.suppliers
    val filteredList = list.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimpleModuleHeader(if (selectedTab == 0) "Customers" else "Suppliers", Icons.Default.People)
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Icon(Icons.Default.PersonAdd, null)
                Spacer(Modifier.width(8.dp))
                Text("Add New ${if (selectedTab == 0) "Customer" else "Supplier"}")
            }
        }

        TabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(vertical = 8.dp)) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Customers", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Suppliers", modifier = Modifier.padding(12.dp))
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by name or phone...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (filteredList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No records found.", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredList) { item ->
                        CustomerItemRow(
                            item = item,
                            onDelete = { 
                                if (selectedTab == 0) viewModel.deleteCustomer(item.id) 
                                else viewModel.deleteSupplier(item.id) 
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(
            isSupplier = selectedTab == 1,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone ->
                val newEntity = Customer(
                    id = "CUST-${(1000..9999).random()}", 
                    name = name, 
                    phone = phone,
                    userName = viewModel.currentUser?.name ?: "Admin"
                )
                if (selectedTab == 0) viewModel.addCustomer(newEntity) else viewModel.addSupplier(newEntity)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CustomerItemRow(item: Customer, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, tint = Color(0xFF1976D2))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold)
                Text(item.phone, fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun AddCustomerDialog(isSupplier: Boolean, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New ${if (isSupplier) "Supplier" else "Customer"}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotEmpty()) onConfirm(name, phone) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ProductScreen(viewModel: SalesViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredProducts = viewModel.products.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.barcode.contains(searchQuery)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimpleModuleHeader("Product Management", Icons.Default.ShoppingBag)
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Product")
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search products by name or barcode...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (filteredProducts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products found. Add some to get started!", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(250.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredProducts.size) { index ->
                        val product = filteredProducts[index]
                        ProductManagementItem(
                            product = product,
                            onDelete = { viewModel.deleteProduct(product.barcode) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { product ->
                viewModel.addProduct(product)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProductManagementItem(product: ProductItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ShoppingBag, null, tint = Color(0xFF1976D2))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Barcode: ${product.barcode}", fontSize = 12.sp, color = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Sale Price", fontSize = 12.sp, color = Color.Gray)
                    Text("${product.salePrice}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Unit", fontSize = 12.sp, color = Color.Gray)
                    Text(product.unit, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(onDismiss: () -> Unit, onConfirm: (ProductItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("PCS") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Sale Price") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (e.g. PCS, KG)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (name.isNotEmpty() && barcode.isNotEmpty()) {
                    onConfirm(ProductItem(name = name, barcode = barcode, salePrice = price, unit = unit))
                }
            }) {
                Text("Add Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
@Composable fun ProfileScreen() = ModulePlaceholder("User Account", Icons.Default.Person)
@Composable fun PurchaseInvoiceScreen() = ModulePlaceholder("Purchase Invoices", Icons.Default.Inventory)
@Composable
fun SaleScreen(viewModel: SalesViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SimpleModuleHeader("Sales Counter (POS)", Icons.Default.PointOfSale)
        
        Row(modifier = Modifier.fillMaxSize()) {
            // Cart Section (Left)
            Card(
                modifier = Modifier.weight(1.5f).fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Order", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    
                    Box(modifier = Modifier.weight(1f)) {
                        if (viewModel.cartItems.isEmpty()) {
                            Text("Cart is empty", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                        } else {
                            LazyVerticalGrid(columns = GridCells.Fixed(1), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(viewModel.cartItems.size) { index ->
                                    val item = viewModel.cartItems[index]
                                    CartRow(item, 
                                        onIncrease = { viewModel.updateQuantity(item.productId, item.quantity + 1) },
                                        onDecrease = { viewModel.updateQuantity(item.productId, item.quantity - 1) },
                                        onRemove = { viewModel.removeFromCart(item.productId) }
                                    )
                                }
                            }
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 18.sp)
                        Text("${viewModel.currencySymbol} ${viewModel.getSubtotal()}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("${viewModel.currencySymbol} ${viewModel.getTotal()}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.processPayment() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Complete Payment", fontSize = 18.sp)
                    }
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Product Selection Section (Right)
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Products", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    
                    var searchQuery by remember { mutableStateOf("") }
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Products...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    val filteredProducts = if (searchQuery.isEmpty()) {
                        viewModel.products
                    } else {
                        viewModel.products.filter { it.name.contains(searchQuery, ignoreCase = true) || it.barcode.contains(searchQuery) }
                    }
                    
                    if (filteredProducts.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No products found.", color = Color.Gray)
                        }
                    } else {
                        LazyVerticalGrid(columns = GridCells.Adaptive(120.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredProducts.size) { index ->
                                val product = filteredProducts[index]
                                ProductGridItem(product, onClick = { viewModel.addToCart(product) })
                            }
                        }
                    }
                }
            }
        }
    }

    viewModel.lastInvoice?.let { invoice ->
        InvoiceDialog(
            invoice = invoice,
            currencySymbol = viewModel.currencySymbol,
            onDismiss = { viewModel.clearInvoice() },
            onPrint = { viewModel.clearInvoice() }
        )
    }
}

@Composable
fun InvoiceDialog(invoice: Invoice, currencySymbol: String, onDismiss: () -> Unit, onPrint: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invoice Processed") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Invoice Number: ${invoice.invoiceNumber}")
                Text("Total Amount: $currencySymbol ${invoice.totalAmount}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Payment Method: ${invoice.paymentMethod}")
            }
        },
        confirmButton = {
            Button(onClick = onPrint) {
                Text("Print & Close")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CartRow(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, fontWeight = FontWeight.Bold)
            Text("${item.price} x ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrease) { Icon(Icons.Default.Remove, null) }
            Text(item.quantity.toInt().toString(), fontWeight = FontWeight.Bold)
            IconButton(onClick = onIncrease) { Icon(Icons.Default.Add, null) }
            IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
        }
    }
}

@Composable
fun ProductGridItem(product: ProductItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(60.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ShoppingBag, null, tint = Color(0xFF1976D2))
            }
            Spacer(Modifier.height(8.dp))
            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${product.salePrice}", color = Color.Gray, fontSize = 12.sp)
        }
    }
}
@Composable fun SalesReportScreen() = ModulePlaceholder("Advanced Sales Reports", Icons.Default.PieChart)
@Composable
fun TaxRatesScreen(viewModel: SalesViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SimpleModuleHeader("Taxation Settings", Icons.Default.Percent)
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B))
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Tax Rate")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            if (viewModel.taxRates.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tax rates configured.", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(viewModel.taxRates) { tax ->
                        TaxRateRow(tax) { viewModel.deleteTaxRate(tax.code) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaxDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, rate ->
                viewModel.addTaxRate(TaxRateItem(code = name, name = name, rate = rate, enabled = true))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TaxRateRow(tax: TaxRateItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tax.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Rate: ${tax.rate}%", color = Color.Gray, fontSize = 14.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun AddTaxDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Tax Rate") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tax Name (e.g. VAT, GST)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Rate (%)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotEmpty() && rate.isNotEmpty()) onConfirm(name, rate) }) {
                Text("Add Rate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable fun UsersSecurityScreen() = ModulePlaceholder("Security & Permissions", Icons.Default.Security)
@Composable fun InvoiceDialogScreen() = ModulePlaceholder("Invoice Viewer", Icons.Default.ReceiptLong)
