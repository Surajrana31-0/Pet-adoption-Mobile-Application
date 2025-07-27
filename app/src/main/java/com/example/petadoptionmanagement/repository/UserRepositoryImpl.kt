package com.example.petadoptionmanagement.repository

import android.content.ContentValues.TAG // Consider removing if TAG constant is not actually used from here
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

// Explicitly showing the empty constructor, though it's implicit if not defined
class UserRepositoryImpl() : UserRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val tag = "UserRepositoryImpl" // Class-level constant for logging

    override suspend fun createUserInAuth(email: String, password: String): FirebaseUser? {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            authResult.user
        } catch (e: FirebaseAuthException) {
            Log.e(tag, "Error creating user in Auth: ${e.errorCode} - ${e.message}", e) // Use class-level tag
            throw e // Re-throw to be handled by ViewModel
        } catch (e: Exception) {
            Log.e(tag, "Generic error creating user in Auth: ${e.message}", e) // Use class-level tag
            throw e
        }
    }

    override fun signUp(
        email: String,
        password: String,
        userModel: UserModel,
        callback: (Boolean, String, String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firebaseUser = createUserInAuth(email, password)
                if (firebaseUser != null) {
                    val userWithUid = userModel.copy(userId = firebaseUser.uid)
                    saveUserDetails(firebaseUser.uid, userWithUid)
                    withContext(Dispatchers.Main) {
                        callback(true, "Sign up successful.", firebaseUser.uid)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(false, "Sign up failed: Could not create user in Auth.", null)
                    }
                }
            } catch (e: FirebaseAuthException) {
                withContext(Dispatchers.Main) {
                    callback(false, "Sign up failed: ${e.message}", null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, "Sign up failed: ${e.message}", null)
                }
            }
        }
    }

    override suspend fun saveUserDetails(userId: String, userModel: UserModel) {
        try {
            usersCollection.document(userId).set(userModel).await()
            Log.d(tag, "User details saved for UID: $userId") // Use class-level tag
        } catch (e: Exception) {
            Log.e(tag, "Error saving user details to Firestore for UID $userId: ${e.message}", e) // Use class-level tag
            throw e // Re-throw to be handled by ViewModel
        }
    }

    override fun signIn(email: String, password: String, callback: (Boolean, String, UserModel?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        getUserFromDatabaseInternal(firebaseUser.uid) { success, msg, userModel ->
                            if (success && userModel != null) {
                                callback(true, "Sign in successful.", userModel)
                            } else {
                                callback(false, "Authenticated, but failed to fetch user details: $msg", null)
                            }
                        }
                    } else {
                        callback(false, "Sign in failed: User data not found after auth.", null)
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Sign in failed: Unknown error."
                    Log.w(tag, "signInWithEmail:failure", task.exception) // Use class-level tag
                    callback(false, errorMsg, null)
                }
            }
    }

    override fun signOut(callback: (Boolean, String) -> Unit) {
        try {
            firebaseAuth.signOut()
            callback(true, "Signed out successfully.")
        } catch (e: Exception) {
            Log.e(tag, "Error signing out: ${e.message}", e) // Use class-level tag
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
                        callback(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Error fetching current user model: ${e.message}", e) // Use class-level tag
                    callback(null)
                }
        } else {
            callback(null)
        }
    }


    override fun getUserFromDatabase(userId: String, callback: (Boolean, String, UserModel?) -> Unit) {
        getUserFromDatabaseInternal(userId, callback)
    }

    // Note: I noticed you have getCurrentFirebaseUser() and getCurrentUser() doing the same thing.
    // This is fine, just be aware. Let's keep getCurrentUser() as it's defined in your UserRepository interface (if it is).
    // If getCurrentUser() is not in your interface, you might want to remove it or ensure it's used appropriately.
    // For now, I'll assume it's part of the interface or intentionally duplicated.
    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
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
                Log.e(tag, "Error fetching user from database $userId: ${e.message}", e) // Use class-level tag
                callback(false, "Error fetching user data: ${e.message}", null)
            }
    }

    override fun observeAuthState(observer: (Boolean, UserModel?) -> Unit) {
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                usersCollection.document(firebaseUser.uid).get()
                    .addOnSuccessListener { documentSnapshot ->
                        val userModel = documentSnapshot.toObject(UserModel::class.java)
                        observer(true, userModel)
                    }
                    .addOnFailureListener { e ->
                        Log.e(tag, "observeAuthState: Error fetching user details for ${firebaseUser.uid}: ${e.message}", e) // Use class-level tag
                        observer(true, null)
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
                    Log.d(tag, "User data deleted from Firestore for UID: $userId") // Use class-level tag

                    user.delete().await()
                    Log.d(tag, "User account deleted from Firebase Auth for UID: $userId") // Use class-level tag

                    withContext(Dispatchers.Main) {
                        callback(true, "Account deleted successfully.")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error deleting account for UID $userId: ${e.message}", e) // Use class-level tag
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
                Log.e(tag, "Error updating profile for $userId: ${e.message}", e) // Use class-level tag
                callback(false, "Profile update failed: ${e.message}")
            }
    }

    override fun addUserToDatabase(
        userID: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        usersCollection.document(userID).set(model)
            .addOnSuccessListener {
                Log.d(tag, "User $userID added to database successfully.") // Use class-level tag
                callback(true, "User added to database successfully.")
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error adding user $userID to database: ${e.message}", e) // Use class-level tag
                callback(false, "Failed to add user to database: ${e.message}")
            }
    }

    override fun getUserByID(
        userID: String,
        callback: (UserModel?, Boolean, String) -> Unit
    ) {
        usersCollection.document(userID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userModel = documentSnapshot.toObject(UserModel::class.java)
                    if (userModel != null) {
                        callback(userModel, true, "User data fetched successfully.")
                    } else {
                        callback(null, false, "Failed to parse user data.")
                    }
                } else {
                    callback(null, false, "User not found in database.")
                }
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error fetching user $userID: ${e.message}", e) // Use class-level tag
                callback(null, false, "Error fetching user data: ${e.message}")
            }
    }
}