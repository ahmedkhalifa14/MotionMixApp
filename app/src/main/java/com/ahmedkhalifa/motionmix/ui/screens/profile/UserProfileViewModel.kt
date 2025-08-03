package com.ahmedkhalifa.motionmix.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.ProfileUiState
import com.ahmedkhalifa.motionmix.data.model.User
import com.ahmedkhalifa.motionmix.domain.repo.user_profile.UserProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userProfileRepo: UserProfileRepo
) : ViewModel() {
    private val _saveUserProfileState =
        MutableStateFlow<Event<Resource<Unit>>>(Event(Resource.Init()))
    val saveUserProfileState: StateFlow<Event<Resource<Unit>>> = _saveUserProfileState


    private val _getUserProfileState =
        MutableStateFlow<Event<Resource<User?>>>(Event(Resource.Init()))
    val getUserProfileState: StateFlow<Event<Resource<User?>>> = _getUserProfileState


    private val _updateUserProfileState =
        MutableStateFlow<Event<Resource<Unit>>>(Event(Resource.Init()))
    val updateUserProfileState: StateFlow<Event<Resource<Unit>>> = _updateUserProfileState


    private val _userState = MutableStateFlow(ProfileUiState())
    val userState: StateFlow<ProfileUiState> = _userState.asStateFlow()



    fun saveUserProfileData(user: User) {
        viewModelScope.launch {
            _saveUserProfileState.emit(Event(Resource.Loading()))
            try {
                userProfileRepo.saveUserInfo(user)
                _saveUserProfileState.emit(Event(Resource.Success(Unit)))
            } catch (e: Exception) {
                _saveUserProfileState.emit(Event(Resource.Error(e.message.toString())))
            }
        }
    }


    fun getUserProfileData() {
        viewModelScope.launch {
            _getUserProfileState.emit((Event(Resource.Loading())))
            try {
                val getUserProfileDataResult = userProfileRepo.getUserInfo()
                _getUserProfileState.emit(Event(getUserProfileDataResult))
            } catch (e: Exception) {
                _getUserProfileState.emit(Event(Resource.Error(e.message.toString())))
            }
        }
    }

    fun updateUserProfileData(user: User) {
        viewModelScope.launch {
            _updateUserProfileState.emit((Event(Resource.Loading())))
            try {
                val updateUserProfileDataResult = userProfileRepo.updateUserInfo(user)
                _updateUserProfileState.emit(Event(updateUserProfileDataResult))
            } catch (e: Exception) {
                _updateUserProfileState.emit(Event(Resource.Error(e.message.toString())))
            }
        }
    }


}