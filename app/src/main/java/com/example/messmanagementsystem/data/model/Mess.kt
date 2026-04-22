package com.example.messmanagementsystem.data.model

data class Mess(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val adminId: String = "",
    val inviteCode: String = "",
    val deleted: Boolean = false
)
