package com.example.petadoptionmanagement.data.model

import com.google.firebase.firestore.DocumentId

data class AdoptionApplication(
    @DocumentId
    val id: String = "",
    val petId: String = "",
    val adopterId: String = "", // This would be the UID of the logged-in user
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending" // e.g., Pending, Approved, Rejected
)
