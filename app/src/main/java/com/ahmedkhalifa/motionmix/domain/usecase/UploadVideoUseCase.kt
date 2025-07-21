package com.ahmedkhalifa.motionmix.domain.usecase

import android.net.Uri
import com.ahmedkhalifa.motionmix.domain.repo.video_upload.VideoUploadRepository
import javax.inject.Inject

class UploadVideoUseCase @Inject constructor(
    val repository: VideoUploadRepository
) {
    operator fun invoke(videoUri: Uri) {
        repository.startUpload(videoUri)
    }
}
