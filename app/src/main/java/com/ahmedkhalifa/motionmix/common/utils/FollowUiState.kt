package com.ahmedkhalifa.motionmix.common.utils

import com.ahmedkhalifa.motionmix.data.model.User

data class FollowUiState(
    val followers: List<User> = emptyList(),
    val following: List<User> = emptyList(),
    val friends: List<User> = emptyList(),
    val suggestions: List<User> = emptyList(),
    val isFollowingMap: Map<String, Boolean> = emptyMap(),
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val friendsCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)