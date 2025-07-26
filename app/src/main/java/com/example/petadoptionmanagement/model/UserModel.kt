package com.example.petadoptionmanagement.model

/**
 * Data class representing a User in the application.
 * This acts as the Model in the MVVM architecture for user data.
 * Given the application's focus on admin operations, this model primarily supports admin functionalities.
 *
 * @property userId Unique identifier for the user (Firebase Authentication UID).
 * @property username The chosen username for the user.
 * @property firstname The first name of the user.
 * @property lastname The last name of the user.
 * @property email The email address of the user.
 * @property profilePictureUrl URL of the user's profile picture. Nullable if the user hasn't set one.
 * @property role The role of the user within the system (primarily "admin").
 *                 While adopters exist, their data is managed indirectly through adoption applications
 *                 and their user accounts might be simplified or handled differently outside this model if not directly managed by admin.
 * @property notificationPreferences Settings for user notifications (e.g., new pet matches, application updates).
 */
data class UserModel(
    var userId: String = "",
    var username: String = "",
    var firstname: String = "",
    var lastname: String = "",
    var contact: String = "",
    var email: String = "",
    var profilePictureUrl: String? = null,
    var role: String = "admin", // Default role, reflecting admin-centric application
    var notificationPreferences: Map<String, Boolean> = emptyMap(), // Example: {"newMatch": true, "appUpdate": true}
    var createdAt: Long = System.currentTimeMillis(),
    var lastLogin: Long? = null
) {
    // No-argument constructor required for Firebase Realtime Database deserialization
    constructor() : this("", "", "", "", "", "", null, "admin", emptyMap(), System.currentTimeMillis(), null)
}