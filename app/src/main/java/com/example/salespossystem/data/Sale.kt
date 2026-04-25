package com.example.salespossystem.data

import com.google.firebase.Timestamp

// প্রতিটি বিক্রিত পণ্যের জন্য
data class SaleItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Double = 0.0,
    val unitPrice: Double = 0.0,
    val subTotal: Double = 0.0
)

// পুরো বিক্রির ইনভয়েসের জন্য
data class Sale(
    val saleId: String = "",
    val adminId: String = "",
    val staffId: String = "",
    val staffName: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val discount: Double = 0.0,
    val payableAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentType: String = "CASH",
    val items: List<SaleItem> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Timestamp = Timestamp.now()
)
