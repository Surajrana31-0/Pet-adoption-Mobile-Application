// /repository/UserRepository.kt

package com.example.petadoptionmanagement.repository

import android.net.Uri
import com.example.petadoptionmanagement.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

interface UserRepository {

    fun createUser(userModel: UserModel, password: String, onResult: (Result<FirebaseUser>) -> Unit)

    fun signIn(email: String, password: String, onResult: (Result<UserModel>) -> Unit)

    fun signOut(onResult: (Result<Unit>) -> Unit)

    fun forgetPassword(email: String, onResult: (Result<Unit>) -> Unit)

    fun getCurrentFirebaseUser(): FirebaseUser?

    fun observeAuthState(onResult: (Result<UserModel?>) -> Unit): FirebaseAuth.AuthStateListener

    fun getUserModel(userId: String, onResult: (Result<UserModel?>) -> Unit)

    fun deleteAccount(onResult: (Result<Unit>) -> Unit)

    fun editProfile(userId: String, data: Map<String, Any>, onResult: (Result<Unit>) -> Unit)

    fun uploadProfileImage(imageUri: Uri, onResult: (Result<String>) -> Unit)

    fun getUsersByRole(role: String, onResult: (Result<List<UserModel>>) -> Unit)
}
