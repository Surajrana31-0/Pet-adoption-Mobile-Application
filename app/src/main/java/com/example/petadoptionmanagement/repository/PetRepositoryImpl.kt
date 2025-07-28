// /repository/PetRepositoryImpl.kt

package com.example.petadoptionmanagement.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.petadoptionmanagement.model.PetModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import javax.inject.Inject
import kotlin.concurrent.thread

class PetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cloudinary: Cloudinary,
    private val context: Context // Injected Application Context
) : PetRepository {

    companion object {
        // Use a constant for the collection name to avoid typos
        private const val PETS_COLLECTION = "pets"
    }

    private val petsCollection = firestore.collection(PETS_COLLECTION)

    override fun addPet(pet: PetModel, onResult: (Result<Unit>) -> Unit) {
        // If petId is empty, generate a new one. Otherwise, use the provided one.
        val docRef = if (pet.petId.isEmpty()) {
            petsCollection.document()
        } else {
            petsCollection.document(pet.petId)
        }

        // Set the final ID in the model and save it
        docRef.set(pet.copy(petId = docRef.id))
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun getPetById(petId: String, onResult: (Result<PetModel?>) -> Unit): ListenerRegistration {
        return petsCollection.document(petId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                onResult(Result.failure(error))
                return@addSnapshotListener
            }
            // Use toObject to deserialize the document into your PetModel
            val pet = snapshot?.toObject<PetModel>()
            onResult(Result.success(pet))
        }
    }

    override fun getAllPets(onResult: (Result<List<PetModel>>) -> Unit): ListenerRegistration {
        return petsCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                onResult(Result.failure(error))
                return@addSnapshotListener
            }
            // Use toObjects to deserialize the query snapshot into a list of PetModels
            val pets = querySnapshot?.toObjects<PetModel>() ?: emptyList()
            onResult(Result.success(pets))
        }
    }

    override fun updatePet(petId: String, data: Map<String, Any>, onResult: (Result<Unit>) -> Unit) {
        petsCollection.document(petId).update(data)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun deletePet(petId: String, onResult: (Result<Unit>) -> Unit) {
        petsCollection.document(petId).delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun uploadPetImage(imageUri: Uri, onResult: (Result<String>) -> Unit) {
        // Network operations must not be performed on the main thread
        thread {
            try {
                // Use the application context's content resolver to open the image stream
                val inputStream = context.contentResolver.openInputStream(imageUri)

                // Upload the stream to Cloudinary
                val response = cloudinary.uploader().upload(
                    inputStream,
                    ObjectUtils.asMap("resource_type", "image")
                )

                // Extract the secure URL from the response
                val url = response["secure_url"] as? String
                    ?: throw IllegalStateException("Cloudinary did not return a secure URL.")

                // Report success
                onResult(Result.success(url))
            } catch (e: Exception) {
                // Report failure
                onResult(Result.failure(e))
            }
        }
    }
}
