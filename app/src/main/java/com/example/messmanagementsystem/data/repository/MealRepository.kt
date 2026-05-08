package com.example.messmanagementsystem.data.repository

import com.example.messmanagementsystem.data.model.MealSelection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MealRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Returns a list of MealSelections for the upcoming 'daysToLoad'
    suspend fun getUpcomingMeals(daysToLoad: Int = 7): List<MealSelection> {
        val user = auth.currentUser ?: return emptyList()
        val userId = user.uid
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now()
        
        val mealSelections = mutableListOf<MealSelection>()

        for (i in 0 until daysToLoad) {
            val date = today.plusDays(i.toLong())
            val dateId = date.format(formatter)
            
            try {
                val docSnapshot = db.collection("users")
                    .document(userId)
                    .collection("meals")
                    .document(dateId)
                    .get()
                    .await()

                if (docSnapshot.exists()) {
                    val selection = docSnapshot.toObject(MealSelection::class.java)
                    if (selection != null) {
                        mealSelections.add(selection)
                        continue
                    }
                }
                
                // If it doesn't exist, create a default one
                mealSelections.add(MealSelection(dateId = dateId, userId = userId))

            } catch (e: Exception) {
                // If fetch fails, provide a default object so UI doesn't crash
                mealSelections.add(MealSelection(dateId = dateId, userId = userId))
            }
        }
        
        return mealSelections
    }

    suspend fun updateMealSelection(selection: MealSelection): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
        
        return try {
            db.collection("users")
                .document(user.uid)
                .collection("meals")
                .document(selection.dateId)
                .set(selection)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setMealForUser(userId: String, selection: MealSelection): Result<Unit> {
        return try {
            db.collection("users")
                .document(userId)
                .collection("meals")
                .document(selection.dateId)
                .set(selection)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMealsForMessByMonth(userIds: List<String>, monthId: String): List<MealSelection> {
        val result = mutableListOf<MealSelection>()
        for (userId in userIds) {
            try {
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("meals")
                    .get()
                    .await()
                
                val userMeals = snapshot.documents.mapNotNull { it.toObject(MealSelection::class.java) }
                    .filter { it.dateId.startsWith(monthId) }
                
                result.addAll(userMeals)
            } catch (e: Exception) {
                // ignore
            }
        }
        return result
    }
}
