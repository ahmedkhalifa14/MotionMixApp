package com.ahmedkhalifa.motionmix

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import androidx.core.net.toUri

class VideoUploadService : Service() {
    companion object {
        const val ACTION_UPLOAD_STATUS = "com.ahmedkhalifa.motionmix.UPLOAD_STATUS"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_COMPLETE = "complete"
        const val EXTRA_FAILED = "failed"
    }

    private val channelId = "VideoUploadChannel"
    private val notificationId = 1
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        // Initialize notificationManager first
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Then create the notification channel
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val videoUriString = intent?.getStringExtra("videoUri")
        if (videoUriString == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        val videoUri = videoUriString.toUri()
        startForeground(notificationId, createNotification(0))

        uploadVideoToFirebase(videoUri)
        return START_STICKY
    }

    private fun uploadVideoToFirebase(videoUri: Uri) {
        //val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference
            .child("videos/$51/${System.currentTimeMillis()}.mp4")

        val uploadTask = storageRef.putFile(videoUri)
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            sendBroadcast(progress, false, false, "Uploading: $progress%")
            updateNotification(progress)
        }.addOnSuccessListener {
            sendBroadcast(100, true, false, "Upload complete")
            updateNotification(100)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }.addOnFailureListener {
            sendBroadcast(-1, false, true, "Upload failed")
            updateNotification(-1)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun sendBroadcast(progress: Int, isComplete: Boolean, isFailed: Boolean, message: String) {
        val intent = Intent(ACTION_UPLOAD_STATUS).apply {
            putExtra(EXTRA_PROGRESS, progress)
            putExtra(EXTRA_COMPLETE, isComplete)
            putExtra(EXTRA_FAILED, isFailed)
            putExtra(EXTRA_MESSAGE, message)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Video Upload Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(progress: Int): Notification {
        val builder = NotificationCompat.Builder(this, channelId)
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

        return builder.build()
    }

    private fun updateNotification(progress: Int) {
        notificationManager.notify(notificationId, createNotification(progress))
    }

    override fun onBind(intent: Intent?): IBinder? = null
}