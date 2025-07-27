package com.example.petadoptionmanagement.viewmodel // Or your ViewModel package

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petadoptionmanagement.repository.PetRepository

@Suppress("UNCHECKED_CAST")
class PetViewModelFactory( // This defines it as a class
    private val petRepository: PetRepository
) : ViewModelProvider.Factory { // It implements an interface
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetViewModel::class.java)) {
            return PetViewModel(petRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}