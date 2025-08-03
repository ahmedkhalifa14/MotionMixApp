package com.ahmedkhalifa.motionmix.domain.repo.app_preferences

import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepo {
    suspend fun setDarkMode(isDarkMode: Boolean)
    fun getDarkMode(): Flow<Boolean>
    suspend fun setUserLoggedIn(isLoggedIn: Boolean)
    fun isUserLoggedIn(): Flow<Boolean>
    suspend fun setFirstTimeLaunch(isFirstTimeLaunch: Boolean)
    fun isFirstTimeLaunch(): Flow<Boolean>
}