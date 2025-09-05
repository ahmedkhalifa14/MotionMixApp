package com.ahmedkhalifa.motionmix.data.repository.follow

import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.common.utils.Utils.tryCatch
import com.ahmedkhalifa.motionmix.data.model.User
import com.ahmedkhalifa.motionmix.data.remote_data_source.FireStoreService
import com.ahmedkhalifa.motionmix.domain.repo.follow.FollowRepository
import com.google.firebase.firestore.DocumentSnapshot
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FollowRepositoryImpl @Inject constructor(
    private val fireStoreService: FireStoreService
) : FollowRepository {

    override suspend fun followUser(targetUserId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            tryCatch {
                fireStoreService.followUser(targetUserId).let { result ->
                    when {
                        result.isSuccess -> Resource.Success(Unit)
                        else -> Resource.Error(
                            result.exceptionOrNull()?.message ?: "Failed to follow user"
                        )
                    }
                }
            }
        }

    override suspend fun unfollowUser(targetUserId: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            tryCatch {
                fireStoreService.unfollowUser(targetUserId).let { result ->
                    when {
                        result.isSuccess -> Resource.Success(Unit)
                        else -> Resource.Error(
                            result.exceptionOrNull()?.message ?: "Failed to unfollow user"
                        )
                    }
                }
            }
        }

    override suspend fun isFollowing(targetUserId: String): Resource<Boolean> =
        withContext(Dispatchers.IO) {
            tryCatch {
                Resource.Success(fireStoreService.isFollowing(targetUserId))
            }
        }

    override suspend fun getFollowers(
        userId: String,
        limit: Long,
        lastDocument: DocumentSnapshot?
    ): Resource<Pair<List<User>, DocumentSnapshot?>> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val (users, lastDoc) = fireStoreService.getFollowers(userId, limit, lastDocument)
                Resource.Success(Pair(users, lastDoc))
            }
        }

    override suspend fun getFollowing(
        userId: String,
        limit: Long,
        lastDocument: DocumentSnapshot?
    ): Resource<Pair<List<User>, DocumentSnapshot?>> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val (users, lastDoc) = fireStoreService.getFollowing(userId, limit, lastDocument)
                Resource.Success(Pair(users, lastDoc))
            }
        }

    override suspend fun getUserSuggestions(limit: Long): Resource<List<User>> =
        withContext(Dispatchers.IO) {
            tryCatch {
                Resource.Success(fireStoreService.getUserSuggestions(limit))
            }
        }

    override suspend fun getNearbySuggestions(
        currentLat: Double,
        currentLon: Double,
        radiusKm: Double
    ): Resource<List<User>> =
        withContext(Dispatchers.IO) {
            tryCatch {
                Resource.Success(
                    fireStoreService.getNearbySuggestions(
                        currentLat,
                        currentLon,
                        radiusKm
                    )
                )
            }
        }

    override suspend fun getFollowCounts(userId: String): Resource<Triple<Int, Int, Int>> =
        withContext(Dispatchers.IO) {
            tryCatch {
                Resource.Success(fireStoreService.getFollowCounts(userId))
            }
        }

    override suspend fun getFriends(
        userId: String,
        limit: Long,
        lastDocument: DocumentSnapshot?
    ): Resource<Pair<List<User>, DocumentSnapshot?>> =
        withContext(Dispatchers.IO) {
            tryCatch {
                Resource.Success(fireStoreService.getFriends(userId, limit, lastDocument))
            }
        }

}