package com.ahmedkhalifa.motionmix.domain.repo.reel_actions

import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.Comment
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.google.firebase.firestore.DocumentSnapshot

interface ReelActionsRepo {
    //suspend fun getReels(): Resource< Pair<List<Reel>, DocumentSnapshot?> >
    suspend fun getReelsPaginated(limit: Long = 10, lastDocument: DocumentSnapshot? = null): Resource<Pair<List<Reel>, DocumentSnapshot?>>
    suspend fun toggleLike(reelId: String, userId: String, isLiked: Boolean): Resource<Reel?>
    suspend fun addComment(reelId: String, comment: Comment): Resource<Boolean>
    suspend fun getCommentsForReel(reelId: String): List<Comment>


}