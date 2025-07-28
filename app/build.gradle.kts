// app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.petadoptionmanagement"
    compileSdk = 36 // CONFIRMED: Keep at 36 as per previous fix

    defaultConfig {
        applicationId = "com.example.petadoptionmanagement"
        minSdk = 27
        targetSdk = 36 // CONFIRMED: Match compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8 // Can be 1.8 for compatibility or 11/17 for newer features
        targetCompatibility = JavaVersion.VERSION_1_8 // Match sourceCompatibility
    }

    kotlinOptions {
        jvmTarget = "1.8" // Match Java version, or "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        // This version should be compatible with your Kotlin and Compose library versions.
        // For compileSdk 36 and recent Compose/Kotlin, try 1.5.10 or 1.6.x.
        // If 1.5.1 still gives warnings, increase this.
        kotlinCompilerExtensionVersion = "1.5.10" // Updated to a more recent stable version
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Import the Compose BOM first for consistent versions
    // Ensure you're using androidx.compose:compose-bom:2024.04.00 or higher
    // (Check your libs.versions.toml for the actual compose.bom version)
    implementation(platform(libs.androidx.compose.bom))

    // Core AndroidX & UI (using libs.versions.toml managed versions)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose) // This handles enableEdgeToEdge and activity-compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata") // For LiveData observation

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Image Loading (Coil)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Cloudinary SDK
    implementation("com.cloudinary:cloudinary-android:2.4.0")

    // Firebase (using the BOM for version management)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    // If you are ONLY using Firestore, you can remove firebase-database-ktx
    // If you indeed use Realtime Database, keep it.
    // Based on our conversation, you primarily use Firestore.
    // implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
