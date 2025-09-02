package com.ahmedkhalifa.motionmix.data.remote_data_source.chat

import com.ahmedkhalifa.motionmix.data.model.Conversation
import com.ahmedkhalifa.motionmix.data.model.Message
import com.ahmedkhalifa.motionmix.data.model.User
import kotlinx.coroutines.flow.Flow

interface ChatFireStoreInterface {
    suspend fun sendMessage(message: Message)
    fun getMessagesRealtime(conversationId: String): Flow<List<Message>>
    fun getConversationsRealtime(userId: String): Flow<List<Conversation>>
    suspend fun markMessagesAsRead(conversationId: String, userId: String)
    suspend fun createConversation(conversation: Conversation)
    suspend fun updateConversation(message: Message)
    suspend fun getUserInfo(userId: String): User?
}