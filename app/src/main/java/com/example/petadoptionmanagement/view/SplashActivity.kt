package com.example.petadoptionmanagement.view

import android.annotation.SuppressLint
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState // Added this import
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                // Initialize ViewModel here using a factory
                // Assuming UserRepositoryImpl() does not require applicationContext
                val userRepository = remember { UserRepositoryImpl() }
                val userViewModelFactory = remember { UserViewModelFactory(userRepository) }
                val userViewModel: UserViewModel = viewModel(factory = userViewModelFactory)

                SplashScreen(userViewModel = userViewModel)
            }
        }
    }
}

@Composable
fun SplashScreen(userViewModel: UserViewModel) {
    val context = LocalContext.current

    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "logo_scale_animation"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "logo_alpha_animation"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 600, easing = FastOutSlowInEasing),
        label = "title_alpha_animation"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = 800, easing = FastOutSlowInEasing),
        label = "subtitle_alpha_animation"
    )
    val progressAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress_alpha_animation"
    )

    // Observe LiveData from ViewModel
    // Make sure UserViewModel exposes these LiveData objects correctly
    val isLoggedIn by userViewModel.isLoggedIn.observeAsState(initial = false)
    val currentUser by userViewModel.currentUser.observeAsState(initial = null)

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Animation display time

        // An additional small delay to ensure LiveData observers have a chance to react
        // to any initial state changes from the ViewModel's init block.
        delay(500)

        val targetClass = if (isLoggedIn && currentUser != null) { // Ensure currentUser is also not null
            if (currentUser?.role == "admin") {
                Log.d("SplashActivity", "User is admin. Navigating to AdminDashboardActivity.")
                AdminDashboardActivity::class.java // Corrected to AdminDashboardActivity
            } else {
                Log.d("SplashActivity", "User is logged in (not admin). Navigating to AdopterDashboardActivity.")
                AdopterDashboardActivity::class.java
            }
        } else {
            Log.d("SplashActivity", "User not logged in or currentUser is null. Navigating to SignInActivity.")
            SignInActivity::class.java // Removed unnecessary 'as'
        }

        val intent = Intent(context, targetClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)

        // Finish current activity (SplashActivity)
        (context as? ComponentActivity)?.finish()
    }

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
            Image(
                painter = painterResource(R.drawable.petadoptionlogo),
                contentDescription = "Pet Adoption Logo",
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PetConnect Adoptions",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Connecting Hearts, One Paw at a Time",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha) // Assuming this should also use titleAlpha or a similar delayed alpha
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Find Your Fur-ever Friend",
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                modifier = Modifier.alpha(progressAlpha) // Assuming this uses progressAlpha
            )
        }
    }
}