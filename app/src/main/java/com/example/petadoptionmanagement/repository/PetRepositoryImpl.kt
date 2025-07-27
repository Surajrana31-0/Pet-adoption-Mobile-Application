package com.example.petadoptionmanagement.repository

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.petadoptionmanagement.model.PetModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.InputStream
import java.util.concurrent.Executors

class PetRepositoryImpl : PetRepository {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.reference.child("pets")
    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dd9sooenk",
            "api_key" to "281858352367463",
            "api_secret" to "dj8vgOz6YCPGqqvQIGEa-dhQ0Ig"
        )
    )

    override fun addPet(petModel: PetModel, callback: (Boolean, String) -> Unit) {
        val id = ref.push().key ?: run {
            callback(false, "Failed to generate pet ID")
            return
        }
        petModel.petId = id
        ref.child(id).setValue(petModel).addOnCompleteListener { task ->
            if (task.isSuccessful) callback(true, "Pet added successfully!")
            else callback(false, "Failed to add pet: ${task.exception?.message}")
        }
    }

    override fun getPetById(petId: String, callback: (Boolean, String, PetModel?) -> Unit) {
        ref.child(petId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val pet = snapshot.getValue(PetModel::class.java)
                    callback(true, "Pet fetched successfully!", pet)
                } else {
                    callback(false, "Pet not found.", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}", null)
            }
        })
    }

    override fun getAllPets(callback: (Boolean, String, List<PetModel>) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pets = mutableListOf<PetModel>()
                for (petSnap in snapshot.children) {
                    val pet = petSnap.getValue(PetModel::class.java)
                    pet?.let { pets.add(it) }
                }
                callback(true, "All pets fetched successfully!", pets)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}", emptyList())
            }
        })
    }

    override fun updatePet(petId: String, data: Map<String, Any>, callback: (Boolean, String) -> Unit) {
        ref.child(petId).updateChildren(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Pet updated successfully!")
            } else {
                callback(false, "Failed to update pet: ${task.exception?.message}")
            }
        }
    }

    override fun deletePet(petId: String, callback: (Boolean, String) -> Unit) {
        ref.child(petId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Pet deleted successfully!")
            } else {
                callback(false, "Failed to delete pet: ${task.exception?.message}")
            }
        }
    }

    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val fileName = getFileNameFromUri(context, imageUri)?.substringBeforeLast(".") ?: "pet_${System.currentTimeMillis()}"
                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )
                var imageUrl = response["url"] as String?
                imageUrl = imageUrl?.replace("http://", "https://")
                Handler(Looper.getMainLooper()).post { callback(imageUrl) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { callback(null) }
            }
        }
    }

    override fun getFileNameFromUri(context: Context, uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return null
    }

    override fun getMyAdoptedPets(userId: String, callback: (Boolean, String, List<PetModel>) -> Unit) {
        ref.orderByChild("adopterId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adoptedPets = mutableListOf<PetModel>()
                    for (petSnapshot in snapshot.children) {
                        val pet = petSnapshot.getValue(PetModel::class.java)
                        pet?.let { adoptedPets.add(it) }
                    }
                    callback(true, "Adopted pets fetched successfully.", adoptedPets)
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(false, "Database error: ${error.message}", emptyList())
                }
            })
    }

    override fun applyForAdoption(petId: String, adopterId: String, applicationDetails: Map<String, Any>, callback: (Boolean, String) -> Unit) {
        ref.child(petId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pet = snapshot.getValue(PetModel::class.java)
                if (pet == null) {
                    callback(false, "Pet not found.")
                    return
                }
                if (!pet.petStatus.equals("Available", ignoreCase = true)) {
                    callback(false, "Pet is not available for adoption.")
                    return
                }
                val updates = mapOf(
                    "petStatus" to "Pending Adoption",
                    "adopterId" to adopterId
                )
                ref.child(petId).updateChildren(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, "Application submitted successfully.")
                    } else {
                        callback(false, "Failed to submit application: ${task.exception?.message}")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, "Database error: ${error.message}")
            }
        })
    }

    override fun updatePetAdoptionStatus(petId: String, newStatus: String, adoptionId: String?, callback: (Boolean, String) -> Unit) {
        val updates = mutableMapOf<String, Any>("petStatus" to newStatus)
        if (newStatus.equals("Adopted", ignoreCase = true) && adoptionId != null) {
            updates["adopterId"] = adoptionId
        } else if (newStatus.equals("Available", ignoreCase = true)) {
            updates["adopterId"] = "" // Clear adopterId
        }
        ref.child(petId).updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "Pet adoption status updated successfully.")
            } else {
                callback(false, "Failed to update pet status: ${task.exception?.message}")
            }
        }
    }
}
