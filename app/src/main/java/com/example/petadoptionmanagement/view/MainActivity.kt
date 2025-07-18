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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType // Added import
import androidx.navigation.navArgument // Added import

// Import the Composable functions for your screens (assuming conversion)
import com.example.petadoptionmanagement.view.HomePageScreen
import com.example.petadoptionmanagement.view.SignInScreen // Assuming you've converted this to a Composable
import com.example.petadoptionmanagement.view.SignUpScreen // Assuming you've converted this to a Composable
import com.example.petadoptionmanagement.view.ResetPasswordScreen // Assuming you've converted this to a Composable
import com.example.petadoptionmanagement.view.ContactScreen // Assuming you've converted this to a Composable
import com.example.petadoptionmanagement.view.ConsultScreen // Assuming you've converted this to a Composable
import com.example.petadoptionmanagement.view.AdoptionScreen // Assuming you've converted this to a Composable
import com.example.petadoptionmanagement.view.ProfileViewScreen // Assuming you've converted this to a Composable
import com.example.petadoptionmanagement.view.UserProfile // Assuming UserProfile is accessible

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        // Home Screen
                        composable("home") {
                            HomePageScreen(navController = navController)
                        }

                        // Authentication Screens (assuming they are now Composable functions)
                        composable("login") {
                            SignInScreen(
                                navController = navController,
                                onSignInClick = { email, password -> /* TODO: Implement actual sign-in logic */ },
                                onForgotPasswordClick = { navController.navigate("resetPassword") },
                                onSignUpClick = { navController.navigate("signup") }
                            )
                        }
                        composable("signup") {
                            SignUpScreen(
                                navController = navController, // Pass navController to SignUpScreen
                                onCreateAccountClick = { username, email, password -> /* TODO: Implement actual sign-up logic */ },
                                onSignInClick = { navController.navigate("login") }
                            )
                        }
                        composable("resetPassword") {
                            ResetPasswordScreen(
                                navController = navController, // Pass navController to ResetPasswordScreen
                                onResetPasswordClick = { email, newPassword -> /* TODO: Implement actual password reset logic */ },
                                onBackToSignInClick = { navController.navigate("login") }
                            )
                        }

                        // Main App Screens
                        composable("contact") {
                            ContactScreen(
                                navController = navController,
                                onSendMessage = { message -> /* TODO: Send message logic */ },
                                onBackClick = { navController.popBackStack() },
                                onLogout = { /* TODO: Logout logic, navigate to login */ navController.navigate("login") { popUpTo("home") { inclusive = true } } },
                                onViewProfile = { navController.navigate("profileView/current_user_id") }, // Example: Pass a user ID
                                userProfile = UserProfile(name = "User", email = "user@example.com"), // Dummy data
                                isLoggedIn = true // Dummy state
                            )
                        }

                        composable("consult") {
                            ConsultScreen(
                                navController = navController,
                                onBackClick = { navController.popBackStack() },
                                onLogout = { /* TODO: Logout logic */ navController.navigate("login") { popUpTo("home") { inclusive = true } } },
                                onViewProfile = { navController.navigate("profileView/current_user_id") },
                                userProfile = UserProfile(name = "User", email = "user@example.com"),
                                isLoggedIn = true
                            )
                        }

                        // --- Adoption Detail Screen ---
                        composable(
                            "adoption/{petId}", // Define the route with a placeholder for petId
                            arguments = listOf(navArgument("petId") { type = NavType.StringType }) // Declare the argument type
                        ) { backStackEntry ->
                            // Retrieve the petId from the navigation arguments
                            val petId = backStackEntry.arguments?.getString("petId") ?: "default_pet_id"
                            AdoptionScreen(
                                navController = navController,
                                petId = petId, // Pass the retrieved petId to the AdoptionScreen
                                onBackClick = { navController.popBackStack() },
                                onContactClick = { navController.navigate("contact") },
                                onAdoptClick = { petName -> /* TODO: Adoption logic */ },
                                onLogout = { /* TODO: Logout logic */ navController.navigate("login") { popUpTo("home") { inclusive = true } } },
                                onViewProfile = { navController.navigate("profileView/current_user_id") },
                                userProfile = UserProfile(name = "User", email = "user@example.com"),
                                isLoggedIn = true
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
                                userProfile = UserProfile(name = "User $userId", email = "$userId@example.com"), // Dummy data
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Placeholder Composable for About Page (now AboutScreen) ---
@Composable
fun AboutScreen(navController: NavController) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
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

// NOTE: The placeholder `SignUpActivity()` and `ContactActivity()`
// and `AboutActivity()` composables from your previous code have been
// removed here, as the expectation is that their actual UI content
// has been moved into dedicated Composable functions (e.g., `SignUpScreen`,
// `ContactScreen`, `AboutScreen`) as discussed in the previous turn.
// If you still have `SignUpActivity` defined as a Composable, please rename it to `SignUpScreen`
// to avoid confusion with an actual Android Activity class.