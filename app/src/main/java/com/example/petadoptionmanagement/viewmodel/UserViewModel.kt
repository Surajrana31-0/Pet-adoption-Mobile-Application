package com.example.petadoptionmanagement.viewmodel

import androidx.lifecycle.ViewModel
import com.example.petadoptionmanagement.model.UserModel
import com.example.petadoptionmanagement.repository.UserRepository

/**
 * ViewModel for user authentication and profile management.
 * This class handles UI-related data and logic, communicating with the UserRepository.
 */
class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    /**
     * Initiates the user registration process.
     * Delegates the sign-up operation to the UserRepository.
     *
     * @param username The username for the new account.
     * @param email The email for the new account.
     * @param password The password for the new account.
     * @param callback A lambda to inform the UI about the success or failure of the operation.
     */
    fun signUp(
        username: String,
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        val userModel = UserModel(username = username, email = email)
        userRepository.signUp(userModel, password, callback)
    }

    /**
     * Initiates the user login process.
     * Delegates the sign-in operation to the UserRepository.
     *
     * @param email The email for login.
     * @param password The password for login.
     * @param callback A lambda to inform the UI about the success or failure of the operation.
     */
    fun signIn(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        userRepository.signIn(email, password, callback)
    }

    // You can add more LiveData/functions here to expose user state (e.g., loggedInUser, error messages)
    // and other user-related operations like signOut, updateProfile, etc.
}