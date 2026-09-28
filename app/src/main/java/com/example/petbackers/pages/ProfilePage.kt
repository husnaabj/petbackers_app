package com.example.petbackers.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import com.example.petbackers.R
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.petbackers.ChatRepository
import com.example.petbackers.viewmodel.AuthState
import com.example.petbackers.viewmodel.AuthViewModel
import com.example.petbackers.viewmodel.CatViewModel
import com.example.petbackers.viewmodel.ChatViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.petbackers.viewmodel.EditProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    val authState = authViewModel.authState.observeAsState()
    val userId = authViewModel.getCurrentUser()?.uid // Fetch current user ID
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phonenumber by remember { mutableStateOf("") }

    // Lifted state for editing fields
    var editUsername by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()

    val chatRepository = ChatRepository(firestore, auth, database)

    var showEditDialog by remember { mutableStateOf(false) }
    val editProfileViewModel: EditProfileViewModel = viewModel()
    val updateState by editProfileViewModel.updateState.observeAsState()

    LaunchedEffect(userId) {
        if (userId != null) {
            // Fetch user data from Firebase
            val database = Firebase.database
            val userRef = database.getReference("users").child(userId)

            userRef.get().addOnSuccessListener { snapshot ->
                username = snapshot.child("username").getValue(String::class.java) ?: ""
                email = snapshot.child("email").getValue(String::class.java) ?: ""
                phonenumber = snapshot.child("phone").getValue(String::class.java) ?: ""

            }.addOnFailureListener {
                Toast.makeText(context, "Error fetching data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(authState.value){
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sign Out Button at Top Right
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable {
                    chatRepository.removeChatListener()
                    authViewModel.signout()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon image (logout.png should be in res/drawable)
            Icon(
                painter = painterResource(id = R.drawable.logout),
                contentDescription = "Logout Icon",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp)) // spacing between icon and text

            Text(
                text = "Logout",
                color = MaterialTheme.colorScheme.error
            )
        }



        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Profile Picture and Edit Icon Row
            Box(modifier = Modifier.size(100.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "Logout Icon",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(100.dp)
                )


            }
            Spacer(modifier = Modifier.height(16.dp))

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = {},
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    disabledTextColor = MaterialTheme.colorScheme.secondary,
                    disabledLabelColor = MaterialTheme.colorScheme.secondary,
                    disabledBorderColor = MaterialTheme.colorScheme.secondary,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.secondary,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.secondary
                )
            )


            Spacer(modifier = Modifier.height(8.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false, // Makes the field uneditable
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    disabledTextColor = MaterialTheme.colorScheme.secondary,
                    disabledLabelColor = MaterialTheme.colorScheme.secondary,
                    disabledBorderColor = MaterialTheme.colorScheme.secondary,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.secondary,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // phone number Field
            OutlinedTextField(
                value = phonenumber,
                onValueChange = {},
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false, // Makes the field uneditable
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    disabledTextColor = MaterialTheme.colorScheme.secondary,
                    disabledLabelColor = MaterialTheme.colorScheme.secondary,
                    disabledBorderColor = MaterialTheme.colorScheme.secondary,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.secondary,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Profile Button (replaces old sign out button)
            Button(
                onClick = {
                    editUsername = username
                    editPhone = phonenumber
                    showEditDialog = true
                },
            ) {
                Text(text = "Edit Profile", fontSize = 16.sp)
            }
        }
        // Edit Profile Dialog
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Profile") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = editUsername,
                            onValueChange = { editUsername = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        editProfileViewModel.updateProfile(editUsername, editPhone)
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        // Show update result
        updateState?.let {
            when (it) {
                is EditProfileViewModel.UpdateState.Success -> {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    showEditDialog = false
                    username = editUsername
                    phonenumber = editPhone
                    // Re-fetch from Firebase for extra reliability
                    if (userId != null) {
                        val userRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users").child(userId)
                        userRef.get().addOnSuccessListener { snapshot ->
                            username = snapshot.child("username").getValue(String::class.java) ?: ""
                            email = snapshot.child("email").getValue(String::class.java) ?: ""
                            phonenumber = snapshot.child("phone").getValue(String::class.java) ?: ""
                        }
                    }
                    // Reset update state so dialog can be opened again
                    editProfileViewModel.resetUpdateState()
                }
                is EditProfileViewModel.UpdateState.Error -> {
                    Toast.makeText(context, it.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                    // Reset update state so dialog can be opened again
                    editProfileViewModel.resetUpdateState()
                }
            }
        }
    }
}
