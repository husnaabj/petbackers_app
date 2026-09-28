package com.example.petbackers.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.petbackers.model.ChatMessage
import com.example.petbackers.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatPage(
    viewModel: ChatViewModel
) {
    // Ensure chat listener is removed when leaving the page
    DisposableEffect(Unit) {
        onDispose {
            viewModel.repository.removeChatListener()
        }
    }

    val messages by viewModel.messages.observeAsState(initial = emptyList())
    val isAdmin by viewModel.isAdmin.observeAsState(initial = false)
    var text by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        // Header
        Text(
            text = if (isAdmin) "Admin Chat" else "Chatbox",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )

        // Group messages by date
        val groupedMessages = remember(messages) {
            messages.groupBy { message ->
                message.timestamp?.toDate()?.let { date ->
                    Calendar.getInstance().apply {
                        time = date
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                }
            }.toList().reversed() // Reverse to show newest first
        }

        // Message list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = true
        ) {

            groupedMessages.forEach { (date, messagesForDate) ->


                // Add messages for this date
                items(messagesForDate.reversed()) { message ->
                    MessageBubble(message, isAdmin)
                }

                // Add date header
                item {
                    DateHeader((date?.let { formatMessageDate(it) } ?: "Unknown date").toString())
                }
            }
        }

            // Input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            viewModel.sendMessage(text)
                            text = ""
                        }
                    },
                    enabled = text.isNotBlank()
                ) {
                    Text("Send")
                }
            }
        }
    }

private fun formatTimestamp(date: Date): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}

@Composable
fun DateHeader(dateText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
@Composable
fun MessageBubble(message: ChatMessage, isAdmin: Boolean) {
    // Check if the message is from the admin (special ID)
    val isFromAdmin = message.senderId == "Xnpre2W31RhR7cBu0HoaZIDrIQ33"

    // Determine if the message is from the current user
    val isCurrentUser = when {
        isAdmin -> !isFromAdmin  // If current user is admin, their messages are from user
        else -> isFromAdmin       // If current user is not admin, admin messages are from user
    }

    val bubbleColor = if (isCurrentUser)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.Start else Alignment.End
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isCurrentUser) 4.dp else 16.dp,
                        bottomEnd = if (isCurrentUser) 16.dp else 4.dp
                    )
                )
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (isCurrentUser)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        message.timestamp?.let { timestamp ->
            Text(
                text = formatTimestamp(timestamp.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
private fun formatMessageDate(date: Date): String {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    val messageDate = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    return when {
        messageDate.time == today.time -> "Today"
        messageDate.time == today.time - 86400000 -> "Yesterday" // Compare milliseconds
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }
}