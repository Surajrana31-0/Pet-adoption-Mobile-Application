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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock // For lock icon
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme

class ResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                val activityContext = this@ResetPasswordActivity

                ResetPasswordScreen(
                    onResetPasswordClick = { email, newPassword ->
                        if (email.isBlank() || newPassword.isBlank()) {
                            // This case should ideally be caught by client-side validation before this callback
                            Toast.makeText(activityContext, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                        } else {
                            // In a real app: Perform API call to reset password using email and newPassword
                            Toast.makeText(activityContext, "Resetting password for $email with new password...", Toast.LENGTH_LONG).show()
                            // If successful, navigate back to sign in
                            val intent = Intent(activityContext, SignInActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                    },
                    onBackToSignInClick = {
                        Toast.makeText(activityContext, "Navigating back to Sign In.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(activityContext, SignInActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    },
                    onMenuClick = {
                        Toast.makeText(activityContext, "Menu icon clicked!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun ResetPasswordScreen(
    onResetPasswordClick: (String, String) -> Unit, // Callback now takes email and newPassword
    onBackToSignInClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) } // State for new password visibility
    var confirmNewPasswordVisible by remember { mutableStateOf(false) } // State for confirm new password visibility

    val backgroundColor = Color(0xFF6B8E23)
    val cardBackgroundColor = Color(0xFFDCDCDC)
    val buttonColor = Color(0xFF8B4513)
    val textFieldBackgroundColor = Color(0xFFFFFFFF)

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
                .padding(top = 14.dp)
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
                        .clickable { onMenuClick() }
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

        // Reset Password Form Card
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center)
                .offset(y = 150.dp), // Adjusted offset to accommodate content
            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Reset Password" Title
                Text(
                    "Reset Password",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(25.dp)) // Adjusted spacing

                // Email Address Input Field
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Registered Email", color = Color.Gray) }, // Changed label for clarity
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email icon") },
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
                Spacer(modifier = Modifier.height(18.dp)) // Spacing for new fields

                // New Password Input Field with Eye Button
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password", color = Color.Gray) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock icon") }, // Lock icon
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                        val image = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (newPasswordVisible) "Hide new password" else "Show new password"
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))

                // Confirm New Password Input Field with Eye Button
                TextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    label = { Text("Confirm New Password", color = Color.Gray) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock icon") }, // Lock icon
                    visualTransformation = if (confirmNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                        val image = if (confirmNewPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (confirmNewPasswordVisible) "Hide confirm new password" else "Show confirm new password"
                        IconButton(onClick = { confirmNewPasswordVisible = !confirmNewPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(25.dp)) // Spacing before button

                // Reset Password Button
                Button(
                    onClick = {
                        if (email.isBlank() || newPassword.isBlank() || confirmNewPassword.isBlank()) {
                            Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                        } else if (newPassword != confirmNewPassword) {
                            Toast.makeText(context, "New passwords do not match.", Toast.LENGTH_SHORT).show()
                        } else {
                            // If all client-side validation passes, invoke callback
                            onResetPasswordClick(email, newPassword)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Text(
                        "Reset Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // "Back to Sign In" section
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onBackToSignInClick() }) {
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
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordPreview() {
    PetAdoptionManagementTheme {
        ResetPasswordScreen(
            onResetPasswordClick = { _, _ -> /* No action in preview */ },
            onBackToSignInClick = { /* No action in preview */ },
            onMenuClick = { /* No action in preview */ }
        )
    }
}