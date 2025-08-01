// /view/AdminDashboardActivity.kt

package com.example.petadoptionmanagement.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cloudinary.Cloudinary
import com.example.petadoption.viewmodel.PetViewModel
import com.example.petadoptionmanagement.model.AdoptionApplicationModel
import com.example.petadoptionmanagement.model.ApplicationStatus
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.repository.AdoptionApplicationRepositoryImpl
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : ComponentActivity() {
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
                        "cloud_name" to "dd9sooenk",
                        "api_key" to "281858352367463",
                        "api_secret" to "dj8vgOz6YCPGqqvQIGEa-dhQ0Ig"
                    )
                    val cloudinary = Cloudinary(config)
                    UserRepositoryImpl(auth, firestore, cloudinary, applicationContext)
                }

                val petRepo = remember {
                    val firestore = FirebaseFirestore.getInstance()
                    val config = mapOf(
                        "cloud_name" to "dd9sooenk",
                        "api_key" to "281858352367463Y",
                        "api_secret" to "dj8vgOz6YCPGqqvQIGEa-dhQ0Ig"
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

                AdminDashboardScreen(userViewModel, petViewModel, adoptionViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    userViewModel: UserViewModel,
    petViewModel: PetViewModel,
    adoptionViewModel: AdoptionApplicationViewModel
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Manage Pets", "Applications")

    val currentUser by userViewModel.currentUser.observeAsState()
    val allPets by petViewModel.allPets.observeAsState(emptyList())
    val allApplications by adoptionViewModel.allApplications.observeAsState(emptyList())

    var selectedPetId by remember { mutableStateOf<String?>(null) }
    val selectedPet by petViewModel.pet.observeAsState()
    var showPetDetails by remember { mutableStateOf(false) }

    LaunchedEffect(selectedPetId) {
        // Fetch the pet details when a new ID is selected
        selectedPetId?.let { id ->
            if (id.isNotBlank()) {
                petViewModel.getPetById(id)
                showPetDetails = true
            }
        }
    }


    // Fetch data when the screen is first composed
    LaunchedEffect(Unit) {
        adoptionViewModel.getAllApplications() // Fetches all applications for the "Applications" tab
    }

    // To show a confirmation dialog before deleting a pet
    var showDeleteDialog by remember { mutableStateOf(false) }
    var petToDelete by remember { mutableStateOf<PetModel?>(null) }

    if (showDeleteDialog && petToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Pet") },
            text = { Text("Are you sure you want to delete '${petToDelete!!.petName}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        petViewModel.deletePet(petToDelete!!.petId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, EditProfileActivity::class.java))
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Edit Profile")
                    }

                    IconButton(onClick = { userViewModel.logout()
                        context.startActivity(Intent(context, SignInActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        (context as? Activity)?.finish()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 1) {
                FloatingActionButton(onClick = {
                    context.startActivity(Intent(context, AddPetActivity::class.java))
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Pet")
                }
            }
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
                0 -> DashboardContent(allPets)
                1 -> PetManagementContent(
                    pets = allPets,
                    onEdit = { petIdToEdit ->
                        context.startActivity(Intent(context, EditPetActivity::class.java).apply{
                        putExtra("petId", petIdToEdit)
                    })
                    },
                    onDelete = { pet ->
                        petToDelete = pet
                        showDeleteDialog = true

                    },
                    onPetClick = { petId ->
                        selectedPetId = petId}
                )
                2 -> ApplicationManagementContent(
                    applications = allApplications.filter { it.status == ApplicationStatus.PENDING },
                    onApprove = { appId -> adoptionViewModel.updateApplicationStatus(appId, ApplicationStatus.APPROVED) },
                    onReject = { appId -> adoptionViewModel.updateApplicationStatus(appId, ApplicationStatus.REJECTED) }
                )
            }
        }
    }
}

@Composable
fun DashboardContent(pets: List<PetModel>) {

    // This composable holds the statistics cards from your original design
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome, Admin!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Example Stat Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatCard("Total Pets", pets.size.toString())
            StatCard("Available", pets.count { it.petStatus == com.example.petadoptionmanagement.model.PetStatus.AVAILABLE }.toString())
            StatCard("Adopted", pets.count { it.petStatus == com.example.petadoptionmanagement.model.PetStatus.ADOPTED }.toString())
        }
    }
}

@Composable
fun PetManagementContent(
    pets: List<PetModel>,
    onEdit: (String) -> Unit,
    onDelete: (PetModel) -> Unit,
    onPetClick: (String) -> Unit
) {
    if (pets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pets found. Add one to get started!")
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(pets) { pet ->
            PetListItem(pet = pet, onEdit = onEdit, onDelete = onDelete, onPetClick)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PetListItem(pet: PetModel, onEdit: (String) -> Unit, onDelete: (PetModel) -> Unit, onPetClick: (String) -> Unit) {
    Card(elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier
        .fillMaxWidth()
        .clickable {onPetClick(pet.petId)}) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = pet.petImageUrl,
                contentDescription = pet.petName,
                modifier = Modifier.size(60.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.petName, style = MaterialTheme.typography.titleMedium)
                Text(pet.petStatus.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = { onEdit(pet.petId) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Pet")
            }
            IconButton(onClick = { onDelete(pet) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Pet", tint = Color.Red)
            }
        }
    }
}

@Composable
fun ApplicationManagementContent(
    applications: List<AdoptionApplicationModel>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    if (applications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No pending applications.")
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(applications) { application ->
            ApplicationListItem(application = application, onApprove = onApprove, onReject = onReject)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ApplicationListItem(application: AdoptionApplicationModel, onApprove: (String) -> Unit, onReject: (String) -> Unit) {
    val context = LocalContext.current // Get context
    Card(elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
            .clickable {
                val intent = Intent(context, ViewApplicationDetailsActivity::class.java).apply{
                    putExtra("applicationId", application.applicationId)
                }
                context.startActivity(intent)
            }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pet: ${application.petName}", style = MaterialTheme.typography.titleMedium)
            Text("Applicant: ${application.applicantName}", style = MaterialTheme.typography.bodyMedium)
            Text("Message: \"${application.message}\"", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = { onReject(application.applicationId) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("Reject")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onApprove(application.applicationId) }) {
                    Text("Approve")
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.padding(8.dp).size(width = 100.dp, height = 80.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
