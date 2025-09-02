package com.ahmedkhalifa.motionmix.data.remote_data_source.chat

import com.ahmedkhalifa.motionmix.data.model.Conversation
import com.ahmedkhalifa.motionmix.data.model.Message
import com.ahmedkhalifa.motionmix.data.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatFireStoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatFireStoreInterface {
    override suspend fun sendMessage(message: Message) {
        firestore.collection("message")
            .document(message.conversationId)
            .collection("message")
            .document(message.id)
            .set(message)
            .await()
    }

    override fun getMessagesRealtime(conversationId: String): Flow<List<Message>> {
        return callbackFlow {
            val listener = firestore
                .collection("message")
                .document(conversationId)
                .collection("message")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val message = snapshot?.toObjects(Message::class.java) ?: emptyList()
                    trySend(message)

                }
            awaitClose { (listener.remove()) }

        }.flowOn(Dispatchers.IO)
    }

    override fun getConversationsRealtime(userId: String): Flow<List<Conversation>> {
        return callbackFlow {
            val listener = firestore
                .collection("conversations")
                .whereArrayContains("participants", userId)
                .orderBy(
                    "lastMessageTime",
                    com.google.firebase.firestore.Query.Direction.DESCENDING
                )
                .addSnapshotListener { snapshot,error ->
                    if (error!=null){
                        close(error)
                        return@addSnapshotListener
                    }
                    val conversations = snapshot?.toObjects(Conversation::class.java)?:emptyList()
                    trySend(conversations)
                }
            awaitClose{listener.remove()}
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun markMessagesAsRead(conversationId: String, userId: String) {
        firestore.collection("conversations")
            .document(conversationId)
            .update("unreadCount.$userId",0)
            .await()
    }

    override suspend fun createConversation(conversation: Conversation) {
        firestore.collection("conversations")
            .document(conversation.id)
            .set(conversation)
            .await()
    }

    override suspend fun updateConversation(message: Message) {
        val conversationsRef = firestore.collection("conversations")
            .document(message.conversationId)
        firestore.runBatch { batch ->
            batch.update(conversationsRef, mapOf(
                "lastMessage" to message.content,
                "lastMessageTime" to message.timestamp,
                "unreadCount.${message.receiverId}" to FieldValue.increment(1)
            ))

        }.await()
    }

    override suspend fun getUserInfo(userId: String): User? {
        return firestore.collection("Users")
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)
    }


}