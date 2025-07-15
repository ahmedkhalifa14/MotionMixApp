package com.ahmedkhalifa.motionmix.common.utils

data class AuthMethod(
    val text: String,
    val iconResId: Int,
    val onClick: () -> Unit
)
