package com.ahmedkhalifa.motionmix.domain.repo.video_upload

import android.net.Uri
import com.ahmedkhalifa.motionmix.common.utils.UploadEvent
import kotlinx.coroutines.flow.SharedFlow

interface VideoUploadRepository {
    val uploadEvents: SharedFlow<UploadEvent>
    fun startUpload(videoUri: Uri)
}
