package com.ahmedkhalifa.motionmix.ui.screens.auth.login

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.LoginResult
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.common.utils.Resource.*
import com.ahmedkhalifa.motionmix.domain.repo.auth.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepo,
): ViewModel() {
    private val _loginState = MutableStateFlow<Event<Resource<Unit>>>(Event(Init()))
    val loginState: StateFlow<Event<Resource<Unit>>> = _loginState
    fun loginWithEmailAndPassword(userEmail: String, userPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _loginState.value= Event(Loading())
            val loginResult = authRepo.loginWithEmailAndPassword(email = userEmail, password = userPassword)
            _loginState.value = when (loginResult){
                LoginResult.Success -> Event(Success(Unit))
                LoginResult.EmailNotVerified -> Event(Error("Email not verified"))
                LoginResult.EmailNotFound -> Event(Error("Email not found"))
                is LoginResult.Error -> Event(Error(loginResult.message))
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                authRepo.logout()
            } catch (e: Exception) {
                Log.d("AuthViewModel", "logout: ${e.message}")
            }
        }
    }
}