package com.ahmedkhalifa.motionmix.data.model

data class UploadStatus(
    val isComplete: Boolean = false,
    val isFailed: Boolean = false,
    val progress: Int = 0,
    val message: String = "",
    val mediaUrl: String? = null,
    val thumbnailUrl: String? = null
)