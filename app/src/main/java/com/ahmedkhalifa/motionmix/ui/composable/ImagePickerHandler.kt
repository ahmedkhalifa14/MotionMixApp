package com.ahmedkhalifa.motionmix.ui.composable

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.ahmedkhalifa.motionmix.common.helpers.ImagePickerManager
import com.yalantis.ucrop.UCrop

@Composable
fun ImagePickerHandler(
    showImagePicker: Boolean,
    onDismiss: () -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    val imagePickerManager = remember { ImagePickerManager(context) }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // UCrop result launcher - DECLARE THIS FIRST
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val resultUri = UCrop.getOutput(result.data!!)
                resultUri?.let { croppedUri ->
                    onImageSelected(croppedUri)
                }
            }
            UCrop.RESULT_ERROR -> {
                val cropError = UCrop.getError(result.data!!)
                cropError?.printStackTrace()
            }
        }
        // Clean up temp files
        imagePickerManager.cleanupTempFiles()
        onDismiss()
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            tempImageUri = selectedUri
            // Start UCrop
            val destinationFile = imagePickerManager.getCacheImageFile()
            val destinationUri = Uri.fromFile(destinationFile)

            val uCrop = UCrop.of(selectedUri, destinationUri)
                .withAspectRatio(1f, 1f) // Square aspect ratio for profile picture
                .withMaxResultSize(800, 800) // Max resolution
                .withOptions(UCrop.Options().apply {
                    setCompressionQuality(90)
                    setHideBottomControls(false)
                    setFreeStyleCropEnabled(false)
                    setShowCropFrame(true)
                    setShowCropGrid(true)
                    setCircleDimmedLayer(true) // Circular overlay for profile pictures
                    setCropFrameColor(android.graphics.Color.WHITE)
                    setCropGridStrokeWidth(2)
                    setCropFrameStrokeWidth(4)
                })

            cropLauncher.launch(uCrop.getIntent(context))
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempImageUri != null) {
            // Start UCrop for camera image
            val destinationFile = imagePickerManager.getCacheImageFile()
            val destinationUri = Uri.fromFile(destinationFile)

            val uCrop = UCrop.of(tempImageUri!!, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(800, 800)
                .withOptions(UCrop.Options().apply {
                    setCompressionQuality(90)
                    setHideBottomControls(false)
                    setFreeStyleCropEnabled(false)
                    setShowCropFrame(true)
                    setShowCropGrid(true)
                    setCircleDimmedLayer(true)
                    setCropFrameColor(android.graphics.Color.WHITE)
                    setCropGridStrokeWidth(2)
                    setCropFrameStrokeWidth(4)
                })

            cropLauncher.launch(uCrop.getIntent(context))
        }
    }

    // Show selection dialog
    if (showImagePicker) {
        ImageSourceSelectionDialog(
            onDismiss = onDismiss,
            onGallerySelected = {
                galleryLauncher.launch("image/*")
            },
            onCameraSelected = {
                val tempFile = imagePickerManager.getCacheImageFile()
                tempImageUri = Uri.fromFile(tempFile)
                cameraLauncher.launch(tempImageUri!!)
            }
        )
    }
}