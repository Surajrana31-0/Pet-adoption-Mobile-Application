// /viewmodel/AdoptionApplicationViewModel.kt

package com.example.petadoptionmanagement.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petadoptionmanagement.model.AdoptionApplicationModel
import com.example.petadoptionmanagement.model.ApplicationStatus
import com.example.petadoptionmanagement.repository.AdoptionApplicationRepository
import com.google.firebase.firestore.ListenerRegistration

class AdoptionApplicationViewModel(private val repository: AdoptionApplicationRepository) : ViewModel() {

    // LiveData for applications submitted by the current user
    private val _userApplications = MutableLiveData<List<AdoptionApplicationModel>>()
    val userApplications: LiveData<List<AdoptionApplicationModel>> get() = _userApplications

    // LiveData for all applications (for admin view)
    private val _allApplications = MutableLiveData<List<AdoptionApplicationModel>>()
    val allApplications: LiveData<List<AdoptionApplicationModel>> get() = _allApplications

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    // To hold listener registrations for cleanup
    private var userAppsListener: ListenerRegistration? = null
    private var allAppsListener: ListenerRegistration? = null

    /**
     * Submits an application for a pet. This is a transactional operation.
     */
    fun applyForPet(application: AdoptionApplicationModel, petId: String) {
        _isLoading.postValue(true)
        repository.applyForPet(application, petId) { result ->
            result.fold(
                onSuccess = { _message.postValue("Application submitted successfully!") },
                onFailure = { e -> _message.postValue("Application failed: ${e.message}") }
            )
            _isLoading.postValue(false)
        }
    }

    /**
     * Fetches and listens for real-time updates on applications for a specific user.
     */
    fun getApplicationsForUser(userId: String) {
        _isLoading.postValue(true)
        // Ensure the old listener is removed before creating a new one
        userAppsListener?.remove()
        userAppsListener = repository.getApplicationsForUser(userId) { result ->
            result.fold(
                onSuccess = { applications -> _userApplications.postValue(applications) },
                onFailure = { e ->
                    _userApplications.postValue(emptyList())
                    _message.postValue("Failed to load your applications: ${e.message}")
                }
            )
            _isLoading.postValue(false)
        }
    }

    /**
     * Fetches a single adoption application by its ID (one-time fetch).
     * This calls the repository's getApplicationById which returns a ListenerRegistration,
     * so we manage that ListenerRegistration here.
     * @param applicationId The unique ID of the application.
     * @param onResult Callback invoked with the application data or an error.
     */
    private var singleApplicationListener: ListenerRegistration? = null // Add this to manage listener
    fun getApplicationById(applicationId: String, onResult: (Result<AdoptionApplicationModel?>) -> Unit) {
        _isLoading.postValue(true)
        // Remove previous listener if exists
        singleApplicationListener?.remove()
        singleApplicationListener = repository.getApplicationById(applicationId) { result ->
            result.fold(
                onSuccess = { app ->
                    onResult(Result.success(app))
                },
                onFailure = { e ->
                    onResult(Result.failure(e))
                    _message.postValue("Failed to load application details: ${e.message}")
                }
            )
            _isLoading.postValue(false)
        }
    }

    /**
     * Fetches and listens for real-time updates on all applications (for admin use).
     */
    fun getAllApplications() {
        _isLoading.postValue(true)
        // Ensure the old listener is removed
        allAppsListener?.remove()
        allAppsListener = repository.getAllApplications { result ->
            result.fold(
                onSuccess = { applications -> _allApplications.postValue(applications) },
                onFailure = { e ->
                    _allApplications.postValue(emptyList())
                    _message.postValue("Failed to load all applications: ${e.message}")
                }
            )
            _isLoading.postValue(false)
        }
    }

    /**
     * Updates the status of an application (e.g., to APPROVED or REJECTED).
     * NOTE: This should also trigger an update in the Pet's status, which should
     * be handled in a more advanced implementation (e.g., via Cloud Functions or in the repo).
     */
    fun updateApplicationStatus(applicationId: String, newStatus: ApplicationStatus) {
        _isLoading.postValue(true)
        repository.updateApplicationStatus(applicationId, newStatus) { result ->
            result.fold(
                onSuccess = { _message.postValue("Application status updated.") },
                onFailure = { e -> _message.postValue("Failed to update status: ${e.message}") }
            )
            _isLoading.postValue(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // CRITICAL: Remove listeners to prevent memory leaks
        userAppsListener?.remove()
        allAppsListener?.remove()
        singleApplicationListener?.remove()
    }
}
