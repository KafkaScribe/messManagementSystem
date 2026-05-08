package com.example.messmanagementsystem.data.repository

import com.example.messmanagementsystem.data.model.MonthInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MonthRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun startNewMonth(messId: String, name: String = ""): Result<String> {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
            val id = LocalDate.now().format(formatter)
            val ref = db.collection("messes").document(messId)
                .collection("months").document(id)
            val info = MonthInfo(id = id, messId = messId, name = name, createdAt = System.currentTimeMillis())
            ref.set(info).await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

