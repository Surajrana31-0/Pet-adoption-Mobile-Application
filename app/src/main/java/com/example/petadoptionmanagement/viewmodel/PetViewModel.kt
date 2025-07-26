package com.example.petadoptionmanagement.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.repository.PetRepository
import kotlinx.coroutines.launch // Import for launch

/**
 * ViewModel for Pet-related operations.
 * This class prepares and manages data for the UI (View) and handles
 * communication with the PetRepository.
 */
class PetViewModel(private val repo: PetRepository) : ViewModel() {

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit){
        repo.uploadImage(context,imageUri,callback)
    }

    // LiveData for a single pet (e.g., when fetching by ID)
    private val _pet = MutableLiveData<PetModel?>()
    val pet: LiveData<PetModel?> get() = _pet

    // LiveData for a list of all pets
    private val _allPets = MutableLiveData<List<PetModel?>>()
    val allPets: LiveData<List<PetModel?>> get() = _allPets

    // LiveData for indicating loading state
    private val _loading = MutableLiveData<Boolean>()
    var loading: MutableLiveData<Boolean> = _loading
        get() = _loading // Ensure public var uses the backing field for getter

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
        repo.addPet(petModel) { success, msg ->
            _message.postValue(msg) // Post the message from the repository callback
            callback(success, msg) // Keep callback for now, can be refactored later
        }
    }

    /**
     * Fetches a pet by its ID. Updates the `_pet` LiveData.
     * @param petID The ID of the pet to fetch.
     */
    fun getPetById(
        petID: String,
    ) {
        repo.getPetById(petID) { value, success, msg -> // Adjusted order to match repo
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
        repo.getAllPets { data, success, message -> // Adjusted to match repo
            _loading.postValue(false) // Indicate loading finished
            if (success) {
                Log.d("PetViewModel", "GetAllPets successful: $message")
                _allPets.postValue(data) // Post the list of pets
            } else {
                Log.d("PetViewModel", "GetAllPets failed: $message")
                _allPets.postValue(emptyList()) // Post an empty list on failure
                _message.postValue(message ?: "Failed to fetch pets.") // Post an error message
            } // Added missing closing brace
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