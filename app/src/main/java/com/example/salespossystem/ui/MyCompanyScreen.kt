package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.salespossystem.ui.theme.*

@Composable
fun MyCompanyScreen(viewModel: SalesViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf("Company") }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        MyCompanyTabs(selectedTab) { selectedTab = it }

        when (selectedTab) {
            "Company" -> CompanyDataScreen(viewModel)
            "Void" -> VoidReasonsScreen(viewModel)
            "Logo" -> MyLogoScreen(viewModel)
            "Reset" -> ResetDatabaseScreen(viewModel)
        }
    }
}

@Composable
fun MyCompanyTabs(selectedTab: String, onTabSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBackground)
            .padding(horizontal = 4.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabItem("Company", selectedTab == "Company") { onTabSelected("Company") }
        TabItem("Void reasons", selectedTab == "Void") { onTabSelected("Void") }
        TabItem("My logo", selectedTab == "Logo") { onTabSelected("Logo") }
        TabItem("Reset", selectedTab == "Reset") { onTabSelected("Reset") }
    }
}

@Composable
fun CompanyDataScreen(viewModel: SalesViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(viewModel.companyName) }
    var address by remember { mutableStateOf(viewModel.companyAddress) }
    var phone by remember { mutableStateOf(viewModel.companyPhone) }
    var taxNumber by remember { mutableStateOf(viewModel.companyTaxNumber) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBackground)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProductAction(
                icon = Icons.Default.Save,
                label = "Save",
                onClick = {
                    viewModel.updateCompanyData(name, address, phone, taxNumber)
                    Toast.makeText(context, "Company data saved successfully!", Toast.LENGTH_SHORT).show()
                }
            )
            ProductAction(icon = Icons.Default.Help, label = "Help")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader("My company data")

            CompanyInputField("Company Name", name) { name = it }
            CompanyInputField("Tax Number", taxNumber) { taxNumber = it }
            CompanyInputField("Full Address", address, isTextArea = true) { address = it }
            CompanyInputField("Phone Number", phone) { phone = it }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Bank account")
            CompanyInputField("Bank details", "", isTextArea = true) {}

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun VoidReasonsScreen(viewModel: SalesViewModel) {
    var newReason by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Manage Void Reasons", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newReason,
                onValueChange = { newReason = it },
                label = { Text("New Reason") },
                modifier = Modifier.weight(1f),
                 colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SidebarSelected,
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (newReason.isNotBlank()) {
                    viewModel.addVoidReason(newReason)
                    newReason = ""
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected)) {
                Icon(Icons.Default.Add, contentDescription = "Add Reason")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewModel.voidReasons) { reason ->
                Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(reason, modifier = Modifier.weight(1f), color = Color.White)
                    IconButton(onClick = { viewModel.deleteVoidReason(reason) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Reason", tint = AccentRed)
                    }
                }
            }
        }
    }
}

@Composable
fun MyLogoScreen(viewModel: SalesViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.saveLogoUri(it.toString()) }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Company Logo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.companyLogoUri != null) {
            Image(
                painter = rememberAsyncImagePainter(viewModel.companyLogoUri),
                contentDescription = "Company Logo",
                modifier = Modifier
                    .size(150.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(Color.DarkGray, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No Logo", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { launcher.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected)) {
            Text("Select Logo")
        }
    }
}

@Composable
fun ResetDatabaseScreen(viewModel: SalesViewModel) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = AccentRed, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Reset Database", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(
            "This will delete all data permanently, including products, sales, and settings.",
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { showConfirmDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
        ) {
            Text("RESET DATABASE")
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Reset") },
            text = { Text("Are you sure you want to delete all data? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDatabase()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun CompanyInputField(label: String, value: String, isTextArea: Boolean = false, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (isTextArea) 100.dp else 48.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = SidebarSelected,
                unfocusedIndicatorColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(4.dp),
            singleLine = !isTextArea
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyCompanyScreenPreview() {
    MyCompanyScreen()
}

@Preview(showBackground = true)
@Composable
fun VoidReasonsScreenPreview() {
    VoidReasonsScreen(viewModel = viewModel())
}

@Preview(showBackground = true)
@Composable
fun MyLogoScreenPreview() {
    MyLogoScreen(viewModel = viewModel())
}

@Preview(showBackground = true)
@Composable
fun ResetDatabaseScreenPreview() {
    ResetDatabaseScreen(viewModel = viewModel())
}
