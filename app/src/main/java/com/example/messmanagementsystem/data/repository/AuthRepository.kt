package com.example.messmanagementsystem.data.repository

import com.example.messmanagementsystem.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String, phoneNumber: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User creation failed")
            
            // Save user role and data
            val user = User(uid = uid, name = name, email = email, phoneNumber = phoneNumber, role = "user")
            db.collection("users").document(uid).set(user).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getCurrentUserDetails(): Result<User?> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("No user logged in"))
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val user = doc.toObject(User::class.java)
                Result.success(user)
            } else {
                val email = auth.currentUser?.email ?: ""
                val newUser = User(uid = uid, email = email, name = email.substringBefore("@"))
                db.collection("users").document(uid).set(newUser).await()
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
