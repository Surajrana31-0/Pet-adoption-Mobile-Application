// /viewmodel/UserViewModel.kt

package com.example.petadoptionmanagement.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petadoptionmanagement.model.UserModel
import com.example.petadoptionmanagement.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _currentUser = MutableLiveData<UserModel?>()
    val currentUser: LiveData<UserModel?> get() = _currentUser

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _viewedUser = MutableLiveData<UserModel?>()
    val viewedUser: LiveData<UserModel?> get() = _viewedUser

    private val _userList = MutableLiveData<List<UserModel>>()
    val userList: LiveData<List<UserModel>> get() = _userList

    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    init {
        observeAuthenticationState()
    }

    private fun observeAuthenticationState() {
        _isLoading.postValue(true)
        authStateListener = userRepository.observeAuthState { result ->
            result.fold(
                onSuccess = { userModel ->
                    _currentUser.postValue(userModel)
                    _isLoggedIn.postValue(userModel != null)
                },
                onFailure = { error ->
                    _message.postValue("Error observing auth state: ${error.message}")
                    _isLoggedIn.postValue(false)
                    _currentUser.postValue(null)
                }
            )
            _isLoading.postValue(false)
        }
    }

    fun signUp(userModel: UserModel, password: String) {
        _isLoading.postValue(true)
        userRepository.createUser(userModel, password) { result ->
            result.fold(
                onSuccess = { _message.postValue("Sign up successful! Please check your email to verify.") },
                onFailure = { e -> _message.postValue("Sign up failed: ${e.message}") }
            )
            _isLoading.postValue(false)
        }
    }

    fun signIn(email: String, password: String) {
        _isLoading.postValue(true)
        userRepository.signIn(email, password) { result ->
            result.fold(
                onSuccess = { user ->
                    // The auth state listener will automatically update the current user
                    _message.postValue("Welcome back, ${user.username}!")
                },
                onFailure = { e -> _message.postValue("Sign in failed: ${e.message}") }
            )
            _isLoading.postValue(false)
        }
    }

    fun logout() {
        _isLoading.postValue(true)
        userRepository.signOut { result ->
            result.fold(
                onSuccess = { _message.postValue("You have been signed out.") },
                onFailure = { e -> _message.postValue("Sign out failed: ${e.message}") }
            )
            _isLoading.postValue(false)
        }
    }

    fun deleteAccount() {
        _isLoading.postValue(true)
        userRepository.deleteAccount { result ->
            result.fold(
                onSuccess = { _message.postValue("Account deleted successfully.") },
                onFailure = { e -> _message.postValue("Failed to delete account: ${e.message}") }
            )
            _isLoading.postValue(false)
        }
    }

    fun forgetPassword(email: String) {
        _isLoading.postValue(true)
        userRepository.forgetPassword(email) { result ->
            result.fold(
                onSuccess = { _message.postValue("Password reset email sent.") },
                onFailure = { e -> _message.postValue("Failed to send reset email: ${e.message}") }
            )
            _isLoading.postValue(false)
        }
    }

    fun editProfile(userId: String, data: Map<String, Any>, newImageUri: Uri? = null, context: Context? = null) {
        _isLoading.postValue(true)
        if (newImageUri != null && context != null) {
            // If a new image is provided, upload it first
            userRepository.uploadProfileImage(newImageUri) { uploadResult ->
                uploadResult.fold(
                    onSuccess = { imageUrl ->
                        // Add the new image URL to the data map and update the profile
                        val finalData = data.toMutableMap().apply { put("profilePictureUrl", imageUrl) }
                        updateUserProfile(userId, finalData)
                    },
                    onFailure = { e ->
                        _message.postValue("Image upload failed: ${e.message}")
                        _isLoading.postValue(false)
                    }
                )
            }
        } else {
            // If no new image, update the profile with the provided data
            updateUserProfile(userId, data)
        }
    }

    private fun updateUserProfile(userId: String, data: Map<String, Any>) {
        userRepository.editProfile(userId, data) { editResult ->
            editResult.fold(
                onSuccess = { _message.postValue("Profile updated successfully.") },
                onFailure = { e -> _message.postValue("Profile update failed: ${e.message}") }
            )
            _isLoading.postValue(false)
        }
    }

    fun getUserFromDatabase(userId: String) {
        _isLoading.postValue(true)
        userRepository.getUserModel(userId) { result ->
            result.fold(
                onSuccess = { user -> _viewedUser.postValue(user) },
                onFailure = { e ->
                    _viewedUser.postValue(null)
                    _message.postValue("Failed to fetch user details: ${e.message}")
                }
            )
            _isLoading.postValue(false)
        }
    }

    fun fetchUsersByRole(role: String) {
        _isLoading.postValue(true)
        userRepository.getUsersByRole(role) { result ->
            result.fold(
                onSuccess = { users -> _userList.postValue(users) },
                onFailure = { e ->
                    _userList.postValue(emptyList())
                    _message.postValue("Failed to fetch users: ${e.message}")
                }
            )
            _isLoading.postValue(false)
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? = userRepository.getCurrentFirebaseUser()

    override fun onCleared() {
        super.onCleared()
        // Important: Remove the auth state listener to prevent memory leaks
        authStateListener?.let {
            FirebaseAuth.getInstance().removeAuthStateListener(it)
        }
    }
}
