package com.ahmedkhalifa.motionmix.domain.repo.chat

import android.net.Uri
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.Conversation
import com.ahmedkhalifa.motionmix.data.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(message: Message): Resource<Unit>
    fun getMessages(conversationId: String): Flow<Resource<List<Message>>>
    fun getConversations(userId: String): Flow<Resource<List<Conversation>>>
    suspend fun markMessagesAsRead(conversationId: String, userId: String): Resource<Unit>
    suspend fun createConversation(participants: List<String>): Resource<Conversation>
    suspend fun uploadMedia(uri: Uri, messageId: String): Resource<String>
    suspend fun sendMediaMessage(uri: Uri, message: Message): Resource<Unit>
}