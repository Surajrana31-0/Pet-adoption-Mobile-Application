package com.example.petadoptionmanagement.model

data class UserModel(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val profileImageUrl: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val bio: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)