package com.example.petbackers.model

//booking type of service
data class Booking(
    val bookingId: String = "",
    val serviceType: String = "", // "Basic Grooming" or "Lions Cut"
    val date: String = "", // Format: "YYYY-MM-DD"
    val checkInDate: String = " ",
    val checkOutDate: String = " ",
    val quantity: Int = 0,
    val numberOfNights : Int = 0,
    val totalPrice: Float = 0.0f,
    val selectedCats: Map<String, CatStatus> = emptyMap(), // Changed here
    val addOns: Map<String, Boolean>? = null, // null for Lions Cut
    //val roomAssignments: Map<String, String> = emptyMap(), // catId to roomNumber mapping for boarding
    val timestamp: Long = System.currentTimeMillis() // Optional for sorting
)

data class CatStatus(
    val isSelected: Boolean = true,
    val statusProcess: String? = null, // "Ongoing" or "Done"
    //val assignedRoom: String? = null // Room number for boarding (e.g., "Room1", "Room2")
)

