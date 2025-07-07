package com.ahmedkhalifa.motionmix.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.ahmedkhalifa.motionmix.VideoUploadService
import com.ahmedkhalifa.motionmix.common.utils.UploadEvent
import com.ahmedkhalifa.motionmix.common.utils.UploadEventHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class VideoUploadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val uploadEventHandler: UploadEventHandler
) {
    val uploadEvents: SharedFlow<UploadEvent> = uploadEventHandler.uploadEvents

    fun startUpload(videoUri: Uri) {
        val serviceIntent = Intent(context, VideoUploadService::class.java).apply {
            putExtra("videoUri", videoUri.toString())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}