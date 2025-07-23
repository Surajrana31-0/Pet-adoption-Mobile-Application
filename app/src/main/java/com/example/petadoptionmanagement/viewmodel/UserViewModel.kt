package com.example.petadoptionmanagement.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petadoptionmanagement.model.UserModel
import com.example.petadoptionmanagement.repository.PetRepository
import com.example.petadoptionmanagement.repository.UserRepository
import com.google.firebase.auth.FirebaseUser

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


    init {
        // Start observing authentication state changes from the repository
        // This ensures that _isLoggedIn and _currentUser are always up-to-date
        // based on Firebase Auth and Realtime Database changes.
        userRepository.observeAuthState { loggedIn, user ->
            _isLoggedIn.postValue(loggedIn)
            _currentUser.postValue(user)
        }
        // Also perform an initial check of the current user
        userRepository.getCurrentUser { user ->
            _isLoggedIn.postValue(user != null)
            _currentUser.postValue(user)
        }
    }

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
        callback: (Boolean, String) -> Unit // Callback for immediate UI action, if needed
    ) {
        _isLoading.postValue(true)
        val userModel = UserModel(username = username, email = email) // Create UserModel for the repository
        userRepository.signUp(userModel, password) { success, msg, user ->
            _isLoading.postValue(false)
            _message.postValue(msg) // Post message to LiveData
            if (success) {
                // If sign up is successful, the observeAuthState in init will update LiveData.
                // These manual updates ensure immediate UI reflection if the AuthStateListener
                // takes a moment to propagate changes.
                _isLoggedIn.postValue(true)
                _currentUser.postValue(user)
            }
            callback(success, msg) // Use 'msg' from repository for the callback
        }
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
        _isLoading.postValue(true)
        userRepository.signIn(email, password) { success, msg, user ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            if (success) {
                _isLoggedIn.postValue(true)
                _currentUser.postValue(user)
            }
            callback(success, msg)
        }
    }

    /**
     * Initiates the user logout process.
     * Delegates the sign-out operation to the UserRepository.
     *
     * @param callback A lambda to inform the UI about the success or failure of the operation.
     */
    fun logout(callback: (Boolean, String) -> Unit) {
        _isLoading.postValue(true)
        userRepository.signOut { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            if (success) {
                _isLoggedIn.postValue(false)
                _currentUser.postValue(null)
            }
            callback(success, msg)
        }
    }

    fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit){
        userRepository.deleteAccount(userId,callback)
    }

    fun addUserToDatabase(
        userId: String, model: UserModel,
        callback: (Boolean, String) -> Unit
    ){
        userRepository.addUserToDatabase(userId,model,callback)
    }

    fun getCurrentUser(): FirebaseUser?{
        return userRepository.getCurrentUser()
    }


    fun logOut(
        callback: (Boolean, String) -> Unit){
        userRepository.signOut(callback) // Changed to signOut to match the existing logout function name in UserRepository
    }


    fun editProfile(
        userId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ){
        userRepository.editProfile(userId,data,callback)
    }

    private val _users = MutableLiveData<UserModel?>()
    val users : LiveData<UserModel?> get() = _users

    fun getUserFromDatabase(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ){
        userRepository.getUserFromDatabase(userId){
                success,message,users ->
            if(success){
                _users.postValue(users)
            }else{
                _users.postValue(null)
            }
            callback(success, message, users) // Added callback here to propagate the result to the UI
        }
    }


    /**
     * Clears the current message. Call this after a message has been displayed in the UI.
     */
    fun clearMessage() {
        _message.postValue("") // Clear the message
    }

    // You can add more functions here for user profile updates, password resets, etc.
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