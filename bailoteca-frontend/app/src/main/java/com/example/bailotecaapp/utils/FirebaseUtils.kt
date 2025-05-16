package com.example.bailotecaapp.utils

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object FirebaseUtils {
    suspend fun getJwtToken(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.getIdToken(true)?.await()?.token
    }
}