package com.example.petadoptionmanagement.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Import for viewModelScope
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.repository.PetRepository
import kotlinx.coroutines.launch // Import for launch

/**
 * ViewModel for Pet-related operations.
 * This class prepares and manages data for the UI (View) and handles
 * communication with the PetRepository.
 */
class PetViewModel(private val repo: PetRepository) : ViewModel() {

    // LiveData for a single pet (e.g., when fetching by ID)
    private val _pet = MutableLiveData<PetModel?>()
    val pet: LiveData<PetModel?> get() = _pet

    // LiveData for a list of all pets
    private val _allPets = MutableLiveData<List<PetModel?>>()
    val allPets: LiveData<List<PetModel?>> get() = _allPets

    // LiveData for indicating loading state
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    // **** START OF NEW CODE ****
    // LiveData for UI messages (e.g., success/error toasts)
    private val _message = MutableLiveData<String?>() // String? allows it to be null when no message
    val message: LiveData<String?> = _message // Public LiveData for UI to observe

    /**
     * Call this function after the message has been shown in the UI
     * to prevent it from being shown again on configuration change or recomposition.
     */
    fun clearMessage() {
        _message.value = null
    }
    // **** END OF NEW CODE ****


    /**
     * Initiates the process of adding a new pet via the repository.
     * @param petModel The PetModel object to add.
     * @param callback A lambda to inform the UI about the success or failure of the operation.
     */
    fun addNewPet(
        petModel: PetModel,
        callback: (Boolean, String) -> Unit // You might want to remove this callback if message LiveData handles it
    ) {
        // You'll want to use viewModelScope for coroutines if your repo methods are suspend functions
        // For now, assuming repo.addPet handles its own threading or is synchronous for the callback
        repo.addPet(petModel) { success, msg ->
            _message.postValue(msg) // Post the message from the repository callback
            callback(success, msg)
            // If the callback is solely for showing a Toast, you might remove it
            // and let the UI observe the `message` LiveData.
        }
    }

    /**
     * Fetches a pet by its ID. Updates the `_pet` LiveData.
     * @param petID The ID of the pet to fetch.
     */
    fun getPetById(
        petID: String,
    ) {
        // This function updates _pet, but you might also want to post a message on failure
        repo.getPetById(petID) { success, msg, value -> // Assuming callback provides a message
            if (success) {
                _pet.postValue(value)
            } else {
                _pet.postValue(null) // Post null or handle error state if pet not found
                _message.postValue(msg ?: "Failed to fetch pet details.") // Post an error message
            }
        }
    }

    /**
     * Fetches all pets. Updates `_allPets` and `_loading` LiveData.
     */
    fun getAllPets() {
        _loading.postValue(true) // Indicate loading started
        repo.getAllPets { success, msg, value -> // Assuming callback provides a message
            _loading.postValue(false) // Indicate loading finished
            if (success) {
                _allPets.postValue(value) // Post the list of pets
            } else {
                _allPets.postValue(emptyList()) // Post an empty list on failure
                _message.postValue(msg ?: "Failed to fetch pets.") // Post an error message
            }
        }
    }

    /**
     * Updates an existing pet's information.
     * @param petId The ID of the pet to update.
     * @param data A map of fields to update.
     * @param callback A lambda to inform the UI about the success or failure.
     */
    fun updatePet(
        petId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit // Similar to addNewPet, consider if this callback is still needed
    ) {
        repo.updatePet(petId, data) { success, msg ->
            _message.postValue(msg) // Post the message
            callback(success, msg)
        }
    }

    /**
     * Deletes a pet from the database.
     * @param petId The ID of the pet to delete.
     * @param callback A lambda to inform the UI about the success or failure.
     */
    fun deletePet(
        petId: String,
        callback: (Boolean, String) -> Unit // Similar to addNewPet, consider if this callback is still needed
    ) {
        repo.deletePet(petId) { success, msg ->
            _message.postValue(msg) // Post the message
            callback(success, msg)
        }
    }
}