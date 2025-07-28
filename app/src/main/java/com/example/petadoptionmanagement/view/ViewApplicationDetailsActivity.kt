// /view/ViewApplicationDetailsActivity.kt

package com.example.petadoptionmanagement.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cloudinary.Cloudinary
import com.example.petadoptionmanagement.model.AdoptionApplicationModel
import com.example.petadoptionmanagement.model.ApplicationStatus
import com.example.petadoptionmanagement.repository.AdoptionApplicationRepositoryImpl
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.AdoptionApplicationViewModel
import com.example.petadoptionmanagement.viewmodel.AdoptionApplicationViewModelFactory
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ViewApplicationDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val applicationId = intent.getStringExtra("applicationId") // Get application ID from Intent
        if (applicationId == null) {
            Toast.makeText(this, "Application ID is missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            PetAdoptionManagementTheme {
                // Initialize User and AdoptionApplication repositories and ViewModels
                val userRepo = remember {
                    val auth = FirebaseAuth.getInstance()
                    val firestore = FirebaseFirestore.getInstance()
                    val config = mapOf(
                        "cloud_name" to "YOUR_CLOUD_NAME",
                        "api_key" to "YOUR_API_KEY",
                        "api_secret" to "YOUR_API_SECRET"
                    )
                    val cloudinary = Cloudinary(config)
                    UserRepositoryImpl(auth, firestore, cloudinary, applicationContext)
                }
                val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepo))

                val adoptionRepo = remember {
                    AdoptionApplicationRepositoryImpl(FirebaseFirestore.getInstance())
                }
                val adoptionViewModel: AdoptionApplicationViewModel = viewModel(factory = AdoptionApplicationViewModelFactory(adoptionRepo))

                ViewApplicationDetailsScreen(
                    applicationId = applicationId,
                    adoptionViewModel = adoptionViewModel,
                    userViewModel = userViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewApplicationDetailsScreen(
    applicationId: String,
    adoptionViewModel: AdoptionApplicationViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val currentApplication by remember { mutableStateOf<AdoptionApplicationModel?>(null) } // Use MutableState for direct observation
    val currentApplicant by userViewModel.viewedUser.observeAsState() // Observe applicant's UserModel
    val isLoading by adoptionViewModel.isLoading.observeAsState(false)
    val message by adoptionViewModel.message.observeAsState()

    // State to hold the fetched application model.
    var fetchedApplication by remember { mutableStateOf<AdoptionApplicationModel?>(null) }

    // Fetch application details when screen loads
    LaunchedEffect(applicationId) {
        adoptionViewModel.getApplicationById(applicationId) { result ->
            result.fold(
                onSuccess = { app ->
                    fetchedApplication = app
                    // Once application is fetched, fetch applicant details
                    app?.applicantId?.let { userViewModel.getUserFromDatabase(it) }
                },
                onFailure = { e ->
                    Toast.makeText(context, "Failed to load application: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Effect for showing Toast messages from ViewModel
    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // If the application status was updated, you might want to close this activity.
            // Consider adding a specific success message to the ViewModel for this.
            // Example: if (message.contains("updated successfully")) (context as? Activity)?.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Application Details") },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Application Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Application Info", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//TTTTTTTTTTTTTTTTTTTHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIISSSSSSSSSSSSSSSSSSSSSSS


//                    fetchedApplication?.let { app ->
//                        DetailRow(label = "Pet Name", value = app.petName)
//                        DetailRow(label = "Applicant Name", value = app.applicantName)
//                        DetailRow(label = "Status", value = app.status.name)
//                        DetailRow(label = "Message", value = app.message, isExpandable = true)
//                        app.timestamp?.let {
//                            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
//                            DetailRow(label = "Date Submitted", value = formatter.format(it))




// OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR
                    currentApplicant?.let { user ->
                        // THIS IS THE COLUMN YOU NEED TO MAKE CLICKABLE
                        Column(
                            modifier = Modifier
                                .fillMaxWidth() // Make sure it takes full width of parent
                                .clickable { // Add the clickable modifier here
                                    val intent = Intent(context, AdopterProfileViewActivity::class.java).apply {
                                        putExtra("userId", user.userId) // Pass the adopter's userId
                                    }
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 4.dp) // Add some padding for better click area
                        ) {
                            DetailRow(label = "Username", value = user.username)
                            DetailRow(label = "Email", value = user.email)
                            DetailRow(label = "Contact", value = user.contact)
                            // Add more user details if needed, e.g., profile picture

                        }
                    } ?: run {
                        Text("Loading application details...", modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Applicant Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Applicant Info", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    currentApplicant?.let { user ->
                        DetailRow(label = "Username", value = user.username)
                        DetailRow(label = "Email", value = user.email)
                        DetailRow(label = "Contact", value = user.contact)
                        // Add more user details if needed, e.g., profile picture
                    } ?: run {
                        Text("Loading applicant details...", modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            fetchedApplication?.let { app ->
                if (app.status == ApplicationStatus.PENDING) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = { adoptionViewModel.updateApplicationStatus(app.applicationId, ApplicationStatus.REJECTED) },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Reject")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { adoptionViewModel.updateApplicationStatus(app.applicationId, ApplicationStatus.APPROVED) },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Approve")
                        }
                    }
                } else {
                    Text(
                        "Application already ${app.status.name.lowercase()}.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isExpandable: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "$label:", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.DarkGray,
            modifier = if (isExpandable) Modifier.clickable { expanded = !expanded } else Modifier,
            maxLines = if (expanded) Int.MAX_VALUE else 2 // Show max 2 lines initially if expandable
        )
        if (isExpandable && value.length > 50) { // Simple heuristic to show "Read more"
            Text(
                text = if (expanded) "Show less" else "Show more",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
