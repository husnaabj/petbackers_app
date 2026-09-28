package com.example.petbackers.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ListenerRegistration
import com.example.petbackers.model.UserData

class AuthViewModel :ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _userData = MutableLiveData<UserData?>()
    val userData: LiveData<UserData?> = _userData

    // Add a list to track active listeners
    private val activeListeners = mutableListOf<Any>() // Can be ListenerRegistration for Firestore


    init {
        checkAuthState()
    }

    fun checkAuthState() {
        //val currentUser = auth.currentUser
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
            //fetchUserData()
        }
    }


    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                    fetchUserData()
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signup(
        email: String,
        password: String, /*username: String, phonenumb: String*/
        username: String,
        phonenumb: String,
        role: String = "user"
        ) {
        if (email.isEmpty() || password.isEmpty() /*|| username.isEmpty() || phonenumb.isEmpty()*/) {
            _authState.value = AuthState.Error("All fields are required")
       }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.reload()?.addOnSuccessListener {
                        val userId = firebaseUser.uid
                        saveUserData(userId, email, username, phonenumb, role)
                        _authState.value = AuthState.Authenticated
                    }?.addOnFailureListener {
                        _authState.value = AuthState.Error("Failed to reload user")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    private fun saveUserData(userId: String, email: String, username: String, phonenumb: String, role: String) {
        val userMap = mapOf(
            "username" to username,
            "email" to email,
            "phone" to phonenumb, // Save phone number
            "role" to role
        )
        // Save to Realtime Database (for legacy/other purposes)
        database.getReference("users").child(userId).setValue(userMap)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _authState.value = AuthState.Error("Failed to save user data")
                }
            }

        // 🔥 Save to Firestore (for security rules)
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .set(userMap)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _authState.value = AuthState.Error("Failed to save user data to Firestore")
                }
            }
    }

    
    private fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return
        database.getReference("users").child(userId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val data = task.result?.getValue(UserData::class.java)
                    _userData.value = data
                } else {
                    _userData.value = null
                }
            }
    }

    fun signout() {
        _authState.value = AuthState.Loading
        try {
            // 1. Clear all active listeners first
            clearAllListeners()

            // 2. Perform sign out
            auth.signOut()

            // 3. Update UI state
            _authState.value = AuthState.Unauthenticated
            _userData.value = null
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Sign out failed: ${e.message}")
            // Even if sign out failed, we should consider the user as logged out
            _authState.value = AuthState.Unauthenticated
            _userData.value = null
        }
    }

    private fun clearAllListeners() {
        // Clear Firebase Realtime Database listeners
        activeListeners.forEach {
            when (it) {
                is ListenerRegistration -> it.remove() // For Firestore
                is ValueEventListener -> {
                    // Remove from Realtime Database
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        FirebaseDatabase.getInstance().getReference("users")
                            .child(userId)
                            .removeEventListener(it as ValueEventListener)
                    }
                }
            }
        }
        activeListeners.clear()
    }

    // Method to register listeners for cleanup
    fun registerListener(listener: Any) {
        activeListeners.add(listener)
    }

    // Clean up when ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        clearAllListeners()
    }

    fun getCurrentUser() = FirebaseAuth.getInstance()

}

sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}