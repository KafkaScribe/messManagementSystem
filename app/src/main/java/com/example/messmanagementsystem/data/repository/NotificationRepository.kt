package com.example.messmanagementsystem.data.repository

import com.example.messmanagementsystem.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getNotificationsForUser(userId: String): List<Notification> {
        return try {
            val snapshot = db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Notification::class.java)?.copy(id = it.id) }
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendNotificationToUsers(userIds: List<String>, messId: String, title: String, message: String) {
        try {
            val batch = db.batch()
            userIds.forEach { userId ->
                val docRef = db.collection("notifications").document()
                val notification = Notification(
                    id = docRef.id,
                    messId = messId,
                    userId = userId,
                    title = title,
                    message = message
                )
                batch.set(docRef, notification)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            db.collection("notifications").document(notificationId).update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
