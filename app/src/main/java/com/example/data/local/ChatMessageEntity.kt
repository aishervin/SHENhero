package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sender: String, // "user" or "shen"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "chat" // "chat", "voice", "search", "maps", "agent"
)
