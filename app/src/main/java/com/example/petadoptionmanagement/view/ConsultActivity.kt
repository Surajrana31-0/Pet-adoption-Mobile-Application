package com.example.petadoptionmanagement.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme

// Data class to represent a Doctor
data class Doctor(
    val id: Int,
    val name: String,
    val experience: String,
    val clinic: String,
    val imageRes: Int // Using local drawable for the doctor's profile image as per image
)

// Placeholder for user authentication state (reusing from ContactActivity)
// In a real app, these would be managed in a shared ViewModel or AuthManager
var isLoggedInConsult = mutableStateOf(true) // Simulating logged-in state for demo
var currentUserProfileConsult = mutableStateOf(UserProfile( // Reusing UserProfile data class
    name = "Demo User",
    email = "demo@example.com",
    profileImageUrl = "https://placehold.co/100x100/007BFF/FFFFFF?text=DU" // Placeholder profile image URL
))


class ConsultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                val activityContext = this@ConsultActivity
                val navController = rememberNavController()

                ConsultScreen(
                    navController = navController,
                    onBackClick = { finish() }, // Go back to previous screen/activity
                    onLogout = {
                        isLoggedInConsult.value = false
                        Toast.makeText(activityContext, "Logged out!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(activityContext, SignInActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    },
                    onViewProfile = {
                        val intent = Intent(activityContext, ProfileViewActivity::class.java)
                        startActivity(intent)
                    },
                    userProfile = currentUserProfileConsult.value,
                    isLoggedIn = isLoggedInConsult.value
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onViewProfile: () -> Unit,
    userProfile: UserProfile,
    isLoggedIn: Boolean
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) } // State for dropdown menu

    // Sample data for doctors
    val doctors = remember {
        listOf(
            Doctor(1, "Dr. Siddhartha", "5 yrs", "Clinic in vet hospital", R.drawable.profile_placeholder), // Make sure you have these drawables
            Doctor(2, "Dr. Sahin", "10 yrs", "Clinic in vet hospital", R.drawable.profile_placeholder),
            Doctor(3, "Dr. Sunam", "3 yrs", "Clinic in vet hospital", R.drawable.profile_placeholder),
            Doctor(4, "Dr. Rahil", "6 yrs", "Clinic in vet hospital", R.drawable.profile_placeholder)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PetEy",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val painter = rememberAsyncImagePainter(
                            model = userProfile.profileImageUrl ?: "android.resource://com.example.petadoptionmanagement/drawable/profile_placeholder",
                            error = painterResource(id = R.drawable.profile_placeholder)
                        )
                        Image(
                            painter = painter,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                                .clickable { /* TODO: Navigate to profile view directly if desired */ }
                        )

                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            if (isLoggedIn) {
                                DropdownMenuItem(
                                    text = { Text("View Profile", color = Color.Black) },
                                    onClick = {
                                        showMenu = false
                                        onViewProfile()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Logout", color = Color.Black) },
                                    onClick = {
                                        showMenu = false
                                        onLogout()
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Login", color = Color.Black) },
                                    onClick = {
                                        showMenu = false
                                        val intent = Intent(context, SignInActivity::class.java)
                                        context.startActivity(intent)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sign Up", color = Color.Black) },
                                    onClick = {
                                        showMenu = false
                                        val intent = Intent(context, SignUpActivity::class.java)
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6B8E23), titleContentColor = Color.White) // Green header
            )
        },
        containerColor = Color(0xFF6B8E23) // Green background for the whole screen
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Distribute content
        ) {
            // Doctors List
            LazyColumn(
                modifier = Modifier.weight(1f), // Takes available space
                verticalArrangement = Arrangement.spacedBy(16.dp), // Spacing between cards
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(doctors) { doctor ->
                    DoctorCard(doctor = doctor) {
                        // Handle doctor card click, e.g., navigate to doctor's detail page or booking
                        Toast.makeText(context, "Clicked on ${doctor.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Pagination (1 2 3 .... ->)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = "2",
                    color = Color.White.copy(alpha = 0.7f), // Faded
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = "3",
                    color = Color.White.copy(alpha = 0.7f), // Faded
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = "....",
                    color = Color.White.copy(alpha = 0.7f), // Faded
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24), // You'll need this drawable for the arrow
                    contentDescription = "Next Page",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Cancel Button
            Button(
                onClick = { navController.popBackStack() }, // Go back
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Wider button
                    .height(50.dp)
                    .padding(bottom = 16.dp), // Padding from bottom
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)) // Earthy brown
            ) {
                Text("Cancel", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorCard(doctor: Doctor, onClick: () -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // Fixed height for card
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFDCDCDC)), // Light grey card
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Doctor Image
            Image(
                painter = painterResource(id = doctor.imageRes), // Using local drawable
                contentDescription = doctor.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp) // Size of doctor image
                    .clip(RoundedCornerShape(16.dp)) // Square with rounded corners
                    .background(Color.Gray) // Placeholder background
            )

            // Doctor Details
            Column(
                modifier = Modifier.weight(1f) // Text takes remaining space
            ) {
                Text(
                    text = doctor.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Experience of ${doctor.experience}",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = doctor.clinic,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun ConsultScreenPreview() {
    PetAdoptionManagementTheme {
        val navController = rememberNavController()
        ConsultScreen(
            navController = navController,
            onBackClick = { /* Preview action */ },
            onLogout = { /* Preview action */ },
            onViewProfile = { /* Preview action */ },
            userProfile = UserProfile(
                name = "Preview User",
                email = "preview@example.com",
                profileImageUrl = "https://placehold.co/100x100/FF5733/FFFFFF?text=P"
            ),
            isLoggedIn = true
        )
    }
}