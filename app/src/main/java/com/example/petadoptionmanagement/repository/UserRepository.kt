package com.example.petadoptionmanagement.repository

import com.example.petadoptionmanagement.model.UserModel
import com.google.firebase.auth.FirebaseUser

/**
 * Interface defining the contract for user data operations.
 */
interface UserRepository {

    /**
     * Creates a new user in Firebase Authentication.
     * This is the first step of the registration process.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @return The created FirebaseUser, or null if auth creation fails before an exception.
     * @throws Exception if Firebase Authentication operation encounters an error.
     */
    suspend fun createUserInAuth(email: String, password: String): FirebaseUser?

    /**
     * Saves additional user details (like username, contact, etc.) to Cloud Firestore.
     * This is typically called after successful user creation in Firebase Authentication.
     *
     * @param userId The UID of the authenticated user (from Firebase Auth).
     * @param userModel The UserModel containing all details to be saved.
     * @throws Exception if the Firestore operation encounters an error.
     */
    suspend fun saveUserDetails(userId: String, userModel: UserModel)

    /**
     * Signs in an existing user with email and password using Firebase Authentication.
     * The callback should provide the UserModel upon successful sign-in.
     *
     * @param email The email address of the user.
     * @param password The password of the user.
     * @param callback (success: Boolean, message: String, userModel: UserModel?)
     */
    fun signIn(
        email: String,
        password: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )

    /**
     * Signs out the current user from Firebase Authentication.
     */
    fun signOut(callback: (Boolean, String) -> Unit)

    /**
     * Sends a password reset email to the specified email address.
     */
    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit)

    /**
     * Synchronously retrieves the current FirebaseUser object.
     * Prefer observeAuthState for reactive updates.
     */
    fun getCurrentFirebaseUser(): FirebaseUser? // Renamed from your original getCurrentUser() to avoid overload clash

    /**
     * Asynchronously retrieves the UserModel of the currently logged-in user from Firestore.
     */
    fun getCurrentUserModel(callback: (UserModel?) -> Unit) // Renamed from your original getCurrentUser(callback)

    /**
     * Retrieves a specific user's UserModel from Firestore by their user ID.
     */
    fun getUserFromDatabase(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )

    /**
     * Provides a mechanism to observe real-time changes in the user's authentication state
     * and fetches the corresponding UserModel from Firestore.
     *
     * @param observer (isLoggedIn: Boolean, userModel: UserModel?)
     */
    fun observeAuthState(observer: (Boolean, UserModel?) -> Unit)

    /**
     * Deletes the user's account from Firebase Authentication and their data from Firestore.
     */
    fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit)

    /**
     * Updates specific fields of a user's profile in Firestore.
     */
    fun editProfile(
        userId: String,
        data: Map<String, Any?>, // Changed to Map from MutableMap for broader compatibility
        callback: (Boolean, String) -> Unit
    )

    // Note: The original `signUp` and `addUserToDatabase` are effectively replaced/covered by
    // `createUserInAuth`, `saveUserDetails`, and how the ViewModel now orchestrates these.
    // If you had a specific use case for addUserToDatabase separate from initial signup,
    // you might keep a version of it, perhaps as a suspend function.
}