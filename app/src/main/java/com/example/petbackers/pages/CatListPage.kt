package com.example.petbackers.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petbackers.model.Cat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.petbackers.viewmodel.CatViewModel

@Composable
fun CatListPage(
    cats: List<Cat>,
    onCatSelected: (Cat) -> Unit,
    onDeleteCat: (Cat) -> Unit,
    onAddCat: () -> Unit,
    navigateBackToHome: () -> Unit,
    navController: NavHostController
) {
    val viewModel: CatViewModel = viewModel()
    val cats by viewModel.cats.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Back Arrow and Home Text
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Cat Profile List:",
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (cats.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No cats registered yet")
                    Button(onClick = onAddCat) {
                        Text("Add Your First Cat")
                    }
                }
            } else {
                cats.forEach { cat ->
                    CatListItem(
                        cat = cat,
                        onClick = { onCatSelected(cat) },
                        onDelete = { onDeleteCat(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // To avoid FAB overlap at bottom
        }

        // FAB at bottom-right corner
        FloatingActionButton(
            onClick = onAddCat,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Cat")
        }
    }
}


@Composable
fun CatListItem(
    cat: Cat,
    onClick: () -> Unit,
    onDelete: (Cat) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Make this Row clickable to select the cat
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cat.name,
                    fontSize = 23.sp,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            // Delete icon
            IconButton(onClick = { onDelete(cat) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun CatListItemPreview() {
    val sampleCat = Cat(
        name = "Milo",
        age = "3",
        gender = "Male",
        color = "Orange",
        hairlength = "Short Hair"
    )
    CatListItem(cat = sampleCat, onClick = {}, modifier = Modifier, onDelete = {})
}

@Preview(showBackground = true)
@Composable
fun CatListPagePreview() {
    val sampleCats = listOf(
        Cat(name = "Luna", age = "2", gender = "Female", color = "White", hairlength = "Long Hair"),
        Cat(name = "Leo", age = "4", gender = "Male", color = "Brown", hairlength = "Short Hair")
    )
    val navController = rememberNavController()
    CatListPage(
        cats = sampleCats,
        onCatSelected = {},
        onAddCat = {},
        onDeleteCat = {},
        navigateBackToHome = {},
        navController = navController
    )
}
