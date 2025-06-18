package com.example.petadoptionmanagement.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.petadoptionmanagement.ui.theme.PetAdoptionManagementTheme // Don't forget to import your theme!

class HomePage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetAdoptionManagementTheme {
                HomePageBody()
            }
        }
    }
}

@Composable
fun HomePageBody() {
    // The Scaffold provides basic Material Design layout structures.
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Text(
            text = "Home Page",
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    // Wrap your preview with the theme to see it as it would appear in the app
    PetAdoptionManagementTheme {
        HomePageBody()
    }
}