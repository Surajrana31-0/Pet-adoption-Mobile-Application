package com.example.petadoptionmanagement.repository

import com.example.petadoptionmanagement.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Concrete implementation of UserRepository using Firebase Authentication and Realtime Database.
 * This class handles direct interactions with Firebase for user authentication and data storage.
 */
class UserRepositoryImpl : UserRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef = database.reference.child("users") // Reference to store user profiles

    /**
     * Registers a new user with Firebase Authentication and saves their username to Realtime Database.
     */
    override fun signUp(
        userModel: UserModel,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(userModel.email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val firebaseUser = authTask.result?.user
                    firebaseUser?.let { user ->
                        // Set the Firebase UID as the userId in the UserModel
                        userModel.userId = user.uid
                        // Store the UserModel (specifically username and email) in Realtime Database
                        usersRef.child(user.uid).setValue(userModel)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    callback(true, "Account created successfully!")
                                } else {
                                    // If database write fails, consider deleting auth user or handling rollback
                                    user.delete() // Optional: Delete the auth user if profile save fails
                                    callback(false, "Failed to save user profile: ${dbTask.exception?.message}")
                                }
                            }
                    } ?: callback(false, "Firebase user is null after registration.")
                } else {
                    callback(false, "Sign up failed: ${authTask.exception?.message}")
                }
            }
    }

    /**
     * Signs in an existing user using Firebase Authentication.
     */
    override fun signIn(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Sign in successful!")
                } else {
                    callback(false, "Sign in failed: ${task.exception?.message}")
                }
            }
    }
}