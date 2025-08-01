// /view/AddPetActivity.kt

package com.example.petadoptionmanagement.view

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import coil.compose.AsyncImage
import com.cloudinary.Cloudinary
import com.example.petadoption.viewmodel.PetViewModel
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.model.PetStatus
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.PetViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddPetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PetAdoptionManagementTheme {
                // --- FIX: Correctly initialize and provide all dependencies ---
                val petRepository = remember {
                    val firestore = FirebaseFirestore.getInstance()
                    val config = mapOf(
                        "cloud_name" to "dd9sooenk",
                        "api_key" to "281858352367463",
                        "api_secret" to "dj8vgOz6YCPGqqvQIGEa-dhQ0Ig"
                    )
                    val cloudinary = Cloudinary(config)
                    PetRepositoryImpl(firestore, cloudinary, applicationContext)
                }

                val petViewModel: PetViewModel = viewModel(
                    factory = PetViewModelFactory(petRepository)
                )

                AddPetScreen(petViewModel = petViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(petViewModel: PetViewModel) {
    val context = LocalContext.current
    var petName by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("") }
    var petGender by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var petDescription by remember { mutableStateOf("") }
    var petStatus by remember { mutableStateOf(PetStatus.AVAILABLE) } // Default to AVAILABLE
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isLoading by petViewModel.isLoading.observeAsState(false)
    val message by petViewModel.message.observeAsState()

    // This effect handles showing feedback and finishing the activity on success
    LaunchedEffect(message) {
        val currentMessage = message
        if (!currentMessage.isNullOrBlank()) {
            Toast.makeText(context, currentMessage, Toast.LENGTH_LONG).show()
            if (currentMessage.contains("successfully", ignoreCase = true)) {
                (context as? Activity)?.finish()
               // Go back to the previous screen (dashboard)
            }
            // Optionally clear message in ViewModel to prevent re-showing
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add a New Pet") },
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
            // Image Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = 0.5f))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected Pet Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
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

            // Submit Button
            Button(
                onClick = {
                    if (petName.isBlank() || imageUri == null) {
                        Toast.makeText(context, "Pet name and image are required.", Toast.LENGTH_SHORT).show()
                    } else {
                        val newPet = PetModel(
                            petName = petName,
                            petBreed = petBreed,
                            petType = petType,
                            petGender = petGender,
                            petAge = petAge,
                            petDescription = petDescription,
                            petStatus = petStatus,
                            addedBy = FirebaseAuth.getInstance().currentUser?.uid ?: "admin"
                        )
                        petViewModel.addNewPet(newPet, imageUri)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Add Pet", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
