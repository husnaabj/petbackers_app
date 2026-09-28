package com.example.petbackers.model

data class Cat(
    val id: String = "",
    val name: String = "",
    val age: String = "",
    val gender: String = "",
    val color: String = "",
    val hairlength: String = "", // This will store either "Short Hair" or "Long Hair"
    val birthdate: String = ""
)