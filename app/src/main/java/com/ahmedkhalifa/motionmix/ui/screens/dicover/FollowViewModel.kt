package com.ahmedkhalifa.motionmix.ui.screens.dicover

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.FollowUiState
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.User
import com.ahmedkhalifa.motionmix.data.remote_data_source.FireStoreService
import com.ahmedkhalifa.motionmix.data.remote_data_source.FirebaseAuthenticationService
import com.ahmedkhalifa.motionmix.domain.repo.follow.FollowRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class FollowViewModel @Inject constructor(
    private val followRepository: FollowRepository,
    firebaseAuthenticationService: FirebaseAuthenticationService
) : ViewModel() {

    private val currentUserId = firebaseAuthenticationService.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(FollowUiState())
    val uiState: StateFlow<FollowUiState> = _uiState.asStateFlow()

    private val _followEvent = MutableSharedFlow<Event<Resource<Unit>>>()
    val followEvent: SharedFlow<Event<Resource<Unit>>> = _followEvent.asSharedFlow()


    private var followersLastDoc: DocumentSnapshot? = null
    private var followingLastDoc: DocumentSnapshot? = null
    private var friendsLastDoc: DocumentSnapshot? = null

    init {
        loadFollowCounts(currentUserId) // Load counts on initialization
    }

    fun loadFollowCounts(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = followRepository.getFollowCounts(userId)
            when (result) {
                is Resource.Success -> {
                    val (followersCount, followingCount, friendsCount) = result.data ?: Triple(
                        0,
                        0,
                        0
                    )
                    _uiState.value = _uiState.value.copy(
                        followersCount = followersCount,
                        followingCount = followingCount,
                        friendsCount = friendsCount,
                        isLoading = false,
                        error = null
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }

                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun loadFriends(userId: String, limit: Long = 20, reset: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val lastDoc = if (reset) null else friendsLastDoc
            val result = followRepository.getFriends(userId, limit, lastDoc)
            when (result) {
                is Resource.Success -> {
                    val (users, lastDoc) = result.data ?: Pair(emptyList(), null)
                    friendsLastDoc = lastDoc
                    _uiState.value = _uiState.value.copy(
                        friends = if (reset) users else _uiState.value.friends + users,
                        isLoading = false,
                        error = null
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }

                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun followUser(targetUserId: String) {
        viewModelScope.launch {
            _followEvent.emit(Event(Resource.Loading()))
            val result = followRepository.followUser(targetUserId)
            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isFollowingMap = _uiState.value.isFollowingMap + (targetUserId to true),
                        error = null
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }

                else -> {}
            }
            _followEvent.emit(Event(result))
        }
    }


    fun unfollowUser(targetUserId: String) {
        viewModelScope.launch {
            _followEvent.emit(Event(Resource.Loading()))
            val result = followRepository.unfollowUser(targetUserId)
            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isFollowingMap = _uiState.value.isFollowingMap + (targetUserId to false),
                        error = null
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }

                else -> {}
            }
            _followEvent.emit(Event(result))
        }
    }

    fun checkIsFollowing(targetUserId: String) {
        viewModelScope.launch {
            val result = followRepository.isFollowing(targetUserId)
            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isFollowingMap = _uiState.value.isFollowingMap + (targetUserId to (result.data
                            ?: false)),
                        error = null
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }

                else -> {}
            }
        }
    }

    fun loadFollowers(userId: String, limit: Long = 20, reset: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val lastDoc = if (reset) null else followersLastDoc
            val result = followRepository.getFollowers(userId, limit, lastDoc)
            when (result) {
                is Resource.Success -> {
                    val (users, lastDoc) = result.data ?: Pair(emptyList(), null)
                    followersLastDoc = lastDoc
                    _uiState.value = _uiState.value.copy(
                        followers = if (reset) users else _uiState.value.followers + users,
                        isLoading = false,
                        error = null
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }

                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun loadFollowing(userId: String, limit: Long = 20, reset: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val lastDoc = if (reset) null else followingLastDoc
            val result = followRepository.getFollowing(userId, limit, lastDoc)
            when (result) {
                is Resource.Success -> {
                    val (users, lastDoc) = result.data ?: Pair(emptyList(), null)
                    followingLastDoc = lastDoc
                    _uiState.value = _uiState.value.copy(
                        following = if (reset) users else _uiState.value.following + users,
                        isLoading = false,
                        error = null
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }

                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun loadSuggestions(limit: Long = 10) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = followRepository.getUserSuggestions(limit)
            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        suggestions = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }

                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun loadNearbySuggestions(currentLat: Double, currentLon: Double, radiusKm: Double = 50.0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = followRepository.getNearbySuggestions(currentLat, currentLon, radiusKm)
            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        suggestions = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }

                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}