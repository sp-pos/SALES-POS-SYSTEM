package com.example.salespossystem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.salespossystem.data.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    
    fun registerAdmin(
        name: String,
        email: String,
        pass: String,
        shopName: String,
        onFailure: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.registerAdmin(name, email, pass, shopName)
            result.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it.message ?: "Registration failed")
            }
        }
    }
}
