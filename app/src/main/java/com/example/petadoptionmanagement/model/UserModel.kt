package com.example.petadoptionmanagement.model

/**
 * Data class representing a User in the application.
 * This acts as the Model in the MVVM architecture for user data.
 *
 * @property userId Unique identifier for the user (Firebase Authentication UID).
 * @property username The chosen username for the user.
 * @property email The email address of the user.
 */
data class UserModel(
    var userId: String = "",
    var username: String = "",
    var firstname: String = "",
    var lastname: String = "",
    var contact: String = "",
    var address: String = "",
    var email: String = ""
    // Password is not stored directly in the UserModel for security reasons
    // as Firebase Authentication handles password hashing and storage internally.
)