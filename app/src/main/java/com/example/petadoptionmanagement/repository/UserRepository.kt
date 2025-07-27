package com.example.petadoptionmanagement.repository

import android.content.Context
import android.net.Uri
import com.example.petadoptionmanagement.model.UserModel
import com.google.firebase.auth.FirebaseUser

interface UserRepository {
    suspend fun createUserInAuth(email: String, password: String): FirebaseUser?
    suspend fun saveUserDetails(userId: String, userModel: UserModel)

    fun signIn(email: String, password: String, callback: (Boolean, String, UserModel?) -> Unit)
    fun signOut(callback: (Boolean, String) -> Unit)
    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit)

    fun getCurrentFirebaseUser(): FirebaseUser?
    fun getCurrentUserModel(callback: (UserModel?) -> Unit)
    fun getUserFromDatabase(userId: String, callback: (Boolean, String, UserModel?) -> Unit)
    fun observeAuthState(observer: (Boolean, UserModel?) -> Unit)
    fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit)
    fun editProfile(userId: String, data: Map<String, Any>, callback: (Boolean, String) -> Unit)

    // New: image uploading for user profile
    fun uploadUserImage(context: Context, imageUri: Uri, callback: (String?) -> Unit)

    // New: role-based queries (advanced use)
    fun getUsersByRole(role: String, callback: (Boolean, String, List<UserModel>) -> Unit)
}
