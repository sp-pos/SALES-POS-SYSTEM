package com.example.salespossystem.data

interface AuthRepository {
    suspend fun registerAdmin(
        name: String,
        email: String,
        pass: String,
        shopName: String
    ): Result<User>

    suspend fun loginUser(
        email: String,
        pass: String
    ): Result<User>

    fun getCurrentUser(): User?
    fun logout()
}
