package com.ahmedkhalifa.motionmix.data.model

import com.google.type.DateTime

data class User(
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val joinedAt: DateTime,
    val location: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val profilePictureLink: String = "",
    val numberOfFollowers: Int,
    val numberOfFollowing: Int,
)
