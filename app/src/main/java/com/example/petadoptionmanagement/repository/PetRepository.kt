package com.example.petadoptionmanagement.repository

import android.content.Context
import android.net.Uri
import com.example.petadoptionmanagement.model.PetModel

interface PetRepository {
    fun addPet(petModel: PetModel, callback: (Boolean, String) -> Unit)
    fun getPetById(petId: String, callback: (Boolean, String, PetModel?) -> Unit)
    fun getAllPets(callback: (Boolean, String, List<PetModel>) -> Unit)
    fun updatePet(petId: String, data: Map<String, Any>, callback: (Boolean, String) -> Unit)
    fun deletePet(petId: String, callback: (Boolean, String) -> Unit)

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit)
    fun getFileNameFromUri(context: Context, uri: Uri): String?

    fun getMyAdoptedPets(userId: String, callback: (Boolean, String, List<PetModel>) -> Unit)
    fun applyForAdoption(
        petId: String,
        adopterId: String,
        applicationDetails: Map<String, Any>,
        callback: (Boolean, String) -> Unit
    )
    fun updatePetAdoptionStatus(
        petId: String,
        newStatus: String,
        adoptionId: String?,
        callback: (Boolean, String) -> Unit
    )
}
