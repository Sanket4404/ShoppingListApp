package com.example.shoppinglistapp

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController


// Data class to represent a shopping item with properties for ID, name, quantity, and editing status
data class ShoppingItem(
    val id: Int,
    var name: String,
    var quantity: Int,
    var isEditing: Boolean = false,
    var address: String = ""
)

@Composable
fun ShoppingList(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    navController: NavController,
    context: Context,
    address: String) {

    // State to hold the list of shopping items
    var sItem by remember { mutableStateOf(listOf<ShoppingItem>()) }
    // State to control whether the dialog for adding items is shown
    var showDilog by remember { mutableStateOf(false) }
    // State to hold the item name input by the user
    var itemName by remember { mutableStateOf("") }
    // State to hold the item quantity input by the user
    var itemQuantity by remember { mutableStateOf("") }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
                && permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true){
                //I have access to the location

                locationUtils.requestLocationUpdates(viewModel=viewModel)

            }else{
                // Ask for permission
                val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)

                if (rationalRequired){
                    Toast.makeText(context,
                        "Location Permission is Required for this feature ", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(context,
                        "Location Permission is Required Please enable it from Settings ", Toast.LENGTH_LONG).show()
                }
            }

        })

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.padding(15.dp))
        // Button to show the dialog for adding items
        Button(
            onClick = { showDilog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Add Items")
        }

        // LazyColumn to display the list of shopping items
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(sItem) { item ->
                if (item.isEditing) {
                    // Show editor if the item is in editing mode
                    ShoppingItemEditor(item = item, onEditComplete = { editedName, editedQuantity ->
                        // Update the list with the edited item and disable editing mode
                        sItem = sItem.map {
                            if (it.id == item.id) it.copy(name = editedName, quantity = editedQuantity, isEditing = false)
                            else it
                        }
                    })
                } else {
                    // Show item view with edit and delete options
                    shoppingListItem(item = item, onEditClick = {
                        // Set the clicked item to editing state
                        sItem = sItem.map { it.copy(isEditing = it.id == item.id) }
                    }, onDeleteClick = {
                        // Remove the item from the list
                        sItem = sItem.filterNot { it.id == item.id }
                    })
                }
            }
        }
    }

    // Dialog to add new items to the shopping list
    if (showDilog) {
        AlertDialog(onDismissRequest = { showDilog = false },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Button to add the new item to the list
                    Button(onClick = {
                        val quantity = itemQuantity.toIntOrNull()  // Convert to Int or null
                        if (itemName.isNotBlank() && quantity != null) {
                            val newItem = ShoppingItem(
                                id = sItem.size + 1,
                                name = itemName,
                                quantity = quantity
                            )
                            sItem = sItem + newItem
                            showDilog = false
                            itemName = ""         // Clear itemName after adding
                            itemQuantity = ""      // Clear itemQuantity after adding
                        }
                    }) {
                        Text(text = "ADD")
                    }

                    Spacer(modifier = Modifier.padding(8.dp)) // Add a spacer between buttons

                    // Button to cancel the addition of new items
                    Button(onClick = { showDilog = false }) {
                        Text(text = "CANCEL")
                    }
                }
            },
            title = { Text(text = "Add Shopping Items") },
            text = {
                Column {
                    // Text field to input item name
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text(text = "Item Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    // Text field to input item quantity
                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it },
                        label = { Text(text = "Item Quantity") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    Button(onClick = {
                        if (locationUtils.hasLocationPermission(context)){
                            locationUtils.requestLocationUpdates(viewModel)
                            navController.navigate("locationscreen"){
                                this.launchSingleTop
                            }
                        }else{
                            requestPermissionLauncher.launch(arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        }
                    }) {Text("address") }
                }
            }
        )
    }
}

@Composable
fun ShoppingItemEditor(item: ShoppingItem, onEditComplete: (String, Int) -> Unit) {
    // State to hold the edited item name
    var editedName by remember { mutableStateOf(item.name) }
    // State to hold the edited item quantity
    var editedQuantity by remember { mutableStateOf(item.quantity.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column {
            // Text field to edit item name
            BasicTextField(
                value = editedName,
                onValueChange = { editedName = it },
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            )

            // Text field to edit item quantity
            BasicTextField(
                value = editedQuantity,
                onValueChange = { editedQuantity = it },
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            )
        }

        // Button to save the edited item
        Button(onClick = {
            onEditComplete(editedName, editedQuantity.toIntOrNull() ?: 1)
        }) {
            Text(text = "Save")
        }
    }
}

//Lambda Function
//val lambda_name : Data_type = { argument_List -> code_body }     "Syntax"
//val sum = {a: Int , b: Int -> a + b}

@Composable
fun shoppingListItem(
    item: ShoppingItem,
    onEditClick: () -> Unit,    // Lambda function for edit action
    onDeleteClick: () -> Unit   // Lambda function for delete action
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(
                border = BorderStroke(2.dp, Color.Black),
                shape = RoundedCornerShape(25.dp)
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column (modifier = Modifier.weight(1f).padding(8.dp)) {
            Row{
            // Display item name
            Text(text = item.name, modifier = Modifier.padding(8.dp).align(alignment = Alignment.CenterVertically))
            // Display item quantity
            Text(text = "QTY: ${item.quantity}", modifier = Modifier.padding(8.dp).align(alignment = Alignment.CenterVertically))
            }
            Row (modifier = Modifier.fillMaxWidth()){
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                Text(text = item.address)
            }
        }

        Row(modifier = Modifier.padding(8.dp)) {
            // Edit button with icon
            IconButton(onClick = { onEditClick() }) {  // Invoke onEditClick
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }

            // Delete button with icon
            IconButton(onClick = { onDeleteClick() }) {  // Invoke onDeleteClick
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}