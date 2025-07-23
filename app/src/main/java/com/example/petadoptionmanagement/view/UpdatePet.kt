package com.example.petadoptionmanagement.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.view.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.PetViewModel
import com.example.petadoptionmanagement.viewmodel.PetViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel // For preview

/**
 * Composable screen for updating an existing pet.
 * This screen fetches pet details based on a provided ID, pre-fills a form,
 * and allows for updating the pet's information in the database.
 *
 * @param navController The NavController for navigating back.
 * @param petViewModel The ViewModel instance responsible for pet data operations.
 * @param petId The ID of the pet to be updated, passed as a navigation argument.
 */
@OptIn(ExperimentalMaterial3Api::class) // Required for TopAppBar and ExposedDropdownMenuBox
@Composable
fun UpdatePetScreen(
    navController: NavController,
    petViewModel: PetViewModel,
    petId: String? // Pet ID passed as a navigation argument
) {
    val context = LocalContext.current

    // State variables for form fields, initialized to empty strings
    var petName by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Dog") }
    var petAge by remember { mutableStateOf("") }
    var petDescription by remember { mutableStateOf("") }
    var petStatus by remember { mutableStateOf("Available") }
    var petImageUrl by remember { mutableStateOf("") }

    // Dropdown menu states and options
    val petTypes = listOf("Dog", "Cat", "Bird", "Rabbit", "Other")
    var expandedType by remember { mutableStateOf(false) }

    val petStatuses = listOf("Available", "Adopted", "Pending", "On Hold")
    var expandedStatus by remember { mutableStateOf(false) }

    // Observe the single pet LiveData from ViewModel
    val petToUpdate by petViewModel.pet.observeAsState(initial = null)
    val isLoading by petViewModel.loading.observeAsState(initial = false)
    val message by petViewModel.message.observeAsState(initial = "") // Assuming PetViewModel has a message LiveData


    // LaunchedEffect to fetch pet data when the screen is first composed or petId changes
    LaunchedEffect(petId) {
        if (!petId.isNullOrBlank()) {
            petViewModel.getPetById(petId)
        } else {
            Toast.makeText(context, "Pet ID not provided for update.", Toast.LENGTH_SHORT).show()
            navController.popBackStack() // Go back if no ID
        }
    }

    // LaunchedEffect to pre-fill form fields when petToUpdate data is available
    LaunchedEffect(petToUpdate) {
        petToUpdate?.let { pet ->
            petName = pet.petName
            petBreed = pet.petBreed
            petType = pet.petType
            petAge = pet.petAge
            petDescription = pet.petDescription
            petStatus = pet.petStatus
            petImageUrl = pet.petImageUrl // Pre-fill image URL if you store it
        }
    }

    // Effect to show Toast messages from ViewModel
    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // petViewModel.clearMessage() // You might add this function to PetViewModel
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Update Pet",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = Alignment.CenterHorizontally
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6B8E23))
            )
        },
        containerColor = Color(0xFFF5F6FA)
    ) { innerPadding ->
        if (petToUpdate == null && !isLoading) {
            // Show a message if pet data isn't found and not loading
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pet not found or failed to load.", color = Color.Red)
                Button(onClick = { navController.popBackStack() }) {
                    Text("Go Back")
                }
            }
        } else if (isLoading) {
            // Show loading indicator while fetching data
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text("Loading pet data...")
            }
        } else {
            // Display the form once data is loaded
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Update Pet Details",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22223B),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Name
                            OutlinedTextField(
                                value = petName,
                                onValueChange = { petName = it },
                                label = { Text("Name*", color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6B8E23),
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = Color(0xFF6B8E23),
                                    unfocusedLabelColor = Color.Gray
                                )
                            )

                            // Breed
                            OutlinedTextField(
                                value = petBreed,
                                onValueChange = { petBreed = it },
                                label = { Text("Breed*", color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6B8E23),
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = Color(0xFF6B8E23),
                                    unfocusedLabelColor = Color.Gray
                                )
                            )

                            // Type Dropdown
                            ExposedDropdownMenuBox(
                                expanded = expandedType,
                                onExpandedChange = { expandedType = !expandedType },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = petType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Type*", color = Color.Gray) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6B8E23),
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedLabelColor = Color(0xFF6B8E23),
                                        unfocusedLabelColor = Color.Gray
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedType,
                                    onDismissRequest = { expandedType = false }
                                ) {
                                    petTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type) },
                                            onClick = {
                                                petType = type
                                                expandedType = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Age
                            OutlinedTextField(
                                value = petAge,
                                onValueChange = { petAge = it },
                                label = { Text("Age*", color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6B8E23),
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = Color(0xFF6B8E23),
                                    unfocusedLabelColor = Color.Gray
                                )
                            )

                            // Description
                            OutlinedTextField(
                                value = petDescription,
                                onValueChange = { petDescription = it },
                                label = { Text("Description", color = Color.Gray) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6B8E23),
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = Color(0xFF6B8E23),
                                    unfocusedLabelColor = Color.Gray
                                )
                            )

                            // Status Dropdown
                            ExposedDropdownMenuBox(
                                expanded = expandedStatus,
                                onExpandedChange = { expandedStatus = !expandedStatus },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = petStatus,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Status*", color = Color.Gray) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6B8E23),
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedLabelColor = Color(0xFF6B8E23),
                                        unfocusedLabelColor = Color.Gray
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedStatus,
                                    onDismissRequest = { expandedStatus = false }
                                ) {
                                    petStatuses.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status) },
                                            onClick = {
                                                petStatus = status
                                                expandedStatus = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Image Upload Placeholder
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { Toast.makeText(context, "Image upload not implemented yet", Toast.LENGTH_SHORT).show() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Choose File", color = Color(0xFF22223B))
                                }
                                Text("No file chosen", color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (petId.isNullOrBlank()) {
                                            Toast.makeText(context, "Cannot update: Pet ID is missing.", Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        if (petName.isBlank() || petBreed.isBlank() || petAge.isBlank()) {
                                            Toast.makeText(context, "Please fill all required fields (Name, Breed, Age)", Toast.LENGTH_LONG).show()
                                            return@Button
                                        }

                                        val updatedData = mutableMapOf<String, Any?>(
                                            "petName" to petName,
                                            "petBreed" to petBreed,
                                            "petType" to petType,
                                            "petAge" to petAge,
                                            "petDescription" to petDescription,
                                            "petStatus" to petStatus,
                                            "petImageUrl" to petImageUrl // Update image URL if changed
                                        )

                                        petViewModel.updatePet(petId, updatedData) { success, message ->
                                            if (success) {
                                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                                navController.popBackStack() // Navigate back to dashboard
                                            } else {
                                                Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text("Update Pet", color = Color.White, fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                OutlinedButton(
                                    onClick = { navController.popBackStack() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A4E69)),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp, color = Color(0xFF4A4E69)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    enabled = !isLoading
                                ) {
                                    Text("Cancel", fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun UpdatePetScreenPreview() {
    PetAdoptionManagementTheme {
        val navController = rememberNavController()
        val dummyRepo = remember { PetRepositoryImpl() }
        val dummyViewModel = viewModel<PetViewModel>(factory = PetViewModelFactory(dummyRepo))
        // For preview, provide a dummy petId (e.g., "123")
        UpdatePetScreen(navController = navController, petViewModel = dummyViewModel, petId = "dummy_pet_id_for_preview")
    }
}