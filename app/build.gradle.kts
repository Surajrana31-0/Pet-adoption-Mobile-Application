// /app/build.gradle.kts

// The plugins block should be at the very top.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // The google-services plugin should be applied here.
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.compose)
}

// This logic to load local properties is correct. It should be at the top level.
import java.util.Properties
        import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

// All Android-specific configuration MUST go inside this 'android' block.
android {
    namespace = "com.example.petadoptionmanagement"
    compileSdk = 34 // Sticking to 34 for broad compatibility, but 35 is fine if you have the setup.

    defaultConfig {
        applicationId = "com.example.petadoptionmanagement"
        minSdk = 27
        targetSdk = 34 // Match compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // CORRECT PLACEMENT: These fields must be INSIDE defaultConfig
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${localProperties.getProperty("cloudinary_cloud_name") ?: ""}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"${localProperties.getProperty("cloudinary_api_key") ?: ""}\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${localProperties.getProperty("cloudinary_api_secret") ?: ""}\"")
    }

    // CORRECT PLACEMENT: This block must be INSIDE android
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // CORRECT PLACEMENT: These blocks must be INSIDE android
    compileOptions {
        // For compileSdk 34, VERSION_1_8 is standard. If you use 35, change this to VERSION_17.
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        // For compileSdk 34, "1.8" is standard. If you use 35, change this to "17".
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true // This is essential for BuildConfig generation
    }

    composeOptions {
        // This version should be compatible with your Kotlin and Compose library versions.
        kotlinCompilerExtensionVersion = "1.5.1" // A common stable version
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// The dependencies block is separate from the 'android' block.
dependencies {
    // Core AndroidX & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose (using the BOM for version management)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended") // Keep for now
    implementation("androidx.compose.runtime:runtime-livedata")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Image Loading (Coil)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Cloudinary SDK
    implementation("com.cloudinary:cloudinary-android:2.4.0") // Using a recent version

    // Firebase (using the BOM for version management)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
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
