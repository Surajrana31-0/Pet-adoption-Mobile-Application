package com.example.petadoptionmanagement.repository

import com.example.petadoptionmanagement.model.UserModel
import com.google.firebase.auth.FirebaseUser

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
     * indicating success/failure, a descriptive message, and the created UserModel (if successful).
     */
    fun signUp(
        userModel: UserModel, // IMPORTANT CHANGE: Changed from separate username, email to a UserModel object
        password: String,
        callback: (Boolean, String, UserModel?) -> Unit // IMPORTANT CHANGE: Added UserModel? to callback
    )

    /**
     * Signs in an existing user with email and password using Firebase Authentication.
     *
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param callback A lambda function to be called upon completion,
     * indicating success/failure, a descriptive message, and the signed-in UserModel (if successful).
     */
    fun signIn(
        email: String,
        password: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )

    /**
     * Signs out the current user from Firebase Authentication and clears local session data.
     *
     * @param callback A lambda function to be called upon completion,
     * indicating success/failure and a descriptive message.
     */
    fun signOut(callback: (Boolean, String) -> Unit)

    fun addUserToDatabase(
        userId: String, model: UserModel,
        callback: (Boolean, String) -> Unit

    )

    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit)

    fun getCurrentUser(): FirebaseUser?

    fun getUserFromDatabase(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )


    /**
     * Retrieves the currently logged-in user's data.
     *
     * @param callback A lambda function that returns the UserModel if a user is logged in, null otherwise.
     */
    fun getCurrentUser(callback: (UserModel?) -> Unit)

    /**
     * Provides a mechanism to observe real-time changes in the user's authentication state.
     * This callback will be triggered whenever a user logs in, logs out, or the auth state changes.
     *
     * @param callback A lambda function that returns true if a user is logged in and their UserModel,
     * or false and null if no user is logged in.
     */
    fun observeAuthState(callback: (Boolean, UserModel?) -> Unit)


}


