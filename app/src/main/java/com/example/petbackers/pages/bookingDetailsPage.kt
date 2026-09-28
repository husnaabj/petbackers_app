package com.example.petbackers.pages

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petbackers.viewmodel.BookingDetailsViewModel
import com.example.petbackers.viewmodel.CatViewModel

@Composable
fun BookingDetailsPage(
    viewModel: BookingDetailsViewModel,
    catViewModel: CatViewModel,
    navigateBackToHome: () -> Unit,
    navController: NavController,
    navigateBack: () -> Unit,
    userId: String
) {
    viewModel.setUserId(userId)
    val scrollState = rememberScrollState()
    val bookings by viewModel.bookings.collectAsState()
    val cats by catViewModel.cats.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var bookingToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }

    val grandTotal by remember(bookings) {
        derivedStateOf {
            bookings.sumOf { it.totalPrice.toDouble() }.toFloat()
        }
    }

    val serviceTotals by remember(bookings) {
        derivedStateOf {
            bookings.groupBy { it.serviceType }
                .mapValues { (_, bookings) ->
                    bookings.sumOf { it.totalPrice.toDouble() }.toFloat()
                }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchBookings(userId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 180.dp) // Add padding to prevent overlap with bottom section
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Back Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { navigateBack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Back",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Booking Details",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (bookings.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No bookings found")
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = navigateBackToHome) {
                        Text("Back to Home")
                    }
                }
                return@Box
            }

            // Group bookings by service type
            val groupedBookings = bookings.groupBy { it.serviceType }

            // Display each service type section
            groupedBookings.forEach { (serviceType, serviceBookings) ->
                // Label the service type section
                Text(
                    text = serviceType,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Group by cat (assuming selectedCats map contains cat IDs)
                val catBookings = serviceBookings.flatMap { booking ->
                    booking.selectedCats.keys.map { catId ->
                        Pair(catId, booking)
                    }
                }.groupBy({ it.first }, { it.second })

                catBookings.forEach { (catId, catBookingList) ->
                    // For each cat, display their bookings
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Cat name
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val cat = cats.firstOrNull { it.id == catId }
                                    Text(
                                        text = cat?.name ?: "Unknown Cat",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                // Delete icon
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            val booking = catBookingList.firstOrNull()
                                            booking?.let {
                                                bookingToDelete = Pair(it.bookingId, catId)
                                                showDeleteDialog = true
                                            }
                                        }
                                )
                            }

                            // Booking details
                            catBookingList.forEach { booking ->
                                when (serviceType) {
                                    "Basic Grooming", "Lion Cut" -> {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row {
                                            Box(modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "Date: ${booking.date}")
                                        }
                                    }
                                    "Boarding" -> {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row {
                                            Box(modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "Check In: ${booking.checkInDate}")
                                        }
                                        Row {
                                            Box(modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "Check Out: ${booking.checkOutDate}")
                                        }
                                        Row {
                                            Box(modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "Nights: ${booking.numberOfNights}")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row {
                                    Box(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Price: RM${"%.2f".format(booking.totalPrice)}",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fixed bottom section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(10.dp)
        ) {
            // Service type totals
            serviceTotals.forEach { (serviceType, total) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$serviceType Total:",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "RM${"%.2f".format(total)}",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Grand Total
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Grand Total: RM${"%.2f".format(grandTotal)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.navigate("home2") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add More")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { navController.navigate("paymentProcess/$userId") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Continue")
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog && bookingToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete this booking?") },
                confirmButton = {
                    Button(
                        onClick = {
                            bookingToDelete?.let { (bookingId, catId) ->
                                viewModel.deleteBooking(bookingId, catId)
                            }
                            showDeleteDialog = false
                            bookingToDelete = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showDeleteDialog = false
                        bookingToDelete = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}