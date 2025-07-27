package com.example.petadoptionmanagement.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
// import androidx.lifecycle.viewModelScope // Uncomment if you use viewModelScope.launch
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.repository.PetRepository
// import kotlinx.coroutines.launch // Already imported, but would be used with viewModelScope

/**
 * ViewModel for Pet-related operations.
 * This class prepares and manages data for the UI (View) and handles
 * communication with the PetRepository.
 */
class PetViewModel(private val repo: PetRepository) : ViewModel() {

    // LiveData for a single pet (e.g., when fetching by ID or for editing)
    private val _pet = MutableLiveData<PetModel?>()
    val pet: LiveData<PetModel?> get() = _pet // Public immutable LiveData for observing

    // LiveData for a list of all pets
    private val _allPets = MutableLiveData<List<PetModel?>>()
    val allPets: LiveData<List<PetModel?>> get() = _allPets // Public immutable LiveData

    // LiveData for indicating loading state
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading // Public immutable LiveData

    // LiveData for UI messages (e.g., success/error toasts)
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    /**
     * Clears the LiveData holding a single selected pet.
     * Call this when navigating away from an edit screen or when starting to add a new pet.
     */
    fun clearSelectedPet() {
        _pet.value = null
    }

    /**
     * Call this function after the message has been shown in the UI
     * to prevent it from being shown again on configuration change or recomposition.
     */
    fun clearMessage() {
        _message.value = null
    }

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        // Consider handling loading state here if it's a long operation
        // _loading.postValue(true)
        repo.uploadImage(context, imageUri) { imageUrl ->
            // _loading.postValue(false)
            // You might want to post a message via _message LiveData here too
            callback(imageUrl)
        }
    }

    /**
     * Initiates the process of adding a new pet via the repository.
     * @param petModel The PetModel object to add.
     * @param callback A lambda to inform the UI about the success or failure of the operation.
     */
    fun addNewPet(
        petModel: PetModel,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        // If repo.addPet is a suspend function, wrap in viewModelScope.launch
        // viewModelScope.launch {
        repo.addPet(petModel) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            if (success) {
                getAllPets() // Refresh the list after adding
            }
            callback(success, msg)
        }
        // }
    }

    /**
     * Fetches a pet by its ID. Updates the `_pet` LiveData.
     * @param petID The ID of the pet to fetch.
     */
    fun getPetById(
        petID: String,
        // Optional: Add a callback if the UI needs immediate confirmation beyond observing LiveData
        // callback: ((Boolean, PetModel?) -> Unit)? = null
    ) {
        _loading.postValue(true)
        // If repo.getPetById is a suspend function, wrap in viewModelScope.launch
        // viewModelScope.launch {
        repo.getPetById(petID) { success, msg, petData ->
            _loading.postValue(false)
            if (success) {
                _pet.postValue(petData)
                // callback?.invoke(true, petData)
            } else {
                _pet.postValue(null)
                _message.postValue(msg ?: "Failed to fetch pet details for ID: $petID.")
                // callback?.invoke(false, null)
            }
        }
        // }
    }

    /**
     * Fetches all pets. Updates `_allPets` and `_loading` LiveData.
     */
    fun getAllPets() {
        _loading.postValue(true)
        // If repo.getAllPets is a suspend function, wrap in viewModelScope.launch
        // viewModelScope.launch {
        repo.getAllPets { success, msg, petsList ->
            _loading.postValue(false)
            if (success) {
                Log.d("PetViewModel", "GetAllPets successful.")
                _allPets.postValue(petsList)
            } else {
                Log.d("PetViewModel", "GetAllPets failed: $msg")
                _allPets.postValue(emptyList()) // Post an empty list on failure
                _message.postValue(msg ?: "Failed to fetch pets.")
            }
        }
        // }
    }

    /**
     * Updates an existing pet's information.
     * @param petId The ID of the pet to update.
     * @param data A map of fields to update (or PetModel if your repo takes that).
     * @param callback A lambda to inform the UI about the success or failure.
     */
    fun updatePet(
        petId: String,
        data: PetModel, // Or PetModel
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        // If repo.updatePet is a suspend function, wrap in viewModelScope.launch
        // viewModelScope.launch {
        repo.updatePet(petId, data) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            if (success) {
                getAllPets() // Refresh the list after update
                // Optionally, if the updated pet is the one currently in _pet, update it too
                if (_pet.value?.petId == petId) {
                    // This might be tricky if `data` is a map. You might need to re-fetch or construct the updated PetModel.
                    // For simplicity, re-fetching after update is safer if the full model is needed in _pet.
                    // getPetById(petId) // Uncomment if you want to immediately refresh the _pet LiveData
                }
            }
            callback(success, msg)
        }
        // }
    }

    /**
     * Deletes a pet from the database.
     * @param petId The ID of the pet to delete.
     * @param callback A lambda to inform the UI about the success or failure.
     */
    fun deletePet(
        petId: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        // If repo.deletePet is a suspend function, wrap in viewModelScope.launch
        // viewModelScope.launch {
        repo.deletePet(petId) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            if (success) {
                getAllPets() // Refresh the list after deleting
                // If the deleted pet was the one being viewed, clear it
                if (_pet.value?.petId == petId) {
                    clearSelectedPet()
                }
            }
            callback(success, msg)
        }
        // }
    }
}