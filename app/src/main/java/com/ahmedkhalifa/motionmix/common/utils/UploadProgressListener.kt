package com.ahmedkhalifa.motionmix.common.utils

interface UploadProgressListener {
    fun onProgress(progress: Int)
    fun onSuccess(mediaUrl: String, thumbnailUrl: String)
    fun onFailure()
}

data class UploadEvent(
    val progress: Int = 0,
    val isComplete: Boolean = false,
    val isFailed: Boolean = false,
    val message: String = "",
    val mediaUrl: String? = null,
    val thumbnailUrl: String? = null
)