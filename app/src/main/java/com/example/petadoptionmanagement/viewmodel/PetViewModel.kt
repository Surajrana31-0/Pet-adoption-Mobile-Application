package com.example.petadoptionmanagement.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.repository.PetRepository

class PetViewModel(private val repo: PetRepository) : ViewModel() {
    private val _pet = MutableLiveData<PetModel?>()
    val pet: LiveData<PetModel?> get() = _pet

    private val _allPets = MutableLiveData<List<PetModel>>()
    val allPets: LiveData<List<PetModel>> get() = _allPets

    private val _myAddedPets = MutableLiveData<List<PetModel>>() // For admin ownership
    val myAddedPets: LiveData<List<PetModel>> get() = _myAddedPets

    private val _availablePets = MutableLiveData<List<PetModel>>() // For adopters
    val availablePets: LiveData<List<PetModel>> get() = _availablePets

    private val _myAdoptedPets = MutableLiveData<List<PetModel>>() // For adopters/adopted
    val myAdoptedPets: LiveData<List<PetModel>> get() = _myAdoptedPets

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    fun clearSelectedPet() {
        _pet.value = null
    }

    fun clearMessage() {
        _message.value = null
    }

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        repo.uploadImage(context, imageUri) { imageUrl ->
            callback(imageUrl)
        }
    }

    fun addNewPet(
        petModel: PetModel,
        currentUserId: String,
        currentUserRole: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        // Only admins or the owner can add
        if (currentUserRole != "admin") {
            _loading.postValue(false)
            _message.postValue("Only admins can add pets.")
            callback(false, "Only admins can add pets.")
            return
        }
        petModel.addedBy = currentUserId
        repo.addPet(petModel) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            if (success) getAllPets()
            callback(success, msg)
        }
    }

    fun getPetById(petID: String) {
        _loading.postValue(true)
        repo.getPetById(petID) { success, msg, petData ->
            _loading.postValue(false)
            if (success) {
                _pet.postValue(petData)
            } else {
                _pet.postValue(null)
                _message.postValue(msg ?: "Failed to fetch pet $petID.")
            }
        }
    }

    fun getAllPets() {
        _loading.postValue(true)
        repo.getAllPets { success, msg, petsList ->
            _loading.postValue(false)
            if (success) {
                _allPets.postValue(petsList)
            } else {
                _allPets.postValue(emptyList())
                _message.postValue(msg ?: "Failed to fetch pets.")
            }
        }
    }

    fun getPetsByOwner(ownerId: String) {
        _loading.postValue(true)
        repo.getAllPets { success, msg, petsList ->
            _loading.postValue(false)
            if (success) {
                _myAddedPets.postValue(petsList.filter { it.addedBy == ownerId })
            } else {
                _myAddedPets.postValue(emptyList())
            }
        }
    }

    fun getAvailablePets() {
        _loading.postValue(true)
        repo.getAllPets { success, msg, petsList ->
            _loading.postValue(false)
            if (success) {
                _availablePets.postValue(petsList.filter { it.petStatus == "Available" })
            } else {
                _availablePets.postValue(emptyList())
            }
        }
    }

    fun getMyAdoptedPets(userId: String) {
        _loading.postValue(true)
        repo.getMyAdoptedPets(userId) { success, msg, petsList ->
            _loading.postValue(false)
            if (success) _myAdoptedPets.postValue(petsList)
            else {
                _myAdoptedPets.postValue(emptyList())
                _message.postValue(msg)
            }
        }
    }

    fun updatePet(
        petId: String,
        data: Map<String, Any>,
        currentUserId: String,
        currentUserRole: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        // Only admin or the one who added can update
        repo.getPetById(petId) { success, _, pet ->
            if (success && pet != null && (pet.addedBy == currentUserId || currentUserRole == "admin")) {
                repo.updatePet(petId, data) { s, msg ->
                    _loading.postValue(false)
                    _message.postValue(msg)
                    if (s) getAllPets()
                    callback(s, msg)
                }
            } else {
                _loading.postValue(false)
                _message.postValue("Not authorized to update pet.")
                callback(false, "Not authorized to update pet.")
            }
        }
    }

    fun deletePet(petId: String, currentUserId: String, currentUserRole: String, callback: (Boolean, String) -> Unit) {
        _loading.postValue(true)
        repo.getPetById(petId) { success, _, pet ->
            if (success && pet != null && (pet.addedBy == currentUserId || currentUserRole == "admin")) {
                repo.deletePet(petId) { s, msg ->
                    _loading.postValue(false)
                    _message.postValue(msg)
                    if (s) getAllPets()
                    callback(s, msg)
                }
            } else {
                _loading.postValue(false)
                _message.postValue("Not authorized to delete pet.")
                callback(false, "Not authorized to delete pet.")
            }
        }
    }

    fun applyForAdoption(
        petId: String,
        adopterId: String,
        adopterRole: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        if (adopterRole != "adopter") {
            _loading.postValue(false)
            _message.postValue("Only adopters may apply for adoption.")
            callback(false, "Only adopters may adopt.")
            return
        }
        repo.applyForAdoption(petId, adopterId, mapOf()) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            if (success) getAllPets()
            callback(success, msg)
        }
    }

    fun updatePetAdoptionStatus(
        petId: String,
        newStatus: String,
        adoptionId: String?,
        adminRole: String,
        callback: (Boolean, String) -> Unit
    ) {
        _loading.postValue(true)
        if (adminRole != "admin") {
            _loading.postValue(false)
            _message.postValue("Only admins may update adoption status.")
            callback(false, "Only admins may update status.")
            return
        }
        repo.updatePetAdoptionStatus(petId, newStatus, adoptionId) { success, msg ->
            _loading.postValue(false)
            _message.postValue(msg)
            if (success) getAllPets()
            callback(success, msg)
        }
    }
}
