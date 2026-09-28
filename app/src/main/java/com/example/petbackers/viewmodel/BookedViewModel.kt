package com.example.petbackers.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.petbackers.model.Booked
import com.example.petbackers.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class BookedViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // State flows
    private val _bookedList = MutableStateFlow<List<Booked>>(emptyList())
    private val _currentBooked = MutableStateFlow<Booked?>(null)
    private val _loadingState = MutableStateFlow(false)
    private val _errorState = MutableStateFlow<String?>(null)

    // Public exposed flows
    val bookedList: StateFlow<List<Booked>> = _bookedList
    val currentBooked: StateFlow<Booked?> = _currentBooked
    val loadingState: StateFlow<Boolean> = _loadingState
    val errorState: StateFlow<String?> = _errorState

    private var firebaseListener: ValueEventListener? = null

    init {
        // Listen for auth state changes
        auth.addAuthStateListener { firebaseUser ->
            if (firebaseUser != null) {
                fetchBookedHistory()
            } else {
                _bookedList.value = emptyList()
                _currentBooked.value = null
            }
        }
    }

    fun fetchBookedById(bookedId: String) {
        val currentUser = auth.currentUser ?: run {
            _errorState.value = "User not authenticated"
            return
        }

        _loadingState.value = true
        database.child("users")
            .child(currentUser.uid)
            .child("booked")
            .child(bookedId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _loadingState.value = false
                    try {
                        val booked = snapshot.getValue(Booked::class.java)
                        _currentBooked.value = booked?.copy(bookedId = bookedId)
                    } catch (e: Exception) {
                        _errorState.value = "Error parsing booked data"
                        Log.e("BookedViewModel", "Error parsing booked data", e)
                        _currentBooked.value = null
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _loadingState.value = false
                    _errorState.value = "Database error: ${error.message}"
                    Log.e("BookedViewModel", "Database error: ${error.message}")
                    _currentBooked.value = null
                }
            })
    }

    fun fetchBookedHistory() {
        val currentUser = auth.currentUser ?: run {
            _errorState.value = "User not authenticated"
            _bookedList.value = emptyList()
            return
        }

        // Clear previous listener
        firebaseListener?.let {
            database.child("users").child(currentUser.uid).child("booked").removeEventListener(it)
        }

        _loadingState.value = true
        firebaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _loadingState.value = false
                val bookedItems = mutableListOf<Booked>()

                for (childSnapshot in snapshot.children) {
                    try {
                        val bookedId = childSnapshot.key ?: continue
                        val bankonline = childSnapshot.child("bankonline").getValue(String::class.java) ?: ""
                        val grandTotal = childSnapshot.child("grandTotal").getValue(Float::class.java) ?: 0f
                        val timestamp = childSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                        // Parse bookingsData
                        val bookingsData = mutableListOf<Booking>()
                        val bookingsDataSnapshot = childSnapshot.child("bookingsData")
                        for (bookingSnapshot in bookingsDataSnapshot.children) {
                            bookingSnapshot.getValue(Booking::class.java)?.let {
                                bookingsData.add(it)
                            }
                        }
                        // Parse bookingIds
                        val bookingIds = mutableListOf<String>()
                        val bookingIdsSnapshot = childSnapshot.child("bookingIds")
                        for (idSnapshot in bookingIdsSnapshot.children) {
                            idSnapshot.getValue(String::class.java)?.let {
                                bookingIds.add(it)
                            }
                        }

                        bookedItems.add(Booked(
                            bookedId = bookedId,
                            bookingIds = bookingIds,
                            bookingsData = bookingsData,
                            grandTotal = grandTotal,
                            bankonline = bankonline,
                            timestamp = timestamp
                        ))
                    } catch (e: Exception) {
                        Log.e("BookedViewModel", "Error parsing booking ${childSnapshot.key}", e)
                    }
                }

                _bookedList.value = bookedItems.sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                _loadingState.value = false
                _errorState.value = "Database error: ${error.message}"
                Log.e("BookedViewModel", "Database error: ${error.message}")
                _bookedList.value = emptyList()
            }
        }

        database.child("users").child(currentUser.uid).child("booked")
            .addValueEventListener(firebaseListener!!)
    }

    fun confirmAndMoveBookings(
        bookingIds: List<String>,
        grandTotal: Float,
        bankonline: String,
        onComplete: (String?, Boolean) -> Unit
    ) {
        val currentUser = auth.currentUser ?: run {
            onComplete(null, false)
            _errorState.value = "User not authenticated"
            return
        }

        _loadingState.value = true
        val bookedId = UUID.randomUUID().toString()
        val bookingsRef = database.child("users").child(currentUser.uid).child("bookings")
        val bookedRef = database.child("users").child(currentUser.uid).child("booked")

        // First, fetch all the booking data to be moved
        val bookingDataTasks = bookingIds.map { bookingId ->
            bookingsRef.child(bookingId).get()
        }

        Tasks.whenAllSuccess<DataSnapshot>(bookingDataTasks).addOnSuccessListener { snapshots ->
            val bookingsData = snapshots.mapNotNull { it.getValue(Booking::class.java) }

            // Create the booked entry
            val booked = Booked(
                bookedId = bookedId,
                bookingIds = bookingIds,
                bookingsData = bookingsData,
                grandTotal = grandTotal,
                bankonline = bankonline,
                timestamp = System.currentTimeMillis()
            )

            // Save to booked node
            bookedRef.child(bookedId).setValue(booked)
                .addOnSuccessListener {
                    // After successful save, delete original bookings
                    val deleteTasks = bookingIds.map { bookingId ->
                        bookingsRef.child(bookingId).removeValue()
                    }

                    Tasks.whenAllComplete(deleteTasks).addOnCompleteListener { task ->
                        _loadingState.value = false
                        onComplete(bookedId, !task.isCanceled && task.isSuccessful)
                    }
                }
                .addOnFailureListener { e ->
                    _loadingState.value = false
                    _errorState.value = "Failed to save booking: ${e.message}"
                    onComplete(null, false)
                }
        }.addOnFailureListener { e ->
            _loadingState.value = false
            _errorState.value = "Failed to fetch bookings: ${e.message}"
            onComplete(null, false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseListener?.let {
            auth.currentUser?.uid?.let { uid ->
                database.child("users").child(uid).child("booked").removeEventListener(it)
            }
        }
    }
}