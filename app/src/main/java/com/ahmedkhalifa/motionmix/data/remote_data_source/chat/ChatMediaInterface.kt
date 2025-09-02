package com.ahmedkhalifa.motionmix.data.remote_data_source.chat

import android.net.Uri

interface ChatMediaInterface {
    suspend fun uploadMedia(uri: Uri, messageId: String): String
    suspend fun uploadChatMedia(uri: Uri, conversationId: String, messageId: String): String
    suspend fun deleteMedia(url: String)
}
