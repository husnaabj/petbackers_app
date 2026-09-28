package com.example.petbackers.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.petbackers.model.AddOn
import com.example.petbackers.model.Booking
import com.example.petbackers.model.Cat
import com.example.petbackers.model.CatStatus
import com.example.petbackers.model.RoomAssignment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BookingDetailsViewModel : ViewModel() {
    // Change these to use the new _bookings list
   // val _bookings = mutableStateOf<List<Booking>>(emptyList())
    //val bookings: MutableState<List<Booking>> = _bookings

    val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings

    // ServiceDay management for grooming services
    private val serviceDayManager = ServiceDayManager()

    // UI State
    private val _uiState = mutableStateOf(BookingUiState())
    val uiState: MutableState<BookingUiState> = _uiState

    // Modify your save functions to add to the bookings list
    private fun addBooking(booking: Booking) {
        _bookings.value = _bookings.value + booking
    }

    private var userId: String = ""

    fun setUserId(id: String) {
        userId = id
    }

    // Check availability for grooming services
    fun checkGroomingAvailability(
        date: String,
        serviceType: String,
        catsCount: Int,
        onResult: (Boolean, Int) -> Unit
    ) {
        serviceDayManager.checkAvailability(date, serviceType, catsCount, onResult)
    }

    // Check availability for lion cut services
    fun checkLioncutAvailability(
        date: String,
        catsCount: Int,
        onResult: (Boolean, Int) -> Unit
    ) {
        serviceDayManager.checkAvailability(date, "Lion Cut", catsCount, onResult)
    }

    fun saveBasicGroomingBooking(
        userId: String,
        cats: List<Cat>,
        addOns: List<AddOn>,
        date: String,
        totalPrice: Float,
        onComplete: (Boolean) -> Unit
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val dbRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("bookings")

        val bookingId = dbRef.push().key ?: run {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Failed to generate booking ID"
            )
            onComplete(false)
            return
        }

        val booking = Booking(
            bookingId = bookingId,
            serviceType = "Basic Grooming",
            date = date,
            quantity = cats.size,
            totalPrice = totalPrice,
            selectedCats = cats.associate {
                it.id to CatStatus(isSelected = true, statusProcess = "Ongoing")
            },
            addOns = addOns.associate { it.name to true }
        )

        // First, update ServiceDay to reserve the slots
        serviceDayManager.updateServiceDay(date, "Basic Grooming", bookingId, cats.size) { serviceDaySuccess ->
            if (serviceDaySuccess) {
                // Then save the booking
                dbRef.child(bookingId).setValue(booking)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            addBooking(booking) // Add to bookings list
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                bookingSuccess = true
                            )
                            onComplete(true)
                        } else {
                            // If booking fails, remove from ServiceDay
                            serviceDayManager.removeFromServiceDay(date, "Basic Grooming", bookingId, cats.size) { _ ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = task.exception?.message ?: "Booking failed"
                                )
                                onComplete(false)
                            }
                        }
                    }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to reserve slots. Please try again."
                )
                onComplete(false)
            }
        }
    }

    fun saveLioncutBooking(
        userId: String,
        cats: List<Cat>,
        date: String,
        totalPrice: Float,
        onComplete: (Boolean) -> Unit
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val dbRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("bookings")

        val bookingId = dbRef.push().key ?: run {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Failed to generate booking ID"
            )
            onComplete(false)
            return
        }

        val booking = Booking(
            bookingId = bookingId,
            serviceType = "Lion Cut",
            date = date,
            quantity = cats.size,
            totalPrice = totalPrice,
            selectedCats = cats.associate {
                it.id to CatStatus(isSelected = true, statusProcess = "Ongoing")
            }
        )

        // First, update ServiceDay to reserve the slots
        serviceDayManager.updateServiceDay(date, "Lion Cut", bookingId, cats.size) { serviceDaySuccess ->
            if (serviceDaySuccess) {
                // Then save the booking
                dbRef.child(bookingId).setValue(booking)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            addBooking(booking) // Add to bookings list
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                bookingSuccess = true
                            )
                            onComplete(true)
                        } else {
                            // If booking fails, remove from ServiceDay
                            serviceDayManager.removeFromServiceDay(date, "Lion Cut", bookingId, cats.size) { _ ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = task.exception?.message ?: "Booking failed"
                                )
                                onComplete(false)
                            }
                        }
                    }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to reserve slots. Please try again."
                )
                onComplete(false)
            }
        }
    }

    fun saveBoardingBooking(
        userId: String,
        cats: List<Cat>,
        totalPrice: Float,
        checkInDate: String,
        checkOutDate : String,
        numberOfNights : Int,
        onComplete: (Boolean) -> Unit
    ) {
        android.util.Log.d("BookingViewModel", "Starting boarding booking for user: $userId")
        android.util.Log.d("BookingViewModel", "Cats: ${cats.map { it.name }}")
        android.util.Log.d("BookingViewModel", "Check-in: $checkInDate, Check-out: $checkOutDate, Nights: $numberOfNights")
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val dbRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("bookings")

        val bookingId = dbRef.push().key ?: run {
            val error = "Failed to generate booking ID"
            android.util.Log.e("BookingViewModel", error)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = error
            )
            onComplete(false)
            return
        }

        android.util.Log.d("BookingViewModel", "Generated booking ID: $bookingId")

        val booking = Booking(
            bookingId = bookingId,
            serviceType = "Boarding",
            checkInDate = checkInDate,
            checkOutDate = checkOutDate,
            numberOfNights = numberOfNights,
            quantity = cats.size,
            totalPrice = totalPrice,
            selectedCats = cats.associate { cat ->
                cat.id to CatStatus(
                    isSelected = true,
                    statusProcess = "Ongoing",
                    //assignedRoom = roomAssignments?.get(cat.id)
                )
            },
            //roomAssignments = roomAssignments ?: emptyMap()
        )

        android.util.Log.d("BookingViewModel", "Saving booking to Firebase...")

        dbRef.child(bookingId).setValue(booking)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("BookingViewModel", "Booking saved successfully")
                    addBooking(booking) // Add to bookings list
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bookingSuccess = true
                    )
                    onComplete(true)
                } else {
                    android.util.Log.e("BookingViewModel", "Booking save failed: ${task.exception?.message}")

                    /*
                    // If booking fails, release the assigned rooms
                    roomManager.releaseRooms(bookingId) { _ ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = task.exception?.message ?: "Booking failed"
                        )
                        onComplete(false)
                    }

                     */
                }
            }

        /*
        //First, assign rooms to cats
        roomManager.assignRoomsToCats(
            cats = cats,
            checkInDate = checkInDate,
            checkOutDate = checkOutDate,
            numberOfNights = numberOfNights,
            bookingId = bookingId
        ) { roomAssignments, error ->
            if (error != null) {
                android.util.Log.e("BookingViewModel", "Room assignment failed: $error")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error
                )
                onComplete(false)
                return@assignRoomsToCats
            }

            android.util.Log.d("BookingViewModel", "Room assignment successful: $roomAssignments")

            val booking = Booking(
                bookingId = bookingId,
                serviceType = "Boarding",
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                numberOfNights = numberOfNights,
                quantity = cats.size,
                totalPrice = totalPrice,
                selectedCats = cats.associate { cat ->
                    cat.id to CatStatus(
                        isSelected = true, 
                        statusProcess = "Ongoing",
                        //assignedRoom = roomAssignments?.get(cat.id)
                    )
                },
                //roomAssignments = roomAssignments ?: emptyMap()
            )

            android.util.Log.d("BookingViewModel", "Saving booking to Firebase...")

            dbRef.child(bookingId).setValue(booking)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        android.util.Log.d("BookingViewModel", "Booking saved successfully")
                        addBooking(booking) // Add to bookings list
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            bookingSuccess = true
                        )
                        onComplete(true)
                    } else {
                        android.util.Log.e("BookingViewModel", "Booking save failed: ${task.exception?.message}")

                        /*
                        // If booking fails, release the assigned rooms
                        roomManager.releaseRooms(bookingId) { _ ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = task.exception?.message ?: "Booking failed"
                            )
                            onComplete(false)
                        }

                         */
                    }
                }
        }*/
    }

    private var bookingsListener: ValueEventListener? = null

    fun fetchBookings(userId: String) {
        this.userId = userId // Store userId for reuse

        val dbRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("bookings")

        // Remove any existing listener to avoid duplicates
        bookingsListener?.let {
            dbRef.removeEventListener(it)
        }

        bookingsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookingList = mutableListOf<Booking>()
                for (bookingSnapshot in snapshot.children) {
                    val booking = bookingSnapshot.getValue(Booking::class.java)
                    booking?.let { bookingList.add(it) }
                }
                _bookings.value = bookingList
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.value = _uiState.value.copy(errorMessage = error.message)
            }
        }

        dbRef.addValueEventListener(bookingsListener as ValueEventListener)
    }
    override fun onCleared() {
        super.onCleared()
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("bookings")

        bookingsListener?.let {
            dbRef.removeEventListener(it)
        }
        
        // Cleanup room manager
        //roomManager.cleanup()
    }



    // In BookingDetailsViewModel
    fun deleteBooking(bookingId: String, catId: String) {
        android.util.Log.d("BookingViewModel", "Deleting cat $catId from booking $bookingId")
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val currentBookings = _bookings.value.toMutableList()
        val bookingToUpdate = currentBookings.firstOrNull { it.bookingId == bookingId }

        if (bookingToUpdate != null) {
            val updatedSelectedCats = bookingToUpdate.selectedCats.toMutableMap().apply {
                remove(catId)
            }

            if (updatedSelectedCats.isEmpty()) {
                deleteEntireBooking(bookingId)
                return
            }

            // Calculate number of nights for boarding service
            val numberOfNights = if (bookingToUpdate.serviceType == "Boarding") {
                bookingToUpdate.numberOfNights
            } else {
                1 // For grooming services, we consider it as 1 "night" equivalent
            }

            val updatedBooking = bookingToUpdate.copy(
                selectedCats = updatedSelectedCats,
                quantity = updatedSelectedCats.size,
                totalPrice = calculateUpdatedPrice(bookingToUpdate, updatedSelectedCats.size) *
                        (if (bookingToUpdate.serviceType == "Boarding") numberOfNights.toFloat() else 1f),
            )

            val dbRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("bookings")
                .child(bookingId)

            dbRef.setValue(updatedBooking)
                .addOnSuccessListener {
                    _bookings.value = currentBookings.map {
                        if (it.bookingId == bookingId) updatedBooking else it
                    }
                    
                    // Update ServiceDay for grooming services (reduce by 1 cat)
                    if (bookingToUpdate.serviceType in listOf("Basic Grooming", "Lion Cut")) {
                        serviceDayManager.removeFromServiceDay(
                            bookingToUpdate.date, 
                            bookingToUpdate.serviceType, 
                            bookingId, 
                            1 // Remove 1 cat
                        ) { serviceDaySuccess ->
                            if (!serviceDaySuccess) {
                                android.util.Log.w("BookingViewModel", "Failed to update ServiceDay for cat removal")
                            }
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(isLoading = false)

                }
                .addOnFailureListener { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to update booking"
                    )
                }
        }
    }

    /*
    private fun releaseSpecificCatRoom(bookingId: String, catId: String) {
        android.util.Log.d("BookingViewModel", "Releasing room for cat $catId from booking $bookingId")
        
        // Find the specific room assignment for this cat
        val database = FirebaseDatabase.getInstance().reference
        val assignmentsRef = database.child("roomAssignments")
        
        // First, try to find by bookingId and catId combination
        assignmentsRef.orderByChild("bookingId").equalTo(bookingId).get()
            .addOnSuccessListener { snapshot ->
                var foundAssignment = false
                for (assignmentSnapshot in snapshot.children) {
                    val assignment = assignmentSnapshot.getValue(RoomAssignment::class.java)
                    if (assignment?.catId == catId) {
                        foundAssignment = true
                        android.util.Log.d("BookingViewModel", "Found room assignment for cat $catId: ${assignment.roomNumber}")
                        
                        // Delete the room assignment completely
                        val deleteAssignment = assignmentsRef.child(assignment.assignmentId).removeValue()
                        
                        // Free up the specific room
                        val roomsRef = database.child("rooms")
                        val freeRoom = roomsRef.child(assignment.roomId).updateChildren(
                            mapOf(
                                "isOccupied" to false,
                                "currentBookingId" to null,
                                "currentCatId" to null,
                                "checkInDate" to null,
                                "checkOutDate" to null
                            )
                        )
                        
                        // Execute both operations
                        com.google.android.gms.tasks.Tasks.whenAll(deleteAssignment, freeRoom)
                            .addOnSuccessListener {
                                android.util.Log.d("BookingViewModel", "Successfully deleted room assignment and freed room ${assignment.roomNumber} for cat $catId")
                            }
                            .addOnFailureListener { exception ->
                                android.util.Log.e("BookingViewModel", "Failed to release room for cat $catId", exception)
                                _uiState.value = _uiState.value.copy(
                                    errorMessage = "Booking updated but failed to release room for ${assignment.catName}"
                                )
                            }
                        break
                    }
                }
                
                if (!foundAssignment) {
                    android.util.Log.w("BookingViewModel", "No room assignment found for cat $catId in booking $bookingId")
                }
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("BookingViewModel", "Failed to find room assignment for cat $catId", exception)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to find room assignment for the deleted cat"
                )
            }
    }
    */

    private fun deleteEntireBooking(bookingId: String) {
        android.util.Log.d("BookingViewModel", "Deleting entire booking: $bookingId")
        
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("bookings")
            .child(bookingId)

        // First, get the booking to check if it's a boarding booking
        dbRef.get().addOnSuccessListener { snapshot ->
            val booking = snapshot.getValue(Booking::class.java)
            
            android.util.Log.d("BookingViewModel", "Found booking: ${booking?.serviceType}")
            
            dbRef.removeValue()
                .addOnSuccessListener {
                    android.util.Log.d("BookingViewModel", "Booking deleted successfully")
                    // Update local state
                    _bookings.value = _bookings.value.filter { it.bookingId != bookingId }
                    
                    // Update ServiceDay for grooming services
                    if (booking?.serviceType in listOf("Basic Grooming", "Lion Cut")) {
                        if (booking != null) {
                            serviceDayManager.removeFromServiceDay(
                                booking.date,
                                booking.serviceType,
                                bookingId,
                                booking.quantity
                            ) { serviceDaySuccess ->
                                if (!serviceDaySuccess) {
                                    android.util.Log.w("BookingViewModel", "Failed to update ServiceDay for booking deletion")
                                }
                            }
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(isLoading = false)

                    /*
                    // If it's a boarding booking, release all rooms
                    if (booking?.serviceType == "Boarding") {
                        android.util.Log.d("BookingViewModel", "Releasing all rooms for boarding booking")
                        releaseAllRoomsForBooking(bookingId)
                    }*/
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("BookingViewModel", "Failed to delete booking", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to delete booking"
                    )
                }
        }.addOnFailureListener { e ->
            android.util.Log.e("BookingViewModel", "Failed to get booking for deletion", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Failed to get booking details"
            )
        }
    }

    /*
    private fun releaseAllRoomsForBooking(bookingId: String) {
        android.util.Log.d("BookingViewModel", "Releasing all rooms for booking: $bookingId")
        
        val database = FirebaseDatabase.getInstance().reference
        val assignmentsRef = database.child("roomAssignments")
        
        assignmentsRef.orderByChild("bookingId").equalTo(bookingId).get()
            .addOnSuccessListener { snapshot ->
                val deleteTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()
                val roomUpdateTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()
                
                android.util.Log.d("BookingViewModel", "Found ${snapshot.childrenCount} room assignments to delete")
                
                for (assignmentSnapshot in snapshot.children) {
                    val assignment = assignmentSnapshot.getValue(RoomAssignment::class.java)
                    if (assignment != null) {
                        android.util.Log.d("BookingViewModel", "Deleting room assignment: ${assignment.roomNumber} for cat ${assignment.catName}")
                        
                        // Delete the room assignment
                        val deleteAssignment = assignmentsRef.child(assignment.assignmentId).removeValue()
                        deleteTasks.add(deleteAssignment)
                        
                        // Free up the room
                        val roomsRef = database.child("rooms")
                        val freeRoom = roomsRef.child(assignment.roomId).updateChildren(
                            mapOf(
                                "isOccupied" to false,
                                "currentBookingId" to null,
                                "currentCatId" to null,
                                "checkInDate" to null,
                                "checkOutDate" to null
                            )
                        )
                        roomUpdateTasks.add(freeRoom)
                    }
                }
                
                if (deleteTasks.isEmpty()) {
                    android.util.Log.d("BookingViewModel", "No room assignments found to delete")
                    return@addOnSuccessListener
                }
                
                // Execute all delete and room update operations
                val allTasks = deleteTasks + roomUpdateTasks
                com.google.android.gms.tasks.Tasks.whenAll(allTasks)
                    .addOnSuccessListener {
                        android.util.Log.d("BookingViewModel", "Successfully deleted all room assignments and freed all rooms for booking $bookingId")
                    }
                    .addOnFailureListener { exception ->
                        android.util.Log.e("BookingViewModel", "Failed to delete some room assignments", exception)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Booking deleted but failed to release some rooms"
                        )
                    }
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("BookingViewModel", "Failed to find room assignments for booking $bookingId", exception)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Booking deleted but failed to find room assignments"
                )
            }
    }
    */

    private fun calculateUpdatedPrice(booking: Booking, newQuantity: Int): Float {
        return when (booking.serviceType) {
            "Basic Grooming" -> 30.0f * newQuantity
            "Lion Cut" -> 50.0f * newQuantity
            "Boarding" -> {
                val basePrice = 20.0f // First cat
                val additionalCatPrice = 10.0f // Each additional cat
                if (newQuantity == 0) 0f
                else basePrice + (newQuantity - 1) * additionalCatPrice
            }
            else -> booking.totalPrice
        }
    }


    data class BookingUiState(
        val isLoading: Boolean = false,
        var errorMessage: String? = null,
        val bookingSuccess: Boolean = false
    )

}