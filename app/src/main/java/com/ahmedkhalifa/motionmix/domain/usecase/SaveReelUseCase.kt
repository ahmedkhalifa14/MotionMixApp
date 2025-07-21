package com.ahmedkhalifa.motionmix.domain.usecase


import com.ahmedkhalifa.motionmix.domain.repo.video_upload.ReelRepository
import javax.inject.Inject

class SaveReelUseCase @Inject constructor(
    private val reelRepository: ReelRepository
) {
    suspend operator fun invoke(
        mediaUrl: String,
        thumbnailUrl: String,
        description: String
    ): Result<Unit> {
        return reelRepository.saveReel(mediaUrl, thumbnailUrl, description)
    }
}