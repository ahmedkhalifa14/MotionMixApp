package com.ahmedkhalifa.motionmix.ui.screens.post_reel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.data.model.UploadStatus
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UploadViewModel : ViewModel() {
    private val _uploadStatus = MutableStateFlow(UploadStatus())
    val uploadStatus: StateFlow<UploadStatus> = _uploadStatus

    fun startUpload(videoUri: Uri) {
        viewModelScope.launch {
            try {
                _uploadStatus.value = UploadStatus(progress = 0, message = "Starting upload...")

                // Upload video to Firebase Storage
                val storage = FirebaseStorage.getInstance()
                val videoRef = storage.reference.child("videos/${UUID.randomUUID()}.mp4")
                val uploadTask = videoRef.putFile(videoUri)

                // Update progress
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    _uploadStatus.value = UploadStatus(progress = progress, message = "Uploading... $progress%")
                }
                val videoUriResult = uploadTask.await()
                val mediaUrl = videoRef.downloadUrl.await().toString()
                val thumbnailRef = storage.reference.child("thumbnails/${UUID.randomUUID()}.jpg")
                val thumbnailUrl = mediaUrl

                _uploadStatus.value = UploadStatus(
                    isComplete = true,
                    progress = 100,
                    message = "Upload completed",
                    mediaUrl = mediaUrl,
                    thumbnailUrl = thumbnailUrl
                )
            } catch (e: Exception) {
                _uploadStatus.value = UploadStatus(
                    isFailed = true,
                    message = "Upload failed: ${e.message}",
                    progress = 0
                )
            }
        }
    }

    fun resetUploadStatus() {
        _uploadStatus.value = UploadStatus()
    }
}