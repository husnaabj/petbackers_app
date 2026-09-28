package com.example.petbackers.pages.bookingProcess

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.petbackers.R
import com.example.petbackers.model.AddOn
import com.example.petbackers.viewmodel.BookingDetailsViewModel
import com.example.petbackers.viewmodel.CatViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun groomingDetailsBook(
    navigateBackToHome: () -> Unit,
    navController: NavHostController,
    catViewModel: CatViewModel = viewModel(),
    bookingViewModel: BookingDetailsViewModel
) {
    var selectedHairLength by remember { mutableStateOf("Short") }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showSlotFullDialog by remember { mutableStateOf(false) }
    var showCatLimitDialog by remember { mutableStateOf(false) }

    // Filter cats based on selected hair length
    val filteredCats = remember(catViewModel.cats.value, selectedHairLength) {
        catViewModel.cats.value.filter { cat ->
            when (selectedHairLength) {
                "Short" -> cat.hairlength == "Short Hair"
                "Long" -> cat.hairlength == "Long Hair"
                else -> true
            }
        }
    }

    var quantity by remember { mutableIntStateOf(1) }

    val today = remember { System.currentTimeMillis() }
    val tomorrow = remember { today + TimeUnit.DAYS.toMillis(1) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = tomorrow,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis > today
            }
        }
    )

    //var selectedCat by remember { mutableStateOf<Cat?>(null) }
    val selectedCatIds = remember { mutableStateListOf<String>() }
    val selectedCats = filteredCats.filter { it.id in selectedCatIds }

    // Add-ons state
    val addOns = remember {
        listOf(
            AddOn("Fungus Treatment", 30.00f),
            AddOn("Degreaser Treatment", 25.00f),
            AddOn("Flea Treatment", 60.00f)
        )
    }
    val selectedAddOns = remember { mutableStateListOf<AddOn>() }

    var totalPrice = 0.0f

    // Calculate total price
    for (cat in selectedCats) {
        val pricePerCat = when (cat.hairlength) {
            "Long Hair" -> 45.00f
            "Short Hair" -> 39.00f
            else -> 39.00f // default price
        }
        totalPrice += pricePerCat
    }

    // Add add-ons prices (applies to all selected cats)
    selectedAddOns.forEach { addOn ->
        totalPrice += addOn.price * selectedCats.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp), // Space for bottom fixed elements
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Back Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp).clickable { navigateBackToHome() }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Home",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Basic Grooming",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Image/Video Placeholder
            Image(
                painter = painterResource(id = R.drawable.grooming),
                contentDescription = "grooming Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .width(310.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Hair Length Selection Box
            Text(
                text = "Hair Length: ",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedHairLength == "Short",
                            onClick = { selectedHairLength = "Short" }
                        )
                        Text(
                            text = "Short Hair (RM39.00)",
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedHairLength == "Long",
                            onClick = { selectedHairLength = "Long" }
                        )
                        Text(
                            text = "Long Hair (RM45.00)",
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date Selection
            Text(
                text = "Date:",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
            OutlinedTextField(
                value = selectedDate?.let { dateFormatter.format(it) } ?: "",
                onValueChange = {},
                label = { Text("Select Date") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pick Date"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.onSecondary)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pick Cat Profile Section
            Text(
                text = "Pick Cat Profile:",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
            // Show message if no cats match the selected hair length
            if (filteredCats.isEmpty()) {
                Text(
                    text = when (selectedHairLength) {
                        "Short" -> "No cats with Short Hair found. Please add a Short Hair cat profile."
                        "Long" -> "No cats with Long Hair found. Please add a Long Hair cat profile."
                        else -> "No cats found. Please add a cat profile."
                    },
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Cat Profile Selection Cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                items(filteredCats) { cat ->
                    val isSelected = cat.id in selectedCatIds
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (isSelected) {
                                    selectedCatIds.remove(cat.id)
                                } else {
                                    if (selectedCatIds.size >= 10) {
                                        showCatLimitDialog = true
                                    } else {
                                        selectedCatIds.add(cat.id)
                                    }
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.onSecondary
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (selectedCatIds.size >= 10) {
                                            showCatLimitDialog = true
                                        } else {
                                            selectedCatIds.add(cat.id)
                                        }
                                    } else {
                                        selectedCatIds.remove(cat.id)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = cat.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${cat.age} • ${cat.gender} • ${cat.color} • ${cat.hairlength}",
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display Quantity
            Text(
                text = "Selected Cats: ${selectedCats.size}",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )

            quantity = selectedCats.size

            Spacer(modifier = Modifier.height(8.dp))

            // Add-ons Section
            Text(
                text = "Add-ons:",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    addOns.forEach { addOn ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (selectedAddOns.contains(addOn)) {
                                        selectedAddOns.remove(addOn)
                                    } else {
                                        selectedAddOns.add(addOn)
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = selectedAddOns.contains(addOn),
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedAddOns.add(addOn)
                                    } else {
                                        selectedAddOns.remove(addOn)
                                    }
                                }
                            )
                            Text(
                                text = "${addOn.name} (RM${"%.2f".format(addOn.price)})",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom fixed section with total price and confirm button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(10.dp)
        ) {
            // Total price
            Text(
                text = "Total: RM${"%.2f".format(totalPrice)}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Booking Button
            Button(
                onClick = {
                    if (selectedCats.isNotEmpty() && selectedDate != null) {
                        // Check availability before showing confirmation dialog
                        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(selectedDate ?: Date())
                        
                        bookingViewModel.checkGroomingAvailability(
                            date = formattedDate,
                            serviceType = "Basic Grooming",
                            catsCount = selectedCats.size
                        ) { isAvailable, availableSlots ->
                            if (isAvailable) {
                                showConfirmationDialog = true
                            } else {
                                // Show slot full dialog
                                showSlotFullDialog = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = selectedDate != null
            ) {
                Text("Confirm Book")
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = Date(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Booking") },
            text = {
                Column {
                    Text("Are you sure you want to book grooming for... ")
                    Spacer(modifier = Modifier.height(8.dp))

                    // List selected cats
                    selectedCats.forEach { cat ->
                        Text("- ${cat.name}", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // List selected add-ons if any
                    if (selectedAddOns.isNotEmpty()) {
                        Text("With add-ons:")
                        selectedAddOns.forEach { addOn ->
                            Text("- ${addOn.name} (RM${"%.2f".format(addOn.price)})")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Show total price
                    Text(
                        text = "Total: RM${"%.2f".format(totalPrice)}",
                        fontWeight = FontWeight.Bold
                    )

                    // Show selected date
                    selectedDate?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Date: ${dateFormatter.format(it)}")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        var userId = Firebase.auth.currentUser?.uid ?: run {
                            bookingViewModel.uiState.value.errorMessage = "User not logged in"
                            return@TextButton
                        }

                        bookingViewModel.saveBasicGroomingBooking(
                            userId = userId,
                            cats = selectedCats,
                            addOns = selectedAddOns,
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(selectedDate ?: Date()),
                            totalPrice = totalPrice
                        ) { success ->
                            if (success) {
                                navController.navigate("bookingDetails/${userId}")
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }
    
    // Slot Full Dialog
    if (showSlotFullDialog) {
        AlertDialog(
            onDismissRequest = { showSlotFullDialog = false },
            title = { Text("Slot Full") },
            text = {
                Text(
                    "Sorry, the selected date is fully booked for Basic Grooming service. " +
                    "Please choose another date or try a different service."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showSlotFullDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
    // Cat Limit Dialog
    if (showCatLimitDialog) {
        AlertDialog(
            onDismissRequest = { showCatLimitDialog = false },
            title = { Text("Cat Limit") },
            text = {
                Text("You can only book up to 10 cats.")
            },
            confirmButton = {
                TextButton(
                    onClick = { showCatLimitDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}