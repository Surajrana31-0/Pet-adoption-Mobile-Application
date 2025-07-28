// /view/AdopterDashboardActivity.kt

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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cloudinary.Cloudinary
import com.example.petadoption.viewmodel.PetViewModel
import com.example.petadoptionmanagement.R
import androidx.activity.enableEdgeToEdge

import com.example.petadoptionmanagement.model.AdoptionApplicationModel
import com.example.petadoptionmanagement.model.ApplicationStatus
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.model.PetStatus
import com.example.petadoptionmanagement.repository.AdoptionApplicationRepositoryImpl
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.AdoptionApplicationViewModel
import com.example.petadoptionmanagement.viewmodel.AdoptionApplicationViewModelFactory
import com.example.petadoptionmanagement.viewmodel.PetViewModelFactory
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdopterDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                // --- FIX: Correctly initialize and provide all dependencies for all ViewModels ---
                val userRepo = remember {
                    val auth = FirebaseAuth.getInstance()
                    val firestore = FirebaseFirestore.getInstance()
                    val config = mapOf(
                        "cloud_name" to "dd9sooenk", // REPLACE WITH YOUR CLOUD_NAME
                        "api_key" to "281858352367463",       // REPLACE WITH YOUR API_KEY
                        "api_secret" to "dj8vgOz6YCPGqqvQIGEa-dhQ0Ig"   // REPLACE WITH YOUR API_SECRET
                    )
                    val cloudinary = Cloudinary(config)
                    UserRepositoryImpl(auth, firestore, cloudinary, applicationContext)
                }

                val petRepo = remember {
                    val firestore = FirebaseFirestore.getInstance()
                    val config = mapOf(
                        "cloud_name" to "dd9sooenk", // REPLACE WITH YOUR CLOUD_NAME
                        "api_key" to "281858352367463",       // REPLACE WITH YOUR API_KEY
                        "api_secret" to "dj8vgOz6YCPGqqvQIGEa-dhQ0Ig"   // REPLACE WITH YOUR API_SECRET
                    )
                    val cloudinary = Cloudinary(config)
                    PetRepositoryImpl(firestore, cloudinary, applicationContext)
                }

                val adoptionRepo = remember {
                    AdoptionApplicationRepositoryImpl(FirebaseFirestore.getInstance())
                }

                val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepo))
                val petViewModel: PetViewModel = viewModel(factory = PetViewModelFactory(petRepo))
                val adoptionViewModel: AdoptionApplicationViewModel = viewModel(factory = AdoptionApplicationViewModelFactory(adoptionRepo))

                AdopterDashboardScreen(userViewModel, petViewModel, adoptionViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdopterDashboardScreen(
    userViewModel: UserViewModel,
    petViewModel: PetViewModel,
    adoptionViewModel: AdoptionApplicationViewModel
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Pets", "My Applications", "Profile")

    val currentUser by userViewModel.currentUser.observeAsState()

    // Observe messages from all ViewModels for Snackbar/Toast
    val petMessage by petViewModel.message.observeAsState()
    val adoptionMessage by adoptionViewModel.message.observeAsState()
    val userMessage by userViewModel.message.observeAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAdoptionSuccessDialog by remember { mutableStateOf(false) }


// Unified message handling with SnackbarHost
    LaunchedEffect(petMessage, adoptionMessage, userMessage) {
        petMessage?.let {
            if (it.isNotBlank()) snackbarHostState.showSnackbar(it)
            petViewModel.clearMessage() // Corrected: Call on the ViewModel instance
        }
        adoptionMessage?.let {
            if (it.isNotBlank()) {
                snackbarHostState.showSnackbar(it)
                if (it.contains("successfully!", ignoreCase = true) || it.contains("approved!", ignoreCase = true)) {
                    showAdoptionSuccessDialog = true
                }
            }
            adoptionViewModel.clearMessage() // Corrected: Call on the ViewModel instance
        }
        userMessage?.let {
            if (it.isNotBlank()) snackbarHostState.showSnackbar(it)
            userViewModel.clearMessage() // Corrected: Call on the ViewModel instance
        }
    }



    if (showAdoptionSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showAdoptionSuccessDialog = false },
            title = { Text("Application Success!") },
            text = { Text("Your application has been received/approved. You can now proceed with the adoption process. Please visit 'PetEy' the animal shelter for further steps.") },
            confirmButton = {
                Button(onClick = { showAdoptionSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("PetConnect Adoptions") },
                actions = {
                    IconButton(onClick = {
                        userViewModel.logout()
                        context.startActivity(Intent(context, SignInActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        (context as? Activity)?.finish()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> PetListContent(petViewModel = petViewModel, adoptionViewModel = adoptionViewModel, currentUser = currentUser)
                1 -> MyApplicationsContent(adoptionViewModel = adoptionViewModel, currentUser = currentUser)
                2 -> AdopterProfileContent(userViewModel = userViewModel, currentUser = currentUser)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetListContent(
    petViewModel: PetViewModel,
    adoptionViewModel: AdoptionApplicationViewModel,
    currentUser: com.example.petadoptionmanagement.model.UserModel?
) {
    val context = LocalContext.current
    val allPets by petViewModel.allPets.observeAsState(emptyList())
    val isLoadingPets by petViewModel.isLoading.observeAsState(false)

    var selectedStatusFilter by remember { mutableStateOf<PetStatus?>(null) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var petToApplyFor by remember { mutableStateOf<PetModel?>(null) }
    var applicationMessage by remember { mutableStateOf("") }

    // Fetch all pets initially
    LaunchedEffect(Unit) {
        petViewModel.getPetById("") // Calling getPetById with empty string for getAllPets (check PetViewModel logic for this)
    }

    val filteredPets = remember(allPets, selectedStatusFilter) {
        if (selectedStatusFilter == null) {
            allPets
        } else {
            allPets.filter { it.petStatus == selectedStatusFilter }
        }
    }

    if (showApplyDialog && petToApplyFor != null) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("Apply for ${petToApplyFor!!.petName}") },
            text = {
                Column {
                    Text("Enter your message to the shelter:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = applicationMessage,
                        onValueChange = { applicationMessage = it },
                        label = { Text("Your Message") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (currentUser == null) {
                        Toast.makeText(context, "Please login to apply for a pet.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (applicationMessage.isBlank()) {
                        Toast.makeText(context, "Please enter a message.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val application = AdoptionApplicationModel(
                        petId = petToApplyFor!!.petId,
                        petName = petToApplyFor!!.petName,
                        applicantId = currentUser.userId,
                        applicantName = "${currentUser.firstname} ${currentUser.lastname}",
                        message = applicationMessage,
                        status = ApplicationStatus.PENDING,
                        timestamp = Date()
                    )
                    adoptionViewModel.applyForPet(application, petToApplyFor!!.petId)
                    showApplyDialog = false
                    applicationMessage = "" // Clear message
                }) {
                    Text("Submit Application")
                }
            },
            dismissButton = {
                Button(onClick = { showApplyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter Dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedStatusFilter?.name ?: "All Pets",
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter by Status") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Pets") },
                    onClick = {
                        selectedStatusFilter = null
                        expanded = false
                    }
                )
                PetStatus.values().forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status.name) },
                        onClick = {
                            selectedStatusFilter = status
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoadingPets) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredPets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No pets match your filter.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPets, key = { it.petId }) { pet ->
                    PetCardForAdopter(pet = pet, onApplyClick = {
                        petToApplyFor = it
                        showApplyDialog = true
                    })
                }
            }
        }
    }
}

@Composable
fun PetCardForAdopter(pet: PetModel, onApplyClick: (PetModel) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Maybe navigate to PetDetailActivity if you create one */ },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = pet.petImageUrl,
                contentDescription = "Image of ${pet.petName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(pet.petName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Breed: ${pet.petBreed} | Type: ${pet.petType}", style = MaterialTheme.typography.bodyMedium)
            Text("Age: ${pet.petAge} | Gender: ${pet.petGender}", style = MaterialTheme.typography.bodyMedium)
            Text("Status: ${pet.petStatus.name}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                pet.petDescription,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (pet.petStatus == PetStatus.AVAILABLE) {
                Button(
                    onClick = { onApplyClick(pet) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply for Adoption")
                }
            } else {
                Text(
                    "This pet is ${pet.petStatus.name.lowercase()}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun MyApplicationsContent(
    adoptionViewModel: AdoptionApplicationViewModel,
    currentUser: com.example.petadoptionmanagement.model.UserModel?
) {
    val applications by adoptionViewModel.userApplications.observeAsState(emptyList())
    val isLoadingApps by adoptionViewModel.isLoading.observeAsState(false)

    // Fetch applications for the current user
    LaunchedEffect(currentUser) {
        currentUser?.userId?.let {
            adoptionViewModel.getApplicationsForUser(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoadingApps) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (applications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't submitted any applications yet.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(applications, key = { it.applicationId }) { application ->
                    ApplicationCardForAdopter(application = application)
                }
            }
        }
    }
}

@Composable
fun ApplicationCardForAdopter(application: AdoptionApplicationModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Pet: ${application.petName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Status: ${application.status.name}", style = MaterialTheme.typography.bodyMedium, color = when(application.status) {
                ApplicationStatus.PENDING -> Color.Blue
                ApplicationStatus.APPROVED -> Color.Green
                ApplicationStatus.REJECTED -> Color.Red
            })
            Text("Your Message: ${application.message}", style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            application.timestamp?.let {
                Text("Submitted: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdopterProfileContent(userViewModel: UserViewModel, currentUser: com.example.petadoptionmanagement.model.UserModel?) {
    val context = LocalContext.current
    val isLoading by userViewModel.isLoading.observeAsState(false)

    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) } // URI for new image
    var currentProfileImageUrl by remember { mutableStateOf<String?>(null) } // URL of existing image

    // Pre-populate fields when currentUser changes
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            username = user.username
            firstName = user.firstname
            lastName = user.lastname
            email = user.email
            contact = user.contact
            currentProfileImageUrl = user.profilePictureUrl
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Image Section
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { pickImageLauncher.launch("image/*") },
            contentAlignment = Alignment.BottomEnd
        ) {
            AsyncImage(
                model = profileImageUri ?: currentProfileImageUrl, // Show new image or current one
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = painterResource(id = R.drawable.profile_placeholder), // Fallback image
                placeholder = painterResource(id = R.drawable.profile_placeholder) // Placeholder while loading
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Profile Picture",
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .offset(x = 8.dp, y = 8.dp) // Offset to position correctly
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Input Fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false // Email usually cannot be changed directly from here
            )
            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Contact Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val currentUserId = currentUser?.userId
                    if (currentUserId.isNullOrBlank()) {
                        Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Basic validation
                    if (username.isBlank() || firstName.isBlank() || lastName.isBlank() || contact.isBlank()) {
                        Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val updates = mutableMapOf<String, Any>(
                        "username" to username,
                        "firstname" to firstName,
                        "lastname" to lastName,
                        "contact" to contact
                        // Email and Role are not directly editable here
                    )

                    userViewModel.editProfile(currentUserId, updates, profileImageUri, context)
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
