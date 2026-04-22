package com.example.messmanagementsystem.data.repository

import com.example.messmanagementsystem.data.model.Expense
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun addExpense(expense: Expense): Result<Unit> {
        return try {
            val docRef = db.collection("expenses").document()
            val newExpense = expense.copy(id = docRef.id)
            docRef.set(newExpense).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // In a real app we'd use Flow for realtime updates, but doing a suspend function here as base.
    suspend fun getTotalExpenseForMess(messId: String): Double {
        return try {
            val expenses = db.collection("expenses")
                .whereEqualTo("messId", messId)
                .get()
                .await()
            
            var total = 0.0
            for (doc in expenses.documents) {
                val expense = doc.toObject(Expense::class.java)
                if (expense != null) {
                    total += expense.amount
                }
            }
            total
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getTotalExpenseForMessByMonth(messId: String, monthId: String): Double {
        return try {
            val expenses = db.collection("expenses")
                .whereEqualTo("messId", messId)
                .get()
                .await()
            expenses.documents.fold(0.0) { acc, doc ->
                val amount = (doc.getDouble("amount") ?: 0.0)
                val ts = (doc.getLong("date") ?: 0L)
                if (isSameMonth(ts, monthId)) acc + amount else acc
            }
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getExpensesForMessByMonth(messId: String, monthId: String? = null): List<Expense> {
        return try {
            val query = db.collection("expenses").whereEqualTo("messId", messId)
            val snapshot = query.get().await()
            val allExpenses = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
            if (monthId == null) {
                allExpenses
            } else {
                allExpenses.filter { isSameMonth(it.date, monthId) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun isSameMonth(timestamp: Long, monthId: String): Boolean {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        val y = cal.get(java.util.Calendar.YEAR)
        val m = cal.get(java.util.Calendar.MONTH) + 1
        val current = "%04d-%02d".format(y, m)
        return current == monthId
    }
}
