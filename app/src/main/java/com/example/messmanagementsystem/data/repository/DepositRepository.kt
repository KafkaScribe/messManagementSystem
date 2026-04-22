package com.example.messmanagementsystem.data.repository

import com.example.messmanagementsystem.data.model.Deposit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DepositRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun addDeposit(deposit: Deposit): Result<Unit> {
        return try {
            val docRef = db.collection("deposits").document()
            val withId = deposit.copy(id = docRef.id)
            docRef.set(withId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTotalDepositForMess(messId: String, monthId: String? = null): Double {
        return try {
            var query = db.collection("deposits")
                .whereEqualTo("messId", messId)
            val snapshot = query.get().await()
            snapshot.documents.fold(0.0) { acc, doc ->
                val amount = (doc.getDouble("amount") ?: 0.0)
                val ts = (doc.getLong("date") ?: 0L)
                if (monthId == null || isSameMonth(ts, monthId)) acc + amount else acc
            }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getDepositsForMessByMonth(messId: String, monthId: String? = null): List<Deposit> {
        return try {
            var query = db.collection("deposits").whereEqualTo("messId", messId)
            val snapshot = query.get().await()
            val allDeposits = snapshot.documents.mapNotNull { it.toObject(Deposit::class.java) }
            if (monthId == null) {
                allDeposits
            } else {
                allDeposits.filter { isSameMonth(it.date, monthId) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun isSameMonth(timestamp: Long, monthId: String): Boolean {
        // monthId format "yyyy-MM"
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        val y = cal.get(java.util.Calendar.YEAR)
        val m = cal.get(java.util.Calendar.MONTH) + 1
        val current = "%04d-%02d".format(y, m)
        return current == monthId
    }
}

