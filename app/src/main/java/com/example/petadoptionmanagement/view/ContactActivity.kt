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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter // For loading images from URL
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme

// Placeholder for user authentication state (in a real app, this would be in a ViewModel/AuthManager)
var isLoggedIn = mutableStateOf(true) // Simulating logged-in state for demo
var currentUserProfile = mutableStateOf(UserProfile(
    name = "Demo User",
    email = "demo@example.com",
    profileImageUrl = "https://placehold.co/100x100/007BFF/FFFFFF?text=DU" // Placeholder profile image URL
))

class ContactActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                val activityContext = this@ContactActivity
                val navController = rememberNavController() // NavController for this Activity's composables

                ContactScreen(
                    navController = navController,
                    onSendMessage = { message ->
                        Toast.makeText(activityContext, "Message sent: $message", Toast.LENGTH_SHORT).show()
                        // In a real app: Send message to backend
                    },
                    onBackClick = {
                        // This will pop the current activity from the stack
                        finish()
                    },
                    onLogout = {
                        // In a real app: Perform Firebase logout, clear session
                        isLoggedIn.value = false // Update state for demo
                        Toast.makeText(activityContext, "Logged out!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(activityContext, SignInActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish() // Finish all activities on the stack
                    },
                    onViewProfile = {
                        // In a real app: Navigate to ProfileViewScreen
                        // If ProfileViewScreen is a Composable within the same NavHost:
                        // navController.navigate("profileView")
                        // If ProfileViewScreen is a separate Activity:
                        val intent = Intent(activityContext, ProfileViewActivity::class.java)
                        startActivity(intent)
                    },
                    userProfile = currentUserProfile.value, // Pass current user profile
                    isLoggedIn = isLoggedIn.value // Pass login state
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    navController: NavController,
    onSendMessage: (String) -> Unit,
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onViewProfile: () -> Unit,
    userProfile: UserProfile,
    isLoggedIn: Boolean
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) } // State for dropdown menu

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Centered "PetEy" title
                    Text(
                        "PetEy",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.White, // White text for app name
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                },
                navigationIcon = {
                    // Back button on the left
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Profile Image (or placeholder)
                        val painter = rememberAsyncImagePainter(
                            model = userProfile.profileImageUrl ?: "android.resource://com.example.petadoptionmanagement/drawable/profile_placeholder",
                            error = painterResource(id = R.drawable.profile_placeholder) // Fallback for loading errors
                        )
                        Image(
                            painter = painter,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp) // Size of the profile image in header
                                .clip(CircleShape)
                                .background(Color.Gray) // Placeholder background
                                .clickable { /* TODO: Navigate to profile view directly if desired */ }
                        )

                        // Hamburger Menu Icon
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }

                        // Dropdown Menu for Hamburger
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
                                        // Navigate to login screen
                                        val intent = Intent(context, SignInActivity::class.java)
                                        context.startActivity(intent)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sign Up", color = Color.Black) },
                                    onClick = {
                                        showMenu = false
                                        // Navigate to signup screen
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()), // Make content scrollable
            contentAlignment = Alignment.Center
        ) {
            // Background paw prints
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                for (i in 0..4) { // Adjust count for more/fewer paw prints
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.paw_print), // Ensure this exists
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).offset(x = (-20).dp).alpha(0.1f) // Faded and slightly offset
                        )
                        Image(
                            painter = painterResource(id = R.drawable.paw_print), // Ensure this exists
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).offset(x = 20.dp).alpha(0.1f) // Faded and slightly offset
                        )
                    }
                }
            }


            // Main content card
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.7f) // Make card take up more vertical space
                    .padding(horizontal = 16.dp), // Ensure horizontal padding for the card itself
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCDCDC)), // Light grey card
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center // Center content vertically within the card
                ) {
                    Text(
                        "Message:",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Start) // Align "Message" text to start
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField( // Used OutlinedTextField as it matches the image style better
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = { Text("Your Message", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp), // Increased height for message input
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors( // Custom colors for outlined text field
                            focusedBorderColor = Color(0xFF8B4513),
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Submit Button
                        Button(
                            onClick = { onSendMessage(messageText) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)) // Earthy brown
                        ) {
                            Text("Submit", fontSize = 18.sp, color = Color.White)
                        }

                        // Cancel Button
                        Button(
                            onClick = { messageText = "" }, // Clear message on cancel
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .padding(start = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray) // Grey for cancel
                        ) {
                            Text("Cancel", fontSize = 18.sp, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    // Contact Info
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Contact us: +91 9806497598",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Text(
                            "email: info@pet_Ey.xs4.com",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun ContactScreenPreview() {
    PetAdoptionManagementTheme {
        val navController = rememberNavController()
        ContactScreen(
            navController = navController,
            onSendMessage = { /* Preview action */ },
            onBackClick = { /* Preview action */ },
            onLogout = { /* Preview action */ },
            onViewProfile = { /* Preview action */ },
            userProfile = UserProfile(
                name = "Preview User",
                email = "preview@example.com",
                profileImageUrl = "https://placehold.co/100x100/FF5733/FFFFFF?text=P" // Example URL for Coil
            ),
            isLoggedIn = true
        )
    }
}