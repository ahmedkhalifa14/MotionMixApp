package com.ahmedkhalifa.motionmix.ui.screens.auth.login

import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.domain.repo.auth.AuthRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepo,
) {
    private val _loginState = MutableStateFlow<Event<Resource<Unit>>>(Event(Resource.Init()))
    val loginState: StateFlow<Event<Resource<Unit>>> = _loginState

    private val _logoutState = MutableStateFlow<Event<Resource<Unit>>>(Event(Resource.Init()))
    val logoutState: StateFlow<Event<Resource<Unit>>> = _logoutState




}