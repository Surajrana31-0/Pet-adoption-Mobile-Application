// /model/AdoptionApplicationModel.kt

package com.example.petadoptionmanagement.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Enum for status to ensure type safety and prevent invalid values
enum class ApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class AdoptionApplicationModel(
    val applicationId: String = "",
    val petId: String = "",
    val petName: String = "",
    val applicantId: String = "",
    val applicantName: String = "",
    val message: String = "",
    var status: ApplicationStatus = ApplicationStatus.PENDING, // Use the enum

    @ServerTimestamp
    val timestamp: Date? = null // Use Firebase ServerTimestamp for reliability
) {
    // No-arg constructor for Firebase
    constructor() : this("", "", "", "", "", "", ApplicationStatus.PENDING, null)
}
