// /repository/UserRepositoryImpl.kt

package com.example.petadoptionmanagement.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.petadoptionmanagement.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import javax.inject.Inject
import kotlin.concurrent.thread

class UserRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cloudinary: Cloudinary,
    private val context: Context // Application context
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override fun createUser(userModel: UserModel, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(userModel.email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user!!
                usersCollection.document(firebaseUser.uid).set(userModel.copy(userId = firebaseUser.uid))
                    .addOnSuccessListener { onResult(Result.success(firebaseUser)) }
                    .addOnFailureListener { e -> onResult(Result.failure(e)) }
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun signIn(email: String, password: String, onResult: (Result<UserModel>) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId == null) {
                    onResult(Result.failure(IllegalStateException("User ID not found after sign-in.")))
                    return@addOnSuccessListener
                }
                getUserModel(userId) { result ->
                    result.fold(
                        onSuccess = { userModel ->
                            if (userModel != null) {
                                onResult(Result.success(userModel))
                            } else {
                                onResult(Result.failure(IllegalStateException("User model not found.")))
                            }
                        },
                        onFailure = { e -> onResult(Result.failure(e)) }
                    )
                }
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun signOut(onResult: (Result<Unit>) -> Unit) {
        try {
            firebaseAuth.signOut()
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            onResult(Result.failure(e))
        }
    }

    override fun forgetPassword(email: String, onResult: (Result<Unit>) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(e))
                Log.e("ResetPassword", "Failed to send email: ${e.message}") // <-- Add this
            }
    }


    override fun getCurrentFirebaseUser(): FirebaseUser? = firebaseAuth.currentUser

    override fun observeAuthState(onResult: (Result<UserModel?>) -> Unit): FirebaseAuth.AuthStateListener {
        val authListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                usersCollection.document(user.uid).addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onResult(Result.failure(error))
                        return@addSnapshotListener
                    }
                    val userModel = snapshot?.toObject<UserModel>()
                    onResult(Result.success(userModel))
                }
            } else {
                onResult(Result.success(null))
            }
        }
        firebaseAuth.addAuthStateListener(authListener)
        return authListener // Return the listener so it can be removed later
    }

    override fun getUserModel(userId: String, onResult: (Result<UserModel?>) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { document -> onResult(Result.success(document.toObject<UserModel>())) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun deleteAccount(onResult: (Result<Unit>) -> Unit) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            onResult(Result.failure(IllegalStateException("No user is currently signed in.")))
            return
        }
        usersCollection.document(user.uid).delete()
            .addOnSuccessListener {
                user.delete()
                    .addOnSuccessListener { onResult(Result.success(Unit)) }
                    .addOnFailureListener { e -> onResult(Result.failure(e)) }
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun editProfile(userId: String, data: Map<String, Any>, onResult: (Result<Unit>) -> Unit) {
        usersCollection.document(userId).update(data)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun uploadProfileImage(imageUri: Uri, onResult: (Result<String>) -> Unit) {
        thread { // Execute in a background thread
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val response = cloudinary.uploader().upload(
                    inputStream,
                    ObjectUtils.asMap("resource_type", "image")
                )
                val url = response["secure_url"] as? String
                    ?: throw IllegalStateException("Cloudinary URL was null.")
                onResult(Result.success(url))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    override fun getUsersByRole(role: String, onResult: (Result<List<UserModel>>) -> Unit) {
        usersCollection.whereEqualTo("role", role).get()
            .addOnSuccessListener { querySnapshot ->
                onResult(Result.success(querySnapshot.toObjects<UserModel>()))
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(e))
            }
    }
}
