package com.example.petadoptionmanagement.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
// import androidx.compose.material.icons.filled.FavoriteBorder // Not used, can be removed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// import androidx.compose.ui.geometry.isEmpty // Not used directly, can be removed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// import androidx.compose.ui.unit.sp // Not used directly, can be removed
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.model.UserModel // Assuming UserModel is correctly defined
import com.example.petadoptionmanagement.repository.PetRepository // For PreviewPetViewModel
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.view.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.PetViewModel
import com.example.petadoptionmanagement.viewmodel.PetViewModelFactory
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory

// Define PetDetailActivity if it doesn't exist yet for navigation
// class PetDetailActivity : ComponentActivity() { /* ... */ }
// class ProfileViewScreen : ComponentActivity() { /* ... */ }
// class SignInActivity : ComponentActivity() { /* ... */ }


class AdopterDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                // It's good practice to provide the applicationContext to repositories if needed
                val petRepository = remember { PetRepositoryImpl() } // Consider passing context if needed by Impl
                val petViewModel: PetViewModel = viewModel(factory = PetViewModelFactory(petRepository))

                val userRepository = remember { UserRepositoryImpl(applicationContext) }
                val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))

                AdopterDashboardScreen(petViewModel = petViewModel, userViewModel = userViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdopterDashboardScreen(petViewModel: PetViewModel, userViewModel: UserViewModel) {
    val context = LocalContext.current
    val availablePets by petViewModel.availablePets.observeAsState(initial = emptyList())
    val isLoadingPets by petViewModel.isLoading.observeAsState(initial = true) // Observe isLoading

    LaunchedEffect(Unit) {
        petViewModel.fetchAvailablePets()
    }

    val currentUser by userViewModel.currentUser.observeAsState()
    val petMessage by petViewModel.message.observeAsState() // Observe messages from PetViewModel

    // Show a snackbar for messages from PetViewModel
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(petMessage) {
        petMessage?.let {
            snackbarHostState.showSnackbar(it)
            petViewModel.clearMessage() // Clear message after showing
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Welcome, ${currentUser?.firstname ?: "Adopter"}!") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = {
                        // TODO: Replace ProfileViewScreen::class.java with your actual Profile Activity/Screen
                        context.startActivity(Intent(context, ProfileViewScreen::class.java))
                    }) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                    }
                    IconButton(onClick = {
                        userViewModel.logout()
                        val intent = Intent(context, SignInActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish() // Finish current activity
                    }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Available Pets for Adoption",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoadingPets) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (availablePets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pets currently available for adoption. Check back soon!")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(availablePets, key = { pet -> pet.petId }) { pet -> // Use pet.petId
                        PetCard(pet = pet, onPetClick = { clickedPet ->
                            Log.d("AdopterDashboard", "Clicked on pet: ${clickedPet.petName}, ID: ${clickedPet.petId}")
                            // TODO: Replace PetDetailActivity::class.java with your actual Detail Activity/Screen
                            val intent = Intent(context, PetViewModel::class.java)
                            intent.putExtra("PET_ID", clickedPet.petId) // Pass pet.petId
                            context.startActivity(intent)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun PetCard(pet: PetModel, onPetClick: (PetModel) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPetClick(pet) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = pet.petImageUrl, // USE pet.petImageUrl
                    placeholder = painterResource(id = R.drawable.paw_print),
                    error = painterResource(id = R.drawable.paw_print)
                ),
                contentDescription = "Image of ${pet.petName}", // USE pet.petName
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.petName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) // USE pet.petName
                Spacer(modifier = Modifier.height(4.dp))
                Text("Breed: ${pet.petBreed}", style = MaterialTheme.typography.bodyMedium) // USE pet.petBreed
                Text("Age: ${pet.petAge} | Gender: ${pet.petGender}", style = MaterialTheme.typography.bodyMedium) // USE pet.petAge, pet.petGender
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    pet.petDescription, // USE pet.petDescription
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


// --- Preview Section ---

//// Dummy PetViewModel for Preview using the updated PetViewModel base
//class PreviewPetViewModel(petRepository: PetRepository) : PetViewModel(petRepository) {
//    private val _previewIsLoading = MutableLiveData(false)
//    override val isLoading: LiveData<Boolean> = _previewIsLoading
//
//    private val _previewAvailablePets = MutableLiveData(
//        listOf(
//            // Ensure these match your PetModel structure EXACTLY
//            PetModel(petId="1", petName = "Buddy", petBreed = "Golden Retriever", petType = "Dog", petAge = "2", petGender = "Male", petDescription = "Friendly and playful. Loves walks!", petStatus="available", petImageUrl = "url_or_empty1", adoptionId = null, addedBy = "preview", timestamp = System.currentTimeMillis()),
//            PetModel(petId="2", petName = "Lucy", petBreed = "Siamese", petType = "Cat", petAge = "1", petGender = "Female", petDescription = "Affectionate and calm. Enjoys naps.", petStatus="available", petImageUrl = "url_or_empty2", adoptionId = null, addedBy = "preview", timestamp = System.currentTimeMillis())
//        )
//    )
//    override val availablePets: LiveData<List<PetModel>> = _previewAvailablePets
//
//    override fun fetchAvailablePets() {
//        // In preview, data is already set, simulate loading finished
//        _previewIsLoading.value = false
//    }
//
//    // Override other methods from PetViewModel if your preview needs to interact with them
//    // For example, clearMessage:
//    private val _previewMessage = MutableLiveData<String?>()
//    override val message: LiveData<String?> = _previewMessage
//    override fun clearMessage() {
//        _previewMessage.value = null
//    }
//}
//
//class PreviewUserViewModel(userRepository: UserRepositoryImpl) : UserViewModel(userRepository) {
//    private val _previewCurrentUser = MutableLiveData<UserModel?>(
//        UserModel(firstname = "AdopterPreview", /* fill other UserModel properties as needed */)
//    )
//    override val currentUser: LiveData<UserModel?> = _previewCurrentUser
//
//    // Override other methods from UserViewModel as needed for the preview
//    override fun logout() {
//        Log.d("PreviewUserViewModel", "Logout called in preview")
//        _previewCurrentUser.value = null
//    }
//}
//
//@Preview(showBackground = true, widthDp = 360, heightDp = 780)
//@Composable
//fun AdopterDashboardScreenPreview() {
//    PetAdoptionManagementTheme {
//        // For preview, it's okay to use simpler initializations if complex dependencies are an issue.
//        // Make sure PetRepositoryImpl() can be instantiated without real dependencies for preview.
//        val petRepository = remember { PetRepositoryImpl() } // Or a mock/fake repository
//        val petViewModel = remember { PreviewPetViewModel(petRepository) }
//
//        // Same for UserRepositoryImpl
//        val userRepository = remember { UserRepositoryImpl(LocalContext.current) } // Or a mock/fake
//        val userViewModel = remember { PreviewUserViewModel(userRepository) }
//
//        AdopterDashboardScreen(petViewModel = petViewModel, userViewModel = userViewModel)
//    }
//}