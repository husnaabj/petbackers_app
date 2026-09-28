package com.example.petbackers.pages

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petbackers.model.Booking
import com.example.petbackers.viewmodel.BookingDetailsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.petbackers.viewmodel.BookedViewModel
import com.google.firebase.auth.FirebaseAuth


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentProcess(
    viewModel: BookingDetailsViewModel,
    bookedViewModel: BookedViewModel,
    navigateBack: () -> Unit,
    userId: String,
    navController: NavController
) {
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
        viewModel.fetchBookings(userId)
    }

    val bookings by viewModel.bookings.collectAsState()
    val grandTotal = remember(bookings) {
        bookings.sumOf { it.totalPrice.toDouble() }.toFloat()
    }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog2 by remember { mutableStateOf(false) }
    // Add this state variable at the top with your other state declarations
    var verificationCode by remember { mutableStateOf("") }
    var OnlinePayment by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    val banks = listOf(
        "Maybank2u",
        "CimbClicks",
        "Public Bank",
        "RHB Now",
        "Ambank",
        "MyBSN",
        "Bank Rakyat",
        "Bank Islam"
    )
    val context = LocalContext.current
    // Get the booking IDs you want to move
    val bookingIds = viewModel.bookings.value.map { it.bookingId }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 180.dp) // Space for bottom section
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                Text("Back", color = Color.Gray, fontSize = 14.sp)
            }

            Text(
                text = "Payment Process",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )

            Spacer(Modifier.height(16.dp))

            when {
                bookings.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Loading your bookings...")
                    }
                }
                else -> {
                    Column {
                        bookings.forEach { booking ->
                            BookingCard(
                                booking = booking,
                                viewModel = viewModel,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Fixed bottom payment section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = OnlinePayment,
                    onValueChange = {},
                    label = { Text("Online Banking") },
                    placeholder = { Text("Select a bank") },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    banks.forEach { bank ->
                        DropdownMenuItem(
                            text = { Text(bank) },
                            onClick = {
                                OnlinePayment = bank
                                expanded = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Grand Total: RM${"%.2f".format(grandTotal)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Button(
                onClick = {
                    if (OnlinePayment.isNotEmpty() ) {
                        showConfirmationDialog = true// Pass username here
                    } else {
                        Toast.makeText(context, "Online Banking is empyty", Toast.LENGTH_SHORT).show()
                    }
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Proceed to Payment")
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirm Payment") },
            text = {
                Column {
                    Text("Are you sure you want to proceed with the payment?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Payment Method: ${if (OnlinePayment.isBlank()) "Not specified" else OnlinePayment}",
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Grand Total: RM${"%.2f".format(grandTotal)}",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        showConfirmationDialog2 =   true
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    // showConfirmationDialog2
    if (showConfirmationDialog2) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog2 = false },
            title = {
                Text(
                    text = "Payment Verification",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    
                    // Verification code section
                    Text(
                        text = "A secure 4-digit code has been sent to your registered number ending in *******34.",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                verificationCode = it
                            }
                        },
                        label = { Text("Enter 4-digit code") },
                        placeholder = { Text("e.g 1234") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Total amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "TOTAL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "RM${"%.2f".format(grandTotal)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onClick@{
                        if (verificationCode.length == 4) {
                            // Process payment here
                            showConfirmationDialog2 = false

                            if (FirebaseAuth.getInstance().currentUser == null) {
                                Toast.makeText(context, "Please sign in to complete booking", Toast.LENGTH_SHORT).show()
                                return@onClick
                            }


                            val allBookingIds = bookings.map { it.bookingId }
                            // Call the BookedViewModel to save the data
                            // Save to Firebase
                            bookedViewModel.confirmAndMoveBookings(
                                bookingIds = allBookingIds,
                                grandTotal = grandTotal,
                                bankonline = OnlinePayment,
                            ) { bookedId, success ->
                                if (success && bookedId != null) {
                                    Toast.makeText(context, "Payment successful! & Booking saved!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("paymentSuccess/$bookedId")

                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to save booking: ${bookedViewModel.errorState.value}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = verificationCode.length == 4
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog2 = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    // Observe loading state
    bookedViewModel.loadingState.collectAsState().value.let { isLoading ->
        if (isLoading) {
            // Show loading indicator
            CircularProgressIndicator()
        }
    }
}


@Composable
private fun BookingCard(booking: Booking, viewModel: BookingDetailsViewModel, modifier: Modifier) {
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
                else -> "ShortHair"
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

                IconButton(
                    onClick = { viewModel.deleteBooking(booking.bookingId, booking.selectedCats.keys.first()) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, "Delete Booking")
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