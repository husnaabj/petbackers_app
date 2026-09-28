package com.example.petbackers.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",  // Add this line
    val text: String = "",
    val isUser: Boolean = true,
    val senderId: String = "",
    val timestamp: Timestamp? = null
)