package com.example.petadoptionmanagement.repository

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.petadoptionmanagement.model.PetModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors

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

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dd9sooenk", // Replace with your Cloudinary cloud name
            "api_key" to "281858352367463",    // Replace with your Cloudinary API key
            "api_secret" to "dj8vgOz6YCPGqqvQIGEa-dhQ0Ig" // Replace with your Cloudinary API secret
        )
    )

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

    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)

                // Remove extensions from file name before upload
                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?
                imageUrl = imageUrl?.replace("http://", "https://")

                // Run UI updates on the Main Thread
                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    override fun getFileNameFromUri(context: Context, uri: Uri): String? {
        // Implementation to get file name from URI (similar to ProductRepositoryImpl)
        // Use the content resolver to query the file name
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            return cursor.getString(nameIndex)
        }
        return null // Placeholder
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
        petID: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(petID).updateChildren(data).addOnCompleteListener { task ->
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
        petID: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(petID).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Pet deleted successfully!")
            } else {
                callback(false, "Failed to delete pet: ${task.exception?.message}")
            }
        }
    }
}