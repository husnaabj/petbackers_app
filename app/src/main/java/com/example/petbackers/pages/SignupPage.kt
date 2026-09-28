package com.example.petbackers.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.petbackers.R
import com.example.petbackers.viewmodel.AuthState
import com.example.petbackers.viewmodel.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.database

@Composable
fun SignupPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
)
{
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phonenumb by remember { mutableStateOf("") }
    var showWelcomeDialog by remember { mutableStateOf(false) }

    val database = Firebase.database
    val myRef = database.getReference()

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }


    LaunchedEffect(authState.value){
        when(authState.value){
            //is AuthState.Authenticated -> navController.navigate("home")

            is AuthState.Authenticated -> {
                // Instead of navigating directly to home, show the welcome dialog
                showWelcomeDialog = true
            }

            is AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }
    if (showWelcomeDialog) {
        AlertDialog(
            onDismissRequest = {
                // Don't allow dismissing by clicking outside
            },
            title = {
                Text(text = "Welcome to Petbackers!")
            },
            text = {
                Text("Please fill up your cat profile before starting.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWelcomeDialog = false
                        navController.navigate("catList") {
                            // Clear back stack so user can't go back to signup
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                ) {
                    Text("Add Cat Profile")
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "SINGUP", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username    ,
            onValueChange = {
                username = it
            } ,
            label = {
                Text(text = "Username")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            } ,
            label = {
                Text(text = "Email")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            } ,
            label = {
                Text(text = "Password")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.not_view else R.drawable.view
                        ),
                        modifier = Modifier.size(24.dp),
                        contentDescription = null
                    )
                }
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phonenumb,
            onValueChange = {
                phonenumb = it
            } ,
            label = {
                Text(text = "Phone Number")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number  , imeAction = ImeAction.Done),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty() && phonenumb.isNotEmpty()) {
                authViewModel.signup(email, password, username, phonenumb) // Pass username here
            } else {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Create Account")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("login")
        }) {
            Text(text = "Already have an account, Login here")
        }
    }
}

