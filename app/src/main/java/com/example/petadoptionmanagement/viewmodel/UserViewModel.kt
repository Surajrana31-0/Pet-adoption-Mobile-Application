package com.example.petadoptionmanagement.viewmodel

import android.content.Context
import android.net.Uri
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

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _viewedUser = MutableLiveData<UserModel?>()
    val viewedUser: LiveData<UserModel?> get() = _viewedUser

    private val _roleAdmins = MutableLiveData<List<UserModel>>()
    val roleAdmins: LiveData<List<UserModel>> get() = _roleAdmins

    private val _roleAdopters = MutableLiveData<List<UserModel>>()
    val roleAdopters: LiveData<List<UserModel>> get() = _roleAdopters

    init {
        _isLoading.postValue(true)
        userRepository.observeAuthState { loggedIn, userModel ->
            _isLoggedIn.postValue(loggedIn)
            _currentUser.postValue(userModel)
            _isLoading.postValue(false)
        }
    }

    fun signUp(userModel: UserModel, password: String) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                if (userModel.email.isBlank()) {
                    _message.postValue("Email required.")
                    _isLoading.postValue(false)
                    return@launch
                }
                val firebaseUser = userRepository.createUserInAuth(userModel.email, password)
                if (firebaseUser != null) {
                    val userToSave = userModel.copy(userId = firebaseUser.uid)
                    userRepository.saveUserDetails(firebaseUser.uid, userToSave)
                    _message.postValue("Sign up successful.")
                } else {
                    _message.postValue("Sign up/auth failed.")
                }
            } catch (e: Exception) {
                _isLoggedIn.postValue(false)
                _message.postValue("Sign up failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun signIn(email: String, password: String) {
        _isLoading.postValue(true)
        userRepository.signIn(email, password) { success, msg, userModel ->
            _isLoading.postValue(false)
            _message.postValue(msg)
            if (success && userModel != null) {
                // _currentUser handled by observeAuthState
            }
        }
    }

    fun logout() {
        _isLoading.postValue(true)
        userRepository.signOut { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
        }
    }

    fun deleteAccount() {
        _isLoading.postValue(true)
        val currentUserId = userRepository.getCurrentFirebaseUser()?.uid
        if (currentUserId != null) {
            userRepository.deleteAccount(currentUserId) { success, msg ->
                _isLoading.postValue(false)
                _message.postValue(msg)
            }
        } else {
            _isLoading.postValue(false)
            _message.postValue("Cannot delete account: Not logged in.")
        }
    }

    fun forgetPassword(email: String) {
        _isLoading.postValue(true)
        userRepository.forgetPassword(email) { success, msg ->
            _isLoading.postValue(false)
            _message.postValue(msg)
        }
    }

    fun editProfile(
        userId: String,
        data: Map<String, Any>,
        newImageUri: Uri? = null,
        context: Context? = null
    ) {
        _isLoading.postValue(true)
        if (newImageUri != null && context != null) {
            userRepository.uploadUserImage(context, newImageUri) { imageUrl ->
                val update = data.toMutableMap().apply {
                    if (imageUrl != null) put("profilePictureUrl", imageUrl)
                }
                userRepository.editProfile(userId, update) { success, msg ->
                    _isLoading.postValue(false)
                    _message.postValue(msg)
                    if (success) refreshCurrentUser()
                }
            }
        } else {
            userRepository.editProfile(userId, data) { success, msg ->
                _isLoading.postValue(false)
                _message.postValue(msg)
                if (success) refreshCurrentUser()
            }
        }
    }

    private fun refreshCurrentUser() {
        userRepository.getCurrentUserModel { updatedUserModel ->
            _currentUser.postValue(updatedUserModel)
        }
    }

    fun getUserFromDatabase(userId: String) {
        _isLoading.postValue(true)
        userRepository.getUserFromDatabase(userId) { success, msg, userModel ->
            _isLoading.postValue(false)
            _viewedUser.postValue(if (success) userModel else null)
            _message.postValue(msg)
        }
    }

    fun fetchAdmins() {
        userRepository.getUsersByRole("admin") { success, msg, admins ->
            if (success) _roleAdmins.postValue(admins)
            _message.postValue(msg)
        }
    }

    fun fetchAdopters() {
        userRepository.getUsersByRole("adopter") { success, msg, adopters ->
            if (success) _roleAdopters.postValue(adopters)
            _message.postValue(msg)
        }
    }

    fun clearMessage() {
        _message.postValue(null)
    }

    fun getCurrentFirebaseUser(): FirebaseUser? = userRepository.getCurrentFirebaseUser()
}
