package com.example.petbackers.pages.serviceDetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.LocationOn
import com.example.petbackers.R

@Composable
fun lioncutPage(
    navigateBackToHome: () -> Unit,
    onNextButtonClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
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
                //color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title and Ratings
        Text(
            text = "Lion Cut",
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
                .height(300.dp)
                .width(410.dp),
            //contentScale = ContentScale.Crop // Adjust the scale to fit the image properly
        )


        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Highlights
        Text(
            text = "Basic Lion Cut Details: ",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )

        Text(
            text = "RM180 Grooming with medicated shampoo.\n",
                    //"RM45 LONG HAIR.\n" +
                    //"Basic shampoo, nails cutting, paws trimming and ear cleaning.",
            fontSize = 16.sp,
            //color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom-left "Continue Booking" Button
        Button(
            onClick = { onNextButtonClicked() },
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Text(text = "Continue Booking")
        }
    }
}
