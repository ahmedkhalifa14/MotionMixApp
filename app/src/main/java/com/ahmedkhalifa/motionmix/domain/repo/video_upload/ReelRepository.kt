package com.ahmedkhalifa.motionmix.domain.repo.video_upload

interface ReelRepository {
    suspend fun saveReel(
        mediaUrl: String,
        thumbnailUrl: String,
        description: String
    ): Result<Unit>
}