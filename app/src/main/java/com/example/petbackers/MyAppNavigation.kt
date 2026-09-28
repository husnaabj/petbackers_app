package com.example.petbackers

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petbackers.pages.CatListPage
import com.example.petbackers.pages.CatProfilePage
import com.example.petbackers.pages.LoginPage
import com.example.petbackers.pages.ProfilePage
import com.example.petbackers.pages.SignupPage
import com.example.petbackers.pages.homepages
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.petbackers.model.Cat
import com.example.petbackers.pages.BookingDetailsPage
import com.example.petbackers.pages.ChatPage
import com.example.petbackers.pages.PaymentProcess
//import com.example.petbackers.pages.RoomManagementPage
import com.example.petbackers.pages.StatusProcessPage
import com.example.petbackers.pages.bookingProcess.boardingDetailsBook
import com.example.petbackers.pages.bookingProcess.groomingDetailsBook
import com.example.petbackers.pages.bookingProcess.lioncutDetailsBook
import com.example.petbackers.pages.historyPage
import com.example.petbackers.pages.homepageSecond
import com.example.petbackers.pages.paymentSuccessfulPage
import com.example.petbackers.pages.serviceDetails.boardingPage
import com.example.petbackers.pages.serviceDetails.groomingPage
import com.example.petbackers.pages.serviceDetails.lioncutPage
import com.example.petbackers.viewmodel.AuthViewModel
import com.example.petbackers.viewmodel.BookedViewModel
import com.example.petbackers.viewmodel.BookingDetailsViewModel
import com.example.petbackers.viewmodel.CatViewModel
import com.example.petbackers.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.database.FirebaseDatabase
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.petbackers.viewmodel.AuthState
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    val currentRoute = navController
        .currentBackStackEntryFlow
        .collectAsState(initial = navController.currentBackStackEntry)
        .value?.destination?.route

    val screensWithBottomBar = listOf("home", "profile","history","catList", "boarding", "grooming", "lioncut","paymentSuccess/{bookedId}","statusProcess/{bookedId}","chat","roomManagement")
    val bookingViewModel: BookingDetailsViewModel = viewModel()
    val bookedViewModel: BookedViewModel = viewModel()

    // --- Auth state ---
    val authState by authViewModel.authState.observeAsState()

    Scaffold(
        bottomBar = {
            if (currentRoute in screensWithBottomBar) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login", //start dest
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginPage(modifier, navController, authViewModel)
            }
            composable("signup") {
                SignupPage(modifier, navController, authViewModel)
            }
            composable("home") {
                homepages(modifier, navController, authViewModel)
            }

            composable("home2") {
                homepageSecond(navController)
            }
            composable("profile") {
                ProfilePage(modifier, navController, authViewModel)
            }
            // In your NavHost setup:
            composable("chat") {
                if (authState is AuthState.Authenticated) {
                    val firestore = FirebaseFirestore.getInstance()
                    val auth = FirebaseAuth.getInstance()
                    val database = FirebaseDatabase.getInstance()
                    val chatRepository = remember { ChatRepository(firestore, auth, database) }
                    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(chatRepository))
                    ChatPage(viewModel = chatViewModel)
                }
            }
            composable("catList") {
                CatNavigation(mainNavController = navController, viewModel = viewModel())

            }
            composable("boarding") {
                boardingPage(
                    navigateBackToHome = { navController.popBackStack("home", false) },
                    onNextButtonClicked = { navController.navigate("boardingDetailsBook") }
                )

            }
            composable("grooming") {
                groomingPage(
                    navigateBackToHome = { navController.popBackStack("home", false) },
                    onNextButtonClicked = { navController.navigate("groomingDetailsBook") }
                )
            }
            composable("lioncut") {
                lioncutPage(
                    navigateBackToHome = { navController.popBackStack("home", false) },
                    onNextButtonClicked = { navController.navigate("lioncutDetailsBook") }
                )
            }
            composable("groomingDetailsBook") {
                groomingDetailsBook(
                    navigateBackToHome = { navController.popBackStack("home", false) },
                    navController = navController,
                    bookingViewModel = bookingViewModel
                )
            }
            composable("lioncutDetailsBook") {
                lioncutDetailsBook(
                    navigateBackToHome = { navController.popBackStack("home", false) },
                    navController = navController,
                    bookingViewModel = bookingViewModel
                )
            }
            composable("boardingDetailsBook") {
                boardingDetailsBook(
                    navigateBackToHome = { navController.popBackStack("home", false) },
                    navController = navController,
                    bookingViewModel = bookingViewModel
                )
            }
            composable("bookingDetails/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                val bookingViewModel: BookingDetailsViewModel = viewModel()

                BookingDetailsPage(
                    viewModel = bookingViewModel,
                    navigateBackToHome = { navController.popBackStack("home", false) },
                    navigateBack = { navController.popBackStack() },  // Changed to popBackStack
                    userId = userId,
                    catViewModel = viewModel(),
                    navController = navController,
                )
            }
            composable("paymentProcess/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                PaymentProcess(
                    viewModel = bookingViewModel,
                    userId = userId,
                    bookedViewModel = bookedViewModel,
                    navigateBack = { navController.popBackStack() },  // Changed to popBackStack
                    navController = navController,
                )
            }
            composable(
                "paymentSuccess/{bookedId}",
                arguments = listOf(navArgument("bookedId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookedId = backStackEntry.arguments?.getString("bookedId") ?: ""
                paymentSuccessfulPage(
                    bookedId = bookedId,
                    viewModel = bookedViewModel,
                    navigateBackToHome = { navController.navigate("home") },
                    navController = navController,
                )
            }
            composable(
                "statusProcess/{bookedId}",
                arguments = listOf(navArgument("bookedId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookedId = backStackEntry.arguments?.getString("bookedId") ?: ""
                StatusProcessPage(
                    bookedId = bookedId,
                    viewModel = bookedViewModel,
                    navigateBackToHome = { navController.navigate("home") },
                    catViewModel = viewModel(),
                    )
            }
            composable("history",
            ) {
                val viewModel: BookedViewModel = viewModel()
                historyPage(
                    viewModel = viewModel,
                    navigateBackToHome = { navController.popBackStack("home", false) },
                    navController = navController
                )
            }
            /*
            composable("roomManagement") {
                RoomManagementPage(
                    navController = navController,
                    navigateBackToHome = { navController.popBackStack("home", false) }
                )
            }
            */
        }
    }
}



@Composable
fun BottomNavBar(navController: NavHostController) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clickable { navController.navigate("home") }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = "Home", fontSize = 10.sp)
            }


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clickable { navController.navigate("chat") }
                    .padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bubblechat),
                    contentDescription = "Chat",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black,
                )
                Text(text = "Chat", fontSize = 10.sp)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clickable { navController.navigate("history") }
                    .padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.history),
                    contentDescription = "history",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black,
                )
                Text(text = "History", fontSize = 10.sp)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clickable { navController.navigate("catList") }
                    .padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.cat2),
                    contentDescription = "catList",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black,
                )
                Text(text = "Cat List", fontSize = 10.sp)
            }



            /*
            IconButton(onClick = { navController.navigate("roomManagement") }) {
                Icon(
                    imageVector = Icons.Filled.Home, // You can replace this with a more appropriate icon
                    contentDescription = "Room Management", 
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }*/

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clickable { navController.navigate("profile") }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "profile",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = "User Profile", fontSize = 10.sp)
            }
        }
    }
}

