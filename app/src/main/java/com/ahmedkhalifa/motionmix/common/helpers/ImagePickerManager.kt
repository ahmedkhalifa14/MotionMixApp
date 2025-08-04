package com.ahmedkhalifa.motionmix.common.helpers

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImagePickerManager(private val context: Context) {

    // FIXED: Use filesDir instead of cacheDir for persistent storage
    fun getCacheImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "temp_profile_${timeStamp}.jpg"

        // Create directory in app's internal files (persistent, not cache)
        val storageDir = File(context.filesDir, "profile_images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File(storageDir, imageFileName)
    }

    // Get a persistent file for camera capture
    fun getCameraImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "camera_${timeStamp}.jpg"

        val storageDir = File(context.filesDir, "profile_images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File(storageDir, imageFileName)
    }

    // FIXED: Clean up old files but keep recent ones
    fun cleanupTempFiles() {
        try {
            val storageDir = File(context.filesDir, "profile_images")
            if (storageDir.exists()) {
                val files = storageDir.listFiles()
                files?.forEach { file ->
                    // Keep files created in the last hour, delete older temp files
                    val hourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
                    if (file.lastModified() < hourAgo && file.name.startsWith("temp_")) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Optional: Clean up all temp files (use carefully)
    fun cleanupAllTempFiles() {
        try {
            val storageDir = File(context.filesDir, "profile_images")
            if (storageDir.exists()) {
                val files = storageDir.listFiles()
                files?.forEach { file ->
                    if (file.name.startsWith("temp_") || file.name.startsWith("camera_")) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}