package com.example.salespossystem.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salespossystem.ui.theme.DashboardBackground
import com.example.salespossystem.viewmodel.SalesViewModel

@Composable
fun ProfileScreen(viewModel: SalesViewModel, onLogout: () -> Unit) {
    val user = viewModel.currentUser
    val context = LocalContext.current
    var showPasswordDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBackground)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Picture Placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier.size(60.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Name
        Text(
            text = "${user?.firstName ?: "N/A"} ${user?.lastName ?: ""}".trim(),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        // Role Badge
        Surface(
            color = if (user?.accessLevel == "1") Color(0xFF4CAF50) else Color(0xFF2196F3),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = if (user?.accessLevel == "1") "ADMIN" else "STAFF",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Basic Info
        Text(
            "Account Info",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )
        ProfileInfoItem(icon = Icons.Default.Email, label = "Email", value = user?.email ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.Badge, label = "User ID", value = user?.uid ?: "N/A")
        
        Spacer(modifier = Modifier.height(16.dp))

        // Staff Details Section
        Text(
            "Staff Details",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )
        
        ProfileInfoItem(icon = Icons.Default.Numbers, label = "ID Card Number", value = user?.idCardNumber?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.Fingerprint, label = "Iqama ID", value = user?.iqamaId?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.LibraryBooks, label = "Passport ID", value = user?.passportId?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.Phone, label = "Phone", value = user?.phone?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.LocationOn, label = "Address", value = user?.address?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.Computer, label = "POS NO", value = user?.posNo?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.Public, label = "Nationality", value = user?.nationality?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.Map, label = "Route Area", value = user?.routeArea?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.Star, label = "Rank", value = user?.rank?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.Groups, label = "Section Name", value = user?.sectionName?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.Cake, label = "Date of Birth", value = user?.dateOfBirth?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.Bloodtype, label = "Blood Type", value = user?.bloodType?.ifEmpty { "N/A" } ?: "N/A")
        ProfileInfoItem(icon = Icons.Default.Transgender, label = "Gender", value = user?.gender?.ifEmpty { "N/A" } ?: "N/A")

        Spacer(modifier = Modifier.height(24.dp))

        // Change Password Button
        OutlinedButton(
            onClick = { showPasswordDialog = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.Gray))
        ) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Change Password")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { newPass ->
                viewModel.updatePassword(
                    newPassword = newPass,
                    onSuccess = {
                        showPasswordDialog = false
                        Toast.makeText(context, "পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "ব্যর্থ: $error", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, color = Color.Gray, fontSize = 12.sp)
                Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("পাসওয়ার্ড পরিবর্তন করুন") },
        text = {
            Column {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("নতুন পাসওয়ার্ড") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("নিশ্চিত করুন") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword == confirmPassword && newPassword.isNotEmpty()) {
                        onConfirm(newPassword)
                    }
                },
                enabled = newPassword.isNotEmpty() && newPassword == confirmPassword
            ) {
                Text("পরিবর্তন করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
