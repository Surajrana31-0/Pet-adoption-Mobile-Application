package com.example.petadoptionmanagement.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.petadoptionmanagement.model.UserModel
import com.google.firebase.auth.FirebaseAuth
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
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    // A list of callbacks to notify when authentication state changes
    private val authStateChangeListeners: MutableList<(Boolean, UserModel?) -> Unit> = mutableListOf()

    init {
        // Attach a listener to Firebase Auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in, fetch their profile from Realtime Database
                usersRef.child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Ensure userId and email are explicitly set from FirebaseUser to avoid nulls/inconsistencies
                        val userModel = snapshot.getValue(UserModel::class.java)?.copy(userId = user.uid, email = user.email ?: "")
                        // Save session to SharedPreferences
                        sharedPreferences.edit()
                            .putString("email", user.email)
                            .putString("username", userModel?.username) // Make sure UserModel has a username field
                            .apply()
                        // Notify all registered observers
                        notifyAuthStateChanged(true, userModel)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error, notify observers but provide a basic UserModel from Firebase Auth
                        notifyAuthStateChanged(true, UserModel(userId = user.uid, email = user.email ?: "", username = "Unknown"))
                    }
                })
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
        userModel: UserModel, // userModel should contain the username and email
        password: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(userModel.email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val firebaseUser = authTask.result?.user
                    firebaseUser?.let { user ->
                        // Create a new UserModel with the Firebase UID and email, ensuring consistency
                        val newUserModel = userModel.copy(userId = user.uid, email = user.email ?: userModel.email)
                        // Store the UserModel (username, email, etc.) in Realtime Database under the user's UID
                        usersRef.child(user.uid).setValue(newUserModel)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    // Save session data to SharedPreferences (consistent with sign-in)
                                    sharedPreferences.edit()
                                        .putString("email", user.email)
                                        .putString("username", newUserModel.username) // Ensure username is saved
                                        .apply()
                                    callback(true, "Account created successfully!", newUserModel)
                                } else {
                                    // If database write fails, delete auth user to prevent orphaned accounts
                                    user.delete() // Delete the user from Firebase Auth
                                    callback(false, "Failed to save user profile: ${dbTask.exception?.message}", null)
                                }
                            }
                    } ?: callback(false, "Firebase user is null after registration.", null)
                } else {
                    callback(false, "Sign up failed: ${authTask.exception?.message}", null)
                }
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
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { user ->
                        // Fetch user data from Realtime Database
                        usersRef.child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // Ensure userId and email are explicitly set from FirebaseUser
                                val userModel = snapshot.getValue(UserModel::class.java)?.copy(userId = user.uid, email = user.email ?: "")
                                // Save session data to SharedPreferences
                                sharedPreferences.edit()
                                    .putString("email", user.email)
                                    .putString("username", userModel?.username) // Save username to prefs
                                    .apply()
                                callback(true, "Sign in successful!", userModel)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(false, "Sign in successful but failed to load profile: ${error.message}", null)
                            }
                        })
                    } ?: callback(false, "User not found after sign in.", null)
                } else {
                    callback(false, "Sign in failed: ${task.exception?.message}", null)
                }
            }
    }

    /**
     * Signs out the current user from Firebase Authentication and clears local session data.
     */
    override fun signOut(callback: (Boolean, String) -> Unit) {
        auth.signOut()
        // AuthStateListener will automatically clear SharedPreferences, but explicit clear is safe
        sharedPreferences.edit().clear().apply()
        callback(true, "Signed out successfully.")
    }

    /**
     * Retrieves the currently logged-in user's data.
     * This method prioritizes Firebase current user. If no Firebase user, it returns null.
     * Local SharedPreferences are managed by the AuthStateListener and should not determine active auth state.
     */
    override fun getCurrentUser(callback: (UserModel?) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            // User is signed in, fetch their profile from Realtime Database
            usersRef.child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userModel = snapshot.getValue(UserModel::class.java)?.copy(userId = firebaseUser.uid, email = firebaseUser.email ?: "")
                    callback(userModel)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Fallback to a basic UserModel using Firebase Auth data if RTDB fetch fails
                    callback(UserModel(userId = firebaseUser.uid, email = firebaseUser.email ?: "", username = "Unknown"))
                }
            })
        } else {
            // IMPORTANT CHANGE: If no active Firebase user, return null.
            // The AuthStateListener already handles clearing prefs on sign out.
            // We rely on auth.currentUser for active login status.
            callback(null)
        }
    }

    /**
     * Provides a mechanism to observe real-time changes in the user's authentication state.
     * Callbacks are notified when auth state changes.
     */
    override fun observeAuthState(callback: (Boolean, UserModel?) -> Unit) {
        // Add the callback to our list of listeners
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