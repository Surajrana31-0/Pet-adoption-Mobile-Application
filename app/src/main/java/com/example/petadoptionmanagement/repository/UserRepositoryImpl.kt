package com.example.petadoptionmanagement.repository

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.petadoptionmanagement.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepositoryImpl(private val context: Context) : UserRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dd9sooenk",
            "api_key" to "281858352367463",
            "api_secret" to "dj8vgOz6YCPGqqvQIGEa-dhQ0Ig"
        )
    )

    override suspend fun createUserInAuth(email: String, password: String): FirebaseUser? {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return authResult.user
    }

    override suspend fun saveUserDetails(userId: String, userModel: UserModel) {
        usersCollection.document(userId).set(userModel).await()
    }

    override fun signIn(email: String, password: String, callback: (Boolean, String, UserModel?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    getUserFromDatabase(user.uid) { success, msg, userModel ->
                        callback(success, msg, userModel)
                    }
                } else {
                    callback(false, "No user found after auth.", null)
                }
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to sign in", null)
            }
    }

    override fun signOut(callback: (Boolean, String) -> Unit) {
        try {
            firebaseAuth.signOut()
            callback(true, "Signed out successfully.")
        } catch (e: Exception) {
            callback(false, e.message ?: "Sign out failed.")
        }
    }

    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener { callback(true, "Password reset email sent.") }
            .addOnFailureListener { e -> callback(false, e.message ?: "Failed to send reset email.") }
    }

    override fun getCurrentFirebaseUser(): FirebaseUser? = firebaseAuth.currentUser

    override fun getCurrentUserModel(callback: (UserModel?) -> Unit) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            callback(null)
            return
        }
        usersCollection.document(user.uid).get()
            .addOnSuccessListener { doc ->
                val model = doc.toObject(UserModel::class.java)
                callback(model)
            }
            .addOnFailureListener { callback(null) }
    }

    override fun getUserFromDatabase(userId: String, callback: (Boolean, String, UserModel?) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = doc.toObject(UserModel::class.java)
                    callback(true, "User fetched", user)
                } else {
                    callback(false, "User not found.", null)
                }
            }
            .addOnFailureListener { e -> callback(false, e.message ?: "Failed to fetch user", null) }
    }

    override fun observeAuthState(observer: (Boolean, UserModel?) -> Unit) {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                getUserFromDatabase(user.uid) { success, _, userModel ->
                    observer(success && userModel != null, userModel)
                }
            } else {
                observer(false, null)
            }
        }
    }

    override fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit) {
        val user = firebaseAuth.currentUser
        if (user != null && user.uid == userId) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    usersCollection.document(userId).delete().await()
                    user.delete().await()
                    withContext(Dispatchers.Main) {
                        callback(true, "Account deleted successfully.")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        callback(false, e.message ?: "Failed to delete account.")
                    }
                }
            }
        } else {
            callback(false, "No user logged in or UID mismatch.")
        }
    }

    override fun editProfile(userId: String, data: Map<String, Any>, callback: (Boolean, String) -> Unit) {
        usersCollection.document(userId).update(data)
            .addOnSuccessListener { callback(true, "Profile updated successfully.") }
            .addOnFailureListener { e -> callback(false, e.message ?: "Profile update failed.") }
    }

    override fun uploadUserImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val fileName = "profile_${System.currentTimeMillis()}"
                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )
                var imageUrl = response["url"] as String?
                imageUrl = imageUrl?.replace("http://", "https://")
                withContext(Dispatchers.Main) {
                    callback(imageUrl)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(null) }
            }
        }
    }

    override fun getUsersByRole(role: String, callback: (Boolean, String, List<UserModel>) -> Unit) {
        usersCollection.whereEqualTo("role", role).get()
            .addOnSuccessListener { docs ->
                val result = docs.mapNotNull { it.toObject(UserModel::class.java) }
                callback(true, "Fetched all users with role $role", result)
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to fetch by role", emptyList())
            }
    }
}
