package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salespossystem.data.UserItem
import com.example.salespossystem.ui.theme.DashboardBackground
import com.example.salespossystem.viewmodel.SaleViewModel

@Composable
fun LoginScreen(
    viewModel: SalesViewModel,
    saleViewModel: SaleViewModel,
    onLogin: (UserItem) -> Unit,
    onAdminLogin: (Map<String, Any>) -> Unit,
    onNavigateToAdminSignup: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var isAdminMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (isAdminMode) "Admin Login " else "Login ",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.Gray,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.White
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Email") },
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
            colors = textFieldColors,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
            colors = textFieldColors,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null, tint = Color.LightGray)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red, modifier = Modifier.padding(vertical = 8.dp), fontSize = 14.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    isLoading = true
                    saleViewModel.loginUser(
                        username, password,
                        onSuccess = { data ->
                            isLoading = false
                            val role = data["role"] as? String
                            if (role == "ADMIN") {
                                onAdminLogin(data)
                            } else {
                                // For staff, create a UserItem and login
                                val fullName = data["name"] as? String ?: "Staff"
                                val nameParts = fullName.split(" ", limit = 2)
                                val firstName = nameParts.getOrNull(0) ?: fullName
                                val lastName = nameParts.getOrNull(1) ?: "****"
                                
                                val uid = data["uid"] as? String ?: ""
                                val emailVal = data["email"] as? String ?: ""
                                val adminId = data["adminId"] as? String ?: ""
                                onLogin(UserItem(
                                    firstName = firstName,
                                    lastName = lastName,
                                    email = emailVal,
                                    accessLevel = "9",
                                    active = true,
                                    uid = uid,
                                    adminId = adminId,
                                    idCardNumber = data["idCardNumber"] as? String ?: "",
                                    iqamaId = data["iqamaId"] as? String ?: "",
                                    passportId = data["passportId"] as? String ?: "",
                                    phone = data["phoneNumber"] as? String ?: data["phone"] as? String ?: "",
                                    address = data["address"] as? String ?: "",
                                    posNo = data["posNo"] as? String ?: "",
                                    nationality = data["nationality"] as? String ?: "",
                                    routeArea = data["routeArea"] as? String ?: "",
                                    rank = data["rank"] as? String ?: "",
                                    sectionName = data["sectionName"] as? String ?: "",
                                    dateOfBirth = data["dateOfBirth"] as? String ?: "",
                                    bloodType = data["bloodType"] as? String ?: "",
                                    gender = data["gender"] as? String ?: ""
                                ))
                            }
                        },
                        onFailure = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                } else {
                    errorMessage = "ইমেইল ও পাসওয়ার্ড দিন"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 32.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("LOGIN", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToAdminSignup) {
            Text(
                "Create Admin Account",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
