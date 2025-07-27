package com.example.petadoptionmanagement.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petadoptionmanagement.model.UserModel
import com.example.petadoptionmanagement.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _currentUser = MutableLiveData<UserModel?>()
    val currentUser: LiveData<UserModel?> get() = _currentUser

    private val _message = MutableLiveData<String?>() // Allow null to clear message
    val message: LiveData<String?> get() = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // For viewing a specific user's profile (not the currently logged-in one)
    private val _viewedUser = MutableLiveData<UserModel?>()
    val viewedUser: LiveData<UserModel?> get() = _viewedUser // Renamed from _users for clarity

    init {
        _isLoading.postValue(true) // Initial loading state for auth check
        userRepository.observeAuthState { loggedIn, userModel ->
            _isLoggedIn.postValue(loggedIn)
            _currentUser.postValue(userModel)
            _isLoading.postValue(false) // Auth state determined
            if (!loggedIn) {
                // Optionally clear other user-specific data, e.g., _viewedUser.postValue(null)
            }
        }
    }

    fun signUp(userModel: UserModel, password: String) { // Removed direct callback
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                if (userModel.email.isBlank()) {
                    _message.postValue("Sign up failed: Email is required.")
                    _isLoading.postValue(false)
                    return@launch
                }
                // Assuming userModel already contains uid="" or is handled by saveUserDetails
                val firebaseUser = userRepository.createUserInAuth(userModel.email, password)

                if (firebaseUser != null) {
                    // Create a new UserModel instance with the UID from FirebaseUser
                    // if your original userModel from UI doesn't have it yet.
                    // This is crucial if userModel passed in doesn't have a UID field or it's empty.
                    val userToSave = if (userModel.userId.isBlank()) {
                        userModel.copy(userId = firebaseUser.uid)
                    } else {
                        userModel // Assuming userModel passed in might already have UID if it's an update scenario (unlikely for sign-up)
                    }
                    userRepository.saveUserDetails(firebaseUser.uid, userToSave) // Use userToSave

                    // _isLoggedIn and _currentUser will be updated by observeAuthState
                    _message.postValue("Sign up successful! Welcome.")
                } else {
                    _message.postValue("Sign up failed: Authentication entry could not be created.")
                }
            } catch (e: Exception) {
                _isLoggedIn.postValue(false) // Ensure loggedIn state is false on error
                _message.postValue("Sign up failed: ${e.message ?: "An unexpected error occurred."}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun signIn(email: String, password: String) { // Removed direct callback
        _isLoading.postValue(true)
        // userRepository.signIn will trigger observeAuthState which updates _isLoggedIn and _currentUser
        userRepository.signIn(email, password) { success, msg, userModel -> // userModel comes from repo callback
            _isLoading.postValue(false) // Set loading false after operation completes
            _message.postValue(msg)
            // If signIn callback provides userModel, observeAuthState should ideally handle setting _currentUser.
            // If there's a delay or specific need, you could update _currentUser here too,
            // but it might lead to two updates if observeAuthState is also quick.
            // if (success && userModel != null) {
            // _currentUser.postValue(userModel) // Potentially redundant if observeAuthState is effective
            // }
        }
    }

    fun logout() { // Removed direct callback
        _isLoading.postValue(true)
        userRepository.signOut { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            // _isLoggedIn and _currentUser are updated by observeAuthState
        }
    }

    fun deleteAccount() { // Removed direct callback and userId parameter (assumes current user)
        _isLoading.postValue(true)
        val currentUserId = userRepository.getCurrentFirebaseUser()?.uid
        if (currentUserId != null) {
            userRepository.deleteAccount(currentUserId) { success, msg ->
                _isLoading.postValue(false)
                _message.postValue(msg)
                // _isLoggedIn and _currentUser are updated by observeAuthState
            }
        } else {
            _isLoading.postValue(false)
            _message.postValue("Cannot delete account: No user is currently logged in.")
        }
    }

    fun forgetPassword(email: String) { // Removed direct callback
        _isLoading.postValue(true)
        userRepository.forgetPassword(email) { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        return userRepository.getCurrentFirebaseUser() // Corrected method name
    }

    fun editProfile(userId: String, data: Map<String, Any?>) { // Changed data to Map, removed callback
        _isLoading.postValue(true)
        userRepository.editProfile(userId, data) { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            if (success) {
                // After successful edit, trigger a re-fetch of current user to update _currentUser LiveData
                userRepository.getCurrentUserModel { updatedUserModel -> // Corrected method name
                    _currentUser.postValue(updatedUserModel) // Update the currently logged-in user's details
                }
            }
        }
    }

    fun getUserFromDatabase(userId: String) { // For fetching a specific user profile
        _isLoading.postValue(true)
        userRepository.getUserFromDatabase(userId) { success, message, userModel ->
            _isLoading.postValue(false)
            if (success) {
                _viewedUser.postValue(userModel)
            } else {
                _viewedUser.postValue(null) // Clear if fetch failed
            }
            _message.postValue(message) // Display message regardless of success for this specific fetch
        }
    }

    // This function might be redundant if your signUp process (createUserInAuth + saveUserDetails)
    // correctly handles saving all necessary UserModel details.
    // If you have a specific use case for adding/updating user details outside of signUp,
    // ensure `userRepository.addUserToDatabase` exists and is implemented.
    /*
    fun addUserToDatabase(userId: String, model: UserModel) {
        _isLoading.postValue(true)
        // Make sure userRepository.addUserToDatabase is defined in your interface and implementation
        userRepository.addUserToDatabase(userId, model) { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            if (success) {
                // If this action updates the current user, refresh it:
                if (userId == _currentUser.value?.uid) {
                    userRepository.getCurrentUserModel { updatedUser ->
                        _currentUser.postValue(updatedUser)
                    }
                }
            }
        }
    }
    */

    fun clearMessage() {
        _message.postValue(null) // Set to null to indicate message has been handled
    }
}