// /model/UserModel.kt

package com.example.petadoptionmanagement.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Enum for user roles to ensure consistency
enum class UserRole {
    ADMIN,
    ADOPTER
}

data class UserModel(
    var userId: String = "",
    var username: String = "",
    var firstname: String = "",
    var lastname: String = "",
    var contact: String = "",
    var email: String = "",
    var profilePictureUrl: String? = null, // For Cloudinary profile image URL
    var role: UserRole = UserRole.ADOPTER, // Use the enum
    var notificationPreferences: Map<String, Boolean> = emptyMap(),

    @ServerTimestamp
    var createdAt: Date? = null,

    @ServerTimestamp
    var lastLogin: Date? = null
) {
    // No-arg constructor required by Firebase
    constructor() : this(
        userId = "",
        username = "",
        firstname = "",
        lastname = "",
        contact = "",
        email = "",
        profilePictureUrl = null,
        role = UserRole.ADOPTER,
        notificationPreferences = emptyMap(),
        createdAt = null,
        lastLogin = null
    )
}
