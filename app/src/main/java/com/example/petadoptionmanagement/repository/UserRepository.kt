package com.example.petadoptionmanagement.repository

import com.example.petadoptionmanagement.model.UserModel

/**
 * Interface defining the contract for user data operations.
 * This is part of the Repository layer in the MVVM architecture for user management.
 */
interface UserRepository {

    /**
     * Registers a new user with email and password using Firebase Authentication,
     * and stores additional user details (like username) in Firebase Realtime Database.
     *
     * @param userModel The UserModel object containing username and email for registration.
     * @param password The raw password provided by the user.
     * @param callback A lambda function to be called upon completion,
     * indicating success/failure and a descriptive message.
     */
    fun signUp(
        userModel: UserModel,
        password: String,
        callback: (Boolean, String) -> Unit
    )

    /**
     * Signs in an existing user with email and password using Firebase Authentication.
     *
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param callback A lambda function to be called upon completion,
     * indicating success/failure and a descriptive message.
     */
    fun signIn(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    )

    // You can add more user-related functions here, e.g.,
    // fun signOut(callback: (Boolean, String) -> Unit)
    // fun getCurrentUser(): FirebaseUser?
    // fun resetPassword(email: String, callback: (Boolean, String) -> Unit)
}