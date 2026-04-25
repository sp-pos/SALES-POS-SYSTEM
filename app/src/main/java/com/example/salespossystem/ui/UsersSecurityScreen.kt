package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.salespossystem.ui.theme.*

@Composable
fun UsersSecurityScreen(viewModel: SalesViewModel) {
    var isSecurityUnlocked by remember { mutableStateOf(false) }
    var showPasswordPrompt by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        // Simple Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBackground)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Security, "Security", tint = Color.White)
            Spacer(Modifier.width(12.dp))
            Text("Security Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        if (!isSecurityUnlocked && viewModel.topAdminPassword.isNotEmpty()) {
            SecurityLockScreen(onUnlock = { showPasswordPrompt = true })
        } else {
            SecurityTabContent(viewModel)
        }
    }

    if (showPasswordPrompt) {
        SecurityPasswordDialog(
            viewModel = viewModel,
            onDismiss = { showPasswordPrompt = false },
            onVerified = {
                showPasswordPrompt = false
                isSecurityUnlocked = true
            }
        )
    }
}

@Composable
fun SecurityLockScreen(onUnlock: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Lock, null, tint = SidebarSelected, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(24.dp))
        Text("Security Locked", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Master password required to access Top Admin", color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onUnlock, 
            colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(50.dp).padding(horizontal = 32.dp)
        ) {
            Icon(Icons.Default.Key, null)
            Spacer(Modifier.width(8.dp))
            Text("Unlock Security")
        }
    }
}

@Composable
fun SecurityTabContent(viewModel: SalesViewModel) {
    var topAdminEmail by remember { mutableStateOf(viewModel.topAdminEmail) }
    var topAdminPassword by remember { mutableStateOf(viewModel.topAdminPassword) }
    var isSaving by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(viewModel.topAdminEmail.isEmpty()) }

    LaunchedEffect(viewModel.topAdminEmail, viewModel.topAdminPassword) {
        topAdminEmail = viewModel.topAdminEmail
        topAdminPassword = viewModel.topAdminPassword
        if (viewModel.topAdminEmail.isNotEmpty()) isEditing = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isEditing) {
            // --- TOP ADMIN PROFILE VIEW ---
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFFFF3D00),
                    modifier = Modifier.size(100.dp)
                )
                Icon(
                    Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp).padding(top = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "MASTER ADMIN ACCOUNT",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(topAdminEmail, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFD32F2F).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "CRITICAL ACCESS ENABLED",
                            color = Color(0xFFD32F2F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Text(
                "This account has absolute authority over the entire POS system. Keep these credentials strictly confidential.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Update Master Credentials")
            }

        } else {
            // --- EDIT/SETUP VIEW ---
            Icon(
                Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = SidebarSelected,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Setup Master Admin",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Create a master account for total system control.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = topAdminEmail,
                onValueChange = { topAdminEmail = it },
                label = { Text("Master Admin Email") },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SidebarSelected,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = SidebarSelected
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = topAdminPassword,
                onValueChange = { topAdminPassword = it },
                label = { Text("Master Password") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SidebarSelected,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = SidebarSelected
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isSaving = true
                    viewModel.updateTopAdmin(topAdminEmail, topAdminPassword)
                    isSaving = false
                    isEditing = false
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Secure & Save Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            if (viewModel.topAdminEmail.isNotEmpty()) {
                TextButton(onClick = { isEditing = false }) {
                    Text("Back to Profile", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun SecurityPasswordDialog(
    viewModel: SalesViewModel,
    onDismiss: () -> Unit,
    onVerified: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DashboardBackground,
        title = { Text("Identity Verification", color = Color.White) },
        text = {
            Column {
                Text("Enter Master Password to access security settings.", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = "" },
                    label = { Text("Password") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    isError = error.isNotEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SidebarSelected,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                if (error.isNotEmpty()) {
                    Text(error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password == viewModel.topAdminPassword || (viewModel.topAdminPassword.isEmpty() && password == "admin123")) {
                        onVerified()
                    } else {
                        error = "Incorrect Master Password"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SidebarSelected)
            ) { Text("Verify") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) }
        }
    )
}
