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
import com.example.petadoptionmanagement.model.UserModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator

// NEW IMPORTS FOR PET MANAGEMENT
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.viewmodel.PetViewModel
import com.example.petadoptionmanagement.viewmodel.PetViewModelFactory
import com.example.petadoptionmanagement.view.PetDashboardScreen
import com.example.petadoptionmanagement.view.AddPetScreen
import com.example.petadoptionmanagement.view.UpdatePetScreen

// Assuming you have these composables, otherwise they need to be created
// import com.example.petadoptionmanagement.view.SplashScreen // If you have a dedicated composable splash
// import com.example.petadoptionmanagement.view.SignInScreen // Ensure this is a composable, not an Activity
// import com.example.petadoptionmanagement.view.SignUpScreen // Ensure this is a composable, not an Activity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val context = LocalContext.current.applicationContext

                    val userRepository = remember { UserRepositoryImpl(context) }
                    val petRepository = remember { PetRepositoryImpl() }

                    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))
                    val petViewModel: PetViewModel = viewModel(factory = PetViewModelFactory(petRepository))

                    // Observing login state for potential re-routes if user logs out or session changes
                    val isLoggedIn = userViewModel.isLoggedIn.observeAsState(initial = false) // Assuming initial false if not explicitly logged in

                    NavHost(
                        navController = navController,
                        // As per your logic, Home page is the first for all users
                        startDestination = "splash_screen" // Start with a splash screen or directly home
                    ) {
                        // Splash Screen (Optional, for a short delay)
                        composable("splash_screen") {
                            // You can replace this with your actual splash screen composable
                            LaunchedEffect(key1 = true) {
                                kotlin.io.path.file.delay(2000) // 2 second delay
                                navController.navigate("home") {
                                    popUpTo("splash_screen") { inclusive = true } // Remove splash from back stack
                                }
                            }
                            LoadingScreen() // Or your custom splash screen UI
                        }

                        // Home Screen (Main landing page for all users)
                        composable("home") {
                            // HomePageScreen should have Sign In/Sign Up buttons for unauthenticated users
                            HomePageScreen(navController = navController, userViewModel = userViewModel)
                        }

                        // Authentication Screens (now Composables)
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
                            PetDashboardScreen(navController = navController, viewModel = petViewModel)
                        }
                        composable("add_pet_screen") {
                            AddPetScreen(navController = navController, petViewModel = petViewModel)
                        }
                        composable("update_pet_screen/{petId}",
                            arguments = listOf(navArgument("petId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val petId = backStackEntry.arguments?.getString("petId")
                            UpdatePetScreen(navController = navController, petViewModel = petViewModel, petId = petId)
                        }

                        // Main App Screens (for Users)
                        composable("contact") {
                            ContactScreen(
                                navController = navController,
                                userViewModel = userViewModel,
                                onSendMessage = { message -> /* TODO: Send message logic */ },
                                onBackClick = { navController.popBackStack() },
                                onViewProfile = {
                                    userViewModel.currentUser.value?.userId?.let { userId ->
                                        navController.navigate("profileView/$userId")
                                    }
                                }
                            )
                        }

                        composable("consult") {
                            ConsultScreen(
                                navController = navController,
                                userViewModel = userViewModel,
                                onBackClick = { navController.popBackStack() },
                                onViewProfile = {
                                    userViewModel.currentUser.value?.userId?.let { userId ->
                                        navController.navigate("profileView/$userId")
                                    }
                                }
                            )
                        }

                        composable(
                            "adoption/{petId}",
                            arguments = listOf(navArgument("petId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val petId = backStackEntry.arguments?.getString("petId") ?: "default_pet_id"
                            AdoptionScreen(
                                navController = navController,
                                userViewModel = userViewModel,
                                petId = petId,
                                onBackClick = { navController.popBackStack() },
                                onContactClick = { navController.navigate("contact") },
                                onAdoptClick = { petName -> /* TODO: Adoption logic */ }
                            )
                        }

                        composable("about") {
                            AboutScreen(navController = navController)
                        }

                        composable(
                            "profileView/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: "default_user_id"
                            ProfileViewScreen(
                                navController = navController,
                                userViewModel = userViewModel,
                                userId = userId
                            )
                        }
                    }
                }
            }
        }
    }
}

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