package com.ahmedkhalifa.motionmix.data.repository.reel_actions

import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.common.utils.Utils.tryCatch
import com.ahmedkhalifa.motionmix.data.model.Comment
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.data.remote_data_source.FireStoreService
import com.ahmedkhalifa.motionmix.domain.repo.reel_actions.ReelActionsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReelActionsRepoImpl @Inject constructor(
    private val fireStoreService: FireStoreService
) : ReelActionsRepo {
    override suspend fun getReels(): Resource<List<Reel>> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val result =
                    fireStoreService.getReels()
                Resource.Success(result)
            }
        }


    override suspend fun toggleLike(
        reelId: String,
        userId: String,
        isLiked: Boolean
    ): Resource<Reel?> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val reel =
                    fireStoreService.toggleLike(reelId = reelId, userId = userId, isLiked = isLiked)
                Resource.Success(reel)
            }
        }


    override suspend fun addComment(
        reelId: String,
        comment: Comment
    ): Resource<Boolean> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val addCommentResult =
                    fireStoreService.addComment(reelId = reelId, comment = comment)
                Resource.Success(addCommentResult)
            }
        }
}

