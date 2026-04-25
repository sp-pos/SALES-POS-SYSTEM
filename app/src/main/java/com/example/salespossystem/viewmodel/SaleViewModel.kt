package com.example.salespossystem.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.salespossystem.data.Sale
import com.example.salespossystem.data.SaleItem
import com.example.salespossystem.data.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SaleViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    val salesList = mutableStateListOf<Sale>()
    val totalIncome = mutableStateOf(0.0)
    
    var firebaseUser = mutableStateOf<com.google.firebase.auth.FirebaseUser?>(null)
    var adminData = mutableStateOf<Map<String, Any>?>(null)

    fun registerAdmin(
        name: String, 
        email: String, 
        pass: String, 
        shopName: String,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                val data = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "role" to "ADMIN",          
                    "adminId" to uid,
                    "shopName" to shopName,
                    "isLocked" to false,
                    "latitude" to 0.0,
                    "longitude" to 0.0,
                    "lastActive" to Timestamp.now()
                )
                // Save in Admins collection
                db.collection("Admins").document(uid).set(data)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it.message ?: "Firestore error") }
            }
            .addOnFailureListener { onFailure(it.message ?: "Auth error") }
    }
    
    // Unified Login for both Admin and Staff
    fun loginUser(
        email: String,
        pass: String,
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                
                // 1. First check Admins
                db.collection("Admins").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            onSuccess(doc.data!!)
                        } else {
                            // 2. Then check Users (Staff)
                            db.collection("Users").document(uid).get()
                                .addOnSuccessListener { docStaff ->
                                    if (docStaff.exists()) {
                                        if (docStaff.getBoolean("isLocked") == true) {
                                            auth.signOut()
                                            onFailure("আপনার অ্যাকাউন্টটি লক করা হয়েছে!")
                                        } else {
                                            onSuccess(docStaff.data!!)
                                        }
                                    } else {
                                        // 3. Fallback check for old structure
                                        db.collection("Sales").document(uid).get()
                                            .addOnSuccessListener { docSales ->
                                                if (docSales.exists()) onSuccess(docSales.data!!)
                                                else {
                                                    auth.signOut()
                                                    onFailure("ইউজার ডাটা পাওয়া যায়নি!")
                                                }
                                            }
                                    }
                                }
                        }
                    }
            }
            .addOnFailureListener { onFailure(it.message ?: "Login failed") }
    }

    // Existing functions...
    fun completeSale(cartItems: List<SaleItem>, total: Double, discount: Double, paymentType: String, customerName: String, customerPhone: String, currentUser: User, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val isReturn = paymentType.contains("RETURN", ignoreCase = true)
        val payable = if (isReturn) -(total - discount) else (total - discount)
        val finalTotal = if (isReturn) -total else total
        
        val saleRef = db.collection("Sales").document()
        val saleId = saleRef.id
        val newSale = Sale(saleId = saleId, adminId = currentUser.adminId, staffId = currentUser.uid, staffName = currentUser.name, customerName = customerName, customerPhone = customerPhone, discount = discount, payableAmount = payable, totalAmount = finalTotal, paymentType = paymentType, items = cartItems, latitude = currentUser.latitude, longitude = currentUser.longitude, timestamp = Timestamp.now())
        saleRef.set(newSale).addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
    }

    fun fetchSalesReport(adminId: String) {
        db.collection("Sales").whereEqualTo("adminId", adminId).orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val sales = value?.toObjects(Sale::class.java) ?: emptyList()
                salesList.clear()
                salesList.addAll(sales)
                totalIncome.value = sales.sumOf { it.payableAmount }
            }
    }

    fun deleteSale(saleId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("Sales").document(saleId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Delete failed") }
    }
}
