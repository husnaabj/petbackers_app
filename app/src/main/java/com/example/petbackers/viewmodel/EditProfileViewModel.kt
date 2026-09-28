package com.example.petbackers.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petbackers.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileViewModel : ViewModel() {
    val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _updateState = MutableLiveData<UpdateState?>()
    val updateState: LiveData<UpdateState?> = _updateState

    fun updateProfile(username: String, phone: String) {
        val userId = auth.currentUser?.uid ?: run {
            _updateState.value = UpdateState.Error("User not authenticated")
            return
        }
        val email = auth.currentUser?.email ?: ""
        val userData = UserData(username, email, phone)

        // Update in Realtime Database (only username and phone)
        val rtdbUpdates = hashMapOf<String, Any>(
            "username" to username,
            "phone" to phone
        )
        // Update in Realtime Database
        database.getReference("users").child(userId).updateChildren(rtdbUpdates)
            .addOnCompleteListener { rtdbTask ->
                if (rtdbTask.isSuccessful) {
                    // Update in Firestore (only username and phone)
                    val fsUpdates = hashMapOf<String, Any>(
                        "username" to username,
                        "phone" to phone
                    )
                    // Update in Firestore
                    firestore.collection("users").document(userId).set(fsUpdates, SetOptions.merge())
                        .addOnCompleteListener { fsTask ->
                            if (fsTask.isSuccessful) {
                                _updateState.value = UpdateState.Success
                            } else {
                                _updateState.value = UpdateState.Error("Failed to update Firestore: ${fsTask.exception?.message}")
                            }
                        }
                } else {
                    _updateState.value = UpdateState.Error("Failed to update RTDB: ${rtdbTask.exception?.message}")
                }
            }
    }

    fun resetUpdateState() {
        _updateState.value = null
    }

    sealed class UpdateState {
        object Success : UpdateState()
        data class Error(val message: String?) : UpdateState()
    }
} 