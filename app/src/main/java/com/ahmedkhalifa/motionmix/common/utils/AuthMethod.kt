package com.ahmedkhalifa.motionmix.common.utils

import androidx.compose.ui.graphics.Color

data class AuthMethod(
    val text: String,
    val iconResId: Int,
    val onClick: () -> Unit,
    val tint: Color=Color.Unspecified
)
