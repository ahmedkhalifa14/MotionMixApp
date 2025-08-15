package com.ahmedkhalifa.motionmix.domain.repo.reel_actions

import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.Comment
import com.ahmedkhalifa.motionmix.data.model.Reel

interface ReelActionsRepo {
    suspend fun getReels(): Resource<List<Reel>>
    suspend fun toggleLike(reelId: String, userId: String, isLiked: Boolean): Resource<Reel?>
    suspend fun addComment(reelId: String, comment: Comment): Resource<Boolean>
}