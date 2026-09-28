package com.example.petbackers.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petbackers.R
import com.example.petbackers.model.ServiceData
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@Composable
fun homepageSecond(
    navController: NavController,
)
{

    val service = listOf(
        ServiceData(R.string.service1, R.drawable.grooming, "grooming"),
        ServiceData(R.string.service2, R.drawable.lioncut, "lioncut"),
        ServiceData(R.string.service3, R.drawable.boarding, "boarding")
    )

    val searchQuery = remember { mutableStateOf("") }

    val filteredPlaces = if (searchQuery.value.isEmpty()) {
        service
    } else {
        service.filter {
            stringResource(it.service_name).contains(searchQuery.value, ignoreCase = true)
        }
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()){

        Text(
            text = stringResource(R.string.Petbackers),
            fontSize = 20.sp,
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 30.dp, start = 43.dp)
        )
        HomeSearchBar(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 70.dp, start = 40.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 135.dp, start = 40.dp)
                .verticalScroll(scrollState)
        ) {
            filteredPlaces.forEach { place ->
                Text(
                    text = stringResource(place.service_name),
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.montserrat_regular))
                )
                Image(
                    painter = painterResource(place.service_image),
                    contentDescription = null,
                    //contentScale = ContentScale.Crop, // Or .Fit, .FillBounds, etc.
                    modifier = Modifier
                        .width(310.dp)
                        .height(200.dp) // You can adjust this height as needed
                        .padding(bottom = 8.dp)
                )
                // Use Column to stack the button below the image
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { navController.navigate(place.detailsRoute) },

                        modifier = Modifier
                            .padding(start = 236.dp, bottom = 3.dp)
                            .height(35.dp)
                    ) {
                        Text("Add Booking",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp)
                    }
                }
            }
        }

    }
}
@Preview(showBackground = true)
@Composable
fun Homepage2Preview() {
    val navController = rememberNavController()
    homepageSecond(
        navController = navController,
    )
}
