package com.example.petadoptionmanagement.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.model.PetModel // This import will be for the new PetModel
import com.example.petadoptionmanagement.repository.PetRepositoryImpl // This import will be for the new PetRepositoryImpl
import com.example.petadoptionmanagement.viewmodel.PetViewModel // This import will be for the new PetViewModel

class AddNewPetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                AddNewPetScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar and ExposedDropdownMenuBox
@Composable
fun AddNewPetScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    // Initialize ViewModel and Repository
    val petRepository = remember { PetRepositoryImpl() } // Create an instance of your repository
    val petViewModel = remember { PetViewModel(petRepository) } // Pass the repository to your ViewModel

    // State variables for form fields
    var petName by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Dog") } // Default value as in screenshot
    var petAge by remember { mutableStateOf("") }
    var petDescription by remember { mutableStateOf("") }
    var petStatus by remember { mutableStateOf("Available") } // Default value as in screenshot
    var petImageUrl by remember { mutableStateOf("") } // Placeholder for image URL

    // Dropdown menu state for Type
    val petTypes = listOf("Dog", "Cat", "Bird", "Rabbit", "Other")
    var expandedType by remember { mutableStateOf(false) }

    // Dropdown menu state for Status
    val petStatuses = listOf("Available", "Adopted", "Pending", "On Hold")
    var expandedStatus by remember { mutableStateOf(false) }

    // Theme colors for consistency
    val primaryColor = Color(0xFF6B8E23) // From your existing theme (e.g., AboutActivity background)
    val onPrimaryColor = Color.White
    val textColor = Color(0xFF22223B) // Dark text from your existing theme
    val cardBackgroundColor = Color(0xFFDCDCDC) // Card background from your existing theme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add New Pet",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = Alignment.CenterHorizontally // Center the title
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) { // Go back
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryColor)
            )
        },
        containerColor = Color(0xFFF5F6FA) // Light background color from HomePage
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .background(Color.Transparent), // Set to transparent so the background color shows
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between form elements
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White), // White card background as in screenshot
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
                            text = "Add New Pet",
                            fontSize = 24.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = textColor,
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
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = primaryColor,
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
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = primaryColor,
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
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = primaryColor,
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
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = primaryColor,
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
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = primaryColor,
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
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedLabelColor = primaryColor,
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

                        // Image Upload Placeholder (as in screenshot)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { /* TODO: Implement image selection from gallery/camera */
                                    Toast.makeText(context, "Image upload not implemented yet", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Choose File", color = textColor)
                            }
                            Text("No file chosen", color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(16.dp)) // Spacer before buttons

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    // Basic validation
                                    if (petName.isBlank() || petBreed.isBlank() || petAge.isBlank()) {
                                        Toast.makeText(context, "Please fill all required fields (Name, Breed, Age)", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }

                                    // Create PetModel
                                    val newPet = PetModel(
                                        productId = "", // Firebase will generate this
                                        productName = petName,
                                        productBreed = petBreed,
                                        productType = petType,
                                        productAge = petAge,
                                        productDesc = petDescription,
                                        productStatus = petStatus,
                                        productImageUrl = petImageUrl // Use placeholder for now
                                    )

                                    // Call ViewModel to add pet
                                    petViewModel.addNewPet(newPet) { success, message ->
                                        if (success) {
                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                            activity?.finish() // Go back after successful add
                                        } else {
                                            Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)), // Earthy brown like Home Page Login
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(50.dp)
                            ) {
                                Text("Add Pet", color = Color.White, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            OutlinedButton(
                                onClick = { activity?.finish() }, // Go back
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4A4E69)), // Dark gray text
                                border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp, color = Color(0xFF4A4E69)), // Dark gray border
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(50.dp)
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
fun AddNewPetScreenPreview() {
    PetAdoptionManagementTheme {
        AddNewPetScreen()
    }
}