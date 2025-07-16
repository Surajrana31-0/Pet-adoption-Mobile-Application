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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.google.firebase.firestore.FirebaseFirestore // Firebase Firestore import
import kotlinx.coroutines.tasks.await

// --- Data Classes ---
// Reusing UserProfile from ContactActivity, ConsultActivity
// data class UserProfile(...) // Make sure UserProfile is accessible (e.g., in a common file)

// Data class for Pet details fetched from Firebase
data class Pet(
    val id: String = "", // Firebase document ID
    val name: String = "",
    val breed: String = "",
    val valAge: String = "", // 'Age' is a keyword in some DBs, using valAge
    val medicalHistory: String = "",
    val description: String = "",
    val imageUrl: String = "" // URL for pet's image
)

// Placeholder for user authentication state (reusing from ContactActivity, ConsultActivity)
var isLoggedInAdoption = mutableStateOf(true)
var currentUserProfileAdoption = mutableStateOf(UserProfile(
    name = "Demo User",
    email = "demo@example.com",
    profileImageUrl = "https://placehold.co/100x100/A020F0/FFFFFF?text=AU" // Purple placeholder
))


class AdoptionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                val activityContext = this@AdoptionActivity
                val navController = rememberNavController()

                // Retrieve petId from intent, if passed. Default to a dummy ID for preview.
                val petId = intent.getStringExtra("petId") ?: "dummyPetId123"

                AdoptionScreen(
                    navController = navController,
                    petId = petId, // Pass the petId to the composable
                    onBackClick = { finish() },
                    onContactClick = {
                        val intent = Intent(activityContext, ContactActivity::class.java)
                        startActivity(intent)
                    },
                    onAdoptClick = { petName ->
                        Toast.makeText(activityContext, "Adopt button clicked for $petName!", Toast.LENGTH_SHORT).show()
                        // In a real app: Start adoption process, form, etc.
                    },
                    onLogout = {
                        isLoggedInAdoption.value = false
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
                    userProfile = currentUserProfileAdoption.value,
                    isLoggedIn = isLoggedInAdoption.value
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionScreen(
    navController: NavController,
    petId: String, // Pet ID to fetch from Firebase
    onBackClick: () -> Unit,
    onContactClick: () -> Unit,
    onAdoptClick: (String) -> Unit, // Pass pet name back for toast/logic
    onLogout: () -> Unit,
    onViewProfile: () -> Unit,
    userProfile: UserProfile,
    isLoggedIn: Boolean
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    // State for Firebase data
    val petState: MutableState<Pet?> = remember { mutableStateOf(null) }
    val isLoading: MutableState<Boolean> = remember { mutableStateOf(true) }
    val errorMessage: MutableState<String?> = remember { mutableStateOf(null) }

    // Fetch data from Firebase when the composable enters the composition
    LaunchedEffect(petId) {
        isLoading.value = true
        errorMessage.value = null
        try {
            val db = FirebaseFirestore.getInstance()
            // Assume you have a "pets" collection and petId is the document ID
            val docRef = db.collection("pets").document(petId)
            val document = docRef.get().await()// .await() requires kotlinx-coroutines-play-services dependency

            if (document.exists()) {
                petState.value = document.toObject(Pet::class.java)
            } else {
                errorMessage.value = "Pet not found."
            }
        } catch (e: Exception) {
            errorMessage.value = "Error fetching data: ${e.message}"
            e.printStackTrace()
        } finally {
            isLoading.value = false
        }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()), // Make content scrollable
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading.value -> {
                    // Loading state
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(60.dp))
                }
                errorMessage.value != null -> {
                    // Error state
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${errorMessage.value}",
                            color = Color.Red,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        // Optional: Retry button
                        Button(onClick = {
                            // Re-trigger the data fetch
                            errorMessage.value = null
                            isLoading.value = true
                            // This would re-trigger LaunchedEffect if petId changes, or you can call a specific fetch function
                        }) {
                            Text("Retry")
                        }
                    }
                }
                petState.value != null -> {
                    val pet = petState.value!!
                    // Content when data is loaded successfully
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.9f) // Adjusted width for the card
                            .padding(vertical = 16.dp), // Vertical padding for the whole column
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between elements
                    ) {
                        // Pet Image
                        val petImagePainter = rememberAsyncImagePainter(
                            model = pet.imageUrl,
                            placeholder = painterResource(id = R.drawable.dog_image), // Placeholder while loading
                            error = painterResource(id = R.drawable.dog_image), // Fallback if URL fails
                            contentScale = ContentScale.Crop,
                        )
                        Image(
                            painter = petImagePainter,
                            contentDescription = pet.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp) // Fixed height for the image
                                .clip(RoundedCornerShape(16.dp)), // Rounded corners for image
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Pet Name
                        Text(
                            text = pet.name,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Pet Details (Breed, Age, Medical History, Description)
                        // Using a Column for details to align text to start
                        Column(
                            modifier = Modifier.fillMaxWidth(0.8f), // Adjust width for text block
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DetailRow("breed:", pet.breed)
                            DetailRow("Age:", pet.valAge)
                            DetailRow("medical history:", pet.medicalHistory)
                            DetailRow("Description:", pet.description)
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onContactClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)) // Earthy brown
                            ) {
                                Text("Contact", fontSize = 18.sp, color = Color.White)
                            }

                            Button(
                                onClick = { onAdoptClick(pet.name) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .padding(start = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)) // Earthy brown
                            ) {
                                Text("Adopt", fontSize = 18.sp, color = Color.White)
                            }
                        }
                    }
                }
                // If petState.value is null and not loading and no error (shouldn't happen with good logic)
                else -> {
                    Text("No pet data available.", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.width(IntrinsicSize.Min) // Make label take minimum required width
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun AdoptionScreenPreview() {
    PetAdoptionManagementTheme {
        val navController = rememberNavController()
        // Provide a dummy petId for preview
        AdoptionScreen(
            navController = navController,
            petId = "dummyPetId123", // This ID won't fetch real data in preview unless mocked
            onBackClick = { /* Preview action */ },
            onContactClick = { /* Preview action */ },
            onAdoptClick = { petName -> /* Preview action */ },
            onLogout = { /* Preview action */ },
            onViewProfile = { /* Preview action */ },
            userProfile = UserProfile(
                name = "Preview User",
                email = "preview@example.com",
                profileImageUrl = "https://placehold.co/100x100/FF00FF/FFFFFF?text=P" // Magenta placeholder
            ),
            isLoggedIn = true
        )
    }
}