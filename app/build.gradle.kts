plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services) // Google Services plugin for Firebase
}

android {
    namespace = "com.example.petadoptionmanagement"
    compileSdk = 35 // Updated to 35 as per your input
    // Ensure you have JDK 17 installed and configured for compileSdk 35
    // kotlinOptions.jvmTarget = "17" and source/targetCompatibility = JavaVersion.VERSION_17

    defaultConfig {
        applicationId = "com.example.petadoptionmanagement"
        minSdk = 27
        targetSdk = 35 // Updated to 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true // Recommended for Vector Drawables compatibility
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // Updated to JavaVersion.VERSION_17 for compileSdk 35 (Android 15)
        // Ensure your JDK is 17 or higher
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        // Updated to "17" for compileSdk 35
        jvmTarget = "17"
        // Opt-in for experimental APIs if you're using them (e.g., from Compose)
        // freeCompilerArgs += listOf("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
    buildFeatures {
        compose = true
        viewBinding = true // Add this if you are using View Binding in any XML layouts
    }
    composeOptions {
        // Using Kotlin Compiler Extension version compatible with latest Compose BOM
        kotlinCompilerExtensionVersion = "1.5.11" // Recommended for Compose BOM 2024.04.00 and Kotlin 1.9.23. Check latest compatibility.
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Core AndroidX and Compose UI dependencies
    // These are typically managed by the Compose BOM (Bill of Materials)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Centralizes Compose version management
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Make sure you have the correct Material Icons Extended dependency for Material3
    // It's usually integrated with Material3 or needs a specific 'material-icons-extended' from androidx.compose.material
    // Your current 'androidx.compose.material:material-icons-extended' is for Material2.
    // For Material3 icons, they often come with material3 lib or similar.
    // If you explicitly need extended icons:
    implementation("androidx.compose.material:material-icons-extended") // Keep if this specific version is needed for compatibility with your existing icons

    // LiveData integration for Compose (essential for observeAsState)
    implementation("androidx.compose.runtime:runtime-livedata")

    // Jetpack Compose Navigation
    // Keep 2.7.7 if that's what you want, or update to latest stable like 2.8.0-beta01 or newer if available
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Image loading library (Coil)
    // Keep 2.6.0 or update to a newer stable version if available.
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Cloudinary SDK (Fixed syntax as discussed)
    implementation("com.cloudinary:cloudinary-android:2.2.0") // Syntax fixed here

    // Firebase dependencies (managed by Firebase BOM)
    // Check for the absolute latest Firebase BOM here: https://firebase.google.com/docs/android/setup#available-libraries
    // As of my last update, 33.1.0 is recent.
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}