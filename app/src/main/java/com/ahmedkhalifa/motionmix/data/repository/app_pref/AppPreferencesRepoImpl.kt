package com.ahmedkhalifa.motionmix.data.repository.app_pref

import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import com.ahmedkhalifa.motionmix.common.utils.Constant.DARK_MODE_KEY
import com.ahmedkhalifa.motionmix.common.utils.Constant.FIRST_TIME_LAUNCH_KEY
import com.ahmedkhalifa.motionmix.common.utils.Constant.USER_LOGGED_IN_KEY
import com.ahmedkhalifa.motionmix.data.local_data_source.datastore.DataStoreManager
import com.ahmedkhalifa.motionmix.domain.repo.app_preferences.AppPreferencesRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppPreferencesRepoImpl @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : AppPreferencesRepo {

    override suspend fun setDarkMode(isDarkMode: Boolean) {
        dataStoreManager.setBoolean(DARK_MODE_KEY, isDarkMode)
    }

    override fun getDarkMode(): Flow<Boolean> = dataStoreManager.getBoolean(DARK_MODE_KEY)


    override suspend fun setUserLoggedIn(isLoggedIn: Boolean) {
        dataStoreManager.setBoolean(USER_LOGGED_IN_KEY, isLoggedIn)
    }

    override fun isUserLoggedIn(): Flow<Boolean> = dataStoreManager.getBoolean(USER_LOGGED_IN_KEY)


    override suspend fun setFirstTimeLaunch(isFirstTimeLaunch: Boolean) {
        dataStoreManager.setBoolean(FIRST_TIME_LAUNCH_KEY, isFirstTimeLaunch)
    }

    override fun isFirstTimeLaunch(): Flow<Boolean> =
        dataStoreManager.getBoolean(FIRST_TIME_LAUNCH_KEY)

}