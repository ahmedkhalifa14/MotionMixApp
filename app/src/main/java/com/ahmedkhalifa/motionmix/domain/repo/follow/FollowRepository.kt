package com.ahmedkhalifa.motionmix.domain.repo.follow

import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.User
import com.google.firebase.firestore.DocumentSnapshot

interface FollowRepository {
    suspend fun followUser(targetUserId: String): Resource<Unit>
    suspend fun unfollowUser(targetUserId: String): Resource<Unit>
    suspend fun isFollowing(targetUserId: String): Resource<Boolean>
    suspend fun getFollowers(
        userId: String,
        limit: Long,
        lastDocument: DocumentSnapshot?
    ): Resource<Pair<List<User>, DocumentSnapshot?>>
    suspend fun getFollowing(
        userId: String,
        limit: Long,
        lastDocument: DocumentSnapshot?
    ): Resource<Pair<List<User>, DocumentSnapshot?>>
    suspend fun getUserSuggestions(limit: Long): Resource<List<User>>
    suspend fun getNearbySuggestions(
        currentLat: Double,
        currentLon: Double,
        radiusKm: Double
    ): Resource<List<User>>

    suspend fun getFollowCounts(userId: String): Resource<Triple<Int, Int, Int>>


    suspend fun getFriends(
        userId: String,
        limit: Long = 20,
        lastDocument: DocumentSnapshot? = null
    ): Resource<Pair<List<User>, DocumentSnapshot?>>

}