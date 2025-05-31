package com.ahmedkhalifa.motionmix.common.utils

sealed class LoginResult {
    data object Success : LoginResult()
    data object EmailNotVerified : LoginResult()
    data object EmailNotFound : LoginResult()
    data class Error(val message: String) : LoginResult()
}