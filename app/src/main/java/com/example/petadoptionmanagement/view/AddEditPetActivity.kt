package com.example.petadoptionmanagement.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petadoptionmanagement.data.model.Pet
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme

class AddEditPetActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In a real scenario, you'd get the pet from the intent if it's an edit operation
        val petToEdit: Pet? = null // or intent.getParcelableExtra("PET_EXTRA")

        setContent {
            PetAdoptionManagementTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AddEditPetScreen(pet = petToEdit) {
                        // Handle save logic here
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditPetScreen(pet: Pet?, onSave: (Pet) -> Unit) {
    var name by remember { mutableStateOf(pet?.name ?: "") }
    var breed by remember { mutableStateOf(pet?.breed ?: "") }
    var age by remember { mutableStateOf(pet?.age ?: "") }
    var gender by remember { mutableStateOf(pet?.gender ?: "") }
    var description by remember { mutableStateOf(pet?.description ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (pet == null) "Add a New Pet" else "Edit Pet",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Pet Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = breed,
            onValueChange = { breed = it },
            label = { Text("Breed") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )

        Button(
            onClick = {
                val updatedPet = (pet ?: Pet()).copy(
                    name = name,
                    breed = breed,
                    age = age,
                    gender = gender,
                    description = description
                )
                onSave(updatedPet)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save Pet")
        }
    }
}
