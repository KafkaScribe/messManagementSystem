package com.example.messmanagementsystem.data.model

data class Deposit(
    val id: String = "",
    val messId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val note: String = "",
    val date: Long = 0L
)

