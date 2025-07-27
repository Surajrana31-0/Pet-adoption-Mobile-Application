package com.example.petadoptionmanagement.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Correct import for LazyColumn items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import all filled icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // For using ViewModel with factory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter // For loading images from URL
import com.example.petadoptionmanagement.R // Assuming you have a placeholder image
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.view.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.PetViewModel
import com.example.petadoptionmanagement.viewmodel.PetViewModelFactory
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory


// --- Navigation Setup ---
sealed class AdminScreen(val route: String, val label: String, val icon: ImageVector) {
    object Overview : AdminScreen("overview", "Overview", Icons.Filled.Analytics)
    object PetManagement : AdminScreen("pet_management", "Pets", Icons.Filled.Pets)
    object UserManagement : AdminScreen("user_management", "Users", Icons.Filled.Group)
    object AdoptionRequests : AdminScreen("adoption_requests", "Requests", Icons.Filled.AssignmentInd)
    object AdminSettings : AdminScreen("admin_settings", "Settings", Icons.Filled.Settings)
}

//private val PetModel.imageUrl: String
val adminScreens = listOf(
    AdminScreen.Overview,
    AdminScreen.PetManagement,
    AdminScreen.UserManagement,
    AdminScreen.AdoptionRequests,
    AdminScreen.AdminSettings
)
// --- End Navigation Setup ---

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                // 1. Instantiate Repositories
                // Assuming PetRepositoryImpl also has an empty constructor or doesn't need context
                // If PetRepositoryImpl needs context, change to:
                // val petRepository = remember { PetRepositoryImpl(applicationContext) }
                val petRepository = remember { PetRepositoryImpl() }
                val userRepository = remember { UserRepositoryImpl() } // Correctly uses empty constructor

                // 2. Create ViewModel Factories
                val petViewModelFactory = remember { PetViewModelFactory(petRepository) }
                val userViewModelFactory = remember { UserViewModelFactory(userRepository) }

                // 3. Get ViewModels using the factories
                val petViewModel: PetViewModel = viewModel(factory = petViewModelFactory)
                val userViewModel: UserViewModel = viewModel(factory = userViewModelFactory)

                val navController = rememberNavController()

                AdminMainScreen(
                    navController = navController,
                    petViewModel = petViewModel,
                    userViewModel = userViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    navController: NavHostController,
    petViewModel: PetViewModel,
    userViewModel: UserViewModel
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showPetFab = currentRoute == AdminScreen.PetManagement.route

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = adminScreens.find { it.route == currentRoute }?.label ?: "Admin Panel",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = "Admin Panel",
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            AdminBottomNavigationBar(navController = navController, items = adminScreens)
        },
        floatingActionButton = {
            if (showPetFab) {
                FloatingActionButton(onClick = {
                    navController.navigate("add_pet_screen")
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Pet")
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AdminNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            petViewModel = petViewModel,
            userViewModel = userViewModel
        )
    }
}

@Composable
fun AdminBottomNavigationBar(navController: NavHostController, items: List<AdminScreen>) {
    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AdminNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    petViewModel: PetViewModel,
    userViewModel: UserViewModel
) {
    NavHost(
        navController = navController,
        startDestination = AdminScreen.PetManagement.route,
        modifier = modifier
    ) {
        composable(AdminScreen.Overview.route) {
            AdminOverviewScreen()
        }
        composable(AdminScreen.PetManagement.route) {
            PetListScreen(navController = navController, viewModel = petViewModel)
        }
        composable(AdminScreen.UserManagement.route) {
            UserManagementScreen(userViewModel = userViewModel)
        }
        composable(AdminScreen.AdoptionRequests.route) {
            AdoptionRequestsScreen()
        }
        composable(AdminScreen.AdminSettings.route) {
            AdminSettingsScreen()
        }
        composable("add_pet_screen") {
            AddEditPetScreen(navController = navController, viewModel = petViewModel, petId = null)
        }
        composable("update_pet_screen/{petId}") { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId")
            AddEditPetScreen(navController = navController, viewModel = petViewModel, petId = petId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetListScreen(
    navController: NavHostController,
    viewModel: PetViewModel
) {
    val context = LocalContext.current
    val petsState = viewModel.allPets.observeAsState(initial = emptyList())
    val loading by viewModel.loading.observeAsState(initial = true)
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }

    val filteredPets = remember(searchQuery, petsState.value) {
        petsState.value.filter { pet ->
            pet?.petName?.contains(searchQuery, ignoreCase = true) == true ||
                    pet?.petBreed?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getAllPets()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Pets (Name, Breed)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* Handle search */ })
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { showFilterDialog = true }) {
                Icon(Icons.Outlined.FilterList, contentDescription = "Filter Pets")
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (filteredPets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isNotBlank() || petsState.value.isEmpty()) "No pets found matching your criteria." else "No pets found. Click '+' to add one!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(filteredPets, key = { pet -> pet?.petId ?: "" }) { pet ->
                        pet?.let {
                            PetCardItem(
                                pet = it,
                                onEditClick = {
                                    navController.navigate("update_pet_screen/${it.petId}")
                                },
                                onDeleteClick = {
                                    viewModel.deletePet(it.petId) { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            viewModel.getAllPets() // Refresh list
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Pets") },
            text = { Text("Filter options will go here (e.g., by status, age).") }, // Placeholder
            confirmButton = {
                Button(onClick = { showFilterDialog = false }) { Text("Apply") }
            },
            dismissButton = {
                Button(onClick = { showFilterDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun PetCardItem(pet: PetModel, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = pet.imageUrl.ifEmpty { R.drawable.dog_image }, // Use actual imageUrl or placeholder
                    error = painterResource(id = R.drawable.featuredpet1) // Placeholder on error
                ),
                contentDescription = pet.petName,
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = pet.petName, style = MaterialTheme.typography.titleLarge)
                Text(text = "${pet.petBreed} | Age: ${pet.petAge}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Status: ${pet.petStatus}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (pet.petStatus.equals("Available", ignoreCase = true)) Color.Green.copy(alpha = 0.7f) else Color.Red.copy(alpha = 0.7f)
                )
            }
            Row {
                IconButton(
                    onClick = onEditClick,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Pet")
                }
                IconButton(
                    onClick = onDeleteClick,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Pet")
                }
            }
        }
    }
}

@Composable
fun AdminOverviewScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Admin Overview Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun UserManagementScreen(userViewModel: UserViewModel) { // Pass UserViewModel here
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Example: Observe something from userViewModel
        // val currentUser by userViewModel.currentUser.observeAsState()
        // Text("User Management Screen - Current User: ${currentUser?.email ?: "None"}", style = MaterialTheme.typography.headlineMedium)
        Text("User Management Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun AdoptionRequestsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Adoption Requests Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun AdminSettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Admin Settings Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Added for TopAppBar
@Composable
fun AddEditPetScreen(
    navController: NavHostController,
    viewModel: PetViewModel,
    petId: String?
) {
    val context = LocalContext.current
    val isEditing = petId != null
    var petName by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var petStatus by remember { mutableStateOf("Available") } // Default status
    var petDescription by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") } // For simplicity, manual URL input

    // Observe selectedPet for editing, assuming your PetViewModel has a LiveData for it
    val selectedPetForEditing by viewModel.pet.observeAsState() // Make sure `pet` is the LiveData for a single pet

    LaunchedEffect(petId) {
        if (isEditing && petId != null) {
            viewModel.getPetById(petId) // This should update `viewModel.pet`
        } else {
            // Clear fields if adding a new pet or if navigating back from edit
            petName = ""
            petBreed = ""
            petAge = ""
            petStatus = "Available"
            petDescription = ""
            imageUrl = ""
            viewModel.clearSelectedPet() // Add a method in ViewModel to clear the selected pet LiveData
        }
    }

    LaunchedEffect(selectedPetForEditing) {
        if (isEditing && selectedPetForEditing != null && selectedPetForEditing!!.petId == petId) {
            petName = selectedPetForEditing!!.petName
            petBreed = selectedPetForEditing!!.petBreed
            petAge = selectedPetForEditing!!.petAge
            petStatus = selectedPetForEditing!!.petStatus
            petDescription = selectedPetForEditing!!.petDescription
            imageUrl = selectedPetForEditing!!.imageUrl
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Pet" else "Add New Pet") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp) // Add some spacing
        ) {
            Text(if (isEditing) "Edit Pet Details" else "Add New Pet", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(value = petName, onValueChange = { petName = it }, label = { Text("Pet Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = petBreed, onValueChange = { petBreed = it }, label = { Text("Pet Breed") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = petAge, onValueChange = { petAge = it }, label = { Text("Pet Age") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = petStatus, onValueChange = { petStatus = it }, label = { Text("Pet Status (e.g., Available)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = petDescription, onValueChange = { petDescription = it }, label = { Text("Pet Description") }, modifier = Modifier.fillMaxWidth(),maxLines = 3)
            OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())


            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val petData = PetModel(
                        petId = petId ?: java.util.UUID.randomUUID().toString(), // Generate new ID if adding
                        petName = petName,
                        petBreed = petBreed,
                        petAge = petAge,
                        petStatus = petStatus,
                        petDescription = petDescription,
                        imageUrl = imageUrl
                        // Initialize other fields if your PetModel has them
                    )
                    if (isEditing && petId != null) {
                        viewModel.updatePet(petId, petData) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) navController.popBackStack()
                        }
                    } else {
                        viewModel.addNewPet(petData) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Save Changes" else "Add Pet")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AdminDashboardPreview() {
    PetAdoptionManagementTheme {
        val navController = rememberNavController()
        // Dummy repositories and ViewModels for preview
        val dummyPetRepository = PetRepositoryImpl() // Assuming empty constructor
        val dummyPetViewModel = PetViewModel(dummyPetRepository)

        // For UserRepositoryImpl, if it needs context for some reason in a REAL scenario,
        // you'd pass LocalContext.current.applicationContext.
        // But since we confirmed yours doesn't, an empty constructor is fine for the real app
        // and for the preview.
        val dummyUserRepository = UserRepositoryImpl()
        val dummyUserViewModel = UserViewModel(dummyUserRepository)

        AdminMainScreen(
            navController = navController,
            petViewModel = dummyPetViewModel,
            userViewModel = dummyUserViewModel
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PetCardItemPreview() {
    PetAdoptionManagementTheme {
        PetCardItem(
            pet = PetModel("1", "Buddy", "Golden Retriever", "2 years", "Available", "Friendly and playful", imageUrl = "http://example.com/buddy.jpg"),
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}