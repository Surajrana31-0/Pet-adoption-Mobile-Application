// /viewmodel/AdoptionApplicationViewModelFactory.kt

package com.example.petadoptionmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petadoptionmanagement.repository.AdoptionApplicationRepository

@Suppress("UNCHECKED_CAST")
class AdoptionApplicationViewModelFactory(
    private val repository: AdoptionApplicationRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdoptionApplicationViewModel::class.java)) {
            return AdoptionApplicationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
