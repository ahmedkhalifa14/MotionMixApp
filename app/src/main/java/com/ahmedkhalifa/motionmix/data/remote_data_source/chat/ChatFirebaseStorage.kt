package com.ahmedkhalifa.motionmix.data.remote_data_source.chat

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatFirebaseStorage @Inject constructor(
    private val firebaseStorage: FirebaseStorage
): ChatMediaInterface
{
    override suspend fun uploadMedia(
        uri: Uri,
        messageId: String
    ): String {
        val ref = firebaseStorage.reference.child("chat_media/$messageId")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun uploadChatMedia(
        uri: Uri,
        conversationId: String,
        messageId: String
    ): String {
        val ref = firebaseStorage.reference.child("chat_media/$conversationId/$messageId")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun deleteMedia(url: String) {
        firebaseStorage.getReferenceFromUrl(url).delete().await()
    }

}
