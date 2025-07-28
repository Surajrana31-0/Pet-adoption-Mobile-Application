package com.example.petadoptionmanagement.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // Ensure this import is present
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions // Correct import for KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cloudinary.Cloudinary // Import Cloudinary
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth // Import Firebase Auth
import com.google.firebase.firestore.FirebaseFirestore // Import Firebase Firestore

class ResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                // --- FIX: Correctly initialize and provide all dependencies ---
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

                val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))
                ResetPasswordScreen(userViewModel = userViewModel)
            }
        }
    }
}

@Composable
fun ResetPasswordScreen(userViewModel: UserViewModel) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    val isLoading by userViewModel.isLoading.observeAsState(false)
    val message by userViewModel.message.observeAsState()

    // Toast & navigation handling
    LaunchedEffect(message) {
        val msg = message ?: ""
        if (msg.isNotBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

            // If the message indicates success, navigate to the sign-in screen
            if (msg.contains("reset email sent", ignoreCase = true) ||
                msg.contains("email sent", ignoreCase = true)
            ) {
                // Clear the message in ViewModel *after* showing the Toast and deciding to navigate
                userViewModel.clearMessage()
                val intent = Intent(context, SignInActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
                (context as? ComponentActivity)?.finish() // Finish ResetPasswordActivity
            } else {
                // For other messages (e.g., error messages), just clear the message
                userViewModel.clearMessage()
            }
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF6B8E23), Color(0xFFDCDCDC))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        // Upper UI elements (Paw Print, Menu, Dog Image) - No changes needed to their structure
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.paw_print),
                    contentDescription = "Paw Print",
                    modifier = Modifier.size(48.dp)
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            Toast.makeText(context, "Menu clicked", Toast.LENGTH_SHORT).show()
                        }
                )
            }
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF8B0000), Color(0xFFD3D3D3)),
                            startY = 0f,
                            endY = 250f
                        )
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dog_image),
                    contentDescription = "Dog looking up",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }
        }

        // Main Card (Password Reset Form)
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center)
                .offset(y = 120.dp), // Adjust offset to position the card correctly relative to its content
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCDCDC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reset Password",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(25.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Registered Email", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email Icon")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6B8E23),
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.Black,
                        focusedLabelColor = Color(0xFF6B8E23)
                    ),
                    enabled = !isLoading // Disable input when loading
                )
                Spacer(modifier = Modifier.height(25.dp))
                Button(
                    onClick = {
                        if (email.isBlank()) {
                            Toast.makeText(context, "Please enter your registered email.", Toast.LENGTH_SHORT).show()
                        } else {
                            userViewModel.forgetPassword(email)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading // Disable button when loading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            "Send Reset Email",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        val intent = Intent(context, SignInActivity::class.java)
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    }
                ) {
                    Text(
                        "Back to Sign In",
                        color = Color(0xFF8B4513),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}
