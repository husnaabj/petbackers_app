package com.example.petbackers.model

data class Room(
    val roomId: String = "",
    val roomNumber: String = "", // "Room1", "Room2", etc.
    val isOccupied: Boolean = false,
    val currentBookingId: String? = null,
    val currentCatId: String? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null
)

data class RoomAssignment(
    val assignmentId: String = "",
    val roomId: String = "",
    val roomNumber: String = "",
    val bookingId: String = "",
    val catId: String = "",
    val catName: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val numberOfNights: Int = 0,
    val isActive: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class RoomAvailability(
    val totalRooms: Int = 30,
    val occupiedRooms: Int = 0,
    val availableRooms: Int = 30,
    val roomAssignments: Map<String, RoomAssignment> = emptyMap()
) 