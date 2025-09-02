package com.ahmedkhalifa.motionmix.data.repository.chat

import android.net.Uri
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.common.utils.Utils.tryCatch
import com.ahmedkhalifa.motionmix.data.model.Conversation
import com.ahmedkhalifa.motionmix.data.model.Message
import com.ahmedkhalifa.motionmix.data.remote_data_source.FirebaseAuthenticationService
import com.ahmedkhalifa.motionmix.data.remote_data_source.chat.ChatFireStoreInterface
import com.ahmedkhalifa.motionmix.data.remote_data_source.chat.ChatMediaInterface
import com.ahmedkhalifa.motionmix.domain.repo.chat.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatFireStoreInterface: ChatFireStoreInterface,
    private val chatMediaInterface: ChatMediaInterface,
    private val firebaseAuthenticationService: FirebaseAuthenticationService
) : ChatRepository {
    override suspend fun sendMessage(message: Message): Resource<Unit> =
        withContext(Dispatchers.IO) {
            tryCatch {
                chatFireStoreInterface.sendMessage(message)
                chatFireStoreInterface.updateConversation(message)
                Resource.Success(Unit)
            }
        }


    override fun getMessages(conversationId: String): Flow<Resource<List<Message>>> {
        return chatFireStoreInterface.getMessagesRealtime(conversationId)
            .map<List<Message>, Resource<List<Message>>> { messages ->
                Resource.Success(messages)
            }.catch { exp ->
                emit(Resource.Error(exp.message?:"Failed to load messages"))
            }.onStart {
                emit(Resource.Loading())
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getConversations(userId: String): Flow<Resource<List<Conversation>>> {
        return chatFireStoreInterface.getConversationsRealtime(userId)
            .map<List<Conversation>, Resource<List<Conversation>>> { conversations ->
                Resource.Success(conversations)
            }
            .catch { exception ->
                emit(Resource.Error(exception.message ?: "Failed to load conversations"))
            }
            .onStart {
                emit(Resource.Loading())
            }
            .flowOn(Dispatchers.IO)
    }


    override suspend fun markMessagesAsRead(
        conversationId: String,
        userId: String
    ): Resource<Unit> =
        withContext(Dispatchers.IO) {
            tryCatch {
                chatFireStoreInterface.markMessagesAsRead(conversationId, userId)
                Resource.Success(Unit)
            }
        }
    override suspend fun createConversation(participants: List<String>): Resource<Conversation> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val conversationId = UUID.randomUUID().toString()
                val conversation = Conversation(
                    id = conversationId,
                    participants = participants,
                    lastMessageTime = System.currentTimeMillis(),
                    unreadCount = participants.associateWith { 0 }
                )

                chatFireStoreInterface.createConversation(conversation)
                Resource.Success(conversation)
            }
        }
    override suspend fun uploadMedia(
        uri: Uri,
        messageId: String
    ): Resource<String> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val downloadUrl = chatMediaInterface.uploadMedia(uri, messageId)
                Resource.Success(downloadUrl)
            }
        }
    override suspend fun sendMediaMessage(
        uri: Uri,
        message: Message
    ): Resource<Unit> =
        withContext(Dispatchers.IO) {
            tryCatch {
                // Upload media first
                val downloadUrl = chatMediaInterface.uploadMedia(uri, message.id)
                // Create message with media URL
                val messageWithMedia = message.copy(mediaUrl = downloadUrl)
                // Send message
                chatFireStoreInterface.sendMessage(messageWithMedia)
                // Update conversation
                chatFireStoreInterface.updateConversation(messageWithMedia)

                Resource.Success(Unit)
            }
        }
}