package com.ahmedkhalifa.motionmix.ui.composable

import android.app.Activity
import android.net.Uri
import android.util.Log
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

    // UCrop result launcher
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val resultUri = UCrop.getOutput(result.data!!)
                resultUri?.let { croppedUri ->
                    Log.d("ImagePicker", "Cropped image saved to: $croppedUri")
                    onImageSelected(croppedUri)
                }
            }
            UCrop.RESULT_ERROR -> {
                val cropError = UCrop.getError(result.data!!)
                Log.e("ImagePicker", "Crop error: ${cropError?.message}")
                cropError?.printStackTrace()
            }
        }
        // Don't clean up immediately - the file is still needed for upload
        onDismiss()
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            Log.d("ImagePicker", "Gallery selected URI: $selectedUri")
            tempImageUri = selectedUri

            // Create destination file for cropped image
            val destinationFile = imagePickerManager.getCacheImageFile()
            val destinationUri = Uri.fromFile(destinationFile)

            Log.d("ImagePicker", "Crop destination: $destinationUri")

            val uCrop = UCrop.of(selectedUri, destinationUri)
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

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempImageUri != null) {
            Log.d("ImagePicker", "Camera captured successfully: $tempImageUri")

            // Create destination file for cropped image
            val destinationFile = imagePickerManager.getCacheImageFile()
            val destinationUri = Uri.fromFile(destinationFile)

            Log.d("ImagePicker", "Camera crop destination: $destinationUri")

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
        } else {
            Log.e("ImagePicker", "Camera capture failed")
            onDismiss()
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
                // FIXED: Use the camera-specific method
                val tempFile = imagePickerManager.getCameraImageFile()
                tempImageUri = Uri.fromFile(tempFile)
                Log.d("ImagePicker", "Camera temp file: $tempImageUri")
                cameraLauncher.launch(tempImageUri!!)
            }
        )
    }
}