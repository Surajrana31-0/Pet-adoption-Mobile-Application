package com.example.petadoptionmanagement.repository

import com.example.petadoptionmanagement.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepository {

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    )

    fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    )

    fun addUserToDatabase(
        userId: String,
        user: UserModel,
        callback: (Boolean, String) -> Unit
    )

    fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    )

    fun getCurrentUser(): FirebaseUser?

    fun getUserFromDatabase(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )

    fun logout(
        callback: (Boolean, String) -> Unit
    )

    fun editProfile(
        userId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    )

    fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    )
}