package com.example.petbackers.viewmodel

import android.util.Log
import com.google.firebase.database.*
import com.example.petbackers.model.ServiceCapacity

class ServiceDayManager {
    private val database = FirebaseDatabase.getInstance().reference
    private val serviceDayRef = database.child("ServiceDay")
    
    companion object {
        private const val TAG = "ServiceDayManager"
        private const val MAX_CAPACITY = 10 // Maximum cats per day for grooming services
    }
    
    /**
     * Check availability for a specific date and service type
     * @param date Date in format "YYYY-MM-DD"
     * @param serviceType "Basic Grooming" or "Lion Cut"
     * @param catsCount Number of cats to book
     * @param onResult Callback with (isAvailable, availableSlots)
     */
    fun checkAvailability(
        date: String, 
        serviceType: String, 
        catsCount: Int, 
        onResult: (Boolean, Int) -> Unit
    ) {
        Log.d(TAG, "Checking availability for $serviceType on $date for $catsCount cats")
        
        serviceDayRef.child(date).child(serviceType).get()
            .addOnSuccessListener { snapshot ->
                val currentTotal = snapshot.child("catsTotal").getValue(Int::class.java) ?: 0
                val availableSlots = MAX_CAPACITY - currentTotal
                
                Log.d(TAG, "Current total: $currentTotal, Available slots: $availableSlots")
                
                if (catsCount <= availableSlots) {
                    onResult(true, availableSlots) // Available
                } else {
                    onResult(false, availableSlots) // Not available
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error checking availability", exception)
                onResult(false, 0) // Error, assume not available
            }
    }
    
    /**
     * Update ServiceDay when a new booking is made
     * @param date Date in format "YYYY-MM-DD"
     * @param serviceType "Basic Grooming" or "Lion Cut"
     * @param bookingId The booking ID to add
     * @param catsCount Number of cats in the booking
     * @param onComplete Callback with success status
     */
    fun updateServiceDay(
        date: String, 
        serviceType: String, 
        bookingId: String, 
        catsCount: Int, 
        onComplete: (Boolean) -> Unit
    ) {
        Log.d(TAG, "Updating ServiceDay for $serviceType on $date with booking $bookingId for $catsCount cats")
        
        val serviceRef = serviceDayRef.child(date).child(serviceType)
        
        serviceRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentTotal = currentData.child("catsTotal").getValue(Int::class.java) ?: 0
                val currentBookedIds = currentData.child("bookedIds").children
                    .mapNotNull { it.getValue(String::class.java) }
                    .toMutableList()
                
                // Check if we can still accommodate this booking
                if (currentTotal + catsCount > MAX_CAPACITY) {
                    Log.w(TAG, "Cannot accommodate booking: current=$currentTotal, adding=$catsCount, max=$MAX_CAPACITY")
                    return Transaction.abort()
                }
                
                // Update totals
                currentData.child("catsTotal").value = currentTotal + catsCount
                currentBookedIds.add(bookingId)
                currentData.child("bookedIds").setValue(currentBookedIds)
                
                Log.d(TAG, "Updated ServiceDay: total=${currentTotal + catsCount}, bookings=${currentBookedIds.size}")
                return Transaction.success(currentData)
            }
            
            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed", error.toException())
                    onComplete(false)
                } else if (committed) {
                    Log.d(TAG, "ServiceDay updated successfully")
                    onComplete(true)
                } else {
                    Log.w(TAG, "Transaction aborted - capacity exceeded")
                    onComplete(false)
                }
            }
        })
    }
    
    /**
     * Remove booking from ServiceDay when cancelled/deleted
     * @param date Date in format "YYYY-MM-DD"
     * @param serviceType "Basic Grooming" or "Lion Cut"
     * @param bookingId The booking ID to remove
     * @param catsCount Number of cats in the booking
     * @param onComplete Callback with success status
     */
    fun removeFromServiceDay(
        date: String, 
        serviceType: String, 
        bookingId: String, 
        catsCount: Int, 
        onComplete: (Boolean) -> Unit
    ) {
        Log.d(TAG, "Removing booking $bookingId from ServiceDay for $serviceType on $date")
        
        val serviceRef = serviceDayRef.child(date).child(serviceType)
        
        serviceRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentTotal = currentData.child("catsTotal").getValue(Int::class.java) ?: 0
                val currentBookedIds = currentData.child("bookedIds").children
                    .mapNotNull { it.getValue(String::class.java) }
                    .toMutableList()
                
                // Remove the booking ID
                if (currentBookedIds.remove(bookingId)) {
                    val newTotal = maxOf(0, currentTotal - catsCount)
                    currentData.child("catsTotal").value = newTotal
                    currentData.child("bookedIds").setValue(currentBookedIds)
                    Log.d(TAG, "Removed booking: new total=$newTotal")
                    return Transaction.success(currentData)
                } else {
                    Log.w(TAG, "Booking ID $bookingId not found in ServiceDay")
                    return Transaction.abort()
                }
            }
            
            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed", error.toException())
                    onComplete(false)
                } else if (committed) {
                    Log.d(TAG, "Booking removed from ServiceDay successfully")
                    onComplete(true)
                } else {
                    Log.w(TAG, "Transaction aborted - booking not found")
                    onComplete(false)
                }
            }
        })
    }
    
    /**
     * Get current capacity for a specific date and service
     * @param date Date in format "YYYY-MM-DD"
     * @param serviceType "Basic Grooming" or "Lion Cut"
     * @param onResult Callback with (currentTotal, availableSlots)
     */
    fun getCurrentCapacity(
        date: String, 
        serviceType: String, 
        onResult: (Int, Int) -> Unit
    ) {
        serviceDayRef.child(date).child(serviceType).child("catsTotal").get()
            .addOnSuccessListener { snapshot ->
                val currentTotal = snapshot.getValue(Int::class.java) ?: 0
                val availableSlots = MAX_CAPACITY - currentTotal
                onResult(currentTotal, availableSlots)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting current capacity", exception)
                onResult(0, MAX_CAPACITY) // Assume empty if error
            }
    }
} 