package com.example.petbackers.pages.bookingProcess

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petbackers.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.petbackers.viewmodel.BookingDetailsViewModel
import com.example.petbackers.viewmodel.CatViewModel
import com.example.petbackers.model.AddOn
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SelectableDates
import java.util.concurrent.TimeUnit

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun boardingDetailsBook(
    navigateBackToHome: () -> Unit,
    navController: NavHostController,
    catViewModel: CatViewModel = viewModel(),
    bookingViewModel: BookingDetailsViewModel
){
    var showConfirmationDialog by remember { mutableStateOf(false) } // New state for confirmation dialog

    val filteredCats = catViewModel.cats.value

    var quantity by remember { mutableIntStateOf(1) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val today = remember { System.currentTimeMillis() }
    val tomorrow = remember { today + TimeUnit.DAYS.toMillis(1) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = tomorrow,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis > today
            }
        }
    )

    var checkInDate by remember { mutableStateOf<Date?>(null) }
    var checkOutDate by remember { mutableStateOf<Date?>(null) }
    var pickingCheckIn by remember { mutableStateOf(true) } // or false for check-out
    val dateColors = DatePickerDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,
        selectedDayContainerColor = MaterialTheme.colorScheme.primary
    )

    val scrollState = rememberScrollState()

    //var selectedCat by remember { mutableStateOf<Cat?>(null) }
    val selectedCatIds = remember { mutableStateListOf<String>() }
    val selectedCats = filteredCats.filter { it.id in selectedCatIds }

    // Add these state variables at the top of your composable
    var showDateError by remember { mutableStateOf(false) }
    var dateErrorMessage by remember { mutableStateOf("") }

    // Room availability
    //val roomAvailability by bookingViewModel.getRoomAvailability().collectAsState()

    // First cat is RM20 per night, additional cats are RM10 per night each
    val basePricePerNight = 20.0f
    val additionalCatFeePerNight = 10.0f
    val catCount = selectedCats.size

    // Total price calculation
    //val totalPrice = "%.2f".format(pricePerNight * numberOfNights).toFloat()
    var totalPrice = 0.0f

    var numberOfNights = 0.0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // <-- Make column scrollable
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ){
        // Back Arrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            //modifier = Modifier.clickable { navigateBackToEventPromotions() }
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
            text = "Cat Boarding",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(10.dp))
        /*
        // Room Availability Information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Room Availability",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Available Rooms: ${roomAvailability.availableRooms}/30",
                    fontSize = 14.sp
                )
                Text(
                    text = "Occupied Rooms: ${roomAvailability.occupiedRooms}/30",
                    fontSize = 14.sp
                )
                
                // Show warning if not enough rooms
                if (selectedCats.size > roomAvailability.availableRooms) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Not enough rooms available for ${selectedCats.size} cats. Only ${roomAvailability.availableRooms} rooms are free.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }*/

        Spacer(modifier = Modifier.height(16.dp))

        // Image/Video Placeholder
        Image(
            painter = painterResource(id = R.drawable.boarding), // Replace with your drawable resource name
            contentDescription = "grooming Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .width(310.dp),
            //contentScale = ContentScale.Crop // Adjust the scale to fit the image properly
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Check In Date Selection
        Text(
            text = "Check In Date:",
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
        OutlinedTextField(
            value = checkInDate?.let { dateFormatter.format(it) } ?: "",
            onValueChange = {}, // Read-only, handled by date picker
            label = { Text("Select Check In Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    pickingCheckIn = true
                    showDatePicker = true
                }) {
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
        // Check Out Date Selection
        Text(
            text = "Check Out Date:",
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
        OutlinedTextField(
            value = checkOutDate?.let { dateFormatter.format(it) } ?: "",
            onValueChange = {}, // Read-only, handled by date picker
            label = { Text("Select Check Out Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    pickingCheckIn = false
                    showDatePicker = true
                }) {
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

        //Pick Cat profile
        // Pick Cat Profile Section
        Text(
            text = "Pick Cat Profile:",
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
        // Show message if no cats match the selected hair length
        if (filteredCats.isEmpty()) {
            Text(
                text = "No cats found. Please add a cat profile.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Cat Profile Selection Cards - now using filteredCats
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Limit height with scroll
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
                                selectedCatIds.add(cat.id)
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
                                    selectedCatIds.add(cat.id)
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

        Spacer(modifier = Modifier.height(24.dp))

        // Calculate the number of nights between check-in and check-out
        numberOfNights = if (checkInDate != null && checkOutDate != null) {
            val diff = checkOutDate!!.time - checkInDate!!.time
            (diff / (24 * 60 * 60 * 1000)).toFloat() // Convert milliseconds to days
        } else {
            0f
        }

        // Calculate price per night
        val pricePerNight = when {
            catCount == 0 -> 0f
            catCount == 1 -> basePricePerNight
            else -> basePricePerNight + (catCount - 1) * additionalCatFeePerNight
        }

        //val totalPrice = "%.2f".format(pricePerNight * numberOfNights).toFloat()

        totalPrice = pricePerNight * numberOfNights

        // Display the total price
        Text(
            text = "Total: RM${"%.2f".format(totalPrice)}",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )


        Spacer(modifier = Modifier.height(24.dp))

        // Confirm Booking Button (updated to check for selected cat and room availability)
        Button(
            onClick = {
                if (selectedCats.isNotEmpty() && checkInDate != null && checkOutDate != null) {
                    showConfirmationDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = selectedCats.isNotEmpty() && 
                     checkInDate != null && 
                     checkOutDate != null
                    //&&
                     //selectedCats.size <= roomAvailability.availableRooms

        ) {
            Text("Confirm Book")
        }

        /*
        // Show error message if not enough rooms
        if (selectedCats.isNotEmpty() && selectedCats.size > roomAvailability.availableRooms) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cannot book: Not enough rooms available",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        */


    }
    // Date Picker Dialog
    // Date Picker Dialog
    if (showDatePicker) {
        val dateColors = if (!pickingCheckIn) {
            // When picking check-out date, highlight check-in date
            DatePickerDefaults.colors(

            )
        } else {
            // Default colors when picking check-in date
            DatePickerDefaults.colors(

            )
        }

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val selectedDate = Date(it)
                            if (pickingCheckIn) {
                                if (checkOutDate == null || selectedDate.before(checkOutDate)) {
                                    checkInDate = selectedDate
                                    showDatePicker = false
                                    showDateError = false
                                } else {
                                    dateErrorMessage = "Check-in date must be before check-out date"
                                    showDateError = true
                                }
                            } else {
                                if (checkInDate == null || selectedDate.after(checkInDate)) {
                                    checkOutDate = selectedDate
                                    showDatePicker = false
                                    showDateError = false
                                } else {
                                    dateErrorMessage = "Check-out date must be after check-in date"
                                    showDateError = true
                                }
                            }
                        }
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
            // For older versions without dateValidator, we'll handle validation in the confirm button
            DatePicker(
                state = datePickerState,
                showModeToggle = true,
                colors = dateColors
            )
        }
    }

    // Add this snackbar at the bottom of your Column (but before the date picker dialog)
    if (showDateError) {
        Text(
            text = dateErrorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(
                    onClick = { showDateError = false }
                ) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(dateErrorMessage)
        }
    }


    // Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Booking") },
            text = {
                Column {
                    Text("Are you sure you want to book cat boarding for... ")
                    Spacer(modifier = Modifier.height(8.dp))

                    /*
                    // List selected cats with room assignments
                    selectedCats.forEachIndexed { index, cat ->
                        val roomNumber = "Room${roomAvailability.occupiedRooms + index + 1}"
                        Text("- ${cat.name} → $roomNumber", fontWeight = FontWeight.Bold)
                    }*/

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Check In Date: ${dateFormatter.format(checkInDate!!)}", fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(text = "Check Out Date: ${dateFormatter.format(checkOutDate!!)}", fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(text = "Number of Nights: ${numberOfNights.toInt()}", fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Show total price
                    Text(
                        text = "Total: RM${"%.2f".format(totalPrice)}",
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show room assignment note
                    Text(
                        text = "Each cat will be assigned to a separate room",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

                        // Show loading state
                        bookingViewModel.uiState.value = bookingViewModel.uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )

                        bookingViewModel.saveBoardingBooking(
                            userId = userId,
                            cats = selectedCats,
                            totalPrice = totalPrice,
                            checkInDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(checkInDate ?: Date()),
                            checkOutDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(checkOutDate ?: Date()),
                            numberOfNights = numberOfNights.toInt()
                        ) { success ->
                            if (success) {
                                navController.navigate("bookingDetails/${userId}")
                            } else {
                                // Show error message
                                val errorMessage = bookingViewModel.uiState.value.errorMessage ?: "Unknown error occurred"
                                android.util.Log.e("BoardingBooking", "Booking failed: $errorMessage")
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

    // Show loading indicator
    if (bookingViewModel.uiState.value.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // Show error message if any
    bookingViewModel.uiState.value.errorMessage?.let { errorMessage ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(
                    onClick = { 
                        bookingViewModel.uiState.value = bookingViewModel.uiState.value.copy(errorMessage = null)
                    }
                ) {
                    Text("Dismiss")
                }
            }
        ) {
            Text("Error: $errorMessage")
        }
    }
}