package com.example.petadoptionmanagement

import android.app.Application
import com.cloudinary.android.MediaManager
import java.util.HashMap

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initCloudinary()
    }

    private fun initCloudinary() {
        val config: MutableMap<String, String> = HashMap()
        // Replace "YOUR_CLOUD_NAME" with your actual Cloudinary cloud name from your dashboard.
        // For unsigned uploads (recommended for direct client upload), only cloud_name is strictly needed.
        // If you are doing signed uploads directly from the client, you'd also include api_key and api_secret,
        // but this is NOT RECOMMENDED due to security risks. Backend signing is safer.
        config["cloud_name"] = "YOUR_CLOUD_NAME"
        // config["api_key"] = "YOUR_API_KEY" // ONLY if doing signed uploads directly from client (NOT RECOMMENDED)
        // config["api_secret"] = "YOUR_API_SECRET" // ONLY if doing signed uploads directly from client (NOT RECOMMENDED)

        MediaManager.init(this, config)
    }
}