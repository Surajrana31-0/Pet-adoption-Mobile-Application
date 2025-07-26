package com.example.petadoptionmanagement.repository

import android.util.Log
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

class UserRepositoryImpl : UserRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val TAG = "UserRepositoryImpl"

    override suspend fun createUserInAuth(email: String, password: String): FirebaseUser? {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            authResult.user
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Error creating user in Auth: ${e.errorCode} - ${e.message}", e)
            throw e // Re-throw to be handled by ViewModel
        } catch (e: Exception) {
            Log.e(TAG, "Generic error creating user in Auth: ${e.message}", e)
            throw e
        }
    }

    override suspend fun saveUserDetails(userId: String, userModel: UserModel) {
        try {
            usersCollection.document(userId).set(userModel).await()
            Log.d(TAG, "User details saved for UID: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user details to Firestore for UID $userId: ${e.message}", e)
            throw e // Re-throw to be handled by ViewModel
        }
    }

    override fun signIn(email: String, password: String, callback: (Boolean, String, UserModel?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        // Fetch UserModel from Firestore
                        getUserFromDatabaseInternal(firebaseUser.uid) { success, msg, userModel ->
                            if (success && userModel != null) {
                                callback(true, "Sign in successful.", userModel)
                            } else {
                                // User authenticated but details fetch failed.
                                // You might still consider this a partial success for auth.
                                callback(false, "Authenticated, but failed to fetch user details: $msg", null)
                            }
                        }
                    } else {
                        callback(false, "Sign in failed: User data not found after auth.", null)
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Sign in failed: Unknown error."
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    callback(false, errorMsg, null)
                }
            }
    }

    override fun signOut(callback: (Boolean, String) -> Unit) {
        try {
            firebaseAuth.signOut()
            callback(true, "Signed out successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out: ${e.message}", e)
            callback(false, "Sign out failed: ${e.message}")
        }
    }

    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Password reset email sent.")
                } else {
                    val errorMsg = task.exception?.message ?: "Failed to send reset email."
                    callback(false, errorMsg)
                }
            }
    }

    override fun getCurrentFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun getCurrentUserModel(callback: (UserModel?) -> Unit) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            usersCollection.document(firebaseUser.uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userModel = documentSnapshot.toObject(UserModel::class.java)
                        callback(userModel)
                    } else {
                        callback(null) // User exists in Auth but not in Firestore users collection
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching current user model: ${e.message}", e)
                    callback(null)
                }
        } else {
            callback(null) // No user logged in
        }
    }


    override fun getUserFromDatabase(userId: String, callback: (Boolean, String, UserModel?) -> Unit) {
        getUserFromDatabaseInternal(userId, callback)
    }

    private fun getUserFromDatabaseInternal(userId: String, callback: (Boolean, String, UserModel?) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userModel = documentSnapshot.toObject(UserModel::class.java)
                    if (userModel != null) {
                        callback(true, "User data fetched.", userModel)
                    } else {
                        callback(false, "Failed to parse user data.", null)
                    }
                } else {
                    callback(false, "User not found in database.", null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching user from database $userId: ${e.message}", e)
                callback(false, "Error fetching user data: ${e.message}", null)
            }
    }

    override fun observeAuthState(observer: (Boolean, UserModel?) -> Unit) {
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // User is signed in, fetch their UserModel from Firestore
                usersCollection.document(firebaseUser.uid).get()
                    .addOnSuccessListener { documentSnapshot ->
                        val userModel = documentSnapshot.toObject(UserModel::class.java)
                        observer(true, userModel) // userModel can be null if not found in Firestore
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "observeAuthState: Error fetching user details for ${firebaseUser.uid}: ${e.message}", e)
                        observer(true, null) // Still logged in, but details fetch failed
                    }
            } else {
                // User is signed out
                observer(false, null)
            }
        }
    }

    override fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit) {
        val user = firebaseAuth.currentUser
        if (user != null && user.uid == userId) {
            // CoroutineScope to handle async operations sequentially
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Step 1: Delete from Firestore
                    usersCollection.document(userId).delete().await()
                    Log.d(TAG, "User data deleted from Firestore for UID: $userId")

                    // Step 2: Delete from Firebase Auth
                    user.delete().await()
                    Log.d(TAG, "User account deleted from Firebase Auth for UID: $userId")

                    withContext(Dispatchers.Main) {
                        callback(true, "Account deleted successfully.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting account for UID $userId: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        callback(false, "Failed to delete account: ${e.message}")
                    }
                }
            }
        } else {
            callback(false, "No user logged in or UID mismatch.")
        }
    }

    override fun editProfile(userId: String, data: Map<String, Any?>, callback: (Boolean, String) -> Unit) {
        usersCollection.document(userId).update(data)
            .addOnSuccessListener {
                callback(true, "Profile updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating profile for $userId: ${e.message}", e)
                callback(false, "Profile update failed: ${e.message}")
            }
    }
}