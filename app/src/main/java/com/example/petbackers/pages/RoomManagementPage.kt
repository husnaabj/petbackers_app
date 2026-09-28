package com.example.petbackers.pages

/*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petbackers.model.RoomAssignment
//import com.example.petbackers.viewmodel.RoomManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RoomManagementPage(
    navController: NavController,
    navigateBackToHome: () -> Unit
) {
    val roomManager = remember { RoomManager() }
    val roomAvailability by roomManager.roomAvailability.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Back Arrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            //modifier = Modifier.na { navigateBackToHome() }
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
            text = "Room Management",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Room Availability Summary
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
                    text = "Room Status Overview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Rooms",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${roomAvailability.totalRooms}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            text = "Available",
                            fontSize = 14.sp,
                            color = Color.Green
                        )
                        Text(
                            text = "${roomAvailability.availableRooms}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Green
                        )
                    }
                    Column {
                        Text(
                            text = "Occupied",
                            fontSize = 14.sp,
                            color = Color.Red
                        )
                        Text(
                            text = "${roomAvailability.occupiedRooms}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current Room Assignments
        Text(
            text = "Current Room Assignments",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (roomAvailability.roomAssignments.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No active room assignments",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(400.dp)
            ) {
                items(roomAvailability.roomAssignments.values.toList()) { assignment ->
                    RoomAssignmentCard(assignment = assignment)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // All Rooms Status
        Text(
            text = "All Rooms Status",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Display all 30 rooms
        LazyColumn(
            modifier = Modifier.height(300.dp)
        ) {
            items(30) { roomNumber ->
                val roomId = "room_${roomNumber + 1}"
                val roomNumberText = "Room${roomNumber + 1}"
                val isOccupied = roomAvailability.roomAssignments.values.any { it.roomId == roomId }
                
                RoomStatusCard(
                    roomNumber = roomNumberText,
                    isOccupied = isOccupied,
                    assignment = roomAvailability.roomAssignments.values.find { it.roomId == roomId }
                )
            }
        }
    }
}

@Composable
fun RoomAssignmentCard(assignment: RoomAssignment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assignment.roomNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Active",
                    color = Color.Green,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Cat: ${assignment.catName}",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            
            Text(
                text = "Booking ID: ${assignment.bookingId}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Check In: ${assignment.checkInDate}",
                fontSize = 12.sp
            )
            Text(
                text = "Check Out: ${assignment.checkOutDate}",
                fontSize = 12.sp
            )
            Text(
                text = "Nights: ${assignment.numberOfNights}",
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun RoomStatusCard(
    roomNumber: String,
    isOccupied: Boolean,
    assignment: RoomAssignment?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOccupied) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = roomNumber,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            
            if (isOccupied && assignment != null) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = assignment.catName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Until ${assignment.checkOutDate}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Available",
                    color = Color.Green,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
} */