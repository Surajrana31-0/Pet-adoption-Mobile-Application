// /view/EditPetActivity.kt

package com.example.petadoptionmanagement.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petadoptionmanagement.R
import coil.compose.AsyncImage
import com.cloudinary.Cloudinary
import com.example.petadoption.viewmodel.PetViewModel
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.model.PetStatus
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.PetViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

class EditPetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val petId = intent.getStringExtra("petId") // Get petId from the Intent
        if (petId == null) {
            Toast.makeText(this, "Pet ID is missing.", Toast.LENGTH_SHORT).show()
            finish() // Close activity if no petId is provided
            return
        }

        setContent {
            PetAdoptionManagementTheme {
                // Correctly initialize and provide all dependencies for PetViewModel
                val petRepository = remember {
                    val firestore = FirebaseFirestore.getInstance()
                    val config = mapOf(
                        "cloud_name" to "YOUR_CLOUD_NAME",
                        "api_key" to "YOUR_API_KEY",
                        "api_secret" to "YOUR_API_SECRET"
                    )
                    val cloudinary = Cloudinary(config)
                    PetRepositoryImpl(firestore, cloudinary, applicationContext)
                }

                val petViewModel: PetViewModel = viewModel(
                    factory = PetViewModelFactory(petRepository)
                )

                EditPetScreen(petId = petId, petViewModel = petViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetScreen(petId: String, petViewModel: PetViewModel) {
    val context = LocalContext.current
    val currentPet by petViewModel.pet.observeAsState() // Observe single pet details
    val isLoading by petViewModel.isLoading.observeAsState(false)
    val message by petViewModel.message.observeAsState()

    var petName by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("") }
    var petGender by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var petDescription by remember { mutableStateOf("") }
    var petStatus by remember { mutableStateOf(PetStatus.AVAILABLE) }
    var imageUri by remember { mutableStateOf<Uri?>(null) } // New image selected by user
    var currentImageUrl by remember { mutableStateOf<String?>(null) } // Existing image URL from DB

    // Fetch pet details when the screen starts or petId changes
    LaunchedEffect(petId) {
        petViewModel.getPetById(petId)
    }

    // Pre-populate fields when currentPet data is available
    LaunchedEffect(currentPet) {
        currentPet?.let { pet ->
            petName = pet.petName
            petBreed = pet.petBreed
            petType = pet.petType
            petGender = pet.petGender
            petAge = pet.petAge
            petDescription = pet.petDescription
            petStatus = pet.petStatus
            currentImageUrl = pet.petImageUrl
        }
    }

    // Effect for showing Toast messages and finishing on success
    LaunchedEffect(message) {
        val currentMessage = message
        if (!currentMessage.isNullOrBlank()) {
            Toast.makeText(context, currentMessage, Toast.LENGTH_LONG).show()
            if (currentMessage.contains("successfully", ignoreCase = true)) {
                (context as? Activity)?.finish() // Go back to the previous screen (dashboard)
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri // Update selected image URI
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Pet Details") },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Picker/Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = 0.5f))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                // Display new selected image, or existing image, or placeholder
                AsyncImage(
                    model = imageUri ?: currentImageUrl,
                    contentDescription = "Pet Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = if (imageUri == null && currentImageUrl.isNullOrBlank()) {
                        // Show default icon if no image selected and no current image
                        androidx.compose.ui.res.painterResource(id = R.drawable.paw_print) // Use your paw_print.png
                    } else null,
                    placeholder = if (imageUri == null && currentImageUrl.isNullOrBlank()) {
                        androidx.compose.ui.res.painterResource(id = R.drawable.paw_print) // Use your paw_print.png
                    } else null
                )

                if (imageUri == null && currentImageUrl.isNullOrBlank()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Upload Icon", modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Text("Tap to select an image", color = Color.Gray)
                    }
                }
            }

            // Input Fields
            OutlinedTextField(value = petName, onValueChange = { petName = it }, label = { Text("Pet Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = petBreed, onValueChange = { petBreed = it }, label = { Text("Breed") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = petType, onValueChange = { petType = it }, label = { Text("Type (e.g., Dog, Cat)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = petGender, onValueChange = { petGender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = petAge, onValueChange = { petAge = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = petDescription, onValueChange = { petDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)

            // Status Dropdown
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = !statusExpanded }) {
                OutlinedTextField(
                    value = petStatus.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    PetStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name) },
                            onClick = {
                                petStatus = status
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            // Save Changes Button (MODIFIED LOGIC)
            Button(
                onClick = {
                    if (petName.isBlank()) {
                        Toast.makeText(context, "Pet name cannot be empty.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val updatedData = mutableMapOf<String, Any>(
                        "petName" to petName,
                        "petBreed" to petBreed,
                        "petType" to petType,
                        "petGender" to petGender,
                        "petAge" to petAge,
                        "petDescription" to petDescription,
                        "petStatus" to petStatus.name
                    )

                    // Check if a new image is selected
                    if (imageUri != null) {
                        // Use the new ViewModel function to handle upload and then update
                        petViewModel.updatePetImageAndDetails(petId, imageUri!!, updatedData)
                    } else {
                        // If no new image, just update other details
                        petViewModel.updatePet(petId, updatedData)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
