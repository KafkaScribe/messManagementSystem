package com.example.messmanagementsystem.data.repository

import com.example.messmanagementsystem.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getMembersByMess(messId: String): List<User> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("messId", messId)
                .whereEqualTo("statusInMess", "approved")
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun changeManager(messId: String, currentAdminUserId: String, newAdminUserId: String): Result<Unit> {
        return try {
            // update mess adminId
            db.collection("messes").document(messId).update(mapOf("adminId" to newAdminUserId)).await()
            // elevate new admin role
            db.collection("users").document(newAdminUserId).update(mapOf("role" to "admin")).await()
            // downgrade old admin role
            db.collection("users").document(currentAdminUserId).update(mapOf("role" to "user")).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMemberByEmail(messId: String, email: String): Result<Unit> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            if (snapshot.isEmpty) {
                return Result.failure(Exception("No user found with this email"))
            }
            val userId = snapshot.documents.first().id
            val updates = mapOf(
                "messId" to messId,
                "statusInMess" to "invited",
                "role" to "user"
            )
            db.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptInvite(userId: String): Result<Unit> {
        return try {
            db.collection("users").document(userId).update("statusInMess", "approved").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineInvite(userId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "messId" to null,
                "statusInMess" to null,
                "role" to "user"
            )
            db.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeMember(userId: String): Result<Unit> {
        return declineInvite(userId)
    }

    suspend fun updateProfile(userId: String, name: String, phoneNumber: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "name" to name,
                "phoneNumber" to phoneNumber
            )
            db.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

