package com.ahmedkhalifa.motionmix.data.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val conversationId: String = "",
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val mediaUrl: String? = null,
    val replyToMessageId: String? = null
)

data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Map<String, Int> = emptyMap()
)

enum class MessageType {
    TEXT, IMAGE, VIDEO, FILE, AUDIO
}