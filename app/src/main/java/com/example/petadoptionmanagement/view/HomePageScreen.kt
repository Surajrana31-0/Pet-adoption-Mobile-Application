package com.example.petadoptionmanagement.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme

// --- Data Classes for Demo Data ---
data class Pet(val id: Int, val name: String, val breed: String, val age: String, val imageRes: Int)
data class Feature(val iconRes: Int, val title: String, val description: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageScreen(navController: NavController) {
    Scaffold(
        topBar = { HomeHeader(navController = navController) },
        bottomBar = { HomeFooter(navController = navController) },
        containerColor = Color(0xFFF5F6FA)
    ) { innerPadding ->
        HomeContent(modifier = Modifier.padding(innerPadding), navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader(navController: NavController) {
    TopAppBar(
        title = {
            Text(
                "PetEy",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Color(0xFF22223B),
                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.Start)
            )
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { navController.navigate("login") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B4513)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Login", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Button(
                    onClick = { navController.navigate("signup") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Sign Up", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                IconButton(onClick = {
                    navController.navigate("home")
                }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color(0xFF22223B)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White, titleContentColor = Color.Black)
    )
}

@Composable
fun HomeFooter(navController: NavController) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Contact Us: info@petadoption.com | +91 9806400001",
                color = Color(0xFF4A4E69),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Image(painter = painterResource(id = R.drawable.facebook), contentDescription = "Facebook", modifier = Modifier.size(28.dp).clickable { })
                Image(painter = painterResource(id = R.drawable.twitter), contentDescription = "Twitter", modifier = Modifier.size(28.dp).clickable { })
                Image(painter = painterResource(id = R.drawable.instagram), contentDescription = "Instagram", modifier = Modifier.size(28.dp).clickable { })
            }
            Text(
                "© 2025 Pet Adoption Management. All rights reserved.",
                color = Color(0xFF4A4E69),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HomeContent(modifier: Modifier = Modifier, navController: NavController) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        HeroBannerSection(navController = navController)
        FeaturedPetsSection(navController = navController)
        WhyAdoptWithUsSection()
        AboutInfoSection(navController = navController)
    }
}

@Composable
fun HeroBannerSection(navController: NavController) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.hero_pet),
                contentDescription = "Happy adopted pets banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Find Your Perfect Companion",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 38.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Give a loving home to a pet in need. Every adoption creates a happy story.",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate("login") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B4513)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(0.7f)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Adopt Now ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                append("→")
                            }
                        },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedPetsSection(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Featured Pets",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF22223B),
            modifier = Modifier.padding(bottom = 20.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            val featuredPets = listOf(
                Pet(1, "Buddy", "Golden Retriever", "2 years", R.drawable.featuredpet1),
                Pet(2, "Whiskers", "Tabby Cat", "1 year", R.drawable.featured_pet2),
                Pet(3, "Rocky", "Beagle", "3 years", R.drawable.featuredpet3),
                Pet(4, "Luna", "Siamese Cat", "6 months", R.drawable.featuredpet4),
                Pet(5, "Max", "Labrador", "4 years", R.drawable.dog_image)
            )
            items(featuredPets) { pet ->
                PetCard(pet = pet) {
                    navController.navigate("login")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetCard(pet: Pet, onClick: () -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .width(180.dp)
            .height(250.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = pet.imageRes),
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = pet.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF22223B)
                )
                Text(
                    text = "${pet.breed} | ${pet.age}",
                    fontSize = 14.sp,
                    color = Color(0xFF4A4E69)
                )
            }
        }
    }
}

@Composable
fun WhyAdoptWithUsSection() {
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
            val features = listOf(
                Feature(R.drawable.baseline_fingerprint_24, "Trusted Process", "Verified shelter partners & transparent procedures."),
                Feature(R.drawable.baseline_play_arrow_24, "Vast Selection", "Browse variety of breeds, ages, and sizes."),
                Feature(R.drawable.baseline_domain_verification_24, "Easy Process", "Simple, quick, and supportive adoption experience."),
            )
            features.forEach { feature ->
                FeatureCard(
                    iconRes = feature.iconRes,
                    title = feature.title,
                    description = feature.description,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureCard(
    iconRes: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.height(200.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF22223B),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF4A4E69),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AboutInfoSection(navController: NavController) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.navigate("about") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B4513)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Text("Learn More", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
fun HomePageScreenPreview() {
    PetAdoptionManagementTheme {
        val navController = rememberNavController()
        HomePageScreen(navController = navController)
    }
} 