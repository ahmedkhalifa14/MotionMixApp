package com.ahmedkhalifa.motionmix.common.utils

import com.ahmedkhalifa.motionmix.VideoUploadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadEventHandler @Inject constructor() {
    private val _uploadEvents = MutableSharedFlow<UploadEvent>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uploadEvents: SharedFlow<UploadEvent> = _uploadEvents

    init {
        CoroutineScope(Dispatchers.Default).launch {
            VideoUploadService.uploadEvents.collect { event ->
                _uploadEvents.emit(event)
            }
        }
    }
}
