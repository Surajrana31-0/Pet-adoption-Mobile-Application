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

    /**
     * Adds or updates a user's profile data in the Realtime Database.
     *
     * @param userId The unique ID of the user.
     * @param model The UserModel object containing the user's data.
     * @param callback A lambda function to be called upon completion,
     * indicating success/failure and a descriptive message.
     */
    fun addUserToDatabase(
        userId: String, model: UserModel,
        callback: (Boolean, String) -> Unit
    )

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email The email address to send the reset link to.
     * @param callback A lambda function to be called upon completion,
     * indicating success/failure and a descriptive message.
     */
    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit)

    /**
     * Synchronously retrieves the current FirebaseUser object.
     * Note: For observing changes, use `observeAuthState`.
     *
     * @return The current FirebaseUser, or null if no user is logged in.
     */
    fun getCurrentUser(): FirebaseUser?

    /**
     * Retrieves a user model from the Realtime Database by user ID.
     *
     * @param userId The unique ID of the user to retrieve.
     * @param callback A lambda function to be called upon completion,
     * indicating success/failure, a descriptive message, and the retrieved UserModel (if successful).
     */
    fun getUserFromDatabase(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )

    /**
     * Asynchronously retrieves the currently logged-in user's data from the database.
     *
     * @param callback A lambda function that returns the UserModel if a user is logged in and their data is found, null otherwise.
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

    /**
     * Deletes the user's account from Firebase Authentication and their data from Realtime Database.
     *
     * @param userId The ID of the user to delete.
     * @param callback A lambda function to be called upon completion,
     * indicating success/failure and a descriptive message.
     */
    fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit)

    /**
     * Updates specific fields of a user's profile in the Realtime Database.
     *
     * @param userId The ID of the user whose profile is to be updated.
     * @param data A map containing the fields to update and their new values.
     * @param callback A lambda function to be called upon completion,
     * indicating success/failure and a descriptive message.
     */
    fun editProfile(
        userId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    )
}