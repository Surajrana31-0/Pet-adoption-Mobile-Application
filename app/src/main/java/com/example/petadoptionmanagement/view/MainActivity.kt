package com.example.petadoptionmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.view.AdoptionScreen
import com.example.petadoptionmanagement.view.ContactScreen
import com.example.petadoptionmanagement.view.ConsultScreen
import com.example.petadoptionmanagement.view.HomePageScreen
import com.example.petadoptionmanagement.view.ProfileViewScreen
import com.example.petadoptionmanagement.view.ResetPasswordScreen
import com.example.petadoptionmanagement.view.SignInScreen
import com.example.petadoptionmanagement.view.SignUpScreen
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory
import com.example.petadoptionmanagement.model.UserModel // Import UserModel
import androidx.compose.runtime.getValue // Import for mutableStateOf
import androidx.compose.runtime.mutableStateOf // Import for mutableStateOf
import androidx.compose.runtime.setValue // Import for mutableStateOf
import androidx.compose.material3.CircularProgressIndicator // For LoadingScreen

// NEW IMPORTS FOR PET MANAGEMENT
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.viewmodel.PetViewModel
import com.example.petadoptionmanagement.viewmodel.PetViewModelFactory
import com.example.petadoptionmanagement.view.PetDashboardScreen // Assuming this is your updated PetDashboardScreen
import com.example.petadoptionmanagement.view.AddPetScreen
import com.example.petadoptionmanagement.view.UpdatePetScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val context = LocalContext.current.applicationContext // Use applicationContext for ViewModel factories

                    // Initialize Repositories
                    val userRepository = remember { UserRepositoryImpl(context) }
                    val petRepository = remember { PetRepositoryImpl() } // Assuming no context needed, or pass it if required

                    // Initialize ViewModels using their Factories
                    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))
                    val petViewModel: PetViewModel = viewModel(factory = PetViewModelFactory(petRepository))

                    // Observe the login state from the ViewModel
                    val isLoggedIn = userViewModel.isLoggedIn.observeAsState(initial = null) // Set initial to null for loading state
                    val currentUser = userViewModel.currentUser.observeAsState(initial = null)

                    // Flag to ensure initial navigation happens only once
                    var initialNavigationDone by remember { mutableStateOf(false) }

                    LaunchedEffect(isLoggedIn.value, initialNavigationDone) {
                        // Navigate only if isLoggedIn state is resolved and initial navigation hasn't occurred
                        if (!initialNavigationDone && isLoggedIn.value != null) {
                            if (isLoggedIn.value == true) { // If initially logged in
                                navController.navigate("home") {
                                    popUpTo(navController.graph.id) { // Pop all destinations up to the start of the graph
                                        inclusive = true
                                    }
                                }
                            } else { // If not logged in
                                navController.navigate("login") {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                    }
                                }
                            }
                            initialNavigationDone = true
                        }
                    }

                    NavHost(
                        navController = navController,
                        // Use a loading screen as the initial destination until isLoggedIn is determined
                        startDestination = "loading"
                    ) {
                        // Loading Screen (simple placeholder)
                        composable("loading") {
                            LoadingScreen()
                        }

                        // Home Screen (main dashboard after login)
                        composable("home") {
                            HomePageScreen(navController = navController, userViewModel = userViewModel)
                        }

                        // Authentication Screens
                        composable("login") {
                            SignInScreen(navController = navController, userViewModel = userViewModel)
                        }
                        composable("signup") {
                            SignUpScreen(navController = navController, userViewModel = userViewModel)
                        }
                        composable("resetPassword") {
                            ResetPasswordScreen(navController = navController, userViewModel = userViewModel)
                        }

                        // Pet Management Screens (for Admin)
                        composable("pet_dashboard") {
                            // Ensure PetDashboardScreen accepts NavController and PetViewModel
                            PetDashboardScreen(navController = navController, viewModel = petViewModel)
                        }
                        // Uncommented and Corrected Pet Management Screens
                        composable("add_pet_screen") {
                            AddPetScreen(navController = navController, petViewModel = petViewModel)
                        }
                        composable("update_pet_screen/{petId}",
                            arguments = listOf(navArgument("petId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val petId = backStackEntry.arguments?.getString("petId")
                            UpdatePetScreen(navController = navController, petViewModel = petViewModel, petId = petId)
                        }


                        // Main App Screens (for Users) - Pass UserViewModel directly
                        composable("contact") {
                            ContactScreen(
                                navController = navController,
                                userViewModel = userViewModel, // Pass ViewModel
                                onSendMessage = { message -> /* TODO: Send message logic */ },
                                onBackClick = { navController.popBackStack() },
                                onViewProfile = {
                                    // Navigate to profile view, ensure userId is available
                                    currentUser.value?.userId?.let { userId ->
                                        navController.navigate("profileView/$userId")
                                    } ?: run {
                                        // Handle case where user ID is null (e.g., show a toast)
                                        // Toast.makeText(context, "User not loaded", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                // Removed userModel directly, ContactScreen should observe it internally
                            )
                        }

                        composable("consult") {
                            ConsultScreen(
                                navController = navController,
                                userViewModel = userViewModel, // Pass ViewModel
                                onBackClick = { navController.popBackStack() },
                                onViewProfile = {
                                    currentUser.value?.userId?.let { userId ->
                                        navController.navigate("profileView/$userId")
                                    } ?: run {
                                        // Handle case where user ID is null
                                    }
                                },
                                // Removed userModel directly
                            )
                        }

                        composable(
                            "adoption/{petId}",
                            arguments = listOf(navArgument("petId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val petId = backStackEntry.arguments?.getString("petId") ?: "default_pet_id"
                            AdoptionScreen(
                                navController = navController,
                                userViewModel = userViewModel, // Pass ViewModel
                                petId = petId,
                                onBackClick = { navController.popBackStack() },
                                onContactClick = { navController.navigate("contact") },
                                onAdoptClick = { petName -> /* TODO: Adoption logic */ },
                                // Removed userModel directly
                            )
                        }

                        // About Page
                        composable("about") {
                            AboutScreen(navController = navController)
                        }

                        // Profile View Screen (receives userId argument)
                        composable(
                            "profileView/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: "default_user_id"
                            ProfileViewScreen(
                                navController = navController,
                                userViewModel = userViewModel, // Pass ViewModel
                                userId = userId,
                                // Removed userModel directly
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Placeholder Composable for About Page ---
@Composable
fun AboutScreen(navController: NavController) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("About Us", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("This page will tell users more about your organization and mission.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
    }
}

// --- Placeholder for a simple Loading Screen ---
@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Loading...")
    }
}