package com.ahmedkhalifa.motionmix.ui.screens.post_reel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.common.utils.UploadEvent
import com.ahmedkhalifa.motionmix.domain.usecase.SaveReelUseCase
import com.ahmedkhalifa.motionmix.domain.usecase.UploadVideoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadVideoViewModel @Inject constructor(
    private val uploadVideoUseCase: UploadVideoUseCase,
    private val saveReelUseCase: SaveReelUseCase
) : ViewModel() {

    private val _uploadEvent = MutableStateFlow(UploadEvent(0, false, false, ""))
    val uploadEvent: StateFlow<UploadEvent> = _uploadEvent

    init {
        observeUploadEvents()
    }

    private fun observeUploadEvents() {
        viewModelScope.launch {
            uploadVideoUseCase.repository.uploadEvents.collectLatest { event ->
                _uploadEvent.value = event
            }
        }
    }

    fun uploadVideo(videoUri: Uri) {
        uploadVideoUseCase(videoUri)
    }

    fun resetStatus() {
        _uploadEvent.value = UploadEvent(0, false, false, "")
    }

    fun saveReel(mediaUrl: String, thumbnailUrl: String, description: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = saveReelUseCase(mediaUrl, thumbnailUrl, description)
            onResult(result)
        }
    }
}