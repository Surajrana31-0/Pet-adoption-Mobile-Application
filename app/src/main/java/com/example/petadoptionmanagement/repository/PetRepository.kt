// /repository/PetRepository.kt

package com.example.petadoptionmanagement.repository

import android.net.Uri
import com.example.petadoptionmanagement.model.PetModel
import com.google.firebase.firestore.ListenerRegistration

/**
 * Interface for managing pet data using a callback-based approach.
 * It abstracts the data source (Firestore) and focuses solely on pet operations.
 */
interface PetRepository {

    /**
     * Adds a new pet to the data source.
     * @param pet The pet to add.
     * @param onResult Callback invoked with the result of the operation.
     */
    fun addPet(pet: PetModel, onResult: (Result<Unit>) -> Unit)

    /**
     * Fetches a single pet by its ID and listens for real-time updates.
     * The caller is responsible for removing the listener via the returned ListenerRegistration.
     * @param petId The ID of the pet.
     * @param onResult Callback invoked with the result.
     * @return A ListenerRegistration to allow detaching the listener.
     */
    fun getPetById(petId: String, onResult: (Result<PetModel?>) -> Unit): ListenerRegistration

    /**
     * Fetches all pets and listens for real-time updates.
     * The caller is responsible for removing the listener via the returned ListenerRegistration.
     * @param onResult Callback invoked with the list of pets.
     * @return A ListenerRegistration to allow detaching the listener.
     */
    fun getAllPets(onResult: (Result<List<PetModel>>) -> Unit): ListenerRegistration

    /**
     * Updates an existing pet's details in the data source.
     * @param petId The ID of the pet to update.
     * @param data A map of the fields to update.
     * @param onResult Callback invoked with the result of the operation.
     */
    fun updatePet(petId: String, data: Map<String, Any>, onResult: (Result<Unit>) -> Unit)

    /**
     * Deletes a pet from the data source.
     * @param petId The ID of the pet to delete.
     * @param onResult Callback invoked with the result of the operation.
     */
    fun deletePet(petId: String, onResult: (Result<Unit>) -> Unit)

    /**
     * Uploads a pet's image to a cloud service like Cloudinary.
     * @param imageUri The local URI of the image file.
     * @param onResult Callback invoked with the public URL of the uploaded image.
     */
    fun uploadPetImage(imageUri: Uri, onResult: (Result<String>) -> Unit)
}
