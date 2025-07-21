package com.ahmedkhalifa.motionmix.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.ahmedkhalifa.motionmix.VideoUploadService
import com.ahmedkhalifa.motionmix.common.utils.UploadEvent
import com.ahmedkhalifa.motionmix.common.utils.UploadEventHandler
import com.ahmedkhalifa.motionmix.domain.repo.video_upload.VideoUploadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoUploadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val uploadEventHandler: UploadEventHandler
) : VideoUploadRepository {

    override val uploadEvents: SharedFlow<UploadEvent> = uploadEventHandler.uploadEvents

    override fun startUpload(videoUri: Uri) {
        val intent = Intent(context, VideoUploadService::class.java).apply {
            putExtra("videoUri", videoUri.toString())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}