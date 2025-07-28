// /repository/AdoptionApplicationRepository.kt

package com.example.petadoptionmanagement.repository

import com.example.petadoptionmanagement.model.AdoptionApplicationModel
import com.example.petadoptionmanagement.model.ApplicationStatus
import com.google.firebase.firestore.ListenerRegistration

/**
 * Interface for managing adoption application data using a callback-based approach.
 */
interface AdoptionApplicationRepository {

    /**
     * Submits a new adoption application to the data source.
     * @param application The application data to be saved.
     * @param onResult Callback invoked with the result of the operation.
     */
    fun submitApplication(application: AdoptionApplicationModel, onResult: (Result<Unit>) -> Unit)

    /**
     * Fetches a single adoption application by its ID and listens for real-time updates.
     * @param applicationId The unique ID of the application.
     * @param onResult Callback invoked with the application data or an error.
     * @return A ListenerRegistration to allow detaching the listener.
     */
    fun getApplicationById(applicationId: String, onResult: (Result<AdoptionApplicationModel?>) -> Unit): ListenerRegistration

    /**
     * Fetches all applications submitted by a specific user and listens for real-time updates.
     * @param userId The ID of the applicant.
     * @param onResult Callback invoked with the list of applications or an error.
     * @return A ListenerRegistration to allow detaching the listener.
     */
    fun getApplicationsForUser(userId: String, onResult: (Result<List<AdoptionApplicationModel>>) -> Unit): ListenerRegistration

    /**
     * Fetches all applications for an admin to review and listens for real-time updates.
     * @param onResult Callback invoked with the list of all applications or an error.
     * @return A ListenerRegistration to allow detaching the listener.
     */
    fun getAllApplications(onResult: (Result<List<AdoptionApplicationModel>>) -> Unit): ListenerRegistration

    /**
     * Updates the status of an existing application.
     * @param applicationId The ID of the application to update.
     * @param newStatus The new status to set.
     * @param onResult Callback invoked with the result of the operation.
     */
    fun updateApplicationStatus(applicationId: String, newStatus: ApplicationStatus, onResult: (Result<Unit>) -> Unit)

    /**
     * A transactional operation that submits an application and updates the pet's status.
     * @param application The application to submit.
     * @param petId The ID of the pet to be updated.
     * @param onResult Callback invoked with the result of the transaction.
     */
    fun applyForPet(application: AdoptionApplicationModel, petId: String, onResult: (Result<Unit>) -> Unit)
}
