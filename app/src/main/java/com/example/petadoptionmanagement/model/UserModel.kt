package com.example.petadoptionmanagement.model

/**
 * Represents a User in the Pet Adoption app.
 *
 * Supports multiple roles: "admin" or "adopter".
 * Stores essential user details including contact info, role, and preferences.
 */
data class UserModel(
    var userId: String = "",               // Firebase UID
    var username: String = "",
    var firstname: String = "",
    var lastname: String = "",
    var contact: String = "",
    var email: String = "",
    var profilePictureUrl: String? = null,
    var role: String = "adopter",          // Default to "adopter", explicit admin role if needed
    var notificationPreferences: Map<String, Boolean> = emptyMap(),
    var createdAt: Long = System.currentTimeMillis(),
    var lastLogin: Long? = null
) {
    // No-arg constructor required by Firebase for deserialization
    constructor() : this(
        userId = "",
        username = "",
        firstname = "",
        lastname = "",
        contact = "",
        email = "",
        profilePictureUrl = null,
        role = "adopter",
        notificationPreferences = emptyMap(),
        createdAt = System.currentTimeMillis(),
        lastLogin = null
    )
}
