package com.ahmedkhalifa.motionmix.ui.screens.post_reel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.data.model.UploadStatus
import com.ahmedkhalifa.motionmix.data.repository.VideoUploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val videoUploadRepository: VideoUploadRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uploadStatus = MutableStateFlow(UploadStatus())
    val uploadStatus: StateFlow<UploadStatus> = _uploadStatus

    init {
        viewModelScope.launch {
            videoUploadRepository.uploadEvents.collect { event ->
                _uploadStatus.value = UploadStatus(
                    progress = event.progress,
                    isComplete = event.isComplete,
                    isFailed = event.isFailed,
                    message = event.message
                )
            }
        }
    }

    fun startUpload(videoUri: Uri) {
        videoUploadRepository.startUpload(videoUri)
    }

    fun resetUploadStatus() {
        _uploadStatus.value = UploadStatus()
    }


}