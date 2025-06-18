package com.example.petadoptionmanagement.view

import android.app.Activity
import android.content.Context
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
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashScreenBody()
        }
    }
}

@Composable
fun SplashScreenBody() {
    val context = LocalContext.current
    val activity = context as Activity

    val sharedPreferences = context.getSharedPreferences("User", Context.MODE_PRIVATE)
    val localEmail: String? = sharedPreferences.getString("email", "")

    LaunchedEffect(Unit) {
        delay(2500)
        if (localEmail.isNullOrEmpty()) {
            val intent = Intent(context, SignInActivity::class.java)
            context.startActivity(intent)
            activity.finish()
        } else {
            val intent = Intent(context, HomePage::class.java)
            context.startActivity(intent)
            activity.finish()
        }
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
            // Replace with your logo or splash image
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