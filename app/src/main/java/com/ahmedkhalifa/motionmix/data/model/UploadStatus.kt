package com.ahmedkhalifa.motionmix.data.model


data class UploadStatus(
    val progress: Int = 0,
    val isComplete: Boolean = false,
    val isFailed: Boolean = false,
    val message: String = ""
)