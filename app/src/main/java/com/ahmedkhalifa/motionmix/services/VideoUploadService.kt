package com.ahmedkhalifa.motionmix.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.net.toUri
import com.ahmedkhalifa.motionmix.services.VideoUploadingNotificationHandler
import com.ahmedkhalifa.motionmix.common.utils.UploadEvent
import com.ahmedkhalifa.motionmix.common.utils.UploadProgressListener
import com.ahmedkhalifa.motionmix.data.remote_data_source.VideosUploadingUsingFirebaseCloudStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@AndroidEntryPoint
class VideoUploadService : Service() {

    @Inject
    lateinit var videosUploadingUsingFirebaseCloudStorage: VideosUploadingUsingFirebaseCloudStorage

    @Inject
    lateinit var videoUploadingNotificationHandler: VideoUploadingNotificationHandler

    companion object {
        private val _uploadEvents = MutableSharedFlow<UploadEvent>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        val uploadEvents: SharedFlow<UploadEvent> = _uploadEvents
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val videoUriString = intent?.getStringExtra("videoUri")
        if (videoUriString == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val videoUri = videoUriString.toUri()

        startForeground(
            VideoUploadingNotificationHandler.Companion.NOTIFICATION_ID,
            videoUploadingNotificationHandler.createNotification(0).build()
        )

        videosUploadingUsingFirebaseCloudStorage.uploadVideo(
            videoUri,
            object : UploadProgressListener {
                override fun onProgress(progress: Int) {
                    _uploadEvents.tryEmit(
                        UploadEvent(progress, false, false, "Uploading: $progress%")
                    )
                    videoUploadingNotificationHandler.onProgress(progress)
                }

                override fun onSuccess(mediaUrl: String, thumbnailUrl: String) {
                    _uploadEvents.tryEmit(
                        UploadEvent(100, true, false, mediaUrl)
                    )
                    videoUploadingNotificationHandler.onSuccess(mediaUrl, thumbnailUrl)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }

                override fun onFailure() {
                    _uploadEvents.tryEmit(
                        UploadEvent(-1, false, true, "Upload failed")
                    )
                    videoUploadingNotificationHandler.onFailure()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        )


        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}