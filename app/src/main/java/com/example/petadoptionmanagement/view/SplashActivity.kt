package com.example.petadoptionmanagement.view

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
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                val userRepository = remember { UserRepositoryImpl(applicationContext) }
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

    // --- ANIMATIONS ---
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

    // --- STATE OBSERVATION ---
    //Observe login and user state (Provided by live data)
    val isLoggedIn by userViewModel.isLoggedIn.observeAsState()
    val currentUser by userViewModel.currentUser.observeAsState()

    // --- NAVIGATION HANDLING ---
    var navigated by remember { mutableStateOf(false) }
    LaunchedEffect(isLoggedIn, currentUser) {
        startAnimation = true

        // Wait for splash animation
        delay(2100)

        // Wait for currentUser state to be loaded (max 1.4s after animation)
        val waitStart = System.currentTimeMillis()
        while ((isLoggedIn == null || (isLoggedIn == true && currentUser == null)) &&
            System.currentTimeMillis() - waitStart < 1400
        ) {
            delay(100)
        }

        if (!navigated) {
            navigated = true
            val user = currentUser // Assign to local
            val role = user?.role?.lowercase() ?: ""
            val nextActivity = when {
                isLoggedIn == true && user != null -> {
                    when(role) {
                        "admin" -> AdminDashboardActivity::class.java
                        "adopter" -> AdopterDashboardActivity::class.java
                        else -> SignInActivity::class.java
                    }
                }
                else -> SignInActivity::class.java
            }
            val intent = Intent(context, nextActivity).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
        }
    }

    // --- DESIGN ---
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF8360c3),
            Color(0xFF2ebf91)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.petadoptionlogo), // Use your logo asset here!
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(155.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )
            Spacer(Modifier.height(26.dp))

            Text(
                text = "PetConnect Adoptions",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Connecting Hearts, One Paw at a Time",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.90f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(Modifier.height(36.dp))

            // Slogan
            Text(
                text = "Find Your Fur-ever Friend.",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.96f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha)
            )

            Spacer(Modifier.height(33.dp))

            // Loading spinner & text (nice fade-in)
            if (progressAlpha > 0.02f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 4.dp,
                        modifier = Modifier
                            .size(34.dp)
                            .alpha(progressAlpha)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Loading...",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.alpha(progressAlpha)
                    )
                }
            }
        }

        // Version/footer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 29.dp, end = 15.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = "v1.0.0  ⬤  Adopt, Don't Shop",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.57f),
                modifier = Modifier.alpha(progressAlpha)
            )
        }
    }
}
