package com.example.petadoptionmanagement.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.petadoptionmanagement.model.Pet
import com.example.petadoptionmanagement.repository.PetRepository
import com.example.petadoptionmanagement.repository.PetRepositoryImpl
import com.example.petadoptionmanagement.viewmodel.PetViewModel

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                DashboardBody()
            }
        }
    }
}

@Composable
fun DashboardBody() {
    val context = LocalContext.current
    val activity = context as? Activity

    val repo = remember { PetRepositoryImpl() }
    val viewModel = remember { PetViewModel(repo) }

    val pets = viewModel.allPets.observeAsState(initial = emptyList())
    val loading = viewModel.loading.observeAsState(initial = true)

    LaunchedEffect(Unit) {
        viewModel.getAllPets()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val intent = Intent(context, AddPetActivity::class.java)
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Pet")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (loading.value) {
                item {
                    CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
                }
            } else {
                items(pets.value.size) { index ->
                    val pet = pets.value[index]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "${pet?.name ?: "Unknown"}", style = MaterialTheme.typography.headlineSmall)
                            Text(text = "${pet?.breed ?: "Unknown"} | ${pet?.age ?: "Unknown"}")
                            Text(text = "Adoption Status: ${if (pet?.adopted == true) "Adopted" else "Available"}")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(context, UpdatePetActivity::class.java)
                                        intent.putExtra("petId", pet?.id.toString())
                                        context.startActivity(intent)
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Gray)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Pet")
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.deletePet(pet?.id.toString()) { success, message ->
                                            if (success) {
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Pet")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    PetAdoptionManagementTheme {
        DashboardBody()
    }
}