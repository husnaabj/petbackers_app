package com.example.petbackers.pages

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petbackers.viewmodel.BookedViewModel
import com.example.petbackers.viewmodel.CatViewModel

@Composable
fun StatusProcessPage(
    bookedId: String,
    catViewModel: CatViewModel,
    viewModel: BookedViewModel,
    navigateBackToHome: () -> Unit,
) {
    val cats by catViewModel.cats.collectAsState()
    val booked by viewModel.currentBooked.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(bookedId) {
        viewModel.fetchBookedById(bookedId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Back Arrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { navigateBackToHome() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Home",
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
            text = "Status Process",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (booked == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (booked!!.bookingsData.isEmpty()) {
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
            return@Column
        }

        // Group bookings by service type
        val groupedBookings = booked!!.bookingsData.groupBy { it.serviceType }

        // Display each service type section
        groupedBookings.forEach { (serviceType, bookings) ->
            // Service type header
            Text(
                text = serviceType,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Get all unique cats across all bookings of this service type
            val catsInService = bookings.flatMap { booking ->
                booking.selectedCats.keys
            }.distinct()

            catsInService.forEach { catId ->
                // Find the cat details
                val cat = cats.firstOrNull { it.id == catId } ?: return@forEach

                // Find all bookings for this cat in this service type
                val catBookings = bookings.filter { booking ->
                    booking.selectedCats.containsKey(catId)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Cat name and status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cat.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            // Display status if available
                            val status = catBookings.firstOrNull()?.selectedCats?.get(catId)?.statusProcess
                            if (!status.isNullOrEmpty()) {
                                Text(
                                    text = status,
                                    color = when (status) {
                                        "Done" -> Color.Green
                                        "Ongoing" -> Color(0xFFFFA500) // Orange
                                        else -> Color.Gray
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Booking details for this cat
                        catBookings.forEach { booking ->
                            when (serviceType) {
                                "Basic Grooming", "Lion Cut" -> {
                                    Row {
                                        Text(text = "Date: ${booking.date}")
                                    }
                                }
                                "Boarding" -> {
                                    Row {
                                        Text(text = "Check In: ${booking.checkInDate}")
                                    }
                                    Row {
                                        Text(text = "Check Out: ${booking.checkOutDate}")
                                    }
                                    Row {
                                        Text(text = "Nights: ${booking.numberOfNights}")
                                    }

                                    /*
                                    // Show room assignment for boarding
                                    val catStatus = booking.selectedCats[catId]
                                    val assignedRoom = catStatus?.assignedRoom ?: booking.roomAssignments[catId]
                                    if (!assignedRoom.isNullOrEmpty()) {
                                        Row {
                                            Text(
                                                text = "Room: $assignedRoom",
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }*/
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Text(
                                    text = "Price: RM${"%.2f".format(booking.totalPrice)}",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Add a divider between bookings if there are multiple
                            if (catBookings.size > 1 && booking != catBookings.last()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}