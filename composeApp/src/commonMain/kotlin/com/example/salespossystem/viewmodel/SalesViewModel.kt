package com.example.salespossystem.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.salespossystem.data.*

class SalesViewModel : ViewModel() {
    val products = mutableStateListOf<ProductItem>()
    val cartItems = mutableStateListOf<CartItem>()
    val customers = mutableStateListOf<Customer>()
    val suppliers = mutableStateListOf<Customer>()
    val allInvoices = mutableStateListOf<Invoice>()
    val expenses = mutableStateListOf<Expense>()
    val promotions = mutableStateListOf<Promotion>()
    val paymentTypes = mutableStateListOf<PaymentTypeItem>()
    val taxRates = mutableStateListOf<TaxRateItem>()
    val users = mutableStateListOf<User>()
    val stockMap = mutableStateMapOf<String, Double>()
    
    var companyName by mutableStateOf("SP POS")
    var companyAddress by mutableStateOf("")
    var companyPhone by mutableStateOf("")
    var companyTaxNumber by mutableStateOf("")
    var currencySymbol by mutableStateOf("$")
    
    var currentUser by mutableStateOf<User?>(null)
    var selectedPaymentMethod by mutableStateOf("CASH")
    var discountAmount by mutableDoubleStateOf(0.0)
    var lastInvoice by mutableStateOf<Invoice?>(null)

    fun addToCart(product: ProductItem) {
        val existing = cartItems.find { it.productId == product.barcode }
        if (existing != null) {
            val index = cartItems.indexOf(existing)
            cartItems[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            cartItems.add(CartItem(
                productId = product.barcode,
                productName = product.name,
                quantity = 1.0,
                price = product.salePrice.toDoubleOrNull() ?: 0.0,
                unit = product.unit
            ))
        }
    }

    fun removeFromCart(productId: String) {
        cartItems.removeAll { it.productId == productId }
    }

    fun updateQuantity(productId: String, newQty: Double) {
        val index = cartItems.indexOfFirst { it.productId == productId }
        if (index != -1) {
            if (newQty > 0) {
                cartItems[index] = cartItems[index].copy(quantity = newQty)
            } else {
                cartItems.removeAt(index)
            }
        }
    }

    fun getSubtotal() = cartItems.sumOf { it.price * it.quantity }
    fun getTotal() = getSubtotal() - discountAmount

    fun clearCart() {
        cartItems.clear()
        discountAmount = 0.0
    }

    fun addProduct(product: ProductItem) {
        products.add(product)
    }

    fun deleteProduct(barcode: String) {
        products.removeAll { it.barcode == barcode }
    }

    fun updateProduct(barcode: String, updatedProduct: ProductItem) {
        val index = products.indexOfFirst { it.barcode == barcode }
        if (index != -1) {
            products[index] = updatedProduct
        }
    }

    // Stock Management
    fun updateStock(barcode: String, quantity: Double) {
        stockMap[barcode] = (stockMap[barcode] ?: 0.0) + quantity
    }

    // Customer Management
    fun addCustomer(customer: Customer) {
        val newCustomer = if (customer.id.isEmpty()) {
            customer.copy(id = "CUST-${(1000..9999).random()}")
        } else customer
        customers.add(newCustomer)
    }

    fun deleteCustomer(id: String) {
        customers.removeAll { it.id == id }
    }

    fun addSupplier(supplier: Customer) {
        suppliers.add(supplier)
    }

    fun deleteSupplier(id: String) {
        suppliers.removeAll { it.id == id }
    }

    // Expense Management
    fun addExpense(expense: Expense) {
        val newExpense = if (expense.id == 0L) {
            expense.copy(id = (expense.category.hashCode() + expense.amount.hashCode() + (0..100000).random()).toLong())
        } else expense
        expenses.add(0, newExpense)
    }

    fun deleteExpense(id: Long) {
        expenses.removeAll { it.id == id }
    }

    // Payment Type Management
    fun addPaymentType(item: PaymentTypeItem) {
        paymentTypes.add(item)
    }

    fun deletePaymentType(name: String) {
        paymentTypes.removeAll { it.name == name }
    }

    // Tax Management
    fun addTaxRate(item: TaxRateItem) {
        taxRates.add(item)
    }

    fun deleteTaxRate(code: String) {
        taxRates.removeAll { it.code == code }
    }

    // Promotion Management
    fun addPromotion(name: String, description: String, discountPercent: Double, discountAmount: Double, productBarcodes: List<String>) {
        val newPromo = Promotion(
            id = (promotions.size + 1).toString(),
            name = name,
            description = description,
            discountPercent = discountPercent,
            discountAmount = discountAmount,
            appliedProductBarcodes = productBarcodes,
            isActive = true
        )
        promotions.add(newPromo)
    }

    fun deletePromotion(id: String) {
        promotions.removeAll { it.id == id }
    }

    fun togglePromotion(id: String, isActive: Boolean) {
        val index = promotions.indexOfFirst { it.id == id }
        if (index != -1) {
            promotions[index] = promotions[index].copy(isActive = isActive)
        }
    }

    fun updateCompanyInfo(name: String, address: String, phone: String, taxNo: String) {
        companyName = name
        companyAddress = address
        companyPhone = phone
        companyTaxNumber = taxNo
    }

    // User/Staff Management
    fun addUser(user: User) {
        users.add(user)
    }

    fun deleteUser(uid: String) {
        users.removeAll { it.uid == uid }
    }

    fun processPayment() {
        if (cartItems.isEmpty()) return
        
        val invoiceNumber = "INV-${(10000..99999).random()}"
        val date = "Today" // In a real app, use a proper date formatter
        
        val newInvoice = Invoice(
            invoiceNumber = invoiceNumber,
            date = date,
            items = cartItems.toList(),
            subtotal = getSubtotal(),
            discount = discountAmount,
            totalAmount = getTotal(),
            paymentMethod = selectedPaymentMethod,
            companyName = companyName,
            companyAddress = companyAddress,
            companyPhone = companyPhone,
            companyTaxNumber = companyTaxNumber,
            customerName = "Walk-in Customer",
            customerPhone = "",
            userName = currentUser?.name ?: "Admin"
        )
        
        allInvoices.add(0, newInvoice)
        lastInvoice = newInvoice
        
        // Update stock (optional demo logic)
        cartItems.forEach { item ->
            val currentStock = stockMap[item.productId] ?: 100.0
            stockMap[item.productId] = currentStock - item.quantity
        }
        
        clearCart()
    }

    fun clearInvoice() {
        lastInvoice = null
    }

    fun loadDataFromDatabase() {
        // Initialization logic
    }
}
