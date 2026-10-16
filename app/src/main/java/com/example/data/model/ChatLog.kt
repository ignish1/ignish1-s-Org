package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_logs")
data class ChatLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val incomingMessage: String,
    val repliedMessage: String,
    val matchedRule: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSimulated: Boolean = false
)
