package com.example.petadoptionmanagement.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme // Correct import for MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // New import for viewModel()
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController // New import for rememberNavController
import com.example.petadoptionmanagement.model.PetModel // CORRECT IMPORT AND USAGE
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.view.ui.theme.PetAdoptionManagementTheme // Assuming your theme file
import com.example.petadoptionmanagement.viewmodel.PetViewModel
import com.example.petadoptionmanagement.viewmodel.PetViewModelFactory // New import for the custom factory


class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                // IMPORTANT: Proper ViewModel Instantiation for an Activity
                val context = LocalContext.current
                val petRepository = remember { PetRepositoryImpl() } // Still instantiate repo here
                val petViewModel = viewModel<PetViewModel>(
                    factory = PetViewModelFactory(petRepository)
                )

                // IMPORTANT: NavController for Compose Navigation
                // If this Activity is part of a larger NavHost, you'd get the navController from it.
                // For a standalone Activity, we create one for preview/testing purposes or a simple internal graph.
                val navController = rememberNavController() // Create a NavController instance

                // Now pass the correctly instantiated ViewModel and NavController to the screen
                PetDashboardScreen(navController = navController, viewModel = petViewModel)
            }
        }
    }
}

@Composable
fun PetDashboardScreen(
    navController: NavController, // IMPORTANT CHANGE: Added NavController parameter
    viewModel: PetViewModel // IMPORTANT CHANGE: Added PetViewModel parameter
) {
    val context = LocalContext.current
    // No need for 'activity' variable for navigation or Toast (Toast uses context directly)

    val pets = viewModel.allPets.observeAsState(initial = emptyList())
    val loading = viewModel.loading.observeAsState(initial = true)

    // Using LaunchedEffect to trigger data fetch only once when the composable enters the composition
    LaunchedEffect(Unit) {
        viewModel.getAllPets()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // IMPORTANT CHANGE: Use NavController for navigation
                // Make sure "add_pet_screen" is a defined route in your NavHost
                navController.navigate("add_pet_screen")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Pet")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (loading.value) {
                item {
                    CircularProgressIndicator(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp))
                }
            } else {
                if (pets.value.isEmpty()) {
                    item {
                        Text(
                            text = "No pets found. Click '+' to add one!",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                items(pets.value.size) { index ->
                    val pet = pets.value[index] // This 'pet' is now of type PetModel?

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // CORRECTED: Use petName, petBreed, petAge, petStatus from PetModel
                            Text(text = "${pet?.petName ?: "Unknown"}", style = MaterialTheme.typography.headlineSmall)
                            Text(text = "${pet?.petBreed ?: "Unknown"} | ${pet?.petAge ?: "Unknown"}")
                            Text(text = "Adoption Status: ${pet?.petStatus ?: "Unknown"}") // Display status directly

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        // IMPORTANT CHANGE: Use NavController for navigation
                                        // Make sure "update_pet_screen/{petId}" is a defined route
                                        // and petId is passed as an argument.
                                        pet?.petId?.let { id -> // Use pet.petId here
                                            navController.navigate("update_pet_screen/$id")
                                        } ?: Toast.makeText(context, "Pet ID is null for update.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Gray)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Pet")
                                }
                                IconButton(
                                    onClick = {
                                        pet?.petId?.let { petId -> // Use pet.petId here
                                            viewModel.deletePet(petId) { success, message ->
                                                if (success) {
                                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                    // After deletion, you might want to refresh the list
                                                    viewModel.getAllPets()
                                                } else {
                                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } ?: Toast.makeText(context, "Pet ID is null, cannot delete.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Pet")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    PetAdoptionManagementTheme {
        // For preview, provide a dummy NavController and a dummy ViewModel
        val navController = rememberNavController()
        // This ViewModel is just for preview; it won't actually fetch data
        val dummyViewModel = PetViewModel(remember { PetRepositoryImpl() })
        PetDashboardScreen(navController = navController, viewModel = dummyViewModel)
    }
}