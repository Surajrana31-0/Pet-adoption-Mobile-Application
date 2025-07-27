package com.example.petadoptionmanagement.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

/**
 * Utils for safely picking images (single or multiple), handling gallery permissions, and returning URIs.
 * Supports both Activities and Fragments (supply the appropriate registry owner).
 *
 * Usage:
 * - In onCreate(): val imageUtils = ImageUtils(this, this)
 * - Call imageUtils.registerLaunchers()
 * - Call imageUtils.launchImagePicker(OnImagePicked:{ ... }, OnError:{ ... }, ...)
 */
class ImageUtils(
    private val activity: Activity,
    private val registryOwner: ActivityResultRegistryOwner
) {
    // Hold references to needed launchers and listeners
    private lateinit var pickSingleImageLauncher: ActivityResultLauncher<String>
    private lateinit var pickMultipleImageLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    // To avoid leaks, use WeakReference for callbacks
    private var onImagePickedCallback: WeakReference<(List<Uri>) -> Unit>? = null
    private var onPermissionDeniedCallback: WeakReference<(() -> Unit)?>? = null
    private var onErrorCallback: WeakReference<(String) -> Unit>? = null
    private var allowMultiple: Boolean = false

    // Register launchers -- Call in onCreate
    fun registerLaunchers() {
        pickSingleImageLauncher = registryOwner.activityResultRegistry.register(
            "pick_single_image",
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                onImagePickedCallback?.get()?.invoke(listOf(uri))
            } else {
                onErrorCallback?.get()?.invoke("No image selected.")
            }
        }
        pickMultipleImageLauncher = registryOwner.activityResultRegistry.register(
            "pick_multiple_images",
            ActivityResultContracts.GetMultipleContents()
        ) { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                onImagePickedCallback?.get()?.invoke(uris)
            } else {
                onErrorCallback?.get()?.invoke("No images selected.")
            }
        }
        requestPermissionLauncher = registryOwner.activityResultRegistry.register(
            "request_gallery_permission",
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Launch gallery after permission granted
                launchGalleryInternal()
            } else {
                onPermissionDeniedCallback?.get()?.invoke()
                onErrorCallback?.get()?.invoke("Permission denied.")
            }
        }
    }

    /**
     * Public entry point to pick an image (single or multiple).
     *
     * @param allowMultipleImages Whether to allow the user to pick multiple images (false for most profile-image workflows)
     * @param onImagePicked Callback invoked with a list of Uris
     * @param onPermissionDenied Optional callback for permission denial
     * @param onError Optional callback for errors or cancellation
     */
    fun launchImagePicker(
        allowMultipleImages: Boolean = false,
        onImagePicked: (List<Uri>) -> Unit,
        onPermissionDenied: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        this.allowMultiple = allowMultipleImages
        this.onImagePickedCallback = WeakReference(onImagePicked)
        this.onPermissionDeniedCallback = WeakReference(onPermissionDenied)
        this.onErrorCallback = WeakReference(onError ?: {})
        if (hasGalleryPermission(activity)) {
            launchGalleryInternal()
        } else {
            val permission = getGalleryPermission()
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun launchGalleryInternal() {
        if (allowMultiple) {
            pickMultipleImageLauncher.launch("image/*")
        } else {
            pickSingleImageLauncher.launch("image/*")
        }
    }

    // Permission helpers
    private fun hasGalleryPermission(context: Context): Boolean {
        val permission = getGalleryPermission()
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    private fun getGalleryPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    companion object {
        /**
         * Utility method to get filename from Uri.
         */
        fun getFileNameFromUri(context: Context, uri: Uri): String? {
            return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            }
        }
    }
}
