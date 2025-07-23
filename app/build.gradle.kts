plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services) // Google Services plugin for Firebase
}

android {
    namespace = "com.example.petadoptionmanagement"
    compileSdk = 35 // Updated to 35 as per your input

    defaultConfig {
        applicationId = "com.example.petadoptionmanagement"
        minSdk = 27
        targetSdk = 35 // Updated to 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // Core AndroidX and Compose UI dependencies
    // These are typically managed by the Compose BOM (Bill of Materials)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Centralizes Compose version management
    implementation(libs.androidx.ui) // Explicitly keep if not fully covered by BOM or for specific reasons
    implementation(libs.androidx.ui.graphics) // Explicitly keep
    implementation(libs.androidx.ui.tooling.preview) // Explicitly keep
    implementation(libs.androidx.material3) // Explicitly keep

    // LiveData integration for Compose (essential for observeAsState)
    implementation("androidx.compose.runtime:runtime-livedata")

    // Jetpack Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7") // Use the latest stable version

    // Material Icons Extended (for Icons.Filled.Menu, etc.)
    implementation("androidx.compose.material:material-icons-extended")

    // Image loading library (Coil)
    implementation("io.coil-kt:coil-compose:2.6.0") // Use the latest stable version

    // Firebase dependencies (managed by Firebase BOM)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Firebase Bill of Materials
    implementation("com.google.firebase:firebase-auth-ktx")        // Firebase Authentication with Kotlin extensions
    implementation("com.google.firebase:firebase-database-ktx")    // Firebase Realtime Database with Kotlin extensions
    implementation("com.google.firebase:firebase-firestore-ktx")   // Firebase Firestore with Kotlin extensions

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // For Compose UI tests
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}