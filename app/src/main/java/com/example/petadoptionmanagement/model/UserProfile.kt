package com.example.petadoptionmanagement.model

// Data class to represent a user's profile information
data class UserProfile(
    val name: String,
    val email: String,
    val profileImageUrl: String? = null ,// URL to the profile picture on Cloudinary or other storage
    val bio: String? = null // Ensure 'bio' is present here
){
    // Empty constructor for Firebase
    constructor() : this("", "", null)

}