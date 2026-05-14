package com.example.messmanagementsystem.data.model

data class Notification(
    val id: String = "",
    val messId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
