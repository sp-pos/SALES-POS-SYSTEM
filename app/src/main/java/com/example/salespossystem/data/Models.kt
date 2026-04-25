package com.example.salespossystem.data

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

data class UserItem(
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val email: String = "",
    val accessLevel: String = "",
    val active: Boolean = false,
    val uid: String = "",
    val adminId: String = "",
    val idCardNumber: String = "",
    val iqamaId: String = "",
    val passportId: String = "",
    val phone: String = "",
    val address: String = "",
    val posNo: String = "",
    val nationality: String = "",
    val routeArea: String = "",
    val rank: String = "",
    val sectionName: String = "",
    val dateOfBirth: String = "",
    val bloodType: String = "",
    val gender: String = ""
)

data class TaxRateItem(
    val code: String = "",
    val name: String = "",
    val rate: String = "",
    val enabled: Boolean = false,
    val fixed: Boolean = false
)

data class CartItem(val productId: String = "", val productName: String = "", val quantity: Double = 0.0, val price: Double = 0.0, val unit: String = "")
data class Customer(val id: String = "", val name: String = "", val phone: String = "", val address: String = "", val taxNumber: String = "", val code: String = "", val userName: String = "Admin")
data class Invoice(val invoiceNumber: String = "", val date: String = "", val items: List<CartItem> = emptyList(), val subtotal: Double = 0.0, val discount: Double = 0.0, val totalAmount: Double = 0.0, val paymentMethod: String = "", val companyName: String = "", val companyAddress: String = "", val companyPhone: String = "", val companyTaxNumber: String = "", val customerName: String = "", val customerPhone: String = "", val isPurchase: Boolean = false, val customerAddress: String = "", val customerTaxNumber: String = "", val userName: String = "Admin")
data class Expense(val id: Long = 0L, val date: String = "", val category: String = "", val amount: Double = 0.0, val description: String = "", val userName: String = "", val paymentMethod: String = "CASH")
data class Promotion(val id: String = "", val name: String = "", val description: String = "", val discountPercent: Double = 0.0, val discountAmount: Double = 0.0, val isActive: Boolean = false, val appliedProductBarcodes: List<String> = emptyList())
data class PaymentTypeItem(val name: String = "", val position: String = "", val enabled: Boolean = false, val quickPayment: Boolean = false, val customerRequired: Boolean = false, val changeAllowed: Boolean = false, val markAsPaid: Boolean = false, val printReceipt: Boolean = false, val shortcutKey: String = "")
data class StockMovementEntry(val date: String = "", val productName: String = "", val change: Double = 0.0, val type: String = "", val reference: String = "")
