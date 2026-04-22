package com.example.messmanagementsystem.data.model

data class MealSelection(
    val dateId: String = "", // Format: "yyyy-MM-dd"
    val userId: String = "",
    val messId: String = "",
    val hasBreakfast: Boolean = false,
    val hasLunch: Boolean = false,
    val hasDinner: Boolean = false
)
