package com.example.salespossystem.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "STAFF",
    val adminId: String = "",
    val isLocked: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val shopName: String = "",
    val address: String = "",
    val lastActiveMillis: Long? = null, // Using Long for timestamp in common
    
    val idCardNumber: String = "",
    val iqamaId: String = "",
    val passportId: String = "",
    val posNo: String = "",
    val nationality: String = "",
    val routeArea: String = "",
    val rank: String = "",
    val sectionName: String = "",
    val dateOfBirth: String = "",
    val bloodType: String = "",
    val gender: String = ""
)

@Serializable
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
    val paymentType: String = "",
    val items: List<SaleItem> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestampMillis: Long? = null
)

@Serializable
data class SaleItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Double = 0.0,
    val price: Double = 0.0,
    val total: Double = 0.0
)

@Serializable
data class ProductItem(
    val barcode: String = "",
    val name: String = "",
    val cost: String = "",
    val salePrice: String = "",
    val unit: String = ""
)

@Serializable
data class Promotion(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    var isActive: Boolean = true,
    val appliedProductBarcodes: List<String> = emptyList()
)
