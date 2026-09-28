package com.example.petbackers.model


data class Booked(
    val bookedId: String = "",
    val bookingIds: List<String> = emptyList(),
    val bookingsData: List<Booking> = emptyList(),
    val grandTotal: Float = 0.0f,
    val bankonline: String = "",
    val timestamp: Long = 0L
)
{
    data class BookingData(
        val serviceType: String = "",
        val selectedCats: String = "",
        val price: Double = 0.0
    )
}


