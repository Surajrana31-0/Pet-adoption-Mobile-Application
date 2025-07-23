package com.example.petadoptionmanagement.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.petadoptionmanagement.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Concrete implementation of UserRepository using Firebase Authentication and Realtime Database.
 * This class handles direct interactions with Firebase for user authentication and data storage,
 * and also manages local session persistence using SharedPreferences.
 */
class UserRepositoryImpl(private val context: Context) : UserRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef = database.reference.child("users") // Reference to store user profiles

    // Local session persistence
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    // A list of callbacks to notify when authentication state changes
    private val authStateChangeListeners: MutableList<(Boolean, UserModel?) -> Unit> =
        mutableListOf()

    init {
        // Attach a listener to Firebase Auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in, fetch their profile from Realtime Database
                getUserFromDatabase(user.uid) { success, _, userModel ->
                    if (success && userModel != null) {
                        // Save session to SharedPreferences
                        sharedPreferences.edit {
                            putString("email", userModel.email)
                            putString("username", userModel.username)
                            putString("userId", userModel.userId) // Also save userId for easier access
                        }
                        // Notify all registered observers
                        notifyAuthStateChanged(true, userModel)
                    } else {
                        // Handle case where user exists in Auth but not in DB
                        // Or if there was an error fetching from DB.
                        // Create a basic UserModel from FirebaseUser, but indicate potentially incomplete data
                        val tempUserModel = UserModel(
                            userId = user.uid,
                            email = user.email ?: "",
                            username = sharedPreferences.getString("username", "Unknown") ?: "Unknown" // Try to get username from SharedPreferences
                        )
                        notifyAuthStateChanged(true, tempUserModel)
                        Log.w("UserRepositoryImpl", "User data not fully retrieved from DB for ${user.uid}")
                    }
                }
            } else {
                // User is signed out, clear local session
                sharedPreferences.edit().clear().apply()
                // Notify all registered observers
                notifyAuthStateChanged(false, null)
            }
        }
    }

    /**
     * Registers a new user with Firebase Authentication and saves their username to Realtime Database.
     */
    override fun signUp(
        userModel: UserModel,
        password: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        try {
            // Synchronous validation before making the network call
            if (userModel.email.isBlank() || password.isBlank()) {
                throw IllegalArgumentException("Email and password cannot be empty.")
            }

            auth.createUserWithEmailAndPassword(userModel.email, password)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val firebaseUser = authTask.result?.user
                        firebaseUser?.let { user ->
                            // Ensure the UserModel has the correct UID from Firebase Auth
                            val newUserModel = userModel.copy(userId = user.uid, email = user.email ?: userModel.email)
                            addUserToDatabase(user.uid, newUserModel) { success, message ->
                                if (success) {
                                    callback(true, "Account created successfully!", newUserModel)
                                } else {
                                    // If DB write fails, delete the Auth user to prevent orphaned accounts
                                    user.delete()
                                        .addOnCompleteListener { deleteTask ->
                                            if (deleteTask.isSuccessful) {
                                                Log.e("UserRepositoryImpl", "Auth user deleted due to DB write failure for ${user.uid}")
                                            } else {
                                                Log.e("UserRepositoryImpl", "Failed to delete Auth user after DB write failure: ${deleteTask.exception?.message}")
                                            }
                                        }
                                    callback(false, message, null)
                                }
                            }
                        } ?: callback(false, "Firebase user is null after registration.", null)
                    } else {
                        callback(false, "Sign up failed: ${authTask.exception?.message}", null)
                    }
                }
        } catch (e: IllegalArgumentException) {
            callback(false, e.message ?: "Invalid input.", null)
        } catch (e: Exception) {
            callback(false, "An unexpected local error occurred: ${e.message}", null)
        }
    }

    /**
     * Signs in an existing user using Firebase Authentication.
     */
    override fun signIn(
        email: String,
        password: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        try {
            // Synchronous validation
            if (email.isBlank() || password.isBlank()) {
                throw IllegalArgumentException("Email and password cannot be empty.")
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let { user ->
                            getUserFromDatabase(user.uid) { success, message, userModel ->
                                if (success && userModel != null) {
                                    callback(true, "Sign in successful!", userModel)
                                } else {
                                    // Signed in, but couldn't get profile from DB.
                                    // This is a tricky state. We'll sign out to maintain consistency.
                                    auth.signOut()
                                    callback(false, message, null)
                                }
                            }
                        } ?: callback(false, "User not found after sign in.", null)
                    } else {
                        callback(false, "Sign in failed: ${task.exception?.message}", null)
                    }
                }
        } catch (e: IllegalArgumentException) {
            callback(false, e.message ?: "Invalid input.", null)
        } catch (e: Exception) {
            callback(false, "An unexpected local error occurred: ${e.message}", null)
        }
    }

    /**
     * Signs out the current user. The AuthStateListener handles clearing local data.
     */
    override fun signOut(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            // The AuthStateListener will handle the rest (clearing shared prefs and notifying observers).
            callback(true, "Sign out initiated successfully.")
        } catch (e: Exception) {
            callback(false, "An error occurred during sign out: ${e.message}")
        }
    }

    /**
     * Sends a password reset email to the specified email address.
     */
    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        try {
            if (email.isBlank()) {
                throw IllegalArgumentException("Email cannot be empty.")
            }
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, "Password reset email sent successfully.")
                    } else {
                        callback(false, "Failed to send reset email: ${task.exception?.message}")
                    }
                }
        } catch (e: IllegalArgumentException) {
            callback(false, e.message ?: "Invalid input.")
        } catch (e: Exception) {
            callback(false, "An unexpected local error occurred: ${e.message}")
        }
    }

    /**
     * Adds a user model to the Realtime Database.
     */
    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        usersRef.child(userId).setValue(model)
            .addOnSuccessListener {
                callback(true, "User data saved successfully.")
            }
            .addOnFailureListener { exception ->
                callback(false, "Failed to save user data: ${exception.message}")
            }
    }

    /**
     * Retrieves a user model from the Realtime Database by user ID.
     */
    override fun getUserFromDatabase(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserModel::class.java)
                    // Ensure consistency with Auth data (especially email, if it changed via Auth)
                    val consistentModel = userModel?.copy(
                        userId = userId,
                        email = auth.currentUser?.email ?: userModel.email // Prioritize current FirebaseUser email
                    )
                    callback(true, "User found.", consistentModel)
                } else {
                    callback(false, "User profile not found in database.", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}", null)
            }
        })
    }

    /**
     * Retrieves the currently logged-in user's data from Realtime Database.
     */
    override fun getCurrentUser(callback: (UserModel?) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            getUserFromDatabase(firebaseUser.uid) { _, _, userModel ->
                callback(userModel)
            }
        } else {
            callback(null)
        }
    }

    /**
     * Retrieves the current FirebaseUser object synchronously.
     */
    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Provides a mechanism to observe real-time changes in the user's authentication state.
     */
    override fun observeAuthState(callback: (Boolean, UserModel?) -> Unit) {
        authStateChangeListeners.add(callback)
        // Immediately notify the new listener with the current state
        getCurrentUser { user ->
            callback(user != null, user)
        }
    }

    /**
     * Deletes the user's account from Firebase Authentication and their data from Realtime Database.
     */
    override fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit) {
        val firebaseUser = auth.currentUser

        if (firebaseUser == null || firebaseUser.uid != userId) {
            callback(false, "No authenticated user or mismatched user ID.")
            return
        }

        // 1. Delete user data from Realtime Database
        usersRef.child(userId).removeValue()
            .addOnSuccessListener {
                Log.d("UserRepositoryImpl", "User data removed from Realtime Database for $userId")
                // 2. Delete user from Firebase Authentication
                firebaseUser.delete()
                    .addOnSuccessListener {
                        Log.d("UserRepositoryImpl", "User deleted from Firebase Auth for $userId")
                        callback(true, "Account deleted successfully.")
                    }
                    .addOnFailureListener { authException ->
                        Log.e("UserRepositoryImpl", "Failed to delete user from Firebase Auth for $userId: ${authException.message}")
                        // Consider what to do here: maybe try to re-add DB data or log extensively
                        callback(false, "Failed to delete account from authentication: ${authException.message}")
                    }
            }
            .addOnFailureListener { dbException ->
                Log.e("UserRepositoryImpl", "Failed to delete user data from Realtime Database for $userId: ${dbException.message}")
                callback(false, "Failed to delete user data: ${dbException.message}")
            }
    }

    /**
     * Updates specific fields of a user's profile in the Realtime Database.
     */
    override fun editProfile(
        userId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        if (userId.isBlank()) {
            callback(false, "User ID cannot be empty for profile edit.")
            return
        }
        if (data.isEmpty()) {
            callback(false, "No data provided for profile update.")
            return
        }

        usersRef.child(userId).updateChildren(data)
            .addOnSuccessListener {
                callback(true, "Profile updated successfully.")
            }
            .addOnFailureListener { exception ->
                callback(false, "Failed to update profile: ${exception.message}")
            }
    }


    // Helper function to notify all registered listeners
    private fun notifyAuthStateChanged(isLoggedIn: Boolean, user: UserModel?) {
        // Use a copy to avoid ConcurrentModificationException if a listener removes itself
        authStateChangeListeners.toList().forEach { it(isLoggedIn, user) }
    }
}