package com.ahmedkhalifa.motionmix.data.model


data class User(
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val joinedAt: Long = 0,
    val location: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val profilePictureLink: String = "",
    val numberOfFollowers: Int = 0,
    val numberOfFollowing: Int = 0,
    val likes:Int=0
)
