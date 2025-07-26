package com.example.petadoptionmanagement.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
// import androidx.lifecycle.ViewModelProvider // Not directly used in this file for instantiation
import androidx.lifecycle.viewModelScope // Import viewModelScope
import com.example.petadoptionmanagement.model.UserModel
// import com.example.petadoptionmanagement.repository.PetRepository // Not used in this specific UserViewModel
import com.example.petadoptionmanagement.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
// import com.google.firebase.auth.FirebaseAuthUserCollisionException // Example for specific error handling
// import com.google.firebase.auth.FirebaseAuthWeakPasswordException // Example for specific error handling
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
    // Consider renaming if this is only for one user at a time, e.g., _viewedUserProfile
    private val _users = MutableLiveData<UserModel?>()
    val users : LiveData<UserModel?> get() = _users


    init {
        // Start observing authentication state changes from the repository
        // This ensures that _isLoggedIn and _currentUser are always up-to-date
        // based on Firebase Auth and (potentially) Realtime Database/Firestore changes.
        // Make sure your userRepository.observeAuthState correctly fetches UserModel for _currentUser
        userRepository.observeAuthState { loggedIn, userModel -> // Assuming observeAuthState now provides UserModel
            _isLoggedIn.postValue(loggedIn)
            _currentUser.postValue(userModel)
            if (!loggedIn) {
                // Optionally clear other user-specific data if user logs out
            }
        }
    }

    /**
     * Initiates the user registration process.
     * Creates an account in Firebase Auth, then saves additional user details to Firestore.
     *
     * @param userModel The complete user model with all details from the sign-up form.
     * @param password The password for the new account.
     * @param callback Optional: to notify the caller (Activity/Fragment) directly.
     *                 UI updates should primarily rely on observing LiveData (isLoggedIn, message).
     */
    fun signUp(userModel: UserModel, password: String, callback: ((success: Boolean, message: String) -> Unit)? = null) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                // Ensure email is present in the userModel for Auth creation
                if (userModel.email.isBlank()) {
                    _isLoading.postValue(false)
                    val errorMsg = "Sign up failed: Email is required."
                    _message.postValue(errorMsg)
                    callback?.invoke(false, errorMsg)
                    return@launch
                }

                val firebaseUser = userRepository.createUserInAuth(userModel.email, password)

                if (firebaseUser != null) {
                    // Save additional user details (username, firstname, etc.) to Firestore
                    // The userModel passed in should contain all these details from the form
                    userRepository.saveUserDetails(firebaseUser.uid, userModel)

                    // _isLoggedIn will be updated by observeAuthState when Firebase Auth state changes.
                    // If observeAuthState is quick, explicit setting here might be redundant
                    // but can be useful for immediate feedback before listener fires.
                    // _isLoggedIn.postValue(true) // Handled by observeAuthState

                    val successMsg = "Sign up successful! Welcome."
                    _message.postValue(successMsg) // For Toast/Snackbar in UI
                    callback?.invoke(true, successMsg)
                } else {
                    // This case should ideally be covered by exceptions from createUserInAuth
                    _isLoading.postValue(false)
                    val errorMsg = "Sign up failed: Authentication entry could not be created."
                    _message.postValue(errorMsg)
                    callback?.invoke(false, errorMsg)
                }
            } catch (e: Exception) {
                // Example of more specific error handling:
                // when (e) {
                //    is FirebaseAuthUserCollisionException -> {
                //        _message.postValue("Sign up failed: Email already in use.")
                //    }
                //    is FirebaseAuthWeakPasswordException -> {
                //        _message.postValue("Sign up failed: Password is too weak.")
                //    }
                //    else -> {
                //        _message.postValue("Sign up failed: ${e.message ?: "Unknown error"}")
                //    }
                // }
                _isLoggedIn.postValue(false) // Ensure loggedIn state is false on error
                val errorMsg = "Sign up failed: ${e.message ?: "An unexpected error occurred."}"
                _message.postValue(errorMsg)
                callback?.invoke(false, errorMsg)
            } finally {
                // Only set isLoading to false if it wasn't already set by an early return or error.
                // However, the above logic should ensure it's always set.
                if (_isLoading.value == true) { // Ensure we only set it if it's currently true
                    _isLoading.postValue(false)
                }
            }
        }
    }


    fun forgetPassword(email : String, callback : (Boolean, String) ->Unit) {
        // Assuming userRepository.forgetPassword handles _isLoading and _message,
        // or adapt it like signUp to use viewModelScope.
        _isLoading.postValue(true)
        userRepository.forgetPassword(email) { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            callback(success, msg) // Propagate callback if still needed by UI directly
        }
    }

    fun signIn(email: String, password: String) {
        // Assuming userRepository.signIn handles _isLoading and _message,
        // and that _isLoggedIn will be updated by observeAuthState.
        // Or adapt it like signUp to use viewModelScope for more control here.
        _isLoading.postValue(true)
        userRepository.signIn(email, password) { success, msg, firebaseUser -> // Assuming callback provides firebaseUser
            _isLoading.postValue(false)
            _message.postValue(msg)
            // _isLoggedIn and _currentUser are updated by observeAuthState
            // If signIn in repo also fetches UserModel, observeAuthState should handle it.
        }
    }

    fun logout() {
        // Assuming userRepository.signOut handles _isLoading and _message.
        // _isLoggedIn and _currentUser will be updated by observeAuthState.
        _isLoading.postValue(true)
        userRepository.signOut { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            // _isLoggedIn and _currentUser are updated by observeAuthState
        }
    }

    fun deleteAccount(userId: String) {
        _isLoading.postValue(true)
        userRepository.deleteAccount(userId) { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            // _isLoggedIn and _currentUser are updated by observeAuthState (will become null/false)
        }
    }

    /**
     * Adds a user model to the Realtime Database/Firestore.
     * This might be redundant if signUp already saves user details.
     * Could be used for admin functions or if initial sign-up only creates Auth entry.
     */
    fun addUserToDatabase(userId: String, model: UserModel) {
        _isLoading.postValue(true)
        // If this is part of sign-up, ensure it's called correctly.
        // If it's separate, its usage context is important.
        // For consistency, consider making repository.addUserToDatabase a suspend function.
        userRepository.addUserToDatabase(userId, model) { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            if (success) {
                // Optionally, re-fetch user if this updates the _currentUser
                // userRepository.getCurrentUser { updatedUser -> _currentUser.postValue(updatedUser) }
            }
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        return userRepository.getCurrentUser()
    }

    fun editProfile(userId: String, data: MutableMap<String, Any?>) {
        _isLoading.postValue(true)
        userRepository.editProfile(userId, data) { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            if (success) {
                // After successful edit, trigger a re-fetch of current user to update _currentUser LiveData
                // Ensure userRepository.getCurrentUser callback provides UserModel
                userRepository.getCurrentUser { updatedUserModel ->
                    _currentUser.postValue(updatedUserModel)
                }
            }
        }
    }

    fun getUserFromDatabase(userId: String) {
        _isLoading.postValue(true)
        userRepository.getUserFromDatabase(userId) { success, message, userModel ->
            _isLoading.postValue(false)
            _message.postValue(message)
            _users.postValue(userModel) // For viewing specific user profiles
        }
    }

    fun clearMessage() {
        _message.postValue("")
    }
}