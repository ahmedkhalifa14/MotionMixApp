package com.ahmedkhalifa.motionmix.common.utils

sealed class LocationResult {
    data class Success(val address: String) : LocationResult()
    data class Error(val message: String) : LocationResult()
}