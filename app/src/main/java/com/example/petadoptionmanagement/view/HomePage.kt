package com.example.petadoptionmanagement.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
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

class HomePage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                HomePageScaffold()
            }
        }
    }
}

@Composable
fun HomePageScaffold() {
    Scaffold(
        topBar = { HomeHeader() },
        bottomBar = { HomeFooter() },
        containerColor = Color(0xFFF5F6FA)
    ) { innerPadding ->
        // Scrollable content between header and footer
        HomeContent(Modifier.padding(innerPadding))
    }
}

@Composable
fun HomeHeader() {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Pet Adoption",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF22223B)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Home",
                    color = Color(0xFF4A4E69),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "About",
                    color = Color(0xFF4A4E69),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    "Adopt",
                    color = Color(0xFF4A4E69),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    "Contact",
                    color = Color(0xFF4A4E69),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun HomeFooter() {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "© 2025 Pet Adoption Management. All rights reserved.",
                color = Color(0xFF4A4E69),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HomeContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        // Hero/Banner Section
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Welcome to Pet Adoption!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF22223B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Find your perfect companion from our wide selection of lovable pets waiting for a forever home.",
                        fontSize = 18.sp,
                        color = Color(0xFF4A4E69),
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { /* TODO: Navigate to adoption page */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9A8C98)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Adopt Now", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                Image(
                    painter = painterResource(id = R.drawable.hero_pet), // Replace with your actual image
                    contentDescription = "Hero pet",
                    modifier = Modifier
                        .size(180.dp) // Use size instead of height and width for uniformity
                )
            }
        }

        // "Why Adopt With Us" Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Why Adopt With Us?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22223B)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    iconRes = R.drawable.ic_heart,
                    title = "Trusted",
                    description = "Verified shelter partners and transparent process.",
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    iconRes = R.drawable.ic_heart,
                    title = "Wide Selection",
                    description = "Choose from a variety of breeds, ages, and sizes.",
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    iconRes = R.drawable.ic_heart,
                    title = "Easy Process",
                    description = "Simple, quick, and supportive adoption experience.",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // About/Info Section
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "About Pet Adoption",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF22223B)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Our mission is to connect loving families with pets who need a home. We partner with trusted shelters and make the adoption process as easy as possible. Join our community and give a pet a forever family today!",
                    fontSize = 16.sp,
                    color = Color(0xFF4A4E69),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    iconRes: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.height(170.dp),
        // The elevation parameter is still the same.
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(46.dp)
            )
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF22223B),
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF4A4E69),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    PetAdoptionManagementTheme {
        HomePageScaffold()
    }
}
