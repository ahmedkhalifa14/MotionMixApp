package com.ahmedkhalifa.motionmix.ui.main_activity

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.domain.repo.app_preferences.AppPreferencesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppPreferencesViewModel @Inject constructor(
    private val appPreferencesRepo: AppPreferencesRepo,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _darkModeEnabled = MutableStateFlow<Boolean?>(null)
    val darkModeEnabled: StateFlow<Boolean?> = _darkModeEnabled

    private val _isUserLogin = MutableStateFlow<Boolean?>(null)
    val isUserLogin: StateFlow<Boolean?> = _isUserLogin

    private val _isFirstTimeLaunch = MutableStateFlow<Boolean?>(null)
    val isFirstTimeLaunch: StateFlow<Boolean?> = _isFirstTimeLaunch



    fun setFirstTimeLaunch(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepo.setFirstTimeLaunch(enabled)
            _isFirstTimeLaunch.value = enabled
        }
    }

    fun setUserLogin(enabled: Boolean) {
        viewModelScope.launch {
            appPreferencesRepo.setUserLoggedIn(enabled)
        }
    }

    fun checkFirstTimeLaunch() {
        viewModelScope.launch {
            appPreferencesRepo.isFirstTimeLaunch().collect { isFirstTime ->
                _isFirstTimeLaunch.value = isFirstTime
            }
        }
    }

    fun checkUserLogin() {
        viewModelScope.launch {
            appPreferencesRepo.isUserLoggedIn().collect { isLoggedIn ->
                _isUserLogin.value = isLoggedIn
            }
        }
    }

}