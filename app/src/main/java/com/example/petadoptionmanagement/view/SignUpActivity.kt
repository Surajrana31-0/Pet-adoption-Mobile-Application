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
import androidx.lifecycle.ViewModelProvider // Added import for ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.repository.UserRepositoryImpl // Assuming you have this
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory // Assuming you have this

/**
 * SignUpActivity: This is the Android Activity that will host your SignUpScreen UI.
 * It's responsible for setting up the Compose content, managing the ViewModel lifecycle,
 * and handling navigation that involves transitioning between different Activities.
 */
class SignUpActivity : ComponentActivity() {

    private lateinit var userViewModel: UserViewModel // Declare ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize UserViewModel using the factory
        // Ensure UserRepositoryImpl and UserViewModelFactory are correctly set up.
        val userRepository = UserRepositoryImpl(applicationContext)
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        setContent {
            PetAdoptionManagementTheme {
                val activityContext = LocalContext.current // Get context for Intents

                // Observe isLoggedIn from ViewModel to navigate after successful signup/login
                val isLoggedIn by userViewModel.isLoggedIn.observeAsState(initial = false)
                val message by userViewModel.message.observeAsState(initial = "")

                // Effect for displaying messages (success/failure)
                LaunchedEffect(message) {
                    if (message.isNotBlank()) {
                        Toast.makeText(activityContext, message, Toast.LENGTH_SHORT).show()
                        // Optionally clear the message after showing if ViewModel has clearMessage()
                        // userViewModel.clearMessage()
                    }
                }

                // Effect for handling navigation after successful sign-up and auto-login
                // This logic is now within the Activity, as it handles Activity transitions.
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        Toast.makeText(activityContext, "Sign Up Successful! Welcome.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(activityContext, HomePage::class.java) // Navigate to HomePage
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear back stack
                        startActivity(intent)
                        finish() // Finish SignUpActivity
                    }
                }

                // Pass a dummy NavController for composables that expect one,
                // or if you plan to use an internal NavHost later.
                // For direct Activity-to-Activity navigation, it's not strictly necessary for the main call.
                val dummyNavController = rememberNavController()

                // Call the Composable function that defines the UI content
                SignUpScreenContent(
                    navController = dummyNavController, // Pass it down, even if its main navigation is handled by Activity
                    userViewModel = userViewModel,
                    // Pass current isLoggedIn state and message from ViewModel
                    isLoading = userViewModel.isLoading.observeAsState(initial = false).value,
                    onNavigateToSignIn = {
                        val intent = Intent(activityContext, SignInActivity::class.java)
                        startActivity(intent)
                        finish() // Finish SignUpActivity if moving to SignIn (optional, depends on UX)
                    },
                    onSubmitSignUp = { username, email, password ->
                        // This lambda calls the ViewModel's signUp function
                        userViewModel.signUp(username, email, password) { success, msg ->
                            // The LaunchedEffect above handles success. Errors will update `message` LiveData.
                        }
                    }
                )
            }
        }
    }
}

/**
 * SignUpScreenContent: This is the Composable function that builds the actual UI for the sign-up form.
 * It's designed to be reusable and display the UI, while the hosting Activity/Fragment
 * manages its lifecycle and data (via ViewModel).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreenContent(
    navController: NavController, // Still needed for internal navigation (e.g., to login route if it were a NavHost)
    userViewModel: UserViewModel, // ViewModel for data operations
    isLoading: Boolean, // Pass isLoading state explicitly
    onNavigateToSignIn: () -> Unit, // Callback for "sign in" button
    onSubmitSignUp: (String, String, String) -> Unit // Callback for "Create Account" button
) {
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    // Define custom colors to match the image
    val backgroundColor = Color(0xFF6B8E23) // Olive green background
    val cardBackgroundColor = Color(0xFFDCDCDC) // Light grey for the signup card
    val buttonColor = Color(0xFF8B4513) // Reddish-brown for the Create Account button
    val textFieldBackgroundColor = Color(0xFFFFFFFF) // White for input fields


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
            // Header Row for the paw print icon and menu icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Paw Print Icon
                Image(
                    painter = painterResource(id = R.drawable.paw_print), // Ensure this drawable exists
                    contentDescription = "Paw Print Icon",
                    modifier = Modifier.size(48.dp)
                )
                // Menu Icon with clickable behavior
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

            // Dog Image with a circular gradient background
            Box(
                modifier = Modifier
                    .size(180.dp)
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
                    painter = painterResource(id = R.drawable.dog_image), // Ensure this drawable exists
                    contentDescription = "Dog looking up",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        // Sign Up Form Card
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
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Sign Up" Title
                Text(
                    "Sign Up",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Username Input Field
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.Gray) },
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

                // Email Input Field
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = Color.Gray) },
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

                // Password Input Field with Eye Button
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
                Spacer(modifier = Modifier.height(18.dp))

                // Confirm Password Input Field with Eye Button
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", color = Color.Gray) },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                        val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (confirmPasswordVisible) "Hide confirm password" else "Show confirm password"
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(15.dp))

                // "I accept the terms and policy" Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = buttonColor)
                    )
                    Text(
                        "I accept the terms and policy",
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))


                // Create Account Button
                Button(
                    onClick = {
                        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                            Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                        } else if (password != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                        } else if (!termsAccepted) {
                            Toast.makeText(context, "Please accept the terms and policy.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Call the onSubmitSignUp callback, which is handled by the Activity
                            onSubmitSignUp(username, email, password)
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
                            "Create Account",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // "Already a member? sign in" section
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Already a member?",
                        color = Color.DarkGray,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = {
                        // Use the callback to navigate to SignIn Activity
                        onNavigateToSignIn()
                    }) {
                        Text(
                            "sign in",
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
fun SignUpScreenContentPreview() {
    PetAdoptionManagementTheme {
        val context = LocalContext.current
        val dummyNavController = rememberNavController() // For preview only
        val dummyUserRepository = remember { UserRepositoryImpl(context) }
        val dummyUserViewModel = remember { UserViewModel(dummyUserRepository) }

        SignUpScreenContent(
            navController = dummyNavController,
            userViewModel = dummyUserViewModel,
            isLoading = false,
            onNavigateToSignIn = { /* Preview action */ },
            onSubmitSignUp = { _, _, _ -> /* Preview action */ }
        )
    }
}