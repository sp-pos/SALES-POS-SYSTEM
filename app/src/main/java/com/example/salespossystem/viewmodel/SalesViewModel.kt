package com.example.salespossystem.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toUri
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.salespossystem.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class SalesViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).posDao()
    private val sharedPrefs = application.getSharedPreferences("pos_prefs", Context.MODE_PRIVATE)
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val registrations = mutableListOf<ListenerRegistration>()

    // Observable Lists for UI
    val products = mutableStateListOf<ProductItem>()
    val cartItems = mutableStateListOf<CartItem>()
    val customers = mutableStateListOf<Customer>()
    val suppliers = mutableStateListOf<Customer>()
    val allInvoices = mutableStateListOf<Invoice>()
    val users = mutableStateListOf<UserItem>()
    val paymentTypes = mutableStateListOf<PaymentTypeItem>()
    val taxRates = mutableStateListOf<TaxRateItem>()
    val expenses = mutableStateListOf<Expense>()
    val promotions = mutableStateListOf<Promotion>()
    val stockMap = mutableStateMapOf<String, Double>()
    val voidReasons = mutableStateListOf<String>()
    val printStations = mutableStateListOf<PrintStationEntity>()

    // State Variables
    var companyName by mutableStateOf("")
    var companyAddress by mutableStateOf("")
    var companyPhone by mutableStateOf("")
    var companyTaxNumber by mutableStateOf("")
    var companyLogoUri by mutableStateOf<String?>(null)
    var currencySymbol by mutableStateOf("SAR")
    var currentUser by mutableStateOf<UserItem?>(null)
    var selectedPaymentMethod by mutableStateOf("CASH")
    var selectedCustomer by mutableStateOf<Customer?>(null)
    var discountAmount by mutableDoubleStateOf(0.0)
    var lastInvoice by mutableStateOf<Invoice?>(null)
        private set
    var editingInvoice by mutableStateOf<Invoice?>(null)

    var backupEmail by mutableStateOf("")
    var lastBackupDate by mutableStateOf("Never")
    var selectedCountryCode by mutableStateOf("SA")
    var isRefreshing by mutableStateOf(false)

    var topAdminEmail by mutableStateOf("")
    var topAdminPassword by mutableStateOf("")

    init {
        loadCompanyData()
        loadTopAdminData()
        loadDataFromDatabase()
        loadBackupInfo()
        
        // Automatically restart observers when user logs in/out
        viewModelScope.launch {
            snapshotFlow { currentUser }.collectLatest { user ->
                if (user != null) {
                    Log.d("SalesViewModel", "User session started: ${user.firstName}")
                    sharedPrefs.edit { putString("admin_id", user.adminId) }
                    observeFirebaseData()
                } else {
                    stopObservingFirebase()
                }
            }
        }
    }

    private fun getAdminId(): String? {
        val userAdminId = currentUser?.adminId
        if (!userAdminId.isNullOrEmpty()) return userAdminId
        val savedAdminId = sharedPrefs?.getString("admin_id", null)
        if (!savedAdminId.isNullOrEmpty()) return savedAdminId
        val firebaseUid = auth.currentUser?.uid
        if (!firebaseUid.isNullOrEmpty()) return firebaseUid
        return null
    }

    private fun stopObservingFirebase() {
        registrations.forEach { it.remove() }
        registrations.clear()
    }

    fun observeFirebaseData() {
        val adminId = getAdminId() ?: return
        loadTopAdminData()
        val isAdmin = currentUser?.accessLevel == "1"
        // Consistency fix: Use firstName like in processPayment to ensure filtering works correctly
        val currentUserName = currentUser?.firstName ?: "Admin"

        stopObservingFirebase()

        Log.d("SalesViewModel", "Observing Firebase for AdminId: $adminId, User: $currentUserName")
        
        // Save adminId to SharedPreferences for persistence
        sharedPrefs.edit { putString("admin_id", adminId) }

        // 0. Current User Profile Listener (Real-time updates for Profile Screen)
        val uid = currentUser?.uid
        if (!uid.isNullOrEmpty()) {
            val uListener = db.collection("Users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data ?: return@addSnapshotListener
                        
                        val fullName = data["name"] as? String ?: data["firstName"] as? String ?: "Staff"
                        val nameParts = if (data.containsKey("firstName")) {
                            listOf(data["firstName"] as? String ?: "", data["lastName"] as? String ?: "")
                        } else {
                            fullName.split(" ", limit = 2)
                        }
                        
                        val fName = nameParts.getOrNull(0) ?: fullName
                        val lName = nameParts.getOrNull(1) ?: "****"

                        currentUser = UserItem(
                            uid = uid,
                            firstName = fName,
                            lastName = lName,
                            email = data["email"] as? String ?: "",
                            accessLevel = if ((data["role"] as? String) == "ADMIN") "1" else "9",
                            adminId = data["adminId"] as? String ?: "",
                            active = data["active"] as? Boolean ?: true,
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
                        )
                    }
                }
            registrations.add(uListener)
        }

        // 1. Products Listener (Real-time Stock)
        // এখানে adminId দিয়ে কোয়েরি করা হয়েছে, তাই এই দোকানের সব প্রোডাক্ট আসবে
        val pListener = db.collection("Products").whereEqualTo("adminId", adminId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("SalesViewModel", "Firestore Error: ${error.message}")
                    return@addSnapshotListener
                }

                val allFetched = value?.documents?.mapNotNull { doc ->
                    val pBarcode = doc.getString("barcode") ?: doc.id
                    val pUser = doc.getString("userName") ?: "Admin"
                    val pStock = doc.get("stock")?.toString()?.toDoubleOrNull() ?: 0.0

                    // সংশোধনী: ইজভিবিলিটি সবসময় true থাকবে যেন অ্যাডমিনের প্রোডাক্ট সবাই দেখে
                    val isVisible = true

                    if (isVisible) {
                        // স্টক ম্যাপ আপডেট (সব ইউজারই রিয়েল-টাইম স্টক দেখবে)
                        stockMap[pBarcode] = pStock

                        ProductItem(
                            code = doc.getString("code") ?: "",
                            name = doc.getString("name") ?: "Unnamed",
                            group = doc.getString("group") ?: "General",
                            barcode = pBarcode,
                            cost = doc.get("cost")?.toString() ?: "0",
                            salePrice = doc.get("salePrice")?.toString() ?: "0",
                            active = doc.getBoolean("active") ?: true,
                            unit = doc.getString("unit") ?: "PCS",
                            imageUrl = doc.getString("imageUrl") ?: ""
                        ) to pUser
                    } else null
                } ?: emptyList()

                products.clear()
                val distinctProducts = mutableMapOf<String, ProductItem>()

                allFetched.forEach { (item, _) ->
                    // বারকোড ডুপ্লিকেট হলে লেটেস্টটি রাখবে
                    distinctProducts[item.barcode] = item
                }

                products.addAll(distinctProducts.values.sortedBy { it.name })

                // লোকাল ডাটাবেস (Room DB) আপডেট করা যেন অফলাইনেও দেখা যায়
                viewModelScope.launch {
                    allFetched.forEach { (item, pUser) ->
                        val currentStock = stockMap[item.barcode] ?: 0.0
                        dao.insertProduct(ProductEntity(
                            barcode = item.barcode,
                            userName = pUser,
                            code = item.code,
                            name = item.name,
                            group = item.group,
                            cost = item.cost,
                            salePrice = item.salePrice,
                            active = item.active,
                            unit = item.unit,
                            imageUrl = item.imageUrl,
                            stock = currentStock
                        ))
                    }
                }
            }
        registrations.add(pListener)

        // ... বাকি লিসেনারগুলো (Invoices, Expenses etc.) আগের মতোই থাকবে

        // 2. Invoices Listener
        val iListener = db.collection("Invoices").whereEqualTo("adminId", adminId)
            .addSnapshotListener { value, _ ->
                val firebaseInvoices = value?.map { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val itemsRaw = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    val parsedItems = itemsRaw.map { itemMap ->
                        CartItem(
                            productId = itemMap["productId"] as? String ?: "",
                            productName = itemMap["productName"] as? String ?: "Unknown",
                            quantity = itemMap["quantity"]?.toString()?.toDoubleOrNull() ?: 1.0,
                            price = itemMap["price"]?.toString()?.toDoubleOrNull() ?: 0.0,
                            unit = itemMap["unit"] as? String ?: ""
                        )
                    }
                    Invoice(
                        invoiceNumber = doc.getString("invoiceNumber") ?: "",
                        date = doc.getString("date") ?: "",
                        items = parsedItems,
                        subtotal = doc.get("subtotal")?.toString()?.toDoubleOrNull() ?: 0.0,
                        discount = doc.get("discount")?.toString()?.toDoubleOrNull() ?: 0.0,
                        totalAmount = doc.get("totalAmount")?.toString()?.toDoubleOrNull() ?: 0.0,
                        paymentMethod = doc.getString("paymentMethod") ?: "",
                        companyName = companyName, companyAddress = companyAddress,
                        companyPhone = companyPhone, companyTaxNumber = companyTaxNumber,
                        customerName = doc.getString("customerName") ?: "",
                        customerPhone = doc.getString("customerPhone") ?: "",
                        userName = doc.getString("userName") ?: "Admin",
                        isPurchase = doc.getBoolean("isPurchase") ?: false,
                        customerAddress = doc.getString("customerAddress") ?: "",
                        customerTaxNumber = doc.getString("customerTaxNumber") ?: ""
                    )
                } ?: emptyList()
                
                allInvoices.clear()
                val filtered = if (isAdmin) firebaseInvoices else firebaseInvoices.filter { it.userName.equals(currentUserName, true) }
                allInvoices.addAll(filtered.sortedByDescending { it.date })

                // লোকাল ডাটাবেসে সেভ করা যেন অফলাইনে বা অ্যাপ ডাটা ক্লিয়ার করলে ফিরে আসে
                viewModelScope.launch {
                    firebaseInvoices.forEach { inv ->
                        dao.insertInvoice(InvoiceEntity(
                            invoiceNumber = inv.invoiceNumber,
                            date = inv.date,
                            subtotal = inv.subtotal,
                            discount = inv.discount,
                            totalAmount = inv.totalAmount,
                            paymentMethod = inv.paymentMethod,
                            customerName = inv.customerName,
                            customerPhone = inv.customerPhone,
                            isPurchase = inv.isPurchase,
                            customerAddress = inv.customerAddress,
                            customerTaxNumber = inv.customerTaxNumber,
                            userName = inv.userName
                        ))
                        dao.deleteInvoiceItems(inv.invoiceNumber)
                        inv.items.forEach { item ->
                            dao.insertInvoiceItem(InvoiceItemEntity(
                                invoiceNumber = inv.invoiceNumber,
                                productId = item.productId,
                                productName = item.productName,
                                quantity = item.quantity,
                                price = item.price,
                                unit = item.unit
                            ))
                        }
                    }
                }
            }
        registrations.add(iListener)

        // 3. Expenses Listener
        val eListener = db.collection("Expenses").whereEqualTo("adminId", adminId)
            .addSnapshotListener { value, _ ->
                val firebaseExpenses = value?.map { doc ->
                    Expense(
                        id = doc.getLong("id") ?: 0L,
                        date = doc.getString("date") ?: "",
                        category = doc.getString("category") ?: "",
                        amount = doc.get("amount")?.toString()?.toDoubleOrNull() ?: 0.0,
                        description = doc.getString("description") ?: "",
                        userName = doc.getString("userName") ?: "",
                        paymentMethod = doc.getString("paymentMethod") ?: "CASH"
                    )
                } ?: emptyList()
                
                expenses.clear()
                val filtered = if (isAdmin) firebaseExpenses else firebaseExpenses.filter { it.userName.equals(currentUserName, true) }
                expenses.addAll(filtered.sortedByDescending { it.date })

                // লোকাল ডাটাবেসে সেভ করা
                viewModelScope.launch {
                    firebaseExpenses.forEach { exp ->
                        dao.insertExpense(ExpenseEntity(
                            id = exp.id,
                            date = exp.date,
                            category = exp.category,
                            amount = exp.amount,
                            description = exp.description,
                            userName = exp.userName,
                            paymentMethod = exp.paymentMethod
                        ))
                    }
                }
            }
        registrations.add(eListener)

        // 4. Customers Listener
        val cListener = db.collection("Customers").whereEqualTo("adminId", adminId)
            .addSnapshotListener { value, _ ->
                val firebaseItems = value?.map { doc ->
                    CustomerEntity(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        phone = doc.getString("phone") ?: "",
                        address = doc.getString("address") ?: "",
                        taxNumber = doc.getString("taxNumber") ?: "",
                        code = doc.getString("code") ?: "",
                        isSupplier = doc.getBoolean("isSupplier") ?: false,
                        userName = doc.getString("userName") ?: "Admin"
                    )
                } ?: emptyList()
                
                // পণ্যের তালিকা সবার জন্য উন্মুক্ত রাখা হলো (ফিল্টার ছাড়া)
                val filtered = firebaseItems
                
                customers.clear()
                customers.addAll(filtered.filter { !it.isSupplier }.map { Customer(it.id, it.name, it.phone, it.address, it.taxNumber, it.code, it.userName) })
                suppliers.clear()
                suppliers.addAll(filtered.filter { it.isSupplier }.map { Customer(it.id, it.name, it.phone, it.address, it.taxNumber, it.code, it.userName) })

                // লোকাল ডাটাবেসে সেভ করা
                viewModelScope.launch {
                    firebaseItems.forEach { dao.insertCustomer(it) }
                }
            }
        registrations.add(cListener)
    }

    fun refreshAllData() {
        isRefreshing = true
        val adminId = getAdminId() ?: run { isRefreshing = false; return }

        viewModelScope.launch {
            try {
                // Products
                val pValue = db.collection("Products").whereEqualTo("adminId", adminId).get().await()
                pValue.forEach { doc ->
                    val pUser = doc.getString("userName") ?: "Admin"
                    val pStock = doc.get("stock")?.toString()?.toDoubleOrNull() ?: 0.0
                    dao.insertProduct(ProductEntity(
                        barcode = doc.getString("barcode") ?: doc.id, 
                        userName = pUser,
                        code = doc.getString("code") ?: "", 
                        name = doc.getString("name") ?: "", 
                        group = doc.getString("group") ?: "", 
                        cost = doc.get("cost")?.toString() ?: "0", 
                        salePrice = doc.get("salePrice")?.toString() ?: "0", 
                        active = doc.getBoolean("active") ?: true, 
                        unit = doc.getString("unit") ?: "PCS", 
                        imageUrl = doc.getString("imageUrl") ?: "",
                        stock = pStock
                    ))
                }

                // Invoices
                val iValue = db.collection("Invoices").whereEqualTo("adminId", adminId).get().await()
                iValue.forEach { doc ->
                    val invNum = doc.getString("invoiceNumber") ?: ""
                    dao.insertInvoice(InvoiceEntity(
                        invoiceNumber = invNum,
                        date = doc.getString("date") ?: "",
                        subtotal = doc.get("subtotal")?.toString()?.toDoubleOrNull() ?: 0.0,
                        discount = doc.get("discount")?.toString()?.toDoubleOrNull() ?: 0.0,
                        totalAmount = doc.get("totalAmount")?.toString()?.toDoubleOrNull() ?: 0.0,
                        paymentMethod = doc.getString("paymentMethod") ?: "",
                        customerName = doc.getString("customerName") ?: "",
                        customerPhone = doc.getString("customerPhone") ?: "",
                        isPurchase = doc.getBoolean("isPurchase") ?: false,
                        customerAddress = doc.getString("customerAddress") ?: "",
                        customerTaxNumber = doc.getString("customerTaxNumber") ?: "",
                        userName = doc.getString("userName") ?: "Admin"
                    ))
                    dao.deleteInvoiceItems(invNum)
                    @Suppress("UNCHECKED_CAST")
                    val itemsRaw = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    itemsRaw.forEach { itemMap ->
                        dao.insertInvoiceItem(InvoiceItemEntity(
                            invoiceNumber = invNum,
                            productId = itemMap["productId"] as? String ?: "",
                            productName = itemMap["productName"] as? String ?: "Unknown",
                            quantity = itemMap["quantity"]?.toString()?.toDoubleOrNull() ?: 1.0,
                            price = itemMap["price"]?.toString()?.toDoubleOrNull() ?: 0.0,
                            unit = itemMap["unit"] as? String ?: ""
                        ))
                    }
                }

                // Expenses
                val eValue = db.collection("Expenses").whereEqualTo("adminId", adminId).get().await()
                eValue.forEach { doc ->
                    val id = doc.getLong("id") ?: 0L
                    dao.insertExpense(ExpenseEntity(
                        id = id,
                        date = doc.getString("date") ?: "",
                        category = doc.getString("category") ?: "",
                        amount = doc.get("amount")?.toString()?.toDoubleOrNull() ?: 0.0,
                        description = doc.getString("description") ?: "",
                        userName = doc.getString("userName") ?: "",
                        paymentMethod = doc.getString("paymentMethod") ?: "CASH"
                    ))
                }

                // Customers
                val cValue = db.collection("Customers").whereEqualTo("adminId", adminId).get().await()
                cValue.forEach { doc ->
                    dao.insertCustomer(CustomerEntity(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        phone = doc.getString("phone") ?: "",
                        address = doc.getString("address") ?: "",
                        taxNumber = doc.getString("taxNumber") ?: "",
                        code = doc.getString("code") ?: "",
                        isSupplier = doc.getBoolean("isSupplier") ?: false,
                        userName = doc.getString("userName") ?: "Admin"
                    ))
                }

                loadDataFromDatabase()
                isRefreshing = false
            } catch (e: Exception) {
                Log.e("SalesViewModel", "Refresh failed", e)
                isRefreshing = false
            }
        }
    }

    fun addProduct(product: ProductItem, initialStock: Double = 0.0) {
        val adminId = getAdminId() ?: return
        val currentUserName = currentUser?.firstName ?: "Admin"
        
        viewModelScope.launch {
            var finalImageUrl = product.imageUrl
            // If it's a local URI, upload it first
            if (finalImageUrl.startsWith("content://") || finalImageUrl.startsWith("file://")) {
                finalImageUrl = uploadProductImage(finalImageUrl.toUri(), product.barcode) ?: finalImageUrl
            }
            
            val updatedProduct = product.copy(imageUrl = finalImageUrl)
            products.add(0, updatedProduct)
            stockMap[updatedProduct.barcode] = initialStock
            
            dao.insertProduct(ProductEntity(updatedProduct.barcode, currentUserName, updatedProduct.code, updatedProduct.name, updatedProduct.group, updatedProduct.cost, updatedProduct.salePrice, updatedProduct.active, updatedProduct.unit, updatedProduct.imageUrl, initialStock))
            
            val data = hashMapOf(
                "adminId" to adminId, "userName" to currentUserName, "barcode" to updatedProduct.barcode, "code" to updatedProduct.code, "name" to updatedProduct.name,
                "group" to updatedProduct.group, "cost" to updatedProduct.cost, "salePrice" to updatedProduct.salePrice,
                "active" to updatedProduct.active, "unit" to updatedProduct.unit, "imageUrl" to updatedProduct.imageUrl, "stock" to initialStock
            )
            db.collection("Products").document("${adminId}_${updatedProduct.barcode}").set(data)
        }
    }

    private suspend fun uploadProductImage(uri: Uri, barcode: String): String? {
        return try {
            val adminId = getAdminId() ?: return null
            val ref = storage.reference.child("product_images/${adminId}_$barcode.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteProduct(barcode: String) {
        val adminId = getAdminId() ?: return
        val currentUserName = currentUser?.firstName ?: "Admin"
        products.removeAll { it.barcode == barcode }
        stockMap.remove(barcode)
        viewModelScope.launch {
            dao.deleteProductByBarcode(barcode, currentUserName)
            db.collection("Products").document("${adminId}_${currentUserName}_$barcode").delete()
        }
    }

    fun processPayment(paymentMethod: String) {
        if (cartItems.isEmpty()) return
        val adminId = getAdminId() ?: return
        val currentUserName = currentUser?.firstName ?: "Admin"
        val invoiceNumber = "INV-${System.currentTimeMillis()}"
        val date = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        val isReturn = paymentMethod.contains("RETURN", ignoreCase = true)
        val subtotal = if (isReturn) -getSubtotal() else getSubtotal()
        val total = if (isReturn) -getTotalPrice() else getTotalPrice()
        
        val newInvoice = Invoice(
            invoiceNumber = invoiceNumber,
            date = date,
            items = cartItems.toList(),
            subtotal = subtotal,
            discount = discountAmount,
            totalAmount = total,
            paymentMethod = paymentMethod,
            companyName = companyName,
            companyAddress = companyAddress,
            companyPhone = companyPhone,
            companyTaxNumber = companyTaxNumber,
            customerName = selectedCustomer?.name ?: "Walk-in Customer",
            customerPhone = selectedCustomer?.phone ?: "",
            isPurchase = false,
            customerAddress = selectedCustomer?.address ?: "",
            customerTaxNumber = selectedCustomer?.taxNumber ?: "",
            userName = currentUserName
        )
        
        allInvoices.add(0, newInvoice)
        lastInvoice = newInvoice
        
        viewModelScope.launch {
            // 1. Save to Room (Local Database)
            dao.insertInvoice(
                InvoiceEntity(
                    invoiceNumber = invoiceNumber,
                    date = date,
                    subtotal = subtotal,
                    discount = discountAmount,
                    totalAmount = total,
                    paymentMethod = paymentMethod,
                    customerName = newInvoice.customerName,
                    customerPhone = newInvoice.customerPhone,
                    isPurchase = false,
                    customerAddress = newInvoice.customerAddress,
                    customerTaxNumber = newInvoice.customerTaxNumber,
                    userName = currentUserName
                )
            )
            
            cartItems.forEach { item ->
                dao.insertInvoiceItem(
                    InvoiceItemEntity(
                        id = 0,
                        invoiceNumber = invoiceNumber,
                        productId = item.productId,
                        productName = item.productName,
                        quantity = item.quantity,
                        price = item.price,
                        unit = item.unit
                    )
                )
                
                // Update stock
                val currentStock = stockMap[item.productId] ?: 0.0
                val newStock = if (isReturn) currentStock + item.quantity else currentStock - item.quantity
                stockMap[item.productId] = newStock
                
                // Sync stock with Firebase
                db.collection("Products").document("${adminId}_${item.productId}").update("stock", newStock)
            }
            
            // 2. Save to Firebase (Cloud)
            val invoiceData = hashMapOf(
                "adminId" to adminId,
                "invoiceNumber" to invoiceNumber,
                "date" to date,
                "subtotal" to subtotal,
                "discount" to discountAmount,
                "totalAmount" to total,
                "paymentMethod" to paymentMethod,
                "customerName" to newInvoice.customerName,
                "customerPhone" to newInvoice.customerPhone,
                "customerAddress" to newInvoice.customerAddress,
                "customerTaxNumber" to newInvoice.customerTaxNumber,
                "userName" to currentUserName,
                "isPurchase" to false,
                "items" to cartItems.map { 
                    mapOf(
                        "productId" to it.productId,
                        "productName" to it.productName,
                        "quantity" to it.quantity,
                        "price" to it.price,
                        "unit" to it.unit
                    ) 
                }
            )
            db.collection("Invoices").document(invoiceNumber).set(invoiceData)
            
            clearCart()
        }
    }

    fun completeDamageInvoice(items: List<CartItem>, reason: String) {
        if (items.isEmpty()) return
        val adminId = getAdminId() ?: return
        val currentUserName = currentUser?.firstName ?: "Admin"
        val invoiceNumber = "DMG-${System.currentTimeMillis()}"
        val date = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        
        val totalLoss = items.sumOf { it.price * it.quantity }
        
        val newInvoice = Invoice(
            invoiceNumber, date, items.toList(), totalLoss, 0.0, totalLoss,
            "DAMAGE", companyName, companyAddress, companyPhone, companyTaxNumber,
            "Damage: $reason", "", false, "", "", currentUserName
        )
        
        allInvoices.add(0, newInvoice)
        
        viewModelScope.launch {
            dao.insertInvoice(InvoiceEntity(invoiceNumber, date, totalLoss, 0.0, totalLoss, "DAMAGE", "Damage: $reason", "", false, "", "", currentUserName))
            items.forEach { item ->
                dao.insertInvoiceItem(InvoiceItemEntity(0, invoiceNumber, item.productId, item.productName, item.quantity, item.price, item.unit))
                val currentStock = stockMap[item.productId] ?: 0.0
                val newStock = currentStock - item.quantity
                stockMap[item.productId] = newStock
                db.collection("Products").document("${adminId}_${currentUserName}_${item.productId}").update("stock", newStock)
            }
        }

        val invoiceData = hashMapOf(
            "adminId" to adminId, "invoiceNumber" to invoiceNumber, "date" to date,
            "subtotal" to totalLoss, "discount" to 0.0, "totalAmount" to totalLoss,
            "paymentMethod" to "DAMAGE", "customerName" to "Damage: $reason",
            "userName" to currentUserName, "isPurchase" to false,
            "items" to items.map { mapOf("productId" to it.productId, "productName" to it.productName, "quantity" to it.quantity, "price" to it.price, "unit" to it.unit) }
        )
        db.collection("Invoices").document(invoiceNumber).set(invoiceData)
    }

    fun addExpense(category: String, amount: Double, description: String, paymentMethod: String) {
        val adminId = getAdminId() ?: return
        val currentUserName = currentUser?.firstName ?: "Admin"
        val id = System.currentTimeMillis()
        val date = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        val newExp = Expense(id, date, category, amount, description, currentUserName, paymentMethod)
        
        expenses.add(0, newExp)
        viewModelScope.launch {
            dao.insertExpense(ExpenseEntity(id, date, category, amount, description, currentUserName, paymentMethod))
        }
        db.collection("Expenses").document(id.toString()).set(hashMapOf("adminId" to adminId, "id" to id, "category" to category, "amount" to amount, "userName" to currentUserName, "date" to date))
    }

    fun deleteExpense(id: Long) {
        expenses.removeAll { it.id == id }
        viewModelScope.launch {
            dao.deleteExpense(id)
            db.collection("Expenses").document(id.toString()).delete()
        }
    }

    fun insertCustomer(name: String, phone: String, address: String, taxNumber: String, code: String, isSupplier: Boolean) {
        val adminId = getAdminId() ?: return
        val currentUserName = currentUser?.firstName ?: "Admin"
        val id = UUID.randomUUID().toString()
        val newCust = Customer(id, name, phone, address, taxNumber, code, currentUserName)
        
        if (isSupplier) suppliers.add(0, newCust) else customers.add(0, newCust)
        viewModelScope.launch { dao.insertCustomer(CustomerEntity(id, name, phone, address, taxNumber, code, isSupplier, currentUserName)) }
        db.collection("Customers").document(id).set(hashMapOf(
            "adminId" to adminId, "name" to name, "phone" to phone, "address" to address,
            "taxNumber" to taxNumber, "code" to code, "isSupplier" to isSupplier, "userName" to currentUserName
        ))
    }

    fun updateCustomer(customer: Customer, isSupplier: Boolean) {
        val adminId = getAdminId() ?: return
        val list = if (isSupplier) suppliers else customers
        val index = list.indexOfFirst { it.id == customer.id }
        if (index != -1) {
            list[index] = customer
        }
        viewModelScope.launch {
            dao.insertCustomer(CustomerEntity(customer.id, customer.name, customer.phone, customer.address, customer.taxNumber, customer.code, isSupplier, customer.userName))
        }
        db.collection("Customers").document(customer.id).set(hashMapOf(
            "adminId" to adminId, "name" to customer.name, "phone" to customer.phone, "address" to customer.address,
            "taxNumber" to customer.taxNumber, "code" to customer.code, "isSupplier" to isSupplier, "userName" to customer.userName
        ))
    }

    fun deleteCustomer(id: String, isSupplier: Boolean) {
        if (isSupplier) suppliers.removeAll { it.id == id } else customers.removeAll { it.id == id }
        viewModelScope.launch {
            dao.deleteCustomerById(id)
            db.collection("Customers").document(id).delete()
        }
    }

    fun addToCart(product: ProductItem) {
        val existing = cartItems.find { it.productId == product.barcode }
        if (existing != null) {
            incrementCartItem(product.barcode)
        } else {
            cartItems.add(CartItem(product.barcode, product.name, 1.0, product.salePrice.toDoubleOrNull() ?: 0.0, product.unit))
        }
    }

    fun clearCart() { cartItems.clear(); discountAmount = 0.0; selectedCustomer = null }
    fun getSubtotal() = cartItems.sumOf { it.price * it.quantity }
    fun getTotalPrice() = getSubtotal() - discountAmount

    fun loadDataFromDatabase() {
        viewModelScope.launch {
            val isAdmin = currentUser?.accessLevel == "1"
            val currentUserName = currentUser?.firstName ?: ""
            
            val dbProducts = dao.getAllProducts()
            // স্টাফ মেম্বারদের জন্য ফিল্টার সরিয়ে দেওয়া হলো যাতে তারা অ্যাডমিনের তৈরি করা বা অন্য স্টাফের তৈরি করা সব প্রোডাক্ট দেখতে পায়
            products.clear()
            products.addAll(dbProducts.map { 
                ProductItem(
                    code = it.code, 
                    name = it.name, 
                    group = it.group, 
                    barcode = it.barcode, 
                    cost = it.cost, 
                    salePrice = it.salePrice, 
                    active = it.active, 
                    unit = it.unit,
                    imageUrl = it.imageUrl
                ) 
            }.sortedBy { it.name })
            dbProducts.forEach { stockMap[it.barcode] = it.stock }
            
            val dbInvoices = dao.getAllInvoices()
            val filteredInvoices = if (isAdmin) dbInvoices else dbInvoices.filter { it.userName.equals(currentUserName, true) }
            
            allInvoices.clear()
            filteredInvoices.forEach { inv ->
                 val items = dao.getItemsForInvoice(inv.invoiceNumber).map { CartItem(it.productId, it.productName, it.quantity, it.price, it.unit) }
                 allInvoices.add(Invoice(inv.invoiceNumber, inv.date, items, inv.subtotal, inv.discount, inv.totalAmount, inv.paymentMethod, companyName, companyAddress, companyPhone, companyTaxNumber, inv.customerName, inv.customerPhone, inv.isPurchase, inv.customerAddress, inv.customerTaxNumber, inv.userName))
            }

            expenses.clear()
            dao.getAllExpenses().forEach {
                expenses.add(Expense(it.id, it.date, it.category, it.amount, it.description, it.userName, it.paymentMethod))
            }

            users.clear()
            dao.getAllUsers().forEach {
                users.add(UserItem(
                    firstName = it.firstName,
                    lastName = it.lastName,
                    password = it.password,
                    email = it.email,
                    accessLevel = it.accessLevel,
                    active = it.active,
                    uid = it.uid,
                    adminId = it.adminId,
                    idCardNumber = it.idCardNumber,
                    iqamaId = it.iqamaId,
                    passportId = it.passportId,
                    phone = it.phone,
                    address = it.address,
                    posNo = it.posNo,
                    nationality = it.nationality,
                    routeArea = it.routeArea,
                    rank = it.rank,
                    sectionName = it.sectionName,
                    dateOfBirth = it.dateOfBirth,
                    bloodType = it.bloodType,
                    gender = it.gender
                ))
            }

            paymentTypes.clear()
            dao.getAllPaymentTypes().forEach {
                paymentTypes.add(PaymentTypeItem(it.name, it.position, it.enabled, it.quickPayment, it.customerRequired, it.changeAllowed, it.markAsPaid, it.printReceipt, it.shortcutKey))
            }

            taxRates.clear()
            dao.getAllTaxRates().forEach {
                taxRates.add(TaxRateItem(it.code, it.name, it.rate, it.enabled, it.fixed))
            }

            printStations.clear()
            printStations.addAll(dao.getAllPrintStations())
        }
    }

    private fun loadCompanyData() {
        companyName = sharedPrefs.getString("company_name", "") ?: ""
        companyAddress = sharedPrefs.getString("company_address", "") ?: ""
        companyPhone = sharedPrefs.getString("company_phone", "") ?: ""
        companyTaxNumber = sharedPrefs.getString("company_tax", "") ?: ""
        currencySymbol = sharedPrefs.getString("currency_symbol", "BDT") ?: "BDT"
        selectedCountryCode = sharedPrefs.getString("selected_country_code", "BD") ?: "BD"
        
        val reasonsStr = sharedPrefs.getString("void_reasons", "Wrong Item,Customer Changed Mind,Testing") ?: ""
        voidReasons.clear()
        if (reasonsStr.isNotEmpty()) voidReasons.addAll(reasonsStr.split(","))
    }

    fun updateCompanyData(name: String, address: String, phone: String, taxNumber: String) {
        companyName = name; companyAddress = address; companyPhone = phone; companyTaxNumber = taxNumber
        sharedPrefs.edit { putString("company_name", name); putString("company_address", address); putString("company_phone", phone); putString("company_tax", taxNumber) }
    }

    fun updateTopAdmin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplication(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        val adminId = getAdminId()
        if (adminId == null) {
            Log.e("SalesViewModel", "Cannot save Top Admin: adminId is null")
            Toast.makeText(getApplication(), "Error: Admin ID not found. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        viewModelScope.launch {
            val data = hashMapOf(
                "email" to email,
                "password" to password,
                "adminId" to adminId
            )
            db.collection("Settings").document("TopAdmin_$adminId").set(data)
                .addOnSuccessListener {
                    Toast.makeText(getApplication(), "Top Admin saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("SalesViewModel", "Firebase save failed", e)
                    Toast.makeText(getApplication(), "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            topAdminEmail = email
            topAdminPassword = password
            sharedPrefs.edit {
                putString("top_admin_email_$adminId", email)
                putString("top_admin_password_$adminId", password)
            }
        }
    }

    private fun loadTopAdminData() {
        val adminId = getAdminId() ?: return
        
        // Load admin-specific cached credentials
        topAdminEmail = sharedPrefs.getString("top_admin_email_$adminId", "") ?: ""
        topAdminPassword = sharedPrefs.getString("top_admin_password_$adminId", "") ?: ""

        db.collection("Settings").document("TopAdmin_$adminId").get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                topAdminEmail = doc.getString("email") ?: ""
                topAdminPassword = doc.getString("password") ?: ""
                sharedPrefs.edit {
                    putString("top_admin_email_$adminId", topAdminEmail)
                    putString("top_admin_password_$adminId", topAdminPassword)
                }
            } else {
                // If no Top Admin found for this specific admin, reset local state
                topAdminEmail = ""
                topAdminPassword = ""
                sharedPrefs.edit {
                    remove("top_admin_email_$adminId")
                    remove("top_admin_password_$adminId")
                }
            }
        }
    }

    fun updateCountry(code: String) {
        selectedCountryCode = code
        // দেশ অনুযায়ী কারেন্সি সেট করা
        currencySymbol = when (code) {
            "BD" -> "BDT"
            "IN" -> "INR"
            "US" -> "$"
            "PK" -> "PKR"
            "SA" -> "SAR"
            "AE" -> "AED"
            else -> "BDT"
        }
        sharedPrefs.edit { 
            putString("selected_country_code", code)
            putString("currency_symbol", currencySymbol)
        }
    }

    fun incrementCartItem(productId: String) {
        val index = cartItems.indexOfFirst { it.productId == productId }
        if (index != -1) cartItems[index] = cartItems[index].copy(quantity = cartItems[index].quantity + 1.0)
    }

    fun decrementCartItem(productId: String) {
        val index = cartItems.indexOfFirst { it.productId == productId }
        if (index != -1) {
            if (cartItems[index].quantity > 1.0) cartItems[index] = cartItems[index].copy(quantity = cartItems[index].quantity - 1.0)
            else cartItems.removeAt(index)
        }
    }

    fun saveLogoUri(uri: String) { companyLogoUri = uri; sharedPrefs.edit { putString("company_logo", uri) } }

    fun saveBackupInfo(email: String, date: String) {
        backupEmail = email
        lastBackupDate = date
        sharedPrefs.edit {
            putString("backup_email", email)
            putString("last_backup", date)
        }
    }

    fun generateBackupJson(): String {
        val root = org.json.JSONObject()
        val productsArr = org.json.JSONArray()
        products.forEach {
            val p = org.json.JSONObject()
            p.put("barcode", it.barcode); p.put("name", it.name); p.put("group", it.group)
            p.put("code", it.code); p.put("cost", it.cost); p.put("salePrice", it.salePrice)
            p.put("active", it.active); p.put("unit", it.unit); p.put("stock", stockMap[it.barcode] ?: 0.0)
            productsArr.put(p)
        }
        root.put("products", productsArr)
        val customersArr = org.json.JSONArray()
        (customers + suppliers).forEach {
            val c = org.json.JSONObject()
            c.put("id", it.id); c.put("name", it.name); c.put("phone", it.phone)
            c.put("address", it.address); c.put("taxNumber", it.taxNumber); c.put("code", it.code)
            c.put("isSupplier", suppliers.contains(it))
            customersArr.put(c)
        }
        root.put("customers", customersArr)
        val invoicesArr = org.json.JSONArray()
        allInvoices.forEach {
            val i = org.json.JSONObject()
            i.put("invoiceNumber", it.invoiceNumber); i.put("date", it.date); i.put("subtotal", it.subtotal)
            i.put("discount", it.discount); i.put("totalAmount", it.totalAmount); i.put("paymentMethod", it.paymentMethod)
            i.put("customerName", it.customerName); i.put("customerPhone", it.customerPhone); i.put("userName", it.userName)
            i.put("isPurchase", it.isPurchase)
            invoicesArr.put(i)
        }
        root.put("invoices", invoicesArr)
        val expensesArr = org.json.JSONArray()
        expenses.forEach {
            val e = org.json.JSONObject()
            e.put("id", it.id); e.put("date", it.date); e.put("category", it.category)
            e.put("amount", it.amount); e.put("description", it.description); e.put("userName", it.userName)
            e.put("paymentMethod", it.paymentMethod)
            expensesArr.put(e)
        }
        root.put("expenses", expensesArr)
        return root.toString(4)
    }

    fun restoreFromJson(json: String) {
        viewModelScope.launch {
            try {
                val root = org.json.JSONObject(json)
                val adminId = getAdminId() ?: "default"
                val currentUserName = currentUser?.firstName ?: "Admin"
                root.optJSONArray("products")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val p = arr.getJSONObject(i)
                        val barcode = p.getString("barcode")
                        val entity = ProductEntity(barcode, currentUserName, p.getString("code"), p.getString("name"), p.getString("group"), p.getString("cost"), p.getString("salePrice"), p.getBoolean("active"), p.getString("unit"), p.optString("imageUrl", ""), p.optDouble("stock", 0.0))
                        dao.insertProduct(entity)
                        db.collection("Products").document("${adminId}_$barcode").set(hashMapOf("adminId" to adminId, "userName" to currentUserName, "barcode" to barcode, "code" to entity.code, "name" to entity.name, "group" to entity.group, "cost" to entity.cost, "salePrice" to entity.salePrice, "active" to entity.active, "unit" to entity.unit, "stock" to entity.stock))
                    }
                }
                root.optJSONArray("customers")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val c = arr.getJSONObject(i)
                        val id = c.getString("id")
                        val entity = CustomerEntity(id, c.getString("name"), c.getString("phone"), c.optString("address"), c.optString("taxNumber"), c.optString("code"), c.optBoolean("isSupplier"), currentUserName)
                        dao.insertCustomer(entity)
                        db.collection("Customers").document(id).set(hashMapOf("adminId" to adminId, "name" to entity.name, "isSupplier" to entity.isSupplier, "userName" to currentUserName, "phone" to entity.phone, "address" to entity.address, "taxNumber" to entity.taxNumber, "code" to entity.code))
                    }
                }
                root.optJSONArray("invoices")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val inv = arr.getJSONObject(i)
                        val num = inv.getString("invoiceNumber")
                        dao.insertInvoice(InvoiceEntity(num, inv.getString("date"), inv.getDouble("subtotal"), inv.getDouble("discount"), inv.getDouble("totalAmount"), inv.getString("paymentMethod"), inv.getString("customerName"), inv.getString("customerPhone"), inv.optBoolean("isPurchase"), "", "", inv.optString("userName", currentUserName)))
                        db.collection("Invoices").document(num).set(hashMapOf("adminId" to adminId, "invoiceNumber" to num, "date" to inv.getString("date"), "subtotal" to inv.getDouble("subtotal"), "discount" to inv.getDouble("discount"), "totalAmount" to inv.getDouble("totalAmount"), "paymentMethod" to inv.getString("paymentMethod"), "customerName" to inv.getString("customerName"), "userName" to inv.optString("userName", currentUserName), "isPurchase" to inv.optBoolean("isPurchase")))
                    }
                }
                root.optJSONArray("expenses")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val e = arr.getJSONObject(i)
                        val id = e.getLong("id")
                        dao.insertExpense(ExpenseEntity(id, e.getString("date"), e.getString("category"), e.getDouble("amount"), e.getString("description"), e.optString("userName", currentUserName), e.optString("paymentMethod", "CASH")))
                        db.collection("Expenses").document(id.toString()).set(hashMapOf("adminId" to adminId, "id" to id, "category" to e.getString("category"), "amount" to e.getDouble("amount"), "userName" to e.optString("userName", currentUserName), "date" to e.getString("date")))
                    }
                }
                loadDataFromDatabase()
            } catch (e: Exception) { Log.e("SalesViewModel", "Restore error: ${e.message}") }
        }
    }

    fun updateProduct(oldBarcode: String, updated: ProductItem) {
        val adminId = getAdminId() ?: return
        val currentUserName = currentUser?.firstName ?: "Admin"
        
        viewModelScope.launch {
            var finalImageUrl = updated.imageUrl
            if (finalImageUrl.startsWith("content://") || finalImageUrl.startsWith("file://")) {
                finalImageUrl = uploadProductImage(finalImageUrl.toUri(), updated.barcode) ?: finalImageUrl
            }
            
            val finalProduct = updated.copy(imageUrl = finalImageUrl)
            val index = products.indexOfFirst { it.barcode == oldBarcode }
            if (index != -1) products[index] = finalProduct
            
            val stock = stockMap[oldBarcode] ?: 0.0
            dao.insertProduct(ProductEntity(finalProduct.barcode, currentUserName, finalProduct.code, finalProduct.name, finalProduct.group, finalProduct.cost, finalProduct.salePrice, finalProduct.active, finalProduct.unit, finalProduct.imageUrl, stock))
            
            val data = hashMapOf(
                "adminId" to adminId, "userName" to currentUserName, "barcode" to finalProduct.barcode, "code" to finalProduct.code, "name" to finalProduct.name, 
                "group" to finalProduct.group, "cost" to finalProduct.cost, "salePrice" to finalProduct.salePrice, 
                "active" to finalProduct.active, "unit" to finalProduct.unit, "imageUrl" to finalProduct.imageUrl, "stock" to stock
            )
            
            db.collection("Products").document("${adminId}_${finalProduct.barcode}").set(data)
            
            if (oldBarcode != finalProduct.barcode) {
                dao.deleteProductByBarcode(oldBarcode, currentUserName)
                db.collection("Products").document("${adminId}_$oldBarcode").delete()
                stockMap[finalProduct.barcode] = stockMap.remove(oldBarcode) ?: 0.0
            }
        }
    }
    
    private fun loadBackupInfo() {
        backupEmail = sharedPrefs.getString("backup_email", "") ?: ""
        lastBackupDate = sharedPrefs.getString("last_backup", "Never") ?: "Never"
    }

    fun deleteInvoice(invoiceNumber: String) {
        allInvoices.removeAll { it.invoiceNumber == invoiceNumber }
        viewModelScope.launch {
            dao.deleteInvoice(invoiceNumber)
            dao.deleteInvoiceItems(invoiceNumber)
            db.collection("Invoices").document(invoiceNumber).delete()
        }
    }

    fun startEditing(invoice: Invoice) {
        editingInvoice = invoice
        cartItems.clear()
        cartItems.addAll(invoice.items)
        discountAmount = invoice.discount
        selectedCustomer = customers.find { it.name == invoice.customerName }
    }

    fun addVoidReason(reason: String) {
        voidReasons.add(reason)
        sharedPrefs.edit { putString("void_reasons", voidReasons.joinToString(",")) }
    }

    fun deleteVoidReason(reason: String) {
        voidReasons.remove(reason)
        sharedPrefs.edit { putString("void_reasons", voidReasons.joinToString(",")) }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            dao.getAllProducts().forEach { deleteProduct(it.barcode) }
            allInvoices.toList().forEach { deleteInvoice(it.invoiceNumber) }
            expenses.toList().forEach { deleteExpense(it.id) }
            customers.toList().forEach { deleteCustomer(it.id, false) }
            suppliers.toList().forEach { deleteCustomer(it.id, true) }
        }
    }

    fun addPaymentType(item: PaymentTypeItem) {
        paymentTypes.add(item)
        viewModelScope.launch {
            dao.insertPaymentType(PaymentTypeEntity(item.name, item.position, item.enabled, item.quickPayment, item.customerRequired, item.changeAllowed, item.markAsPaid, item.printReceipt, item.shortcutKey))
        }
    }

    fun deletePaymentType(item: PaymentTypeItem) {
        paymentTypes.remove(item)
        viewModelScope.launch {
            dao.deletePaymentType(PaymentTypeEntity(item.name, item.position, item.enabled, item.quickPayment, item.customerRequired, item.changeAllowed, item.markAsPaid, item.printReceipt, item.shortcutKey))
        }
    }

    fun addTaxRate(item: TaxRateItem) {
        taxRates.add(item)
        viewModelScope.launch {
            dao.insertTaxRate(TaxRateEntity(item.code, item.name, item.rate, item.enabled, item.fixed))
        }
    }

    fun deleteTaxRate(item: TaxRateItem) {
        taxRates.remove(item)
        viewModelScope.launch {
            dao.deleteTaxRate(TaxRateEntity(item.code, item.name, item.rate, item.enabled, item.fixed))
        }
    }


    fun addPrintStation(item: PrintStationEntity) {
        printStations.add(item)
        viewModelScope.launch {
            dao.insertPrintStation(item)
        }
    }

    fun deletePrintStation(item: PrintStationEntity) {
        printStations.remove(item)
        viewModelScope.launch {
            dao.deletePrintStation(item)
        }
    }

    fun updateCartItemQuantity(productId: String, quantity: Double) {
        val index = cartItems.indexOfFirst { it.productId == productId }
        if (index != -1) {
            if (quantity > 0) {
                cartItems[index] = cartItems[index].copy(quantity = quantity)
            } else {
                cartItems.removeAt(index)
            }
        }
    }

    fun clearInvoice() {
        editingInvoice = null
        lastInvoice = null
        clearCart()
    }

    fun getTaxAmount(): Double {
        val rate = getActiveTaxRateItem()?.rate?.toDoubleOrNull() ?: 0.0
        return getSubtotal() * (rate / 100.0)
    }

    fun getActiveTaxRateItem(): TaxRateItem? {
        return taxRates.find { it.enabled }
    }

    fun completePurchase(items: List<CartItem>, supplierName: String, invoiceNumber: String, paymentMethod: String, applyTax: Boolean) {
        if (items.isEmpty()) return
        val adminId = getAdminId() ?: return
        val currentUserName = currentUser?.firstName ?: "Admin"
        val date = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        val subtotal = items.sumOf { it.price * it.quantity }
        
        val taxRateItem = getActiveTaxRateItem()
        val taxRate = (taxRateItem?.rate?.toDoubleOrNull() ?: 0.0) / 100.0
        val tax = if (applyTax) subtotal * taxRate else 0.0
        val total = subtotal + tax
        
        val newInvoice = Invoice(
            invoiceNumber, date, items.toList(), subtotal, 0.0, total,
            paymentMethod, companyName, companyAddress, companyPhone, companyTaxNumber,
            "Supplier: $supplierName", "", true, "", "", currentUserName
        )
        
        allInvoices.add(0, newInvoice)
        
        viewModelScope.launch {
            dao.insertInvoice(InvoiceEntity(invoiceNumber, date, subtotal, 0.0, total, paymentMethod, newInvoice.customerName, newInvoice.customerPhone, true, newInvoice.customerAddress, newInvoice.customerTaxNumber, currentUserName))
            items.forEach { item ->
                dao.insertInvoiceItem(InvoiceItemEntity(0, invoiceNumber, item.productId, item.productName, item.quantity, item.price, item.unit))
                val currentStock = stockMap[item.productId] ?: 0.0
                val newStock = currentStock + item.quantity
                stockMap[item.productId] = newStock
                db.collection("Products").document("${adminId}_${item.productId}").update("stock", newStock)
            }
        }
        
        val invoiceData = hashMapOf(
            "adminId" to adminId, "invoiceNumber" to invoiceNumber, "date" to date,
            "subtotal" to subtotal, "discount" to 0.0, "totalAmount" to total,
            "paymentMethod" to paymentMethod, "customerName" to newInvoice.customerName,
            "userName" to currentUserName, "isPurchase" to true,
            "items" to items.map { mapOf("productId" to it.productId, "productName" to it.productName, "quantity" to it.quantity, "price" to it.price, "unit" to it.unit) }
        )
        db.collection("Invoices").document(invoiceNumber).set(invoiceData)
    }

    fun updatePassword(newPassword: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null && newPassword.isNotEmpty()) {
            user.updatePassword(newPassword)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e.message ?: "পাসওয়ার্ড পরিবর্তনে সমস্যা হয়েছে")
                }
        } else {
            onFailure("পাসওয়ার্ড খালি হতে পারবে না")
        }
    }

    fun clearAllData() {
        currentUser = null
        products.clear()
        cartItems.clear()
        customers.clear()
        suppliers.clear()
        allInvoices.clear()
        users.clear()
        paymentTypes.clear()
        taxRates.clear()
        expenses.clear()
        promotions.clear()
        stockMap.clear()
        selectedCustomer = null
        discountAmount = 0.0
        lastInvoice = null
        topAdminEmail = ""
        topAdminPassword = ""
        sharedPrefs.edit { 
            remove("admin_id")
        }
        stopObservingFirebase()
    }
}
