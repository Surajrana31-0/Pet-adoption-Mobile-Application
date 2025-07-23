package com.example.petadoptionmanagement.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                // Pass the single navigation action: always go to HomePage
                SplashToHomeContent(
                    onNavigationFinished = {
                        startActivity(Intent(this, HomePage::class.java))
                        finish() // Finish SplashActivity so user can't go back
                    }
                )
            }
        }
    }
}

@Composable
fun SplashToHomeContent(
    onNavigationFinished: () -> Unit // Callback when splash delay is over
) {
    LaunchedEffect(Unit) {
        delay(2500) // Keep your splash screen delay
        onNavigationFinished() // Trigger navigation
    }

    Scaffold(
        containerColor = Color(0xFFF5F6FA) // Match your home page background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F6FA)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Your app logo/splash image
            Image(
                painter = painterResource(id = R.drawable.hero_pet), // Use your splash/logo image!
                contentDescription = null,
                modifier = Modifier
                    .height(120.dp)
                    .width(120.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            CircularProgressIndicator(
                color = Color(0xFF9A8C98)
            )
        }
    }
}