package com.example.petadoptionmanagement.viewmodel // Or your ViewModel package

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petadoptionmanagement.repository.UserRepository

@Suppress("UNCHECKED_CAST")
class UserViewModelFactory( // This defines it as a class
    private val userRepository: UserRepository
) : ViewModelProvider.Factory { // It implements an interface
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

}