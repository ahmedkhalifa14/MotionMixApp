package com.ahmedkhalifa.motionmix.common.helpers


// ImagePickerManager.kt

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImagePickerManager(private val context: Context) {

    /**
     * Save bitmap to cache directory and return URI
     */
    fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "profile_images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val imageFile = File(cacheDir, "profile_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(imageFile)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            Uri.fromFile(imageFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get cache directory for temporary files
     */
    fun getCacheImageFile(): File {
        val cacheDir = File(context.cacheDir, "profile_images")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return File(cacheDir, "temp_${UUID.randomUUID()}.jpg")
    }

    /**
     * Clean up temporary files
     */
    fun cleanupTempFiles() {
        try {
            val cacheDir = File(context.cacheDir, "profile_images")
            if (cacheDir.exists()) {
                cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("temp_")) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
