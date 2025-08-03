package com.ahmedkhalifa.motionmix.data.model


data class ProfileUiState(
    val user: User = User(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)