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
    val lastActiveMillis: Long? = null,
    
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
data class ProductItem(
    val code: String = "",
    val name: String = "",
    val group: String = "",
    val barcode: String = "",
    val cost: String = "",
    val salePrice: String = "",
    val active: Boolean = false,
    val unit: String = "",
    val created: String = "",
    val updated: String = "",
    val imageUrl: String = ""
)

@Serializable
data class CartItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Double = 0.0,
    val price: Double = 0.0,
    val unit: String = ""
)

@Serializable
data class Customer(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val taxNumber: String = "",
    val code: String = "",
    val userName: String = "Admin"
)

@Serializable
data class Invoice(
    val invoiceNumber: String = "",
    val date: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "",
    val companyName: String = "",
    val companyAddress: String = "",
    val companyPhone: String = "",
    val companyTaxNumber: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val isPurchase: Boolean = false,
    val customerAddress: String = "",
    val customerTaxNumber: String = "",
    val userName: String = "Admin"
)

@Serializable
data class Expense(
    val id: Long = 0L,
    val date: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val userName: String = "",
    val paymentMethod: String = "CASH"
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

@Serializable
data class TaxRateItem(
    val code: String = "",
    val name: String = "",
    val rate: String = "",
    val enabled: Boolean = false,
    val fixed: Boolean = false
)

@Serializable
data class PaymentTypeItem(
    val name: String = "",
    val position: String = "",
    val enabled: Boolean = false,
    val quickPayment: Boolean = false,
    val customerRequired: Boolean = false,
    val changeAllowed: Boolean = false,
    val markAsPaid: Boolean = false,
    val printReceipt: Boolean = false,
    val shortcutKey: String = ""
)
