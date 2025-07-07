package com.ahmedkhalifa.motionmix

import com.ahmedkhalifa.motionmix.common.utils.UploadProgressListener
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoUploadingNotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager
) : UploadProgressListener {

    companion object {
        const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "VideoUploadChannel"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Video Upload Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(progress: Int): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setContentTitle("Uploading Video")
            .setPriority(NotificationCompat.PRIORITY_LOW)

        when (progress) {
            -1 -> {
                builder.setContentText("Upload failed")
                    .setProgress(0, 0, false)
            }
            100 -> {
                builder.setContentText("Upload complete")
                    .setProgress(0, 0, false)
            }
            else -> {
                builder.setContentText("Uploading: $progress%")
                    .setProgress(100, progress, false)
            }
        }

        return builder
    }

    override fun onProgress(progress: Int) {
        notificationManager.notify(NOTIFICATION_ID, createNotification(progress).build())
    }

    override fun onSuccess() {
        notificationManager.notify(NOTIFICATION_ID, createNotification(100).build())
    }

    override fun onFailure() {
        notificationManager.notify(NOTIFICATION_ID, createNotification(-1).build())
    }
}






