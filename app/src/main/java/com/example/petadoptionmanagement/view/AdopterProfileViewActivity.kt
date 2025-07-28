// /view/AdopterProfileViewActivity.kt

package com.example.petadoptionmanagement.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cloudinary.Cloudinary
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdopterProfileViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userId = intent.getStringExtra("userId") // Get the user ID from the Intent
        if (userId == null) {
            Toast.makeText(this, "User ID is missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            PetAdoptionManagementTheme {
                // Initialize UserRepository and UserViewModel
                val userRepository = remember {
                    val auth = FirebaseAuth.getInstance()
                    val firestore = FirebaseFirestore.getInstance()
                    val config = mapOf(
                        "cloud_name" to "dd9sooenk", // Replace with actual credentials
                        "api_key" to "281858352367463",       // Replace with actual credentials
                        "api_secret" to "dj8vgOz6YCPGqqvQIGEa-dhQ0Ig"   // Replace with actual credentials
                    )
                    val cloudinary = Cloudinary(config)
                    UserRepositoryImpl(auth, firestore, cloudinary, applicationContext)
                }
                val userViewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(userRepository)
                )

                AdopterProfileViewScreen(userId = userId, userViewModel = userViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdopterProfileViewScreen(userId: String, userViewModel: UserViewModel) {
    val context = LocalContext.current
    val viewedUser by userViewModel.viewedUser.observeAsState() // Observe the specific user details
    val isLoading by userViewModel.isLoading.observeAsState(false)
    val message by userViewModel.message.observeAsState()

    // Fetch user details when the screen starts or userId changes
    LaunchedEffect(userId) {
        userViewModel.getUserFromDatabase(userId)
    }

    // Effect for showing Toast messages
    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adopter Profile") },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F6FA) // Light background color for the screen
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray), // Placeholder background
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = viewedUser?.profilePictureUrl,
                    contentDescription = "Adopter Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.profile_placeholder), // Fallback image if URL is bad
                    placeholder = painterResource(id = R.drawable.profile_placeholder) // Placeholder while loading
                )
                if (viewedUser?.profilePictureUrl.isNullOrBlank()) {
                    Image(
                        painter = painterResource(id = R.drawable.profile_placeholder), // Your generic profile placeholder
                        contentDescription = "Placeholder Profile Picture",
                        modifier = Modifier.fillMaxSize(0.8f) // Make placeholder smaller
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Personal Information", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    if (viewedUser == null && isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text("Loading adopter details...")
                    } else if (viewedUser != null) {
                        ProfileDetailRow(label = "Username", value = viewedUser!!.username)
                        ProfileDetailRow(label = "First Name", value = viewedUser!!.firstname)
                        ProfileDetailRow(label = "Last Name", value = viewedUser!!.lastname)
                        ProfileDetailRow(label = "Email", value = viewedUser!!.email)
                        ProfileDetailRow(label = "Contact", value = viewedUser!!.contact)
                        ProfileDetailRow(label = "Role", value = viewedUser!!.role.name)
                        // Add more details if your UserModel has them (e.g., createdAt)
                    } else {
                        Text("Adopter details not found.", modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(0.6f)
        )
    }
}
