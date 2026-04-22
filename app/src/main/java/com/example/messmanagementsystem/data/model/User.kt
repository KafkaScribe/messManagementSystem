package com.example.messmanagementsystem.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "user", // "user" or "admin" within the system or mess
    val messId: String? = null,
    val statusInMess: String? = null // e.g. null, "pending", "approved"
)
