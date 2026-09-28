package com.example.petbackers.pages

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petbackers.model.Booking
import com.example.petbackers.viewmodel.BookedViewModel
import java.util.Date


@SuppressLint("SimpleDateFormat")
@Composable
fun paymentSuccessfulPage(
    bookedId: String, // This should never be null if navigation is correct
    viewModel: BookedViewModel,
    navigateBackToHome: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val booked by viewModel.currentBooked.collectAsState()

    //Toast.makeText(context, "paymentSuccessfull page", Toast.LENGTH_SHORT).show()
    // Fetch the booked data when the page loads

    LaunchedEffect(bookedId) {
        viewModel.fetchBookedById(bookedId)
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp).clickable { navigateBackToHome() }
                )
                Text("Home", fontSize = 14.sp)
            }

            Text(
                text = "Payment Successful!",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )

            Spacer(Modifier.height(16.dp))

            // Display the booked information
            booked?.let { bookedItem ->
                Text(
                    text = "Order ID: ${bookedItem.bookedId}",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Payment Method: ${bookedItem.bankonline}",
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "Order Date: ${SimpleDateFormat("dd MMM yyyy, HH:mm").format(Date(bookedItem.timestamp))}",
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Services Booked:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(8.dp))

                // Display each booking
                bookedItem.bookingsData.forEach { booking ->
                    BookingCard(
                        booking = booking,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } ?: run {
                // Show loading or error state
                Text("Loading booking details...")
            }
        }

        // Fixed bottom payment section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(10.dp)
        ) {
            booked?.let {
                Text(
                    text = "Grand Total: RM${"%.2f".format(it.grandTotal)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = {navController.navigate("statusProcess/$bookedId")},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Status Process")
            }
        }
    }
}

@Composable
private fun BookingCard(booking: Booking, modifier: Modifier) {
    val addOnsTotal = booking.addOns?.filter { it.value }?.map {
        when (it.key) {
            "Flea Treatment" -> 30f
            "Degreaser Treatment" -> 25f
            "Fungus Treatment" -> 60f
            else -> 0f
        }
    }?.sum() ?: 0f

    val serviceTotal = booking.totalPrice - (addOnsTotal * booking.quantity)
    val servicePricePerQty = if (booking.quantity > 0) serviceTotal / booking.quantity else 0f

    val (displayName, displayPrice) = when {
        booking.serviceType == "Lion Cut" -> Pair("Lion Cut", 180f)
        booking.serviceType == "Basic Grooming" -> {
            val hairLength = when {
                servicePricePerQty >= 45f -> "LongHair"
                servicePricePerQty >= 39f -> "ShortHair"
                else -> "Unknown"
            }
            Pair("Basic Grooming ($hairLength)", servicePricePerQty)
        }
        booking.serviceType == "Boarding" -> Pair("Cat Boarding", 20f)
        else -> Pair(booking.serviceType, servicePricePerQty)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Service: $displayName - RM${"%.2f".format(displayPrice)}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Number of Cats: ${booking.quantity}")

                    if (booking.serviceType == "Boarding") {
                        Spacer(Modifier.height(4.dp))
                        Text("Number of Nights: ${booking.numberOfNights}")
                        Spacer(Modifier.height(4.dp))
                        Text("Check-in: ${booking.checkInDate}")
                        Text("Check-out: ${booking.checkOutDate}")
                    }

                    if (booking.serviceType != "Boarding") {
                        Spacer(Modifier.height(4.dp))
                        Text("Date: ${booking.date}")
                    }

                    if (booking.serviceType != "Boarding" && booking.addOns != null) {
                        Spacer(Modifier.height(8.dp))
                        Text("Add-ons:", fontWeight = FontWeight.Bold)
                        booking.addOns.filter { it.value }.forEach { (addOn, _) ->
                            val price = when (addOn) {
                                "Flea Treatment" -> 30f
                                "Degreaser Treatment" -> 25f
                                "Fungus Treatment" -> 60f
                                else -> 0f
                            }
                            Text("• $addOn - RM${"%.2f".format(price)}")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Total Price: RM${"%.2f".format(booking.totalPrice)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}