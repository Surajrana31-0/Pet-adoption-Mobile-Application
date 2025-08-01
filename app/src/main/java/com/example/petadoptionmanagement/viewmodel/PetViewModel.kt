// /viewmodel/PetViewModel.kt

package com.example.petadoption.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.repository.PetRepository
import com.google.firebase.firestore.ListenerRegistration

class PetViewModel(private val petRepository: PetRepository) : ViewModel() {

    // LiveData for a single selected pet
    private val _pet = MutableLiveData<PetModel?>()
    val pet: LiveData<PetModel?> get() = _pet

    // The single source of truth for all pets, updated in real-time
    private val _allPets = MutableLiveData<List<PetModel>>()
    val allPets: LiveData<List<PetModel>> get() = _allPets

    // Status message for the UI (e.g., for showing Toast messages)
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // Loading state for showing/hiding progress indicators
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Store listener registrations to remove them later
    private var allPetsListener: ListenerRegistration? = null
    private var singlePetListener: ListenerRegistration? = null

    init {
        // Start listening for real-time updates as soon as the ViewModel is created
        fetchAllPets()
    }

     fun fetchAllPets() {
        _isLoading.postValue(true)
        // Remove any existing listener before attaching a new one
        allPetsListener?.remove()
        allPetsListener = petRepository.getAllPets { result ->
            result.fold(
                onSuccess = { pets -> _allPets.postValue(pets) },
                onFailure = { e ->
                    _allPets.postValue(emptyList())
                    _message.postValue("Failed to load pets: ${e.message}")
                }
            )
            _isLoading.postValue(false)
        }
    }

    fun getPetById(petId: String) {
        if (petId.isBlank()) {
            _pet.postValue(null)
            _message.postValue("Cannot fetch pet details: Pet ID is blank.")
            _isLoading.postValue(false)
            return
        }
        _isLoading.postValue(true)
        singlePetListener?.remove()
        singlePetListener = petRepository.getPetById(petId) { result ->
            result.fold(
                onSuccess = { petData -> _pet.postValue(petData) },
                onFailure = { e ->
                    _pet.postValue(null)
                    _message.postValue("Failed to fetch pet details: ${e.message}")
                }
            )
            _isLoading.postValue(false)
        }
    }


    fun addNewPet(petModel: PetModel, imageUri: Uri?) {
        _isLoading.postValue(true)
        if (imageUri != null) {
            petRepository.uploadPetImage(imageUri) { uploadResult ->
                uploadResult.fold(
                    onSuccess = { imageUrl ->
                        // On successful upload, add the pet with the image URL
                        val finalPet = petModel.copy(petImageUrl = imageUrl)
                        createPetInDatabase(finalPet)
                    },
                    onFailure = { e ->
                        _isLoading.postValue(false)
                        _message.postValue("Image upload failed: ${e.message}")
                    }
                )
            }
        } else {
            // If no image, add the pet directly
            createPetInDatabase(petModel)
        }
    }

    private fun createPetInDatabase(pet: PetModel) {
        petRepository.addPet(pet) { result ->
            result.fold(
                onSuccess = { _message.postValue("Pet added successfully.") },
                onFailure = { e -> _message.postValue("Failed to add pet: ${e.message}") }
            )
            _isLoading.postValue(false)
        }
    }

    fun updatePet(petId: String, data: Map<String, Any>) {
        _isLoading.postValue(true)
        petRepository.updatePet(petId, data) { result ->
            result.fold(
                onSuccess = { _message.postValue("Pet updated successfully.") },
                onFailure = { e -> _message.postValue("Failed to update pet: ${e.message}") }
            )
            _isLoading.postValue(false)
        }
    }

    fun deletePet(petId: String) {
        _isLoading.postValue(true)
        petRepository.deletePet(petId) { result ->
            result.fold(
                onSuccess = { _message.postValue("Pet deleted successfully.") },
                onFailure = { e -> _message.postValue("Failed to delete pet: ${e.message}") }
            )
            _isLoading.postValue(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // IMPORTANT: Remove listeners to prevent memory leaks and unnecessary background work
        allPetsListener?.remove()
        singlePetListener?.remove()
    }


    fun clearMessage() {
        _message.value = "" // This is allowed because it's inside the ViewModel
    }

    // NEW FUNCTION FOR IMAGE UPLOAD AND UPDATE
    fun updatePetImageAndDetails(petId: String, imageUri: Uri, currentDetails: Map<String, Any>) {
        _isLoading.postValue(true)
        petRepository.uploadPetImage(imageUri) { uploadResult ->
            uploadResult.fold(
                onSuccess = { newImageUrl ->
                    val updatedData = currentDetails.toMutableMap().apply {
                        put("petImageUrl", newImageUrl)
                    }
                    // Call the existing updatePet function with the new image URL
                    updatePet(petId, updatedData)
                },
                onFailure = { e ->
                    _message.postValue("Image upload failed: ${e.message}")
                    _isLoading.postValue(false)
                }
            )
        }
    }
}
