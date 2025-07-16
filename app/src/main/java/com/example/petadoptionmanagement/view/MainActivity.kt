package com.example.petadoptionmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petadoptionmanagement.view.HomePageScreen // Import the new homepage screen
import com.example.petadoptionmanagement.view.SignInActivity // Import your SignInActivity (assuming it's still an Activity)
import com.example.petadoptionmanagement.view.SignUpActivity // Import your SignUpActivity
import com.example.petadoptionmanagement.view.ResetPasswordActivity // Import your ResetPasswordActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        // Home Screen
                        composable("home") {
                            HomePageScreen(navController = navController)
                        }

                        // Login/Registration Screen (using the actual Activity for now)
                        // In a real app, you might have a Composable for Login/Registration within NavHost
                        // For demonstration, we'll start the Activity directly.
                        composable("login") {
                            // This will typically involve launching an Activity for non-composable screens
                            // or navigating to a Composable login screen.
                            // For simplicity, let's assume we navigate to SignInActivity.
                            // If SignInActivity was also a Composable, you'd call @Composable SignInScreen() here.
                            // As SignInActivity is a ComponentActivity, we'd traditionally launch it.
                            // For direct composable navigation:
                            // navController.navigate("signInComposable") // if you make SignInActivity's UI a composable route
                            // For now, let's just show a placeholder or simplify.
                            // We can create a simple placeholder composable for "login" for now:
                            LoginRegistrationPlaceholder()
                        }

                        // About Page (Placeholder)
                        composable("about") {
                            AboutPagePlaceholder(navController)
                        }

                        // Contact Page (Placeholder)
                        composable("contact") {
                            ContactPagePlaceholder(navController)
                        }

                        // You might also have routes for SignInScreen, SignUpScreen, ResetPasswordScreen
                        // if you convert them to Composable functions instead of separate Activities.
                        // For example:
                        // composable("signInComposable") { SignInScreen(...) }
                        // composable("signUpComposable") { SignUpScreen(...) }
                        // composable("resetPasswordComposable") { ResetPasswordScreen(...) }
                    }
                }
            }
        }
    }
}

// --- Placeholder Composable for Login/Registration ---
@Composable
fun LoginRegistrationPlaceholder() {
    // This is a placeholder. In your actual app, you would navigate to your SignInActivity
    // or to a Composable that represents your sign-in/registration flow.
    // For now, it just shows a simple message.
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login / Registration Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("This is where users would sign in or register to adopt a pet.")
        }
    }
}

// --- Placeholder Composable for About Page ---
@Composable
fun AboutPagePlaceholder(navController: NavController) {
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

// --- Placeholder Composable for Contact Page ---
@Composable
fun ContactPagePlaceholder(navController: NavController) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Contact Us", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Reach out to us via email or phone for any inquiries.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
    }
}