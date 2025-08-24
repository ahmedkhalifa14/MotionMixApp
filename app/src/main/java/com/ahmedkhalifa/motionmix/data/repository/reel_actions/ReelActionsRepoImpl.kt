package com.ahmedkhalifa.motionmix.data.repository.reel_actions

import android.util.Log
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.common.utils.Utils.tryCatch
import com.ahmedkhalifa.motionmix.data.model.Comment
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.data.remote_data_source.FireStoreService
import com.ahmedkhalifa.motionmix.domain.repo.reel_actions.ReelActionsRepo
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReelActionsRepoImpl @Inject constructor(
    private val fireStoreService: FireStoreService
) : ReelActionsRepo {

    override suspend fun getReelsPaginated(limit: Long, lastDocument: DocumentSnapshot?): Resource<Pair<List<Reel>, DocumentSnapshot?>> =
        withContext(Dispatchers.IO) {
            try {
                val result = fireStoreService.getReelsPaginated(limit, lastDocument)
                Resource.Success(result)
            } catch (e: Exception) {
                Log.e("ReelActionsRepoImpl", "Error getting paginated reels: ${e.message}")
                Resource.Error(e.message ?: "Failed to load reels")
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

    override suspend fun addComment(reelId: String, comment: Comment): Resource<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("ReelRepo", "🟡 Calling fireStoreService.addComment...")
                Log.d("ReelRepo", "🟡 Reel ID: $reelId, Comment: $comment")

                val addCommentResult = fireStoreService.addComment(reelId = reelId, comment = comment)

                Log.d("ReelRepo", "🟡 fireStoreService result: $addCommentResult")

                if (addCommentResult) {
                    Log.d("ReelRepo", "✅ Comment added successfully to Firebase")
                    Resource.Success(true)
                } else {
                    Log.e("ReelRepo", "🔴 fireStoreService returned false - comment not added")
                    Resource.Error("Failed to add comment to Firebase")
                }

            } catch (e: Exception) {
                Log.e("ReelRepo", "❌ Exception in addComment: ${e.message}", e)
                Resource.Error(e.message ?: "Unknown error")
            }
        }


    override suspend fun getCommentsForReel(reelId: String): List<Comment> {
        return try {
            fireStoreService.getCommentsForReel(reelId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

