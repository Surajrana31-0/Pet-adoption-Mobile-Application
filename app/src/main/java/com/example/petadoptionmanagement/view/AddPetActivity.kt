package com.example.petadoptionmanagement.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.lifecycle.ViewModelProvider // For ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petadoptionmanagement.model.PetModel // Make sure PetModel is correctly imported from model package
import com.example.petadoptionmanagement.repository.PetRepositoryImpl // Make sure PetRepositoryImpl is correctly imported
import com.example.petadoptionmanagement.view.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.PetViewModel // Make sure PetViewModel is correctly imported
import com.example.petadoptionmanagement.viewmodel.PetViewModelFactory // Make sure PetViewModelFactory is correctly imported
import androidx.lifecycle.viewmodel.compose.viewModel // For @Preview context

/**
 * Activity that hosts the AddPetScreen composable.
 * This acts as the entry point for the "Add New Pet" functionality.
 */
class AddPetActivity : ComponentActivity() {
    private lateinit var petViewModel: PetViewModel // Declare ViewModel here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize PetViewModel using the factory
        // You'll need to provide an actual PetRepository instance here.
        // For a real app, you might use dependency injection (e.g., Hilt/Koin)
        // to provide the repository. For simplicity, we instantiate it here.
        val petRepository = PetRepositoryImpl() // Replace with your actual repository instance
        val factory = PetViewModelFactory(petRepository)
        petViewModel = ViewModelProvider(this, factory).get(PetViewModel::class.java)

        setContent {
            PetAdoptionManagementTheme {
                // Ensure navController is passed
                val navController = rememberNavController() // If AddPetActivity is part of a larger navigation graph,
                // you'd pass a common navController from the parent.
                // For stand-alone activity, this is fine.
                AddPetScreen(
                    navController = navController,
                    petViewModel = petViewModel // Pass the initialized ViewModel
                )
            }
        }
    }
}


/**
 * Composable screen for adding a new pet.
 * This screen provides a form for users to input pet details and submit them to the database.
 * It uses the PetViewModel to handle data operations and navigation.
 *
 * @param navController The NavController for navigating back or to other screens.
 * @param petViewModel The ViewModel instance responsible for pet data operations.
 */
@OptIn(ExperimentalMaterial3Api::class) // Required for TopAppBar and ExposedDropdownMenuBox
@Composable
fun AddPetScreen(
    navController: NavController,
    petViewModel: PetViewModel
) {
    val context = LocalContext.current

    // State variables for form fields
    var petName by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Dog") } // Default value
    var petAge by remember { mutableStateOf("") }
    var petDescription by remember { mutableStateOf("") }
    var petStatus by remember { mutableStateOf("Available") } // Default value
    var petImageUrl by remember { mutableStateOf("") } // For image URL (placeholder for now)

    // Dropdown menu states and options
    val petTypes = listOf("Dog", "Cat", "Bird", "Rabbit", "Other")
    var expandedType by remember { mutableStateOf(false) }

    val petStatuses = listOf("Available", "Adopted", "Pending", "On Hold")
    var expandedStatus by remember { mutableStateOf(false) }

    // Observe loading and message states from ViewModel
    val isLoading by petViewModel.loading.observeAsState(initial = false)
    val message by petViewModel.message.observeAsState(initial = "") // Assuming PetViewModel has a message LiveData

    // Effect to show Toast messages from ViewModel
    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // Optionally, clear the message in ViewModel after showing
            // petViewModel.clearMessage() // You might add this function to PetViewModel to prevent repeat toasts
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add New Pet",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = Alignment.CenterHorizontally
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Navigate back
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6B8E23)) // Theme primary color
            )
        },
        containerColor = Color(0xFFF5F6FA) // Light background color from HomePage
    ) { innerPadding ->
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
                            text = "Pet Details",
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

                        // Image Upload Placeholder (Action needs to be implemented)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    // TODO: Implement actual image picking and Cloudinary upload here
                                    Toast.makeText(context, "Image upload feature coming soon!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Choose File", color = Color(0xFF22223B))
                            }
                            Text("No file chosen", color = Color.Gray) // This should update with selected file name
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
                                    if (petName.isBlank() || petBreed.isBlank() || petAge.isBlank()) {
                                        Toast.makeText(context, "Please fill all required fields (Name, Breed, Age)", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }

                                    val newPet = PetModel(
                                        petId = "", // Firebase will generate this, or you can generate a UUID
                                        petName = petName,
                                        petBreed = petBreed,
                                        petType = petType,
                                        petAge = petAge,
                                        petDescription = petDescription,
                                        petStatus = petStatus,
                                        petImageUrl = petImageUrl // This will be the Cloudinary URL after upload
                                    )

                                    petViewModel.addNewPet(newPet) { success, message ->
                                        if (success) {
                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                            // Clear form fields after successful addition
                                            petName = ""
                                            petBreed = ""
                                            petType = "Dog"
                                            petAge = ""
                                            petDescription = ""
                                            petStatus = "Available"
                                            petImageUrl = ""
                                            navController.popBackStack() // Navigate back to dashboard or appropriate screen
                                        } else {
                                            Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                enabled = !isLoading // Disable button while loading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Add Pet", color = Color.White, fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            OutlinedButton(
                                onClick = { navController.popBackStack() }, // Go back
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A4E69)),
                                border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp, color = Color(0xFF4A4E69)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AddPetScreenPreview() {
    PetAdoptionManagementTheme {
        val navController = rememberNavController()
        // Provide a dummy PetRepositoryImpl for the preview
        val dummyRepository = remember { PetRepositoryImpl() }
        // Use `viewModel` from `androidx.lifecycle.viewmodel.compose.viewModel` for preview context
        val dummyViewModel = viewModel<PetViewModel>(factory = PetViewModelFactory(dummyRepository))
        AddPetScreen(navController = navController, petViewModel = dummyViewModel)
    }
}