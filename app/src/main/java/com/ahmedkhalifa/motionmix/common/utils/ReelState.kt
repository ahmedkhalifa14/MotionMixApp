package com.ahmedkhalifa.motionmix.common.utils

data class ReelState(
    val progress: Float = 0f,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isPlaying: Boolean = false
)
