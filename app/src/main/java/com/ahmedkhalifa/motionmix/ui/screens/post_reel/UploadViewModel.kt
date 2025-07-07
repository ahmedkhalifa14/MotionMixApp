package com.ahmedkhalifa.motionmix.ui.screens.post_reel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ahmedkhalifa.motionmix.VideoUploadService
import com.ahmedkhalifa.motionmix.data.model.UploadStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UploadViewModel(application: Application) : AndroidViewModel(application) {


    private val _uploadStatus = MutableStateFlow<UploadStatus>(value = UploadStatus())
    val uploadStatus: StateFlow<UploadStatus> = _uploadStatus

    private val uploadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val progress = intent.getIntExtra(VideoUploadService.EXTRA_PROGRESS, 0)
            val isComplete = intent.getBooleanExtra(VideoUploadService.EXTRA_COMPLETE, false)
            val isFailed = intent.getBooleanExtra(VideoUploadService.EXTRA_FAILED, false)
            val message = intent.getStringExtra(VideoUploadService.EXTRA_MESSAGE) ?: ""
            _uploadStatus.value = UploadStatus(progress, isComplete, isFailed, message)
        }
    }

    init {
        val filter = IntentFilter(VideoUploadService.ACTION_UPLOAD_STATUS)
        LocalBroadcastManager.getInstance(application).registerReceiver(uploadReceiver, filter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startUpload(videoUri: Uri) {
        val serviceIntent = Intent(getApplication(), VideoUploadService::class.java).apply {
            putExtra("videoUri", videoUri.toString())
        }
        getApplication<Application>().startForegroundService(serviceIntent)
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(uploadReceiver)
    }
}