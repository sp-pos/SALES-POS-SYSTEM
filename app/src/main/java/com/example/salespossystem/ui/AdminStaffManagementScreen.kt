package com.example.salespossystem.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.salespossystem.data.User
import com.example.salespossystem.ui.theme.DashboardBackground
import com.example.salespossystem.util.PrintingService
import com.example.salespossystem.viewmodel.AuthViewModel
import com.example.salespossystem.viewmodel.SalesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStaffManagementScreen(
    authViewModel: AuthViewModel,
    adminId: String,
    salesViewModel: SalesViewModel
) {
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var selectedStaffUids by remember { mutableStateOf(setOf<String>()) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(adminId) {
        if (adminId.isNotEmpty()) {
            authViewModel.fetchStaffUsers(adminId)
        }
    }

    Scaffold(
        containerColor = DashboardBackground,
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedStaffUids.size} Selected", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)),
                    actions = {
                        IconButton(onClick = {
                            val selectedStaff = authViewModel.staffUsers.filter { it.uid in selectedStaffUids }
                            if (selectedStaff.isNotEmpty()) {
                                PrintingService.printStaffList(
                                    context = context,
                                    staffList = selectedStaff,
                                    companyName = salesViewModel.companyName,
                                    companyAddress = salesViewModel.companyAddress,
                                    companyPhone = salesViewModel.companyPhone,
                                    companyTaxNumber = salesViewModel.companyTaxNumber,
                                    currency = salesViewModel.currencySymbol
                                )
                            }
                        }) {
                            Icon(Icons.Default.Print, contentDescription = "Print Selected", tint = Color.White)
                        }
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedStaffUids = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (!isSelectionMode && authViewModel.staffUsers.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = {
                            PrintingService.printStaffList(
                                context = context,
                                staffList = authViewModel.staffUsers,
                                companyName = salesViewModel.companyName,
                                companyAddress = salesViewModel.companyAddress,
                                companyPhone = salesViewModel.companyPhone,
                                companyTaxNumber = salesViewModel.companyTaxNumber,
                                currency = salesViewModel.currencySymbol
                            )
                        },
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print All")
                    }
                }
                
                FloatingActionButton(
                    onClick = { showAddStaffDialog = true },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Staff")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Staff Management",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (!isSelectionMode && authViewModel.staffUsers.isNotEmpty()) {
                    TextButton(onClick = { isSelectionMode = true }) {
                        Text("Select", color = Color(0xFF2196F3))
                    }
                }
            }
            
            Text(
                "নতুন স্টাফ যোগ করুন এবং তাদের প্রোফাইল পরিচালনা করুন",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (authViewModel.staffUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("কোনো স্টাফ পাওয়া যায়নি", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(authViewModel.staffUsers) { staff ->
                        val isSelected = staff.uid in selectedStaffUids
                        StaffUserItem(
                            user = staff,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            salesViewModel = salesViewModel,
                            onToggleSelection = {
                                selectedStaffUids = if (isSelected) {
                                    selectedStaffUids - staff.uid
                                } else {
                                    selectedStaffUids + staff.uid
                                }
                            },
                            onToggleLock = { authViewModel.toggleUserLock(staff.uid, !staff.isLocked) },
                            onDelete = { authViewModel.deleteUser(staff.uid) },
                            onUpdate = { updatedData ->
                                authViewModel.updateStaff(staff.uid, updatedData, 
                                    onSuccess = { Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show() },
                                    onFailure = { Toast.makeText(context, "Error: $it", Toast.LENGTH_SHORT).show() }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddStaffDialog) {
        AddStaffDialog(
            onDismiss = { showAddStaffDialog = false },
            onConfirm = { staffData ->
                authViewModel.registerStaff(
                    context = context,
                    name = staffData["name"] ?: "",
                    email = staffData["email"] ?: "",
                    pass = staffData["password"] ?: "",
                    adminId = adminId,
                    idCardNumber = staffData["idCardNumber"] ?: "",
                    iqamaId = staffData["iqamaId"] ?: "",
                    passportId = staffData["passportId"] ?: "",
                    phone = staffData["phone"] ?: "",
                    address = staffData["address"] ?: "",
                    posNo = staffData["posNo"] ?: "",
                    nationality = staffData["nationality"] ?: "",
                    routeArea = staffData["routeArea"] ?: "",
                    rank = staffData["rank"] ?: "",
                    sectionName = staffData["sectionName"] ?: "",
                    onSuccess = {
                        showAddStaffDialog = false
                        Toast.makeText(context, "স্টাফ অ্যাকাউন্ট তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "ব্যর্থ হয়েছে: $error", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
fun StaffUserItem(
    user: User,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    salesViewModel: SalesViewModel,
    onToggleSelection: () -> Unit = {},
    onToggleLock: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Map<String, Any>) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF388E3C) else Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { 
            if (isSelectionMode) {
                onToggleSelection()
            } else {
                showDetails = !showDetails 
            }
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelection() },
                            colors = CheckboxDefaults.colors(checkedColor = Color.White, checkmarkColor = Color(0xFF388E3C))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(user.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(user.email, color = Color.Gray, fontSize = 14.sp)
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isSelectionMode) {
                        IconButton(onClick = { 
                            PrintingService.printStaffProfile(
                                context = context,
                                staff = user,
                                companyName = salesViewModel.companyName,
                                companyAddress = salesViewModel.companyAddress,
                                companyPhone = salesViewModel.companyPhone,
                                companyTaxNumber = salesViewModel.companyTaxNumber
                            )
                        }) {
                            Icon(Icons.Default.Print, contentDescription = "Print Profile", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Cyan, modifier = Modifier.size(20.dp))
                        }
                    }
                    Surface(
                        color = if (user.isLocked) Color.Red.copy(alpha = 0.2f) else Color.Green.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (user.isLocked) "LOCKED" else "ACTIVE",
                            color = if (user.isLocked) Color.Red else Color.Green,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (showDetails) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                StaffDetailRow("ID Card", user.idCardNumber)
                StaffDetailRow("Iqama ID", user.iqamaId)
                StaffDetailRow("Passport", user.passportId)
                StaffDetailRow("Phone", user.phoneNumber)
                StaffDetailRow("Address", user.address)
                StaffDetailRow("POS NO", user.posNo)
                StaffDetailRow("Nationality", user.nationality)
                StaffDetailRow("Route Area", user.routeArea)
                StaffDetailRow("Rank", user.rank)
                StaffDetailRow("Section", user.sectionName)
                StaffDetailRow("DOB", user.dateOfBirth)
                StaffDetailRow("Blood Type", user.bloodType)
                StaffDetailRow("Gender", user.gender)
            }

            if (!isSelectionMode) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = {
                        if (user.latitude != 0.0 && user.longitude != 0.0) {
                            val gmmIntentUri = Uri.parse("geo:${user.latitude},${user.longitude}?q=${user.latitude},${user.longitude}(${user.name})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        } else {
                            Toast.makeText(context, "লোকেশন ডাটা পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Location", fontSize = 12.sp)
                    }

                    TextButton(onClick = onToggleLock) {
                        Icon(
                            imageVector = if (user.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (user.isLocked) Color.Green else Color.Yellow
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (user.isLocked) "Unlock" else "Lock", fontSize = 12.sp, color = if (user.isLocked) Color.Green else Color.Yellow)
                    }

                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Red)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 12.sp, color = Color.Red)
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditStaffDialog(
            user = user,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedData ->
                onUpdate(updatedData)
                showEditDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF2D2D2D),
            title = { Text("Delete Staff", color = Color.White) },
            text = { Text("আপনি কি নিশ্চিতভাবে ${user.name}-কে ডিলিট করতে চান?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun StaffDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$label: ", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.width(100.dp))
        Text(value.ifEmpty { "N/A" }, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AddStaffDialog(
    onDismiss: () -> Unit,
    onConfirm: (Map<String, String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var idCardNumber by remember { mutableStateOf("") }
    var iqamaId by remember { mutableStateOf("") }
    var passportId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var posNo by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var routeArea by remember { mutableStateOf("") }
    var rank by remember { mutableStateOf("") }
    var sectionName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("নতুন স্টাফ যোগ করুন", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                StaffInputField(value = name, onValueChange = { name = it }, label = "নাম (Full Name)")
                StaffInputField(value = email, onValueChange = { email = it }, label = "ইমেইল (Email)")
                StaffInputField(value = password, onValueChange = { password = it }, label = "পাসওয়ার্ড (Password)")
                
                HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)
                
                StaffInputField(value = idCardNumber, onValueChange = { idCardNumber = it }, label = "ID Card Number")
                StaffInputField(value = iqamaId, onValueChange = { iqamaId = it }, label = "Iqama ID")
                StaffInputField(value = passportId, onValueChange = { passportId = it }, label = "Passport ID")
                StaffInputField(value = phone, onValueChange = { phone = it }, label = "Phone Number")
                StaffInputField(value = address, onValueChange = { address = it }, label = "Address")
                StaffInputField(value = posNo, onValueChange = { posNo = it }, label = "POS NO")
                StaffInputField(value = nationality, onValueChange = { nationality = it }, label = "Nationality")
                StaffInputField(value = routeArea, onValueChange = { routeArea = it }, label = "Route Area")
                StaffInputField(value = rank, onValueChange = { rank = it }, label = "Rank")
                StaffInputField(value = sectionName, onValueChange = { sectionName = it }, label = "Section Name")
                StaffInputField(value = dateOfBirth, onValueChange = { dateOfBirth = it }, label = "Date of Birth (DD-MM-YYYY)")
                StaffInputField(value = bloodType, onValueChange = { bloodType = it }, label = "Blood Type (e.g. A+)")
                StaffInputField(value = gender, onValueChange = { gender = it }, label = "Gender (Male/Female)")

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            if(name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                                onConfirm(mapOf(
                                    "name" to name, "email" to email, "password" to password,
                                    "idCardNumber" to idCardNumber, "iqamaId" to iqamaId,
                                    "passportId" to passportId, "phone" to phone,
                                    "address" to address, "posNo" to posNo,
                                    "nationality" to nationality, "routeArea" to routeArea,
                                    "rank" to rank, "sectionName" to sectionName,
                                    "dateOfBirth" to dateOfBirth, "bloodType" to bloodType, "gender" to gender
                                ))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("তৈরি করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun EditStaffDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Any>) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var idCardNumber by remember { mutableStateOf(user.idCardNumber) }
    var iqamaId by remember { mutableStateOf(user.iqamaId) }
    var passportId by remember { mutableStateOf(user.passportId) }
    var phone by remember { mutableStateOf(user.phoneNumber) }
    var address by remember { mutableStateOf(user.address) }
    var posNo by remember { mutableStateOf(user.posNo) }
    var nationality by remember { mutableStateOf(user.nationality) }
    var routeArea by remember { mutableStateOf(user.routeArea) }
    var rank by remember { mutableStateOf(user.rank) }
    var sectionName by remember { mutableStateOf(user.sectionName) }
    var dateOfBirth by remember { mutableStateOf(user.dateOfBirth) }
    var bloodType by remember { mutableStateOf(user.bloodType) }
    var gender by remember { mutableStateOf(user.gender) }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("স্টাফ তথ্য এডিট করুন", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                StaffInputField(value = name, onValueChange = { name = it }, label = "নাম (Full Name)")
                
                HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)
                
                StaffInputField(value = idCardNumber, onValueChange = { idCardNumber = it }, label = "ID Card Number")
                StaffInputField(value = iqamaId, onValueChange = { iqamaId = it }, label = "Iqama ID")
                StaffInputField(value = passportId, onValueChange = { passportId = it }, label = "Passport ID")
                StaffInputField(value = phone, onValueChange = { phone = it }, label = "Phone Number")
                StaffInputField(value = address, onValueChange = { address = it }, label = "Address")
                StaffInputField(value = posNo, onValueChange = { posNo = it }, label = "POS NO")
                StaffInputField(value = nationality, onValueChange = { nationality = it }, label = "Nationality")
                StaffInputField(value = routeArea, onValueChange = { routeArea = it }, label = "Route Area")
                StaffInputField(value = rank, onValueChange = { rank = it }, label = "Rank")
                StaffInputField(value = sectionName, onValueChange = { sectionName = it }, label = "Section Name")
                StaffInputField(value = dateOfBirth, onValueChange = { dateOfBirth = it }, label = "Date of Birth (DD-MM-YYYY)")
                StaffInputField(value = bloodType, onValueChange = { bloodType = it }, label = "Blood Type (e.g. A+)")
                StaffInputField(value = gender, onValueChange = { gender = it }, label = "Gender (Male/Female)")

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { 
                            onConfirm(mapOf(
                                "name" to name,
                                "idCardNumber" to idCardNumber, "iqamaId" to iqamaId,
                                "passportId" to passportId, "phoneNumber" to phone,
                                "address" to address, "posNo" to posNo,
                                "nationality" to nationality, "routeArea" to routeArea,
                                "rank" to rank, "sectionName" to sectionName,
                                "dateOfBirth" to dateOfBirth, "bloodType" to bloodType, "gender" to gender
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("আপডেট করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun StaffInputField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF4CAF50),
            unfocusedLabelColor = Color.Gray,
            focusedLabelColor = Color(0xFF4CAF50)
        )
    )
}
