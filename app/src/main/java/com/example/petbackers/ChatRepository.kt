package com.example.petbackers

import com.example.petbackers.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    private var chatListener: ListenerRegistration? = null

    companion object {
        const val ADMIN_ID = "Xnpre2W31RhR7cBu0HoaZIDrIQ33" // Replace with your actual admin ID
    }

    fun getOrCreateChat(): Flow<String> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

        // Check if chat exists
        val chatQuery = firestore.collection("chats")
            .whereArrayContains("users", userId)
            .get()
            .await()

        val chatId = if (chatQuery.isEmpty) {
            // Create new chat
            val newChat = firestore.collection("chats").add(
                mapOf(
                    "users" to listOf(userId, ADMIN_ID),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            newChat.id
        } else {
            chatQuery.documents[0].id
        }

        trySend(chatId)
        close()
    }

    fun getMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            close()
            return@callbackFlow
        }

        chatListener = firestore.collection("chats/$chatId/messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                // 1. Handle Firestore errors
                if (error != null) {
                    // Defensive: handle permission denied gracefully
                    if (error.code.name == "PERMISSION_DENIED") {
                        android.util.Log.w("ChatRepository", "Firestore permission denied after sign-out or auth change.")
                        // Do NOT crash, just close the flow and return
                        close(error)
                        return@addSnapshotListener
                    }
                    // Handle other errors as needed
                    close(error)
                    return@addSnapshotListener
                }

                // 2. Defensive: Check if user is still authenticated
                if (auth.currentUser == null) {
                    // User is signed out, ignore this callback and close the flow
                    close()
                    return@addSnapshotListener
                }

                // 3. Only send messages if authenticated and no error
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose {
            chatListener?.remove()
            chatListener = null
        }
    }
    fun removeChatListener() {
        chatListener?.remove()
        chatListener = null
    }

    suspend fun sendMessage(chatId: String, text: String, isUserMessage: Boolean) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

        firestore.collection("chats/$chatId/messages").add(
            mapOf(
                "text" to text,
                "timestamp" to FieldValue.serverTimestamp(),
                "senderId" to userId,
                "isUser" to isUserMessage
            )
        ).await()
    }

    suspend fun isAdmin(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return database.getReference("users")
            .child(userId)
            .child("role")
            .get()
            .await()
            .getValue(String::class.java) == "admin"
    }
}