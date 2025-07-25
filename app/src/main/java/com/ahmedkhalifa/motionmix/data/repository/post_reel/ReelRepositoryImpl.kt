package com.ahmedkhalifa.motionmix.data.repository

import com.ahmedkhalifa.motionmix.data.remote_data_source.FireStoreService
import com.ahmedkhalifa.motionmix.domain.repo.video_upload.ReelRepository
import jakarta.inject.Inject

class ReelRepositoryImpl @Inject constructor(
    private val fireStoreService: FireStoreService
) : ReelRepository {
    override suspend fun saveReel(
        mediaUrl: String,
        thumbnailUrl: String,
        description: String
    ): Result<Unit> {
        return fireStoreService.saveReelToFireStore(mediaUrl, thumbnailUrl, description)
    }
}