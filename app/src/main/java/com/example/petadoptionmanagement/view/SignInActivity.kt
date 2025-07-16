package com.example.petadoptionmanagement.view

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
import androidx.compose.material.icons.filled.Visibility // Import for eye icon
import androidx.compose.material.icons.filled.VisibilityOff // Import for eye-off icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.input.VisualTransformation // Import VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petadoptionmanagement.R // Make sure R is imported for drawables
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme

class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                // Here, in your Activity, you define what happens when the callbacks are triggered.
                // In a real application, you would use a Navigation Component (e.g., NavController)
                // to navigate between screens instead of just showing Toasts.
                SignInScreen(
                    onSignInSuccess = {
                        // This lambda is called when the sign-in button is clicked and validation passes.
                        Toast.makeText(this, "Sign In Successful! Navigating to Home Screen.", Toast.LENGTH_SHORT).show()
                        // Example of real navigation: findNavController().navigate(R.id.action_signIn_to_home)
                    },
                    onSignUpClick = {
                        // This lambda is called when the "sign up" text is clicked.
                        Toast.makeText(this, "Navigating to Sign Up Screen.", Toast.LENGTH_SHORT).show()
                        // Example of real navigation: findNavController().navigate(R.id.action_signIn_to_signUp)
                    },
                    onMenuClick = {
                        // This lambda is called when the menu icon is clicked.
                        Toast.makeText(this, "Menu icon clicked!", Toast.LENGTH_SHORT).show()
                        // You might open a drawer or show a pop-up menu here.
                    }
                )
            }
        }
    }
}

/**
 * Composable function for the Sign In screen UI.
 * It takes callback functions as parameters to handle user interactions,
 * allowing the parent (Activity or another Composable) to define the actions.
 *
 * @param onSignInSuccess Callback invoked upon successful sign-in (after validation).
 * @param onSignUpClick Callback invoked when the "sign up" text is clicked.
 * @param onMenuClick Callback invoked when the menu icon is clicked.
 */
