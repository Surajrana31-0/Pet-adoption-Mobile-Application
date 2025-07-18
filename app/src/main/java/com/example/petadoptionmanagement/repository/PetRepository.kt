package com.example.petadoptionmanagement.repository

import com.example.petadoptionmanagement.model.PetModel

/**
 * Interface defining the contract for pet data operations.
 * This is part of the Repository layer in the MVVM architecture.
 */
interface PetRepository {

    /**
     * Adds a new pet to the database.
     * @param petModel The PetModel object to be added.
     * @param callback A lambda function to be called upon completion, indicating success/failure and a message.
     */
    fun addPet(
        petModel: PetModel,
        callback: (Boolean, String) -> Unit
    )

    /**
     * Retrieves a single pet by its ID.
     * @param petID The unique ID of the pet to retrieve.
     * @param callback A lambda function to be called with success status, a message, and the retrieved PetModel (or null if not found).
     */
    fun getPetById(
        petID: String,
        callback: (Boolean, String, PetModel?) -> Unit
    )

    /**
     * Retrieves all pets from the database.
     * @param callback A lambda function to be called with success status, a message, and a list of PetModel objects.
     */
    fun getAllPets(callback: (Boolean, String, List<PetModel?>) -> Unit)

    /**
     * Updates an existing pet's data.
     * @param petId The ID of the pet to update.
     * @param data A map containing the fields to update and their new values.
     * @param callback A lambda function to be called upon completion, indicating success/failure and a message.
     */
    fun updatePet(
        petId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    )

    /**
     * Deletes a pet from the database.
     * @param petId The ID of the pet to delete.
     * @param callback A lambda function to be called upon completion, indicating success/failure and a message.
     */
    fun deletePet(
        petId: String,
        callback: (Boolean, String) -> Unit
    )
}