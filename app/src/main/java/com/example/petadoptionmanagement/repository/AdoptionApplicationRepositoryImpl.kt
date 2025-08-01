// /repository/AdoptionApplicationRepositoryImpl.kt

package com.example.petadoptionmanagement.repository

import com.example.petadoptionmanagement.model.AdoptionApplicationModel
import com.example.petadoptionmanagement.model.ApplicationStatus
import com.example.petadoptionmanagement.model.PetStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import javax.inject.Inject

class AdoptionApplicationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdoptionApplicationRepository {

    companion object {
        private const val APPLICATIONS_COLLECTION = "adoption_applications"
        private const val PETS_COLLECTION = "pets"
    }

    private val applicationsCollection = firestore.collection(APPLICATIONS_COLLECTION)
    private val petsCollection = firestore.collection(PETS_COLLECTION)

    override fun submitApplication(application: AdoptionApplicationModel, onResult: (Result<Unit>) -> Unit) {
        val docRef = applicationsCollection.document(application.applicationId.ifEmpty { applicationsCollection.document().id })
        docRef.set(application.copy(applicationId = docRef.id))
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    override fun getApplicationById(applicationId: String, onResult: (Result<AdoptionApplicationModel?>) -> Unit): ListenerRegistration {
        return applicationsCollection.document(applicationId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                onResult(Result.failure(error))
                return@addSnapshotListener
            }
            onResult(Result.success(snapshot?.toObject<AdoptionApplicationModel>()))
        }
    }

    override fun getApplicationsForUser(userId: String, onResult: (Result<List<AdoptionApplicationModel>>) -> Unit): ListenerRegistration {
        return applicationsCollection.whereEqualTo("applicantId", userId).addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                onResult(Result.failure(error))
                return@addSnapshotListener
            }
            onResult(Result.success(querySnapshot?.toObjects<AdoptionApplicationModel>() ?: emptyList()))
        }
    }

    override fun getAllApplications(onResult: (Result<List<AdoptionApplicationModel>>) -> Unit): ListenerRegistration {
        return applicationsCollection.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                onResult(Result.failure(error))
                return@addSnapshotListener
            }
            onResult(Result.success(querySnapshot?.toObjects<AdoptionApplicationModel>() ?: emptyList()))
        }
    }

    override fun updateApplicationStatus(applicationId: String, newStatus: ApplicationStatus, onResult: (Result<Unit>) -> Unit) {
        applicationsCollection.document(applicationId).update("status", newStatus)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
        if (newStatus == ApplicationStatus.APPROVED) {
            // Fetch the application to get petId
            applicationsCollection.document(applicationId).get().addOnSuccessListener { doc ->
                val app = doc.toObject(AdoptionApplicationModel::class.java)
                val petId = app?.petId
                if (!petId.isNullOrBlank()) {
                    petsCollection.document(petId).update("petStatus", PetStatus.ADOPTED)
                }
            }
        }

    }

    override fun applyForPet(application: AdoptionApplicationModel, petId: String, onResult: (Result<Unit>) -> Unit) {
        val petRef = petsCollection.document(petId)
        val appRef = applicationsCollection.document() // New application reference

        firestore.runTransaction { transaction ->
            val petSnapshot = transaction.get(petRef)
            val petStatus = petSnapshot.getString("petStatus")

            // Abort transaction if pet is not available
            if (petStatus != PetStatus.AVAILABLE.name) {
                throw IllegalStateException("This pet is no longer available for adoption.")
            }

            // Perform the atomic updates
            transaction.update(petRef, "petStatus", PetStatus.PENDING)
            transaction.set(appRef, application.copy(applicationId = appRef.id))
            null
        }.addOnSuccessListener {
            onResult(Result.success(Unit))
        }.addOnFailureListener { e ->
            onResult(Result.failure(e))
        }
    }


}
