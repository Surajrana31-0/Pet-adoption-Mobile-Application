package com.example.petadoptionmanagement.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope // Import viewModelScope
import com.example.petadoptionmanagement.model.UserModel
import com.example.petadoptionmanagement.repository.PetRepository
import com.example.petadoptionmanagement.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch // Import launch from kotlinx.coroutines

/**
 * ViewModel for user authentication and profile management.
 * This class handles UI-related data and logic, communicating with the UserRepository.
 */
class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    // LiveData to observe the user's login status from the UI
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    // LiveData to observe the current logged-in user's data
    private val _currentUser = MutableLiveData<UserModel?>()
    val currentUser: LiveData<UserModel?> get() = _currentUser

    // LiveData for displaying messages (success/error) to the UI
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // LiveData for loading state (e.g., show progress indicator)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for a specific user retrieved from the database (e.g., for viewing other profiles)
    private val _users = MutableLiveData<UserModel?>()
    val users : LiveData<UserModel?> get() = _users


    init {
        // Start observing authentication state changes from the repository
        // This ensures that _isLoggedIn and _currentUser are always up-to-date
        // based on Firebase Auth and Realtime Database changes.
        userRepository.observeAuthState { loggedIn, user ->
            _isLoggedIn.postValue(loggedIn) // Use postValue for updates from non-main threads
            _currentUser.postValue(user) // Use postValue for updates from non-main threads
            // Optionally, clear message if state changes to logged in or logged out
            // clearMessage() // Consider if this is desired behavior
        }
    }

    /**
     * Initiates the user registration process.
     * Delegates the sign-up operation to the UserRepository.
     *
     * @param username The username for the new account.
     * @param email The email for the new account.
     * @param password The password for the new account.
     */
    fun signUp(
        username: String,
        email: String,
        password: String
    ) {
        _isLoading.postValue(true)
        val userModel = UserModel(username = username, email = email) // Create UserModel for the repository
        userRepository.signUp(userModel, password) { success, msg, user ->
            _isLoading.postValue(false)
            _message.postValue(msg) // Post message to LiveData for UI
            // The _isLoggedIn and _currentUser LiveData will be updated by observeAuthState
            // in the init block, so no need to manually set them here unless you need
            // immediate update *before* the AuthStateListener fires (unlikely for simple cases).
            // If success, the observeAuthState will correctly set _isLoggedIn and _currentUser.
        }
    }

    /**
     * Initiates the user login process.
     * Delegates the sign-in operation to the UserRepository.
     *
     * @param email The email for login.
     * @param password The password for login.
     */
    fun signIn(
        email: String,
        password: String
    ) {
        _isLoading.postValue(true)
        userRepository.signIn(email, password) { success, msg, user ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            // _isLoggedIn and _currentUser are updated by observeAuthState
        }
    }

    /**
     * Initiates the user logout process.
     * Delegates the sign-out operation to the UserRepository.
     */
    fun logout() {
        _isLoading.postValue(true)
        userRepository.signOut { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            // _isLoggedIn and _currentUser are updated by observeAuthState
        }
    }

    /**
     * Deletes the user's account.
     * @param userId The ID of the user to delete.
     */
    fun deleteAccount(userId: String) {
        _isLoading.postValue(true)
        userRepository.deleteAccount(userId) { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            // _isLoggedIn and _currentUser are updated by observeAuthState (will become null/false)
        }
    }

    /**
     * Adds a user model to the Realtime Database.
     * (Typically used during initial sign-up, or for admin functions)
     */
    fun addUserToDatabase(
        userId: String, model: UserModel
    ) {
        _isLoading.postValue(true)
        userRepository.addUserToDatabase(userId, model) { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
        }
    }

    /**
     * Synchronously retrieves the current FirebaseUser object.
     * Use sparingly; observeAuthState and currentUser LiveData are preferred for reactivity.
     */
    fun getCurrentFirebaseUser(): FirebaseUser? { // Renamed to avoid confusion with UserModel
        return userRepository.getCurrentUser()
    }

    /**
     * Updates specific fields of a user's profile in the Realtime Database.
     */
    fun editProfile(
        userId: String,
        data: MutableMap<String, Any?>
    ) {
        _isLoading.postValue(true)
        userRepository.editProfile(userId, data) { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            if (success) {
                // After successful edit, trigger a re-fetch of current user to update _currentUser LiveData
                userRepository.getCurrentUser { updatedUser ->
                    _currentUser.postValue(updatedUser)
                }
            }
        }
    }

    /**
     * Retrieves a user model from the Realtime Database by user ID and updates `_users` LiveData.
     * This is useful for fetching specific user profiles (e.g., viewing another user's profile).
     */
    fun getUserFromDatabase(userId: String) {
        _isLoading.postValue(true)
        userRepository.getUserFromDatabase(userId) { success, message, userModel ->
            _isLoading.postValue(false)
            _message.postValue(message) // Post message specific to this fetch
            _users.postValue(userModel) // Update _users LiveData
        }
    }

    /**
     * Clears the current message. Call this after a message has been displayed in the UI.
     */
    fun clearMessage() {
        _message.postValue("") // Clear the message
    }
}

// ViewModel Factory to provide the UserRepository dependency to the ViewModel
class UserViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PetViewModelFactory(private val petRepository: PetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PetViewModel(petRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}