sealed class Screen(val route: String) {
    object CatList : Screen("catList")
    object CatProfile : Screen("catProfile")
}

@Composable
fun CatNavigation(
    mainNavController: NavHostController,
    viewModel: CatViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.CatList.route
    ) {
        composable(Screen.CatList.route) {
            val cats = viewModel.cats.collectAsState().value

            CatListPage(
                cats = cats,
                onCatSelected = { cat ->
                    viewModel.setCurrentCat(cat)
                    navController.navigate(Screen.CatProfile.route)
                },
                onAddCat = {
                    viewModel.setCurrentCat(Cat())// Create new empty cat
                    navController.navigate(Screen.CatProfile.route)
                },
                navigateBackToHome = {
                    mainNavController.navigate("home") {
                        popUpTo("catList") { inclusive = true }
                    }
                },
                onDeleteCat = { cat ->
                    viewModel.deleteCat(cat)
                },
                navController = navController
            )
        }
        composable(Screen.CatProfile.route) {
            CatProfilePage(
                cat = viewModel.currentCat.collectAsState().value,
                onCatUpdate = { name, age, gender, color, hairlength, birthdate ->
                    viewModel.updateCurrentCat(name, age, gender, color, hairlength, birthdate)
                },
                onSave = {
                    viewModel.saveCat()
                    navController.popBackStack()
                },
                navigateBackToCat = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/*
// Helper function to show notification
@SuppressLint("MissingPermission")
fun showAdminMessageNotification(context: Context, message: String) {
    val channelId = "admin_message_channel"
    val notificationId = 1001
    // Create notification channel if needed
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Admin Messages"
        val descriptionText = "Notifications for new admin messages"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("New Message from Admin")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    // Check permission before posting notification
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    ) {
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}*/
