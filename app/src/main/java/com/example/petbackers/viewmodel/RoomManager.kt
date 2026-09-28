package com.example.petbackers.viewmodel

import com.example.petbackers.model.Room
import com.example.petbackers.model.RoomAssignment
import com.example.petbackers.model.RoomAvailability
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

/*
class RoomManager {
    private val database = FirebaseDatabase.getInstance().reference
    private val roomsRef = database.child("rooms")
    private val assignmentsRef = database.child("roomAssignments")
    
    private val _roomAvailability = MutableStateFlow(RoomAvailability())
    val roomAvailability: StateFlow<RoomAvailability> = _roomAvailability
    
    private var roomsListener: ValueEventListener? = null
    private var assignmentsListener: ValueEventListener? = null
    
    init {
        android.util.Log.d("RoomManager", "Initializing RoomManager")
        initializeRooms()
        fetchRoomAvailability()
    }
    
    private fun initializeRooms() {
        android.util.Log.d("RoomManager", "Initializing 30 rooms")
        // Initialize 30 rooms if they don't exist
        for (i in 1..30) {
            val roomId = "room_$i"
            val roomNumber = "Room$i"
            
            roomsRef.child(roomId).get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    android.util.Log.d("RoomManager", "Creating room: $roomNumber")
                    val room = Room(
                        roomId = roomId,
                        roomNumber = roomNumber,
                        isOccupied = false
                    )
                    roomsRef.child(roomId).setValue(room)
                        .addOnSuccessListener {
                            android.util.Log.d("RoomManager", "Room $roomNumber created successfully")
                        }
                        .addOnFailureListener { exception ->
                            android.util.Log.e("RoomManager", "Failed to create room $roomNumber", exception)
                        }
                } else {
                    android.util.Log.d("RoomManager", "Room $roomNumber already exists")
                }
            }
        }
    }
    
    fun fetchRoomAvailability() {
        android.util.Log.d("RoomManager", "Fetching room availability")
        // Remove existing listeners
        roomsListener?.let { roomsRef.removeEventListener(it) }
        assignmentsListener?.let { assignmentsRef.removeEventListener(it) }
        
        // Listen for room changes
        roomsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                android.util.Log.d("RoomManager", "Rooms data changed")
                updateRoomAvailability()
            }
            
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("RoomManager", "Rooms listener cancelled: ${error.message}")
            }
        }
        
        // Listen for assignment changes
        assignmentsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                android.util.Log.d("RoomManager", "Assignments data changed")
                updateRoomAvailability()
            }
            
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("RoomManager", "Assignments listener cancelled: ${error.message}")
            }
        }
        
        roomsRef.addValueEventListener(roomsListener!!)
        assignmentsRef.addValueEventListener(assignmentsListener!!)
        android.util.Log.d("RoomManager", "Room availability listeners added")
    }
    
    private fun updateRoomAvailability() {
        android.util.Log.d("RoomManager", "Updating room availability")
        roomsRef.get().addOnSuccessListener { roomsSnapshot ->
            assignmentsRef.get().addOnSuccessListener { assignmentsSnapshot ->
                val occupiedRooms = mutableListOf<String>()
                val roomAssignments = mutableMapOf<String, RoomAssignment>()
                
                android.util.Log.d("RoomManager", "Found ${assignmentsSnapshot.childrenCount} assignments")
                
                // Process current assignments (all assignments are active since we delete inactive ones)
                for (assignmentSnapshot in assignmentsSnapshot.children) {
                    val assignment = assignmentSnapshot.getValue(RoomAssignment::class.java)
                    assignment?.let {
                        occupiedRooms.add(it.roomId)
                        roomAssignments[it.catId] = it
                        android.util.Log.d("RoomManager", "Active assignment: ${it.catName} in ${it.roomNumber}")
                    }
                }
                
                val availability = RoomAvailability(
                    totalRooms = 30,
                    occupiedRooms = occupiedRooms.size,
                    availableRooms = 30 - occupiedRooms.size,
                    roomAssignments = roomAssignments
                )
                
                android.util.Log.d("RoomManager", "Updated availability: ${availability.availableRooms} available, ${availability.occupiedRooms} occupied")
                _roomAvailability.value = availability
            }.addOnFailureListener { exception ->
                android.util.Log.e("RoomManager", "Failed to get assignments", exception)
            }
        }.addOnFailureListener { exception ->
            android.util.Log.e("RoomManager", "Failed to get rooms", exception)
        }
    }
    
    fun assignRoomsToCats(
        cats: List<com.example.petbackers.model.Cat>,
        checkInDate: String,
        checkOutDate: String,
        numberOfNights: Int,
        bookingId: String,
        onComplete: (Map<String, String>?, String?) -> Unit
    ) {
        android.util.Log.d("RoomManager", "Starting room assignment for ${cats.size} cats")
        
        // Check if we have enough available rooms
        val availableRooms = _roomAvailability.value.availableRooms
        android.util.Log.d("RoomManager", "Available rooms: $availableRooms")
        
        if (cats.size > availableRooms) {
            val error = "Not enough rooms available. Only $availableRooms rooms are free."
            android.util.Log.e("RoomManager", error)
            onComplete(null, error)
            return
        }
        
        // Get available room numbers
        val occupiedRoomIds = _roomAvailability.value.roomAssignments.values.map { it.roomId }.toSet()
        val availableRoomIds = (1..30).map { "room_$it" }.filter { it !in occupiedRoomIds }
        
        android.util.Log.d("RoomManager", "Occupied rooms: ${occupiedRoomIds.size}")
        android.util.Log.d("RoomManager", "Available room IDs: $availableRoomIds")
        
        if (availableRoomIds.size < cats.size) {
            val error = "Not enough rooms available for all cats."
            android.util.Log.e("RoomManager", error)
            onComplete(null, error)
            return
        }
        
        val assignments = mutableMapOf<String, String>() // catId to roomNumber
        val assignmentTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()
        
        cats.forEachIndexed { index, cat ->
            val roomId = availableRoomIds[index]
            val roomNumber = "Room${roomId.substringAfter("room_")}"
            
            assignments[cat.id] = roomNumber
            
            android.util.Log.d("RoomManager", "Assigning ${cat.name} to $roomNumber")
            
            // Create room assignment
            val assignment = RoomAssignment(
                assignmentId = "${bookingId}_${cat.id}",
                roomId = roomId,
                roomNumber = roomNumber,
                bookingId = bookingId,
                catId = cat.id,
                catName = cat.name,
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                numberOfNights = numberOfNights,
                isActive = true
            )
            
            // Update room status
            val roomUpdate = roomsRef.child(roomId).updateChildren(
                mapOf(
                    "isOccupied" to true,
                    "currentBookingId" to bookingId,
                    "currentCatId" to cat.id,
                    "checkInDate" to checkInDate,
                    "checkOutDate" to checkOutDate
                )
            )
            
            // Save assignment
            val assignmentSave = assignmentsRef.child(assignment.assignmentId).setValue(assignment)
            
            assignmentTasks.add(roomUpdate)
            assignmentTasks.add(assignmentSave)
        }
        
        android.util.Log.d("RoomManager", "Executing ${assignmentTasks.size} tasks")
        
        // Wait for all operations to complete
        com.google.android.gms.tasks.Tasks.whenAll(assignmentTasks)
            .addOnSuccessListener {
                android.util.Log.d("RoomManager", "Room assignment successful: $assignments")
                onComplete(assignments, null)
            }
            .addOnFailureListener { exception ->
                val error = "Failed to assign rooms: ${exception.message}"
                android.util.Log.e("RoomManager", error, exception)
                onComplete(null, error)
            }
    }
    
    fun releaseRooms(bookingId: String, onComplete: (Boolean) -> Unit) {
        android.util.Log.d("RoomManager", "Releasing rooms for booking: $bookingId")
        
        assignmentsRef.orderByChild("bookingId").equalTo(bookingId).get()
            .addOnSuccessListener { snapshot ->
                val releaseTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()
                
                android.util.Log.d("RoomManager", "Found ${snapshot.childrenCount} assignments to release")
                
                for (assignmentSnapshot in snapshot.children) {
                    val assignment = assignmentSnapshot.getValue(RoomAssignment::class.java)
                    assignment?.let {
                        android.util.Log.d("RoomManager", "Deleting room assignment: ${it.roomNumber} for cat ${it.catName}")
                        
                        // Delete the room assignment completely
                        val deleteAssignment = assignmentsRef.child(it.assignmentId).removeValue()
                        
                        // Free up the room
                        val freeRoom = roomsRef.child(it.roomId).updateChildren(
                            mapOf(
                                "isOccupied" to false,
                                "currentBookingId" to null,
                                "currentCatId" to null,
                                "checkInDate" to null,
                                "checkOutDate" to null
                            )
                        )
                        
                        releaseTasks.add(deleteAssignment)
                        releaseTasks.add(freeRoom)
                    }
                }
                
                if (releaseTasks.isEmpty()) {
                    android.util.Log.d("RoomManager", "No assignments found to release")
                    onComplete(true)
                } else {
                    android.util.Log.d("RoomManager", "Executing ${releaseTasks.size} release tasks")
                    com.google.android.gms.tasks.Tasks.whenAll(releaseTasks)
                        .addOnSuccessListener { 
                            android.util.Log.d("RoomManager", "Successfully released all rooms for booking $bookingId")
                            onComplete(true) 
                        }
                        .addOnFailureListener { exception ->
                            android.util.Log.e("RoomManager", "Failed to release rooms", exception)
                            onComplete(false) 
                        }
                }
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("RoomManager", "Failed to find assignments for booking $bookingId", exception)
                onComplete(false) 
            }
    }
    
    fun cleanup() {
        roomsListener?.let { roomsRef.removeEventListener(it) }
        assignmentsListener?.let { assignmentsRef.removeEventListener(it) }
    }
} */