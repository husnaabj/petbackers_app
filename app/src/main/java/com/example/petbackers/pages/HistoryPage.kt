package com.example.petbackers.pages

import androidx.compose.ui.tooling.preview.Preview
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.petbackers.model.Booked
import com.example.petbackers.viewmodel.BookedViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun historyPage(
    viewModel: BookedViewModel,
    navigateBackToHome: () -> Unit,
    navController: NavController,
) {
    val bookedList by viewModel.bookedList.collectAsState()

    LaunchedEffect(bookedList) {
        viewModel.fetchBookedHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            //.verticalScroll(scrollState)

    ) {
        // Back Arrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { navigateBackToHome() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Home", fontSize = 14.sp)
        }


        //TITTLE
        Text(
            text = "Booking History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            bookedList.isEmpty() -> Text("No booking history found")
            else -> BookedHistoryList(
                bookedList = bookedList,
                navController = navController
            )
        }
    }
}

@Composable
fun BookedHistoryList(
    bookedList: List<Booked>,
    navController: NavController
) {
    LazyColumn {
        items(bookedList) { booked ->
            BookedHistoryItem(
                booked = booked,
                navController = navController
            )
        }
    }
}

@Composable
fun BookedHistoryItem(booked: Booked, navController: NavController) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(booked.timestamp))

    Card(
        modifier = Modifier.padding(vertical = 8.dp),
        onClick = {
            navController.navigate("paymentSuccess/${booked.bookedId}")
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                //verticalAlignment = Alignment.CenterVertically
            ) {
                // Your existing content in a Column
                Column {
                    Text(text = "Date: $dateString", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Payment Method: ${booked.bankonline}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Total: RM${"%.2f".format(booked.grandTotal)}", style = MaterialTheme.typography.bodyLarge)
                    // Display booking details
                    booked.bookingsData.forEach { booking ->
                        Text(text = "- ${booking.serviceType} ", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // View Detail text
                Text(
                    text = "View Details",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

