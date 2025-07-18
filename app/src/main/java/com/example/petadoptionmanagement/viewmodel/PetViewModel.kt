package com.example.petadoptionmanagement.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.repository.PetRepository

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

    /**
     * Initiates the process of adding a new pet via the repository.
     * @param petModel The PetModel object to add.
     * @param callback A lambda to inform the UI about the success or failure of the operation.
     */
    fun addNewPet(
        petModel: PetModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addPet(petModel, callback)
    }

    /**
     * Fetches a pet by its ID. Updates the `_pet` LiveData.
     * @param petID The ID of the pet to fetch.
     */
    fun getPetById(
        petID: String,
    ) {
        repo.getPetById(petID) { success, message, value ->
            if (success) {
                _pet.postValue(value)
            } else {
                _pet.postValue(null) // Post null or handle error state if pet not found
            }
        }
    }

    /**
     * Fetches all pets. Updates `_allPets` and `_loading` LiveData.
     */
    fun getAllPets() {
        _loading.postValue(true) // Indicate loading started
        repo.getAllPets { success, message, value ->
            if (success) {
                _loading.postValue(false) // Indicate loading finished
                _allPets.postValue(value) // Post the list of pets
            } else {
                _loading.postValue(false) // Indicate loading finished
                _allPets.postValue(emptyList()) // Post an empty list on failure
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
        callback: (Boolean, String) -> Unit
    ) {
        repo.updatePet(petId, data, callback)
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
        repo.deletePet(petId, callback)
    }
}