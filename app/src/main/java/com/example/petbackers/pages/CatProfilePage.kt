package com.example.petbackers.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petbackers.model.Cat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatProfilePage(
    cat: Cat,
    onCatUpdate: (String, String, String, String, String, String) -> Unit,
    onSave: () -> Unit,
    navigateBackToCat: () -> Unit,
) {
    var name by remember { mutableStateOf(cat.name) }
    var age by remember { mutableStateOf(cat.age) }
    var gender by remember { mutableStateOf(cat.gender) }
    var color by remember { mutableStateOf(cat.color) }
    var birthdate by remember { mutableStateOf(cat.birthdate) }
    var hairlength by remember { mutableStateOf(cat.hairlength) }
    var expanded by remember { mutableStateOf(false) }
    val hairLengthOptions = listOf("Short Hair", "Long Hair")
    var expandedGender by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female")
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Picker
    )
    LaunchedEffect(cat) {
        name = cat.name
        age = cat.age
        gender = cat.gender
        color = cat.color
        hairlength = cat.hairlength
        birthdate = try {
            val parsedDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(cat.birthdate)
            parsedDate?.let { dateFormatter.format(it) } ?: cat.birthdate
        } catch (e: Exception) {
            cat.birthdate
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                modifier = Modifier.size(24.dp).clickable { navigateBackToCat() }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Back",
                //color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Text("Cat Profile", style = MaterialTheme.typography.headlineSmall)

        //name
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                onCatUpdate(name, age, gender, color, hairlength, birthdate)
            },
            label = { Text("Name") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        //color
        OutlinedTextField(
            value = color,
            onValueChange = {
                color = it
                onCatUpdate(name, age, gender, color, hairlength, birthdate)
            },
            label = { Text("Color") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        //age
        OutlinedTextField(
            value = age,
            onValueChange = {
                age = it
                // Automatically update birthdate to Jan 1 of (current year - age)
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                val ageInt = it.toIntOrNull()
                if (ageInt != null) {
                    val calculatedYear = currentYear - ageInt
                    val janFirstDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse("$calculatedYear/01/01")!!)
                    birthdate = janFirstDate
                }
                onCatUpdate(name, age, gender, color, hairlength, birthdate)
            },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Next),

        )

        //birth date
        //birthdate = selectedDate?.let { dateFormatter.format(it) } ?: ""
        OutlinedTextField(
            value = birthdate,
            onValueChange = {
                birthdate = it
                onCatUpdate(name, age, gender, color, hairlength, birthdate)
            },
            label = { Text("Birth Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pick Date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )


        // Gender Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedGender,
            onExpandedChange = { expandedGender = !expandedGender },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                label = { Text("Gender") }
            )

            ExposedDropdownMenu(
                expanded = expandedGender,
                onDismissRequest = { expandedGender = false }
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            gender = option
                            onCatUpdate(name, age, gender, color, hairlength, birthdate)
                            expandedGender = false
                        }
                    )
                }
            }
        }

        // Hair Length Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = hairlength,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                label = { Text("Hair Length") }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                hairLengthOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            hairlength = option
                            onCatUpdate(name, age, gender, color, hairlength, birthdate)
                            expanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("SAVE")
        }
    }
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = Date(it)
                            birthdate = dateFormatter.format(selectedDate!!)
                            onCatUpdate(name, age, gender, color, hairlength, birthdate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CatProfilePagePreview(){
    val sampleCat = Cat(
        name = "Whiskers",
        age = "2",
        gender = "Female",
        color = "Calico",
        hairlength = "Long Hair",
        birthdate = "2002/08/23"
    )
    CatProfilePage(
        cat = sampleCat,
        onCatUpdate = { _, _, _, _, _, _ -> },
        onSave = {},
        navigateBackToCat = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DatePickerPreview() {
    val datePickerState = rememberDatePickerState()
    DatePicker(state = datePickerState)
}
