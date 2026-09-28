package com.example.petbackers.model

data class ServiceDay(
    val date: String = "",
    val services: Map<String, ServiceCapacity> = emptyMap()
)

data class ServiceCapacity(
    val catsTotal: Int = 0,
    val bookedIds: List<String> = emptyList()
) 