package com.example.salespossystem.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.salespossystem.data.User
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp

class AuthViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    var staffUsers = mutableStateListOf<User>()
        private set

    fun registerStaff(
        context: Context,
        name: String,
        email: String,
        pass: String,
        adminId: String,
        idCardNumber: String = "",
        iqamaId: String = "",
        passportId: String = "",
        phone: String = "",
        address: String = "",
        posNo: String = "",
        nationality: String = "",
        routeArea: String = "",
        rank: String = "",
        sectionName: String = "",
        dateOfBirth: String = "",
        bloodType: String = "",
        gender: String = "",
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        // ১. একটি সেকেন্ডারি Firebase App তৈরি করা যাতে মেইন অ্যাডমিন লগ-আউট না হয়
        val secondaryApp = try {
            FirebaseApp.getInstance("secondary")
        } catch (e: Exception) {
            val options = FirebaseApp.getInstance().options
            FirebaseApp.initializeApp(context, options, "secondary")
        }
        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

        // ২. স্টাফ ইউজার তৈরি করা
        secondaryAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                val data = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "role" to "STAFF",
                    "adminId" to adminId,
                    "isLocked" to false,
                    "latitude" to 0.0,
                    "longitude" to 0.0,
                    "lastActive" to Timestamp.now(),
                    "idCardNumber" to idCardNumber,
                    "iqamaId" to iqamaId,
                    "passportId" to passportId,
                    "phoneNumber" to phone, // matched with User.kt field name if possible, or just use key
                    "address" to address,
                    "posNo" to posNo,
                    "nationality" to nationality,
                    "routeArea" to routeArea,
                    "rank" to rank,
                    "sectionName" to sectionName,
                    "dateOfBirth" to dateOfBirth,
                    "bloodType" to bloodType,
                    "gender" to gender
                )

                // ৩. Firestore-এ সেভ করা
                db.collection("Users").document(uid)
                    .set(data)
                    .addOnSuccessListener {
                        Log.d("AUTH", "Staff Account Created Successfully!")
                        secondaryAuth.signOut() // সেকেন্ডারি অ্যাপ থেকে সাইন আউট করা হলো
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Firestore error")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Auth error (হয়তো ইমেইলটি আগে ব্যবহার করা হয়েছে)")
            }
    }

    fun fetchStaffUsers(adminId: String) {
        Log.d("AUTH", "Fetching staff for adminId: $adminId")
        db.collection("Users")
            .whereEqualTo("adminId", adminId)
            .whereEqualTo("role", "STAFF")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("AUTH", "Fetch error: ${error.message}")
                    return@addSnapshotListener
                }
                
                val users = value?.toObjects(User::class.java) ?: emptyList()
                Log.d("AUTH", "Fetched ${users.size} staff users")
                staffUsers.clear()
                staffUsers.addAll(users)
            }
    }

    fun toggleUserLock(uid: String, isLocked: Boolean) {
        db.collection("Users").document(uid)
            .update("isLocked", isLocked)
    }

    fun deleteUser(uid: String) {
        db.collection("Users").document(uid).delete()
    }

    fun updateStaff(
        uid: String,
        data: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        db.collection("Users").document(uid)
            .update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Update failed") }
    }
}
