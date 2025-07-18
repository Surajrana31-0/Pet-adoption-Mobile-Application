package com.example.petadoptionmanagement.repository

import com.example.petadoptionmanagement.model.PetModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Concrete implementation of PetRepository using Firebase Realtime Database.
 * This class handles all direct interactions with the Firebase database.
 */
class PetRepositoryImpl : PetRepository {

    // Get an instance of the Firebase Realtime Database
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    // Get a reference to the "pets" node in your database
    // Ensure this matches the path where you want to store pet data
    private val ref: DatabaseReference = database.reference.child("pets")

    /**
     * Adds a new pet record to Firebase.
     * A unique ID is generated using push().key.
     */
    override fun addPet(
        petModel: PetModel,
        callback: (Boolean, String) -> Unit
    ) {
        // Generate a unique key for the new pet
        val id = ref.push().key.toString()
        petModel.petId = id // Set the generated ID to the pet model

        // Set the pet object under its unique ID in Firebase
        ref.child(petModel.petId).setValue(petModel).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Pet added successfully!")
            } else {
                callback(false, "Failed to add pet: ${task.exception?.message}")
            }
        }
    }

    /**
     * Fetches a single pet record by its ID from Firebase.
     */
    override fun getPetById(
        petID: String,
        callback: (Boolean, String, PetModel?) -> Unit
    ) {
        ref.child(petID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Convert the DataSnapshot to a PetModel object
                    val pet = snapshot.getValue(PetModel::class.java)
                    if (pet != null) {
                        callback(true, "Pet fetched successfully!", pet)
                    } else {
                        callback(false, "Failed to parse pet data.", null)
                    }
                } else {
                    callback(false, "Pet not found.", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}", null)
            }
        })
    }

    /**
     * Fetches all pet records from Firebase.
     */
    override fun getAllPets(callback: (Boolean, String, List<PetModel?>) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allPets = mutableListOf<PetModel>()
                for (eachPetSnapshot in snapshot.children) {
                    val pet = eachPetSnapshot.getValue(PetModel::class.java)
                    if (pet != null) {
                        allPets.add(pet)
                    }
                }
                callback(true, "All pets fetched successfully!", allPets)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}", emptyList())
            }
        })
    }

    /**
     * Updates specific fields of an existing pet record in Firebase.
     */
    override fun updatePet(
        petId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(petId).updateChildren(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Pet updated successfully!")
            } else {
                callback(false, "Failed to update pet: ${task.exception?.message}")
            }
        }
    }

    /**
     * Deletes a pet record from Firebase.
     */
    override fun deletePet(
        petId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(petId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Pet deleted successfully!")
            } else {
                callback(false, "Failed to delete pet: ${task.exception?.message}")
            }
        }
    }
}