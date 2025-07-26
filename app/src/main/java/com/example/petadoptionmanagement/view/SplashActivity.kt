package com.example.petadoptionmanagement.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Import for ViewModel
import com.example.petadoptionmanagement.R // Make sure you have R.drawable.logo and R.drawable.hero_pet or similar
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen") // Suppress lint warning for custom splash screen
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme { // Apply your app's theme
                // Initialize ViewModel here using a factory
                val userRepository = UserRepositoryImpl(applicationContext)
                val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))

                SplashScreen(userViewModel = userViewModel)
            }
        }
    }
}

@Composable
fun SplashScreen(userViewModel: UserViewModel) {
    val context = LocalContext.current

    // Animation states
    var startAnimation by remember { mutableStateOf(false) }

    // Animated values (from your EcoSajha example, adapted)
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "logo_alpha"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 600, easing = FastOutSlowInEasing),
        label = "title_alpha"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 800, easing = FastOutSlowInEasing),
        label = "subtitle_alpha"
    )
    val progressAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress_alpha"
    )

    // Observe LiveData from ViewModel
    val isLoggedIn by userViewModel.isLoggedIn.observeAsState(initial = false)
    val currentUser by userViewModel.currentUser.observeAsState(initial = null)

    // Navigation logic
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Keep your splash screen delay for animation

        // Fetch current user status from the ViewModel
        // The ViewModel observes changes via UserRepository's authStateListener
        // so `currentUser` and `isLoggedIn` LiveData should be up-to-date here.
        // We'll give it a moment to ensure the observer has fired.
        delay(500) // Small additional delay to ensure LiveData updates from init block are processed

        val targetClass = if (isLoggedIn) {
            // Check for admin role
            if (currentUser?.role == "admin") {
                Log.d("SplashActivity", "User is admin. Navigating to AdminDashboardActivity.")
                AdminDashboardActivity::class.java
            } else {
                Log.d("SplashActivity", "User is logged in (not admin). Navigating to HomePage.")
                HomePage::class.java // Assuming HomePage is the main screen for general users
            }
        } else {
            Log.d("SplashActivity", "User not logged in. Navigating to HomePage (with Login/SignUp options).")
            HomePage::class.java // Go to HomePage for unauthenticated users
        }

        val intent = Intent(context, targetClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        // Finish current activity
        if (context is ComponentActivity) {
            context.finish()
        }
    }

    // UI Content (adapted from EcoSajha, but with Pet Adoption theme)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF8A6F87), // Soft purple/grey
            Color(0xFF5A4D59), // Deeper purple/grey
            Color(0xFF3B333B)  // Darkest purple/grey
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Logo with animation - Use a pet-themed logo or your existing hero_pet
            Image(
                painter = painterResource(R.drawable.hero_pet), // Make sure R.drawable.hero_pet exists
                contentDescription = "Pet Adoption Logo",
                modifier = Modifier
                    .size(160.dp) // Slightly larger logo
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main title with animation - Changed to Pet Adoption theme
            Text(
                text = "PetConnect Adoptions",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Connecting Hearts, One Paw at a Time",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Welcome text with animation
            Text(
                text = "Find Your Fur-ever Friend",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading indicator with animation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(progressAlpha)
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Loading...",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Light
                )
            }
        }

        // Version info at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "v1.0.0 • Adopt, Don't Shop",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.alpha(progressAlpha)
            )
        }
    }
}

// You can keep a Preview if you like, but it won't show the navigation logic
// @Preview(showBackground = true)
// @Composable
// fun SplashPreview() {
//     PetAdoptionManagementTheme {
//         // SplashPreviewContent() // You'd need a mock ViewModel if you want to preview the actual Composable
//     }
// }