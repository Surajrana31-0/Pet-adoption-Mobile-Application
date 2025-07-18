package com.example.petadoptionmanagement.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.IconButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll // Important import for scrolling
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                AboutScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // For TopAppBar
@Composable
fun AboutScreen() {
    val backgroundColor = Color(0xFF6B8E23)
    val cardBackgroundColor = Color(0xFFDCDCDC)
    val textColor = Color(0xFF22223B)
    val context = LocalContext.current

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PetEy",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Handle back navigation */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Handle profile click */ }) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { /* TODO: Handle menu click */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6B8E23)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // FIX: Make the column scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo or Icon
            Image(
                painter = painterResource(id = R.drawable.paw_print), // Ensure paw_print.png/xml exists in drawable
                contentDescription = "Pet Adoption Logo",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // About Title
            Text(
                text = "About Pet Adoption Management",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // About Content Card
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Our Mission",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "At Pet Adoption Management, our mission is to create a world where every pet finds a loving forever home. We connect compassionate families with pets in need through a seamless and trustworthy adoption process.",
                        fontSize = 16.sp,
                        color = Color(0xFF4A4E69),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Our Vision",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "To be the leading platform for pet adoption, promoting animal welfare and building communities that celebrate the bond between humans and pets.",
                        fontSize = 16.sp,
                        color = Color(0xFF4A4E69),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Key Features",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "- Verified shelter partnerships\n- Wide variety of pets to choose from\n- Easy and supportive adoption process\n- Community support and resources",
                        fontSize = 16.sp,
                        color = Color(0xFF4A4E69),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // The original contact info at the bottom of the About section
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Contact Us: info@petadoption.com | +91 9806400001",
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp)) // Spacer before the form

            // Contact Us Form
            Text(
                text = "Send us a message!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            ContactUsForm(textColor = textColor, cardBackgroundColor = cardBackgroundColor)

            Spacer(modifier = Modifier.height(32.dp))

            // Social Media Links
            Text(
                text = "Find us on Social Media",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SocialMediaLinks(context = context)

            Spacer(modifier = Modifier.height(32.dp)) // Final spacer
        }
    }
}

@Composable
fun ContactUsForm(textColor: Color, cardBackgroundColor: Color) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    errorContainerColor = Color.White,
                    focusedIndicatorColor = textColor,
                    unfocusedIndicatorColor = Color.LightGray
                )
            )
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Your Email", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    errorContainerColor = Color.White,
                    focusedIndicatorColor = textColor,
                    unfocusedIndicatorColor = Color.LightGray
                )
            )
            TextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Your Message", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    errorContainerColor = Color.White,
                    focusedIndicatorColor = textColor,
                    unfocusedIndicatorColor = Color.LightGray
                )
            )
            Button(
                onClick = { /* TODO: Implement message sending logic */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9A8C98)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Message", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun SocialMediaLinks(context: android.content.Context) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Facebook
        IconButton(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/yourpetadoptionpage"))
            context.startActivity(intent)
        }) {
            Image(
                painter = painterResource(id = R.drawable.facebook), // Ensure this drawable exists
                contentDescription = "Facebook",
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        // Instagram
        IconButton(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/yourpetadoptionpage"))
            context.startActivity(intent)
        }) {
            Image(
                painter = painterResource(id = R.drawable.instagram), // Ensure this drawable exists
                contentDescription = "Instagram",
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        // Twitter (X)
        IconButton(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/yourpetadoptionpage"))
            context.startActivity(intent)
        }) {
            Image(
                painter = painterResource(id = R.drawable.twitter), // Ensure this drawable exists
                contentDescription = "Twitter/X",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AboutScreenPreview() {
    PetAdoptionManagementTheme {
        AboutScreen()
    }
}