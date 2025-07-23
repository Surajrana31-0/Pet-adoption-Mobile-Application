package com.example.petadoptionmanagement.repository

import android.content.Context
import android.content.SharedPreferences
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
                getUserFromDatabase(user.uid) { _, _, userModel ->
                    if (userModel != null) {
                        // Save session to SharedPreferences
                        sharedPreferences.edit {
                            putString("email", userModel.email)
                            putString("username", userModel.username)
                        }
                        // Notify all registered observers
                        notifyAuthStateChanged(true, userModel)
                    } else {
                        // Handle case where user exists in Auth but not in DB
                        notifyAuthStateChanged(true, UserModel(userId = user.uid, email = user.email ?: "", username = "Unknown"))
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
                            val newUserModel = userModel.copy(userId = user.uid, email = user.email ?: userModel.email)
                            addUserToDatabase(user.uid, newUserModel) { success, message ->
                                if (success) {
                                    callback(true, "Account created successfully!", newUserModel)
                                } else {
                                    user.delete() // Clean up auth user if DB write fails
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
                                if (success) {
                                    callback(true, "Sign in successful!", userModel)
                                } else {
                                    // Signed in, but couldn't get profile.
                                    // Log out to avoid inconsistent state, or handle as needed.
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
            // The AuthStateListener will handle the rest.
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
                    // Ensure consistency with Auth data
                    val consistentModel = userModel?.copy(userId = userId, email = auth.currentUser?.email ?: userModel.email)
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

    // Helper function to notify all registered listeners
    private fun notifyAuthStateChanged(isLoggedIn: Boolean, user: UserModel?) {
        // Use a copy to avoid ConcurrentModificationException if a listener removes itself
        authStateChangeListeners.toList().forEach { it(isLoggedIn, user) }
    }
}