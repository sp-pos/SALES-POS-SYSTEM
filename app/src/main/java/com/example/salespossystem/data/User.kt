package com.example.salespossystem.data

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "STAFF", // "ADMIN" অথবা "STAFF"
    val adminId: String = "",   // অ্যাডমিনের নিজের আইডি অথবা স্টাফের মালিকের আইডি
    val isLocked: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val shopName: String = "",
    val address: String = "",
    val lastActive: Timestamp? = null,
    
    // New fields
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
