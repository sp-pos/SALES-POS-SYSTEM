package com.example.salespossystem.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AndroidAuthRepository : AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override suspend fun registerAdmin(
        name: String,
        email: String,
        pass: String,
        shopName: String
    ): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Auth failed")
            
            val user = User(
                uid = uid,
                name = name,
                email = email,
                role = "ADMIN",
                adminId = uid,
                shopName = shopName,
                lastActiveMillis = System.currentTimeMillis()
            )

            db.collection("Admins").document(uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginUser(email: String, pass: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Login failed")

            // Check Admins first
            var doc = db.collection("Admins").document(uid).get().await()
            if (!doc.exists()) {
                // Check Users
                doc = db.collection("Users").document(uid).get().await()
            }

            if (doc.exists()) {
                val user = doc.toObject(User::class.java) ?: throw Exception("User parsing failed")
                if (user.isLocked) throw Exception("আপনার অ্যাকাউন্টটি লক করা হয়েছে!")
                Result.success(user)
            } else {
                Result.failure(Exception("ইউজার ডাটা পাওয়া যায়নি!"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        // This would need a more robust implementation to convert FirebaseUser to our User model
        return null 
    }

    override fun logout() {
        auth.signOut()
    }
}
