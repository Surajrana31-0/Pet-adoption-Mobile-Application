package com.example.petadoptionmanagement.view

import android.content.Intent
import android.os.Bundle
import android.util.Log // Import Log for debugging (optional but good practice)
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petadoptionmanagement.R
// Make sure you have your Dashboard Activities imported, for example:
import com.example.petadoptionmanagement.view.AdminDashboardActivity
// import com.example.petadoptionmanagement.view.UserDashboardActivity // Or your AdopterDashboardActivity
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory

class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                val userRepository = remember { UserRepositoryImpl(applicationContext) }
                val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))
                SignInScreen(userViewModel = userViewModel)
            }
        }
    }
}

@Composable
fun SignInScreen(userViewModel: UserViewModel) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoggedIn by userViewModel.isLoggedIn.observeAsState(initial = false) // Simplified initial
    val isLoading by userViewModel.isLoading.observeAsState(initial = false)  // Simplified initial
    val message by userViewModel.message.observeAsState(initial = null)     // Initial null for clearer logic
    val currentUser by userViewModel.currentUser.observeAsState(initial = null) // Observe currentUser

    val backgroundColor = Color(0xFF6B8E23)
    val cardBackgroundColor = Color(0xFFDCDCDC)
    val buttonColor = Color(0xFF8B4513)
    val textFieldBackgroundColor = Color(0xFFFFFFFF)

    // Effect for handling navigation after successful sign-in
    LaunchedEffect(isLoggedIn, currentUser) { // Depend on both isLoggedIn and currentUser
        if (isLoggedIn && currentUser != null) {
            // User is logged in and we have user details
            val userRole = currentUser?.role // Get the role from UserModel

            Toast.makeText(context, "Sign In Successful! Role: $userRole", Toast.LENGTH_LONG).show() // Feedback with role

            val intent = when (userRole?.lowercase()) { // Use lowercase for case-insensitive role matching
                "admin" -> Intent(context, AdminDashboardActivity::class.java)
                "user" -> Intent(context, AdopterDashboardActivity::class.java) // CHANGE THIS if your user dashboard is different
                else -> {
                    Log.w("SignInScreen", "Unknown or null user role: '$userRole', defaulting to User Dashboard.")
                    Intent(context, AdopterDashboardActivity::class.java) // Default to user dashboard
                }
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
        }
    }

    // Effect for displaying messages (success/failure)
    LaunchedEffect(message) {
        if (message?.isNotBlank() == true) { // More concise check
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            userViewModel.clearMessage() // IMPORTANT: Clear message after showing to prevent re-display
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Top section with decorative elements
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .align(Alignment.TopCenter),
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
                    contentDescription = "Paw Print Icon",
                    modifier = Modifier.size(48.dp)
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu Icon",
                    tint = Color.White,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            Toast.makeText(context, "Menu icon clicked!", Toast.LENGTH_SHORT).show()
                        }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(250.dp)
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
                        .height(200.dp)
                )
            }
        }

        // Login Form Card
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center)
                .offset(y = 120.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Log In", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(30.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color.Gray) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors( /* your colors */ ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(18.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.Gray) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors( /* your colors */ ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                        } else {
                            userViewModel.signIn(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Sign in", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("No account?", color = Color.DarkGray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = {
                        context.startActivity(Intent(context, SignUpActivity::class.java))
                    }) {
                        Text(
                            "sign up",
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
}

@Preview(showBackground = true)
@Composable
fun SignInPreview() {
    PetAdoptionManagementTheme {
        val context = LocalContext.current
        val userRepository = remember { UserRepositoryImpl(context) }
        // Use the actual UserViewModel constructor as it's defined
        val userViewModel = remember { UserViewModel(userRepository) }
        SignInScreen(userViewModel = userViewModel)
    }
}