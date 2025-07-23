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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory // Import the factory

class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                // Initialize UserRepository and UserViewModelFactory
                val userRepository = remember { UserRepositoryImpl(applicationContext) }
                val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))

                SignInScreen(userViewModel = userViewModel)
            }
        }
    }
}

/**
 * Composable function for the Sign In screen UI.
 * It now takes the UserViewModel as a parameter to handle authentication logic.
 *
 * @param userViewModel The ViewModel instance for user authentication and management.
 */
@Composable
fun SignInScreen(userViewModel: UserViewModel) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Observe LiveData from the UserViewModel
    val isLoggedIn by userViewModel.isLoggedIn.observeAsState(initial = userViewModel.isLoggedIn.value ?: false)
    val isLoading by userViewModel.isLoading.observeAsState(initial = userViewModel.isLoading.value ?: false)
    val message by userViewModel.message.observeAsState(initial = userViewModel.message.value ?: "")

    // Define custom colors to match the image
    val backgroundColor = Color(0xFF6B8E23) // Olive green background
    val cardBackgroundColor = Color(0xFFDCDCDC) // Light grey for the login card
    val buttonColor = Color(0xFF8B4513) // Reddish-brown for the Sign In button
    val textFieldBackgroundColor = Color(0xFFFFFFFF) // White for input fields

    // Effect for handling navigation after successful sign-in
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            // Navigate to the main application screen (e.g., MainActivity or HomePage)
            // Replace MainActivity::class.java with your actual main activity class if different
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear back stack
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish() // Finish SignInActivity
        }
    }

    // Effect for displaying messages (success/failure)
    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // Optionally clear the message after showing
            // userViewModel.clearMessage() // You might add a clearMessage() function in ViewModel
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Top section with decorative elements: paw print icon, menu icon, and dog image
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
                            // This will still show a Toast as per your original UI
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
                Text(
                    "Log In",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(30.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color.Gray) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = textFieldBackgroundColor,
                        unfocusedContainerColor = textFieldBackgroundColor,
                        disabledContainerColor = textFieldBackgroundColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black
                    ),
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
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = textFieldBackgroundColor,
                        unfocusedContainerColor = textFieldBackgroundColor,
                        disabledContainerColor = textFieldBackgroundColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                        } else {
                            userViewModel.signIn(email, password) { success, msg ->
                                // The LaunchedEffect for 'message' and 'isLoggedIn' will handle feedback and navigation.
                                // No need for direct Toast or navigation here.
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    enabled = !isLoading // Disable button while loading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            "Sign in",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "No account?",
                        color = Color.DarkGray,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = {
                        // Navigate to SignUpActivity
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

/**
 * Preview function for the SignInScreen.
 * Note: ViewModel is mocked or not used directly in previews.
 */
@Preview(showBackground = true)
@Composable
fun SignInPreview() {
    PetAdoptionManagementTheme {
        // For preview, you'd typically pass a mock ViewModel or handle states manually.
        // For simplicity, we'll create a dummy ViewModel here. In a real app,
        // you might use a Hilt preview setup or similar.
        val context = LocalContext.current
        val userRepository = remember { UserRepositoryImpl(context) }
        val userViewModel = remember { UserViewModel(userRepository) }
        SignInScreen(userViewModel = userViewModel)
    }
}