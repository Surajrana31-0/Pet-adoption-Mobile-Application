package com.example.petadoptionmanagement.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.petadoptionmanagement.R
import com.example.petadoptionmanagement.model.PetModel
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.repository.UserRepositoryImpl
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme
import com.example.petadoptionmanagement.viewmodel.PetViewModel
import com.example.petadoptionmanagement.viewmodel.PetViewModelFactory
import com.example.petadoptionmanagement.viewmodel.UserViewModel
import com.example.petadoptionmanagement.viewmodel.UserViewModelFactory

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                val petRepo = remember { PetRepositoryImpl() }
                val petViewModel: PetViewModel = viewModel(factory = PetViewModelFactory(petRepo))
                val userRepo = remember { UserRepositoryImpl(applicationContext) }
                val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepo))
                AdminDashboardScreen(petViewModel, userViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    petViewModel: PetViewModel,
    userViewModel: UserViewModel,
) {
    val context = LocalContext.current
    val allPets by petViewModel.allPets.observeAsState(initial = emptyList())
    val currentUser by userViewModel.currentUser.observeAsState()
    val isLoading by petViewModel.loading.observeAsState(false)

    // Basic stats
    val totalPets = allPets.size
    val availablePets = allPets.count { it.petStatus.equals("available", true) }
    val adoptedPets = allPets.count { it.petStatus.equals("adopted", true) }
    val pendingAdoptions = allPets.count { it.petStatus.equals("pending adoption", true) }
    // For demo purposes; plug in user stats from your ViewModel if needed!
    val totalUsers = 1

    // Fetch pets on entering the screen
    LaunchedEffect(Unit) { petViewModel.getAllPets() }

    // -- FAB opens AddPet screen --
    val onAddPet = {
        context.startActivity(Intent(context, AddPetActivity::class.java))
    }

    val recentEvents = remember { // TODO: Fetch real events, e.g., from fireStore/Repo
        listOf(
            "3 new adoption requests received",
            "Bella was adopted successfully",
            "Max was added to 'Available' pets"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!currentUser?.profilePictureUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = currentUser?.profilePictureUrl,
                                contentDescription = "Admin profile photo",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Avatar fallback",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Hello, ${currentUser?.firstname ?: "Admin"}!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                color = Color(0xFF607D8B)
                            )
                            Text(
                                "Admin Dashboard",
                                color = Color(0xFF6B8E23),
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Go to Admin Profile
                            context.startActivity(Intent(context, ProfileViewScreen::class.java))
                        }
                    ) {
                        Icon(Icons.Outlined.Settings, "Settings", tint = Color(0xFF6B8E23))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Pet") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onAddPet,
                containerColor = Color(0xFF8B4513)
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F8FF), Color(0xFFE6F2E6))
                    )
                )
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                // Analytics stats
                AdminStatRow(
                    stats = listOf(
                        Triple("Pets", totalPets, Color(0xFF8B4513)),
                        Triple("Available", availablePets, Color(0xFF43A047)),
                        Triple("Pending", pendingAdoptions, Color(0xFFED6C02)),
                        Triple("Adopted", adoptedPets, Color(0xFF2196F3)),
                        Triple("Users", totalUsers, Color(0xFF795548))
                    )
                )
                Spacer(Modifier.height(24.dp))

                // Quick action row
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AdminActionButton(
                        text = "All Pets",
                        icon = Icons.Filled.Pets
                    ) {
                        context.startActivity(Intent(context, PetViewActivity::class.java))
                    }
                    AdminActionButton(
                        text = "Adoptions",
                        icon = Icons.Default.TaskAlt
                    ) {
                        context.startActivity(Intent(context, AdoptionActivity::class.java))
                    }
                    AdminActionButton(
                        text = "Users",
                        icon = Icons.Default.Group
                    ) {
                        context.startActivity(Intent(context, ProfileViewScreen::class.java))
                    }
                }
                Spacer(Modifier.height(26.dp))

                Text(
                    "Recent Activity",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(8.dp))

                if (recentEvents.isEmpty()) {
                    Text("No recent actions.", color = Color.Gray)
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 190.dp)
                    ) {
                        items(recentEvents) { event ->
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                            ) {
                                Text(event, Modifier.padding(14.dp), fontSize = 15.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
fun AdminStatRow(stats: List<Triple<String, Int, Color>>) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        stats.forEach { (label, value, color) ->
            AdminStatCard(label, value, color = color)
        }
    }
}

@Composable
fun AdminStatCard(label: String, value: Int, color: Color) {
    Card(
        Modifier
            .width(97.dp)
            .height(90.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.14f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$value", fontWeight = FontWeight.ExtraBold, fontSize = 27.sp, color = color)
            Spacer(Modifier.height(5.dp))
            Text(label, fontSize = 14.sp, color = color)
        }
    }
}

@Composable
fun AdminActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8B4513)),
        modifier = Modifier
            .height(44.dp)
            .width(124.dp)
    ) {
        Icon(icon, contentDescription = text, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, fontWeight = FontWeight.Medium)
    }
}
