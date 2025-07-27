package com.example.petadoptionmanagement.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.petadoptionmanagement.R // Important: Ensure R is imported for drawables
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme

class ProfileViewScreen : ComponentActivity() { // Renamed from ProfileViewActivity to ProfileViewScreen
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                val navController = rememberNavController() // For preview/testing
                // In a real app, you might fetch userProfile from a ViewModel
                // or pass it via intent extras if navigating from another activity.
                ProfileViewScreenContent(
                    navController = navController,
                    userProfile = UserProfile(
                        name = "Suraj Rana",
                        email = "Admin@example.com",
                        profileImageUrl = "https://placehold.co/100x100/FF0000/FFFFFF?text=SR" // Placeholder image URL
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewScreenContent(navController: NavController, userProfile: UserProfile) { // Renamed from ProfileViewScreen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6B8E23), titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F6FA) // Light background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Make content scrollable if it exceeds screen height
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Image
            val painter = rememberAsyncImagePainter(
                // Use profileImageUrl if available, otherwise fall back to local drawable
                model = userProfile.profileImageUrl,
                error = painterResource(id = R.drawable.profile_placeholder), // Ensure this drawable exists
                placeholder = painterResource(id = R.drawable.profile_placeholder) // Optional: show placeholder while loading
            )
            Image(
                painter = painter,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray) // Placeholder background while loading
            )
            Spacer(modifier = Modifier.height(16.dp))

            // User Details
            Text(
                text = userProfile.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22223B)
            )
            Text(
                text = userProfile.email,
                fontSize = 18.sp,
                color = Color(0xFF4A4E69)
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "Bio:",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF22223B)
            )
            Text(
                text = userProfile.bio,
                fontSize = 16.sp,
                color = Color(0xFF4A4E69),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Example Action Button
            Button(
                onClick = { /* TODO: Implement Edit Profile Navigation/Logic */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp)
            ) {
                Text("Edit Profile", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileViewScreenPreview() {
    PetAdoptionManagementTheme {
        val navController = rememberNavController()
        ProfileViewScreenContent(
            navController = navController,
            userProfile = UserProfile(
                name = "Jane Doe",
                email = "jane.doe@example.com",
                bio = "Passionate pet lover and advocate for animal welfare.",
                profileImageUrl = "https://placehold.co/100x100/0000FF/FFFFFF?text=JD"
            )
        )
    }
}