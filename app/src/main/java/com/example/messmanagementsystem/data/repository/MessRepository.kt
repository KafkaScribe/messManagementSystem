package com.example.messmanagementsystem.data.repository

import com.example.messmanagementsystem.data.model.Mess
import com.example.messmanagementsystem.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MessRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun createMess(name: String, location: String, userId: String): Result<String> {
        return try {
            val inviteCode = UUID.randomUUID().toString().substring(0, 6).uppercase()
            val messId = db.collection("messes").document().id
            val mess = Mess(
                id = messId,
                name = name,
                location = location,
                adminId = userId,
                inviteCode = inviteCode
            )
            
            // 1. Create the mess
            db.collection("messes").document(messId).set(mess).await()
            
            // 2. Update the user
            val userUpdates = mapOf(
                "messId" to messId,
                "statusInMess" to "approved",
                "role" to "admin"
            )
            db.collection("users").document(userId).update(userUpdates).await()
            
            Result.success(messId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinMess(inviteCode: String, userId: String): Result<String> {
        return try {
            // Find mess by invite code
            val messes = db.collection("messes")
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .await()
            
            if (messes.isEmpty) {
                return Result.failure(Exception("Invalid invite code"))
            }

            val messId = messes.documents[0].id

            // Update user to pending
            val userUpdates = mapOf(
                "messId" to messId,
                "statusInMess" to "pending",
                "role" to "user"
            )
            db.collection("users").document(userId).update(userUpdates).await()
            
            Result.success(messId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAssignedMessInfo(messId: String): Mess? {
        return try {
            val doc = db.collection("messes").document(messId).get().await()
            doc.toObject(Mess::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPendingRequests(messId: String): List<User> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("messId", messId)
                .whereEqualTo("statusInMess", "pending")
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun approveMember(userId: String): Result<Unit> {
        return try {
            val userUpdates = mapOf("statusInMess" to "approved")
            db.collection("users").document(userId).update(userUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun softDeleteMess(messId: String): Result<Unit> {
        return try {
            db.collection("messes").document(messId).update(mapOf("deleted" to true)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
