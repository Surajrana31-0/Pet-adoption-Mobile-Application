package com.example.petadoptionmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadoptionmanagement.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    // State holding the current user (null if not logged in)
    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> get() = _currentUser

    // State for loading and error messages
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // Example: Simulate sign-in (replace with actual authentication logic)
    fun signIn(email: String, password: String) {
        _loading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(1500)
            if (email == "test@example.com" && password == "password") {
                _currentUser.value = UserModel(
                    id = "1",
                    username = "TestUser",
                    email = email
                )
            } else {
                _errorMessage.value = "Invalid email or password"
            }
            _loading.value = false
        }
    }

    // Example: Simulate sign-up (replace with actual backend logic)
    fun signUp(username: String, email: String, password: String) {
        _loading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(1500)
            if (email.contains("@") && password.length >= 6) {
                _currentUser.value = UserModel(
                    id = "2",
                    username = username,
                    email = email
                )
            } else {
                _errorMessage.value = "Invalid signup details"
            }
            _loading.value = false
        }
    }

    // Example: Simulate password reset
    fun resetPassword(email: String, newPassword: String) {
        _loading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(1000)
            if (email == "test@example.com") {
                // In real app, update password in backend
                _errorMessage.value = "Password reset successful!"
            } else {
                _errorMessage.value = "Email not found"
            }
            _loading.value = false
        }
    }

    // Sign out
    fun signOut() {
        _currentUser.value = null
    }

    // Clear error
    fun clearError() {
        _errorMessage.value = null
    }
}