// /view/SplashActivity.kt

package com.example.petadoptionmanagement.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.cloudinary.Cloudinary
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                // Correctly providing all dependencies for the repository
                val userRepository = remember {
                    val auth = FirebaseAuth.getInstance()
                    val firestore = FirebaseFirestore.getInstance()

                    // IMPORTANT: Replace with your actual Cloudinary credentials
                    val config = mapOf(
                        "cloud_name" to "dd9sooenk",
                        "api_key" to "281858352367463",
                        "api_secret" to "dj8vgOz6YCPGqqvQIGEa-dhQ0Ig"
                    )
                    val cloudinary = Cloudinary(config)

                    UserRepositoryImpl(auth, firestore, cloudinary, applicationContext)
                }

                val userViewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(userRepository)
                )

                SplashScreen(userViewModel = userViewModel)
            }
        }
    }
}

@Composable
fun SplashScreen(userViewModel: UserViewModel) {
    val context = LocalContext.current
    var startAnimation by remember { mutableStateOf(false) }

    // --- FIX: ALL animation values are now defined here ---
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(900, easing = FastOutSlowInEasing), label = "logo_scale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(900, delayMillis = 120, easing = FastOutSlowInEasing), label = "logo_alpha"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(540, delayMillis = 580, easing = FastOutSlowInEasing), label = "title_alpha"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(540, delayMillis = 780, easing = FastOutSlowInEasing), label = "subtitle_alpha"
    )
    val progressAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(350, delayMillis = 1120, easing = FastOutSlowInEasing), label = "progress_alpha"
    )

    // --- State Observation & Navigation ---
    val isLoggedIn by userViewModel.isLoggedIn.observeAsState()
    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn != null && !hasNavigated) {
            delay(1500) // Ensure splash has time to animate
            val nextActivity = if (isLoggedIn == true) {
                when (userViewModel.currentUser.value?.role?.name?.lowercase()) {
                    "admin" -> AdminDashboardActivity::class.java
                    else -> AdopterDashboardActivity::class.java
                }
            } else {
                SignInActivity::class.java
            }

            val intent = Intent(context, nextActivity).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            (context as? Activity)?.finish()
            hasNavigated = true
        }
    }

    // --- UI Design ---
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF8360c3), Color(0xFF2ebf91))
    )

    Box(
        modifier = Modifier.fillMaxSize().background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.petadoptionlogo),
                contentDescription = "App Logo",
                modifier = Modifier.size(155.dp).scale(logoScale).alpha(logoAlpha)
            )
            Spacer(Modifier.height(26.dp))
            Text(
                text = "PetConnect Adoptions",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha) // Will now resolve
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Find Your Fur-ever Friend.",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.90f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha) // Will now resolve
            )
            Spacer(Modifier.height(36.dp))
            if (progressAlpha > 0.02f) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(34.dp).alpha(progressAlpha) // Will now resolve
                )
            }
        }
    }
}
