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
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.petbackers.viewmodel.BookingDetailsViewModel
import com.example.petbackers.viewmodel.CatViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun lioncutDetailsBook(
    navigateBackToHome: () -> Unit,
    navController: NavHostController,
    catViewModel: CatViewModel = viewModel(),
    bookingViewModel: BookingDetailsViewModel
){
    var showConfirmationDialog by remember { mutableStateOf(false) } // New state for confirmation dialog
    var showSlotFullDialog by remember { mutableStateOf(false) }
    var showCatLimitDialog by remember { mutableStateOf(false) }
    val filteredCats = catViewModel.cats.value
    var quantity by remember { mutableIntStateOf(1) }

    var showDatePicker by remember { mutableStateOf(false) }
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
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val scrollState = rememberScrollState()
    //var selectedCat by remember { mutableStateOf<Cat?>(null) }
    val selectedCatIds = remember { mutableStateListOf<String>() }
    val selectedCats = filteredCats.filter { it.id in selectedCatIds }

    var totalPrice = 0.0f

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
            text = "Lion Cut Grooming",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Image/Video Placeholder
        Image(
            painter = painterResource(id = R.drawable.lioncut), // Replace with your drawable resource name
            contentDescription = "lioncut Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .width(310.dp),
            //contentScale = ContentScale.Crop // Adjust the scale to fit the image properly
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Lion Cut Price: RM180",
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )

        // Date Selection
        Text(
            text = "Date:",
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
        OutlinedTextField(
            value = selectedDate?.let { dateFormatter.format(it) } ?: "",
            onValueChange = {}, // Read-only, handled by date picker
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

        //Pick Cat profile
        // Pick Cat Profile Section
        Text(
            text = "Pick Cat Profile:",
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp
        )
        // Show message if no cats
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

        Spacer(modifier = Modifier.height(24.dp))

        //total price
        //var totalPrice = 0.0f
        totalPrice = 180.0f * quantity

        Text(
            text = "Total: RM${"%.2f".format(totalPrice)}",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm Booking Button (updated to check for selected cat)
        Button(
            onClick = {
                if (selectedCats.isNotEmpty() && selectedDate != null) {
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(selectedDate ?: Date())
                    bookingViewModel.checkLioncutAvailability(
                        date = formattedDate,
                        catsCount = selectedCats.size
                    ) { isAvailable, availableSlots ->
                        if (isAvailable) {
                            showConfirmationDialog = true
                        } else {
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
                    Text("Are you sure you want to book lion cut for... ")
                    Spacer(modifier = Modifier.height(8.dp))

                    // List selected cats
                    selectedCats.forEach { cat ->
                        Text("- ${cat.name}", fontWeight = FontWeight.Bold)
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

                        bookingViewModel.saveLioncutBooking(
                            userId = userId,
                            cats = selectedCats,
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(selectedDate ?: Date()),
                            totalPrice = totalPrice,
                        ) { success ->
                            if (success) {
                                navController.navigate("bookingDetails/${userId}")
                            }
                        }
                    }
                ) {
                    Text("Yes")
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
                    "Sorry, the selected date is fully booked for Lion Cut service. " +
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