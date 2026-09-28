package com.example.petbackers.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petbackers.ChatRepository
import com.example.petbackers.model.ChatMessage
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Event wrapper to handle one-time events (notification)
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
    fun peekContent(): T = content
}

class ChatViewModel(
    val repository: ChatRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _chatId = MutableLiveData<String>()
    val chatId: LiveData<String> = _chatId

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin

    private val _newAdminMessageEvent = MutableLiveData<Event<ChatMessage>>() // Event for new admin message
    val newAdminMessageEvent: LiveData<Event<ChatMessage>> = _newAdminMessageEvent

    private var lastMessageId: String? = null // Track last message to avoid duplicate notifications

    init {
        viewModelScope.launch {
            _chatId.value = repository.getOrCreateChat().first()
            _isAdmin.value = repository.isAdmin()
            loadMessages()
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            _chatId.value?.let { chatId ->
                repository.getMessages(chatId).collect { messages ->
                    // Detect new admin message
                    val adminMessages = messages.filter { it.senderId == ChatRepository.ADMIN_ID }
                    val latestAdminMessage = adminMessages.lastOrNull()
                    if (latestAdminMessage != null && latestAdminMessage.id != lastMessageId) {
                        // Only notify if this is a new admin message
                        lastMessageId = latestAdminMessage.id
                        // Only notify if not sent by current user
                        if (_isAdmin.value == false) {
                            _newAdminMessageEvent.postValue(Event(latestAdminMessage))
                        }
                    }
                    _messages.value = messages
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            _chatId.value?.let { chatId ->
                // Determine if the sender is the user (not admin)
                val isUserMessage = !(_isAdmin.value ?: false)
                repository.sendMessage(chatId, text, isUserMessage)
            }
        }
    }


}