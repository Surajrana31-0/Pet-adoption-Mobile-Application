// /model/PetModel.kt

package com.example.petadoptionmanagement.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Enum for pet status to maintain data integrity
enum class PetStatus {
    AVAILABLE,
    PENDING, // "Pending" is clearer than "Pending Adoption"
    ADOPTED
}

data class PetModel(
    var petId: String = "",
    var petName: String = "",
    var petBreed: String = "",
    var petType: String = "",
    var petGender: String = "",
    var petAge: String = "",
    var petDescription: String = "",
    var petStatus: PetStatus = PetStatus.AVAILABLE, // Use the enum
    var petImageUrl: String = "", // Holds the Cloudinary URL
    var adoptionId: String? = null,
    var adopterId: String? = null,
    var addedBy: String = "",

    @ServerTimestamp
    var timestamp: Date? = null // Using Date with @ServerTimestamp
) {
    // No-arg constructor for Firebase deserialization
    constructor() : this(
        petId = "",
        petName = "",
        petBreed = "",
        petType = "",
        petGender = "",
        petAge = "",
        petDescription = "",
        petStatus = PetStatus.AVAILABLE,
        petImageUrl = "",
        adoptionId = null,
        adopterId = null,
        addedBy = "",
        timestamp = null
    )
}
