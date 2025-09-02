package com.ahmedkhalifa.motionmix.ui.screens.inbox

import android.net.Uri
import android.view.View
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.common.utils.ChatUiState
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.Message
import com.ahmedkhalifa.motionmix.data.model.MessageType
import com.ahmedkhalifa.motionmix.data.remote_data_source.FirebaseAuthenticationService
import com.ahmedkhalifa.motionmix.domain.repo.chat.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle,
    firebaseAuthenticationService: FirebaseAuthenticationService
): ViewModel(){
    private val conversationId: String = savedStateHandle.get<String>("conversationId") ?: ""
    private val currentUserId = firebaseAuthenticationService.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(ChatUiState(currentUser = currentUserId))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _sendMessageEvent = MutableSharedFlow<Event<Resource<Unit>>>()
    val sendMessageEvent: SharedFlow<Event<Resource<Unit>>> = _sendMessageEvent.asSharedFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getMessages(conversationId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            messages = resource.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                        markMessagesAsRead()
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    fun sendMessage(content: String, type: MessageType = MessageType.TEXT) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _sendMessageEvent.emit(Event(Resource.Loading()))

            val message = Message(
                id = UUID.randomUUID().toString(),
                senderId = currentUserId,
                receiverId = getOtherParticipant(),
                conversationId = conversationId,
                content = content,
                type = type,
                timestamp = System.currentTimeMillis()
            )

            val result = chatRepository.sendMessage(message)
            _sendMessageEvent.emit(Event(result))
        }
    }

    fun sendMediaMessage(uri: Uri, type: MessageType) {
        viewModelScope.launch {
            _sendMessageEvent.emit(Event(Resource.Loading()))

            val message = Message(
                id = UUID.randomUUID().toString(),
                senderId = currentUserId,
                receiverId = getOtherParticipant(),
                conversationId = conversationId,
                content = when (type) {
                    MessageType.IMAGE -> "Photo"
                    MessageType.VIDEO -> "Video"
                    MessageType.FILE -> "File"
                    MessageType.AUDIO -> "Audio"
                    else -> ""
                },
                type = type,
                timestamp = System.currentTimeMillis()
            )

            val result = chatRepository.sendMediaMessage(uri, message)
            _sendMessageEvent.emit(Event(result))
        }
    }

    private suspend fun markMessagesAsRead() {
        chatRepository.markMessagesAsRead(conversationId, currentUserId)
    }

    private fun getOtherParticipant(): String {
        // Get other participant from conversation
        // This should be implemented based on your conversation logic
        return ""
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}