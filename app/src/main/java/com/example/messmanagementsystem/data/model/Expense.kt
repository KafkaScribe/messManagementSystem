package com.example.messmanagementsystem.data.model

data class Expense(
    val id: String = "",
    val messId: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val date: Long = 0L, // Timestamp of entry creation
    val addedBy: String = "", // User ID of who added the expense
    val expenseCategory: String = "meal_cost", // "meal_cost" or "other_cost"
    val expenseDate: Long = 0L, // Timestamp associated with the actual cost
    val shoppers: List<String> = emptyList(), // list of user IDs
    val otherCostType: String = "shared", // "shared" or "individual"
    val targetUsers: List<String> = emptyList() // list of user IDs for individual or specific shared
)