@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current // Get the current Android context for Toast messages
    var email by remember { mutableStateOf("") } // State for the email input field
    var password by remember { mutableStateOf("") } // State for the password input field
    var passwordVisible by remember { mutableStateOf(false) } // State to toggle password visibility

    // Define custom colors to match the image
    val backgroundColor = Color(0xFF6B8E23) // Olive green background
    val cardBackgroundColor = Color(0xFFDCDCDC) // Light grey for the login card
    val buttonColor = Color(0xFF8B4513) // Reddish-brown for the Sign In button
    val textFieldBackgroundColor = Color(0xFFFFFFFF) // White for input fields

    Box(
        modifier = Modifier
            .fillMaxSize() // Occupy the entire screen
            .background(backgroundColor), // Set the main background color
        contentAlignment = Alignment.Center // Center the content within the Box (specifically the Card)
    ) {
        // Top section with decorative elements: paw print icon, menu icon, and dog image
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp) // Padding from the top of the screen
                .align(Alignment.TopCenter), // Align this column to the top center of the parent Box
            horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally within this column
        ) {
            // Header Row for the paw print icon and menu icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp), // Horizontal padding for the row
                horizontalArrangement = Arrangement.SpaceBetween, // Space items evenly
                verticalAlignment = Alignment.CenterVertically // Vertically align items in the row
            ) {
                // Paw Print Icon
                // IMPORTANT: Ensure you have 'ic_paw_print.png' in your res/drawable folder.
                Image(
                    painter = painterResource(id = R.drawable.paw_print),
                    contentDescription = "Paw Print Icon",
                    modifier = Modifier.size(48.dp) // Size of the icon
                )
                // Menu Icon with clickable behavior
                Icon(
                    imageVector = Icons.Default.Menu, // Using Material Icons for the menu
                    contentDescription = "Menu Icon",
                    tint = Color.White, // Set icon color to white
                    modifier = Modifier
                        .size(36.dp) // Size of the icon
                        .clickable { onMenuClick() } // Attach the onMenuClick callback here
                )
            }
            Spacer(modifier = Modifier.height(20.dp)) // Space between header row and dog image

            // Dog Image with a circular gradient background
            // IMPORTANT: Ensure you have 'dog_image.png' in your res/drawable folder.
            Box(
                modifier = Modifier
                    .size(250.dp) // Size of the circular container for the image
                    .clip(CircleShape) // Clip the Box to a circular shape
                    .background(
                        Brush.verticalGradient( // Apply a vertical gradient background
                            colors = listOf(Color(0xFF8B0000), Color(0xFFD3D3D3)), // Dark red to light grey
                            startY = 0f,
                            endY = 250f
                        )
                    ),
                contentAlignment = Alignment.BottomCenter // Align the image to the bottom of the circle
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dog_image),
                    contentDescription = "Dog looking up",
                    contentScale = ContentScale.Crop, // Crop the image to fit the bounds
                    modifier = Modifier
                        .fillMaxWidth() // Fill the width of the circular container (250.dp)
                        .height(200.dp) // Set a fixed height for the image within the circular container
                )
            }
        }

        // Login Form Card
        Card(
            shape = RoundedCornerShape(24.dp), // Rounded corners for the card
            modifier = Modifier
                .fillMaxWidth(0.9f) // Card takes 90% of the screen width
                .align(Alignment.Center) // Center the card within the parent Box
                .offset(y = 120.dp), // Adjust this 'y' offset value to move the card further down
            // Increase 'y' to move down, decrease to move up.
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor), // Set card background color
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Add elevation for a raised effect
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp), // Padding inside the card
                horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally within the card
            ) {
                // "Log In" Title
                Text(
                    "Log In",
                    fontSize = 32.sp, // Font size for the title
                    fontWeight = FontWeight.Bold, // Bold font weight
                    color = Color.Black // Black text color
                )
                Spacer(modifier = Modifier.height(30.dp)) // Vertical space

                // Email Input Field
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

                // Password Input Field with Eye Button
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.Gray) },
                    singleLine = true,
                    // Conditional visualTransformation based on passwordVisible state
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
                    // Trailing icon for password visibility toggle
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        // Add a content description for accessibility
                        val description = if (passwordVisible) "Hide password" else "Show password"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(30.dp))

                // Sign In Button
                Button(
                    onClick = {
                        // Logic executed when the button is clicked
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                        } else {
                            // If validation passes, invoke the onSignInSuccess callback
                            // This is where you'd typically call your authentication logic (e.g., API call)
                            // After successful authentication, you'd trigger the navigation.
                            onSignInSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth() // Button fills width
                        .height(55.dp), // Set button height
                    shape = RoundedCornerShape(16.dp), // Rounded corners for the button
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor) // Custom button background color
                ) {
                    Text(
                        "Sign in",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White // White text on the button
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // "No account? sign up" section
                Row(
                    horizontalArrangement = Arrangement.Center, // Center items horizontally
                    verticalAlignment = Alignment.CenterVertically // Vertically align items
                ) {
                    Text(
                        "No account?",
                        color = Color.DarkGray, // Dark gray text color
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Small horizontal space
                    // "sign up" TextButton with clickable behavior
                    TextButton(onClick = {
                        onSignUpClick() // Invoke the onSignUpClick callback
                    }) {
                        Text(
                            "sign up",
                            color = Color(0xFF8B4513), // Color matching the button
                            fontWeight = FontWeight.SemiBold, // Semi-bold font weight
                            fontSize = 16.sp,
                            textDecoration = TextDecoration.Underline // Underline the text
                        )
                    }
                }
            }
        }
    }
}

/**
 * Preview function for the SignInScreen.
 * Callbacks are provided as empty lambdas for preview purposes, as actual navigation
 * or Toast messages cannot be performed in the preview environment.
 */
@Preview(showBackground = true)
@Composable
fun SignInPreview() {
    PetAdoptionManagementTheme {
        SignInScreen(
            onSignInSuccess = { /* No action in preview */ },
            onSignUpClick = { /* No action in preview */ },
            onMenuClick = { /* No action in preview */ }
        )
    }
}