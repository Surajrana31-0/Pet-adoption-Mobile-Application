package com.example.petadoptionmanagement.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.petadoptionmanagement.model.UserModel // Ensure UserModel class is correctly defined
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
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
                getUserFromDatabase(user.uid) { success, message, userModel ->
                    if (success && userModel != null) {
                        // Save session to SharedPreferences
                        sharedPreferences.edit {
                            putString("email", userModel.email)
                            putString("username", userModel.username)
                            putString("userId", userModel.userId) // Also save userId for easier access
                            // Consider saving other crucial profile data if needed by the app quickly
                        }
                        // Notify all registered observers
                        notifyAuthStateChanged(true, userModel)
                        Log.d("UserRepositoryImpl", "Auth state changed: User ${user.uid} signed in. Data from DB: $userModel") //
                    } else {
                        // Handle case where user exists in Auth but not in DB, or DB fetch failed.
                        // Create a basic UserModel from FirebaseUser, but indicate potentially incomplete data
                        // This might happen if DB write failed during signup or data was deleted from DB.
                        Log.w("UserRepositoryImpl", "Auth state changed: User ${user.uid} signed in, but data not fully retrieved from DB. Message: $message") //
                        val tempUserModel = UserModel(
                            userId = user.uid,
                            email = user.email ?: "",
                            // Try to get username from Firebase Auth profile or SharedPreferences
                            username = user.displayName ?: sharedPreferences.getString("username", "Unknown") ?: "Unknown"
                        )
                        notifyAuthStateChanged(true, tempUserModel) //
                    }
                }
            } else {
                // User is signed out, clear local session
                sharedPreferences.edit().clear().apply() //
                // Notify all registered observers
                notifyAuthStateChanged(false, null) //
                Log.d("UserRepositoryImpl", "Auth state changed: User signed out. Local session cleared.") //
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
                Log.e("UserRepositoryImpl", "Sign up failed: Email or password cannot be empty.") //
                callback(false, "Email and password cannot be empty.", null) //
                return // Exit early
            }
            if (userModel.username.isBlank()) {
                Log.e("UserRepositoryImpl", "Sign up failed: Username cannot be empty.") //
                callback(false, "Username cannot be empty.", null) //
                return // Exit early
            }

            auth.createUserWithEmailAndPassword(userModel.email, password)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val firebaseUser = authTask.result?.user
                        firebaseUser?.let { user ->
                            // 1. Update Firebase Auth profile with display name (username)
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(userModel.username)
                                .build()

                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { profileTask ->
                                    if (profileTask.isSuccessful) {
                                        Log.d("UserRepositoryImpl", "Firebase Auth profile updated with username for ${user.uid}") //

                                        // 2. Save additional user data to Realtime Database
                                        // Ensure the UserModel has the correct UID from Firebase Auth
                                        val newUserModel = userModel.copy(
                                            userId = user.uid,
                                            email = user.email ?: userModel.email,
                                            username = user.displayName ?: userModel.username // Prioritize display name from Auth if set
                                        )

                                        addUserToDatabase(user.uid, newUserModel) { success, message ->
                                            if (success) {
                                                Log.d("UserRepositoryImpl", "User data saved to Realtime Database for ${user.uid}") //
                                                callback(true, "Account created successfully!", newUserModel) //
                                            } else {
                                                Log.e("UserRepositoryImpl", "Failed to save user data to Realtime Database for ${user.uid}: $message") //
                                                // If DB write fails, delete the Auth user to prevent orphaned accounts
                                                user.delete()
                                                    .addOnCompleteListener { deleteTask ->
                                                        if (deleteTask.isSuccessful) {
                                                            Log.e("UserRepositoryImpl", "Auth user deleted due to DB write failure for ${user.uid}") //
                                                        } else {
                                                            Log.e("UserRepositoryImpl", "Failed to delete Auth user after DB write failure: ${deleteTask.exception?.message}") //
                                                        }
                                                    }
                                                callback(false, "Account created, but failed to save profile. Please try again or contact support. Error: $message", null) //
                                            }
                                        }
                                    } else {
                                        val errorMessage = profileTask.exception?.localizedMessage ?: "Failed to set username on Firebase Auth profile." //
                                        Log.e("UserRepositoryImpl", "Failed to set username on Firebase Auth profile for ${user.uid}: $errorMessage", profileTask.exception) //
                                        // User is created but username might not be set.
                                        // Still try to save to DB, but acknowledge the Auth profile issue.
                                        val newUserModel = userModel.copy(
                                            userId = user.uid,
                                            email = user.email ?: userModel.email
                                        )
                                        addUserToDatabase(user.uid, newUserModel) { success, dbMessage ->
                                            if (success) {
                                                callback(true, "Account created, but username not fully set on Auth. Error: $errorMessage", newUserModel) //
                                            } else {
                                                user.delete() // Clean up if both Auth profile and DB failed
                                                callback(false, "Account creation failed due to profile and DB issues. Error: $errorMessage, DB Error: $dbMessage", null) //
                                            }
                                        }
                                    }
                                }
                        } ?: callback(false, "Firebase user is null after registration. Unexpected error.", null) //
                    } else {
                        val exceptionMessage = authTask.exception?.message ?: "Unknown error." //
                        Log.e("UserRepositoryImpl", "Firebase Auth sign up failed: $exceptionMessage", authTask.exception) //
                        callback(false, "Sign up failed: $exceptionMessage", null) //
                    }
                }
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "An unexpected local error occurred during sign up: ${e.message}", e) //
            callback(false, "An unexpected local error occurred: ${e.message}", null) //
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
                callback(false, "Email and password cannot be empty.", null) //
                return //
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let { user ->
                            getUserFromDatabase(user.uid) { success, message, userModel ->
                                if (success && userModel != null) {
                                    Log.d("UserRepositoryImpl", "User ${user.uid} signed in successfully and data retrieved from DB.") //
                                    callback(true, "Sign in successful!", userModel) //
                                } else {
                                    // Signed in to Auth, but couldn't get profile from DB.
                                    // This is a tricky state. We'll sign out to maintain consistency.
                                    Log.w("UserRepositoryImpl", "User ${user.uid} signed in, but data not found/retrieved from DB. Signing out for consistency. Message: $message") //
                                    auth.signOut() // Sign out to prevent inconsistent state
                                    callback(false, message, null) //
                                }
                            }
                        } ?: callback(false, "User not found after sign in. Unexpected error.", null) //
                    } else {
                        val exceptionMessage = task.exception?.message ?: "Unknown error." //
                        Log.e("UserRepositoryImpl", "Firebase Auth sign in failed: $exceptionMessage", task.exception) //
                        callback(false, "Sign in failed: $exceptionMessage", null) //
                    }
                }
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "An unexpected local error occurred during sign in: ${e.message}", e) //
            callback(false, "An unexpected local error occurred: ${e.message}", null) //
        }
    }

    /**
     * Signs out the current user. The AuthStateListener handles clearing local data.
     */
    override fun signOut(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut() //
            // The AuthStateListener will handle the rest (clearing shared prefs and notifying observers).
            Log.d("UserRepositoryImpl", "Sign out initiated. AuthStateListener will complete the process.") //
            callback(true, "Sign out initiated successfully.") //
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "An error occurred during sign out: ${e.message}", e) //
            callback(false, "An error occurred during sign out: ${e.message}") //
        }
    }

    /**
     * Sends a password reset email to the specified email address.
     */
    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        try {
            if (email.isBlank()) {
                callback(false, "Email cannot be empty.") //
                return //
            }
            auth.sendPasswordResetEmail(email) //
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("UserRepositoryImpl", "Password reset email sent to $email") //
                        callback(true, "Password reset email sent successfully.") //
                    } else {
                        val exceptionMessage = task.exception?.message ?: "Unknown error." //
                        Log.e("UserRepositoryImpl", "Failed to send reset email to $email: $exceptionMessage", task.exception) //
                        callback(false, "Failed to send reset email: ${exceptionMessage}") //
                    }
                }
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "An unexpected local error occurred during forget password: ${e.message}", e) //
            callback(false, "An unexpected local error occurred: ${e.message}") //
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
        usersRef.child(userId).setValue(model) //
            .addOnSuccessListener {
                Log.d("UserRepositoryImpl", "User data saved to Realtime Database: $userId") //
                callback(true, "User data saved successfully.") //
            }
            .addOnFailureListener { exception ->
                Log.e("UserRepositoryImpl", "Failed to save user data to Realtime Database for $userId: ${exception.message}", exception) //
                callback(false, "Failed to save user data: ${exception.message}") //
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
                    val userModel = snapshot.getValue(UserModel::class.java) //
                    // It's good practice to ensure consistency with Auth data where possible
                    val currentAuthUser = auth.currentUser //
                    val consistentModel = userModel?.copy(
                        userId = userId, // Ensure userId is always correct
                        email = currentAuthUser?.email ?: userModel.email, // Prioritize current FirebaseUser email
                        username = currentAuthUser?.displayName ?: userModel.username // Prioritize current FirebaseUser display name
                    )
                    Log.d("UserRepositoryImpl", "User data retrieved from DB for $userId: $consistentModel") //
                    callback(true, "User found.", consistentModel) //
                } else {
                    Log.w("UserRepositoryImpl", "User profile not found in database for $userId.") //
                    callback(false, "User profile not found in database.", null) //
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserRepositoryImpl", "Database error fetching user for $userId: ${error.message}", error.toException()) //
                callback(false, "Database error: ${error.message}", null) //
            }
        })
    }

    /**
     * Retrieves the currently logged-in user's data from Realtime Database.
     */
    override fun getCurrentUser(callback: (UserModel?) -> Unit) {
        val firebaseUser = auth.currentUser //
        if (firebaseUser != null) { //
            getUserFromDatabase(firebaseUser.uid) { _, _, userModel -> //
                callback(userModel) //
            }
        } else {
            callback(null) //
        }
    }

    /**
     * Retrieves the current FirebaseUser object synchronously.
     */
    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser //
    }

    /**
     * Provides a mechanism to observe real-time changes in the user's authentication state.
     */
    override fun observeAuthState(callback: (Boolean, UserModel?) -> Unit) {
        authStateChangeListeners.add(callback) //
        // Immediately notify the new listener with the current state
        getCurrentUser { user ->
            callback(user != null, user) //
        }
        Log.d("UserRepositoryImpl", "Added auth state listener. Current state notified.") //
    }

    /**
     * Deletes the user's account from Firebase Authentication and their data from Realtime Database.
     */
    override fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit) {
        val firebaseUser = auth.currentUser //

        if (firebaseUser == null || firebaseUser.uid != userId) {
            callback(false, "No authenticated user or mismatched user ID for deletion.") //
            return //
        }

        // 1. Delete user data from Realtime Database first
        usersRef.child(userId).removeValue() //
            .addOnSuccessListener {
                Log.d("UserRepositoryImpl", "User data removed from Realtime Database for $userId") //
                // 2. Delete user from Firebase Authentication
                firebaseUser.delete() //
                    .addOnSuccessListener {
                        Log.d("UserRepositoryImpl", "User deleted from Firebase Auth for $userId") //
                        callback(true, "Account deleted successfully.") //
                    }
                    .addOnFailureListener { authException ->
                        Log.e("UserRepositoryImpl", "Failed to delete user from Firebase Auth for $userId: ${authException.message}", authException) //
                        // If Auth deletion fails, consider re-adding DB data or alerting admin
                        callback(false, "Failed to delete account from authentication: ${authException.message}") //
                    }
            }
            .addOnFailureListener { dbException ->
                Log.e("UserRepositoryImpl", "Failed to delete user data from Realtime Database for $userId: ${dbException.message}", dbException) //
                callback(false, "Failed to delete user data: ${dbException.message}") //
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
            callback(false, "User ID cannot be empty for profile edit.") //
            return //
        }
        if (data.isEmpty()) {
            callback(false, "No data provided for profile update.") //
            return //
        }

        usersRef.child(userId).updateChildren(data) //
            .addOnSuccessListener {
                Log.d("UserRepositoryImpl", "Profile updated in Realtime Database for $userId: $data") //
                callback(true, "Profile updated successfully.") //
            }
            .addOnFailureListener { exception ->
                Log.e("UserRepositoryImpl", "Failed to update profile in Realtime Database for $userId: ${exception.message}", exception) //
                callback(false, "Failed to update profile: ${exception.message}") //
            }
    }

    // Helper function to notify all registered listeners
    private fun notifyAuthStateChanged(isLoggedIn: Boolean, user: UserModel?) {
        // Use a copy to avoid ConcurrentModificationException if a listener removes itself
        authStateChangeListeners.toList().forEach { it(isLoggedIn, user) } //
    }
}