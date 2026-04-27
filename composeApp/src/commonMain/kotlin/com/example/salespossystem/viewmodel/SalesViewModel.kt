package com.example.salespossystem.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.salespossystem.data.*

class SalesViewModel : ViewModel() {
    // Observable Lists
    val products = mutableStateListOf<ProductItem>()
    val cartItems = mutableStateListOf<CartItem>()
    val customers = mutableStateListOf<Customer>()
    val suppliers = mutableStateListOf<Customer>()
    val allInvoices = mutableStateListOf<Invoice>()
    val expenses = mutableStateListOf<Expense>()
    val promotions = mutableStateListOf<Promotion>()
    val stockMap = mutableStateMapOf<String, Double>()
    
    // Company Info
    var companyName by mutableStateOf("Dina POS")
    var companyAddress by mutableStateOf("")
    var companyPhone by mutableStateOf("")
    var companyTaxNumber by mutableStateOf("")
    var currencySymbol by mutableStateOf("$")
    
    // Current User
    var currentUser by mutableStateOf<User?>(null)

    // Helper functions for UI (Migrations)
    fun loadDataFromDatabase() {
        // To be implemented with actual Firebase logic
    }
}
