package com.example.petbackers.viewmodel

import androidx.lifecycle.ViewModel
import com.example.petbackers.model.Cat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CatViewModel : ViewModel() {
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val database = FirebaseDatabase.getInstance().reference.child("users").child(userId ?: "").child("cats")

    private val _cats = MutableStateFlow<List<Cat>>(emptyList())
    val cats: StateFlow<List<Cat>> = _cats

    private val _currentCat = MutableStateFlow(Cat())
    val currentCat: StateFlow<Cat> = _currentCat

    init {
        loadCats()
    }

    fun loadCats() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val catList = mutableListOf<Cat>()
                for (child in snapshot.children) {
                    child.getValue(Cat::class.java)?.let { catList.add(it) }
                }
                _cats.value = catList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun setCurrentCat(cat: Cat) {
        _currentCat.value = cat
    }

    fun updateCurrentCat(
        name: String = _currentCat.value.name,
        age: String = _currentCat.value.age,
        gender: String = _currentCat.value.gender,
        color: String = _currentCat.value.color,
        hairlength: String = _currentCat.value.hairlength,
        birthdate: String = _currentCat.value.birthdate,
    ) {
        _currentCat.value = _currentCat.value.copy(
            name = name,
            age = age,
            gender = gender,
            color = color,
            hairlength = hairlength,
            birthdate = birthdate
        )
    }

    fun saveCat() {
        val cat = _currentCat.value
        val catRef = if (cat.id.isEmpty()) {
            database.push()
        } else {
            database.child(cat.id)
        }

        val catToSave = if (cat.id.isEmpty()) cat.copy(id = catRef.key ?: "") else cat
        catRef.setValue(catToSave)

        _currentCat.value = Cat() // Reset after save
    }

    fun deleteCat(cat: Cat) {
        if (cat.id.isNotEmpty()) {
            database.child(cat.id).removeValue()
        }
    }

}