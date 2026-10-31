package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.models.Product
import com.example.providers.BillingProvider
import com.example.utils.IconUtils

@Composable
fun ProductsScreen(
    provider: BillingProvider,
    paddingValues: PaddingValues
) {
    val products by provider.products.collectAsState()

    // Dialog trigger states
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var deletingProduct by remember { mutableStateOf<Product?>(null) }

    // Dialog state fields
    var nameField by remember { mutableStateOf("") }
    var priceField by remember { mutableStateOf("") }
    var iconTypeField by remember { mutableStateOf("OTHER") }
    var showIconDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Products",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Manage inventory and pricing",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Button(
                        onClick = {
                            nameField = ""
                            priceField = ""
                            iconTypeField = "OTHER"
                            showAddDialog = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New")
                    }
                }
            }

            item {
                Text(
                    text = "ALL PRODUCTS (${products.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (products.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No products yet. Click 'New' or reset data in settings to populate.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                items(products, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: icon
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconUtils.getIconForType(product.iconType),
                                    contentDescription = product.name,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Middle: details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (product.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Preset", fontSize = 10.sp) },
                                            modifier = Modifier.height(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "৳ ${product.price} Taka",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // Right: edit / delete actions
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        editingProduct = product
                                        nameField = product.name
                                        priceField = product.price.toString()
                                        iconTypeField = product.iconType
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit product")
                                }

                                IconButton(
                                    onClick = { deletingProduct = product },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete product")
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Dialogs ---

        // 1. ADD NEW PRODUCT DIALOG
        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Create New Product",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = nameField,
                            onValueChange = { nameField = it },
                            label = { Text("Product Name") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = priceField,
                            onValueChange = { priceField = it },
                            label = { Text("Price (৳)") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Icon option picker dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = iconTypeField,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Visual Icon Theme") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showIconDropdown = true },
                                enabled = false,
                                colors = TextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledContainerColor = Color.Transparent,
                                    disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { showIconDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                }
                            )

                            DropdownMenu(
                                expanded = showIconDropdown,
                                onDismissRequest = { showIconDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.55f)
                            ) {
                                IconUtils.availableIconTypes.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = option.icon,
                                                    contentDescription = option.name,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(option.name)
                                            }
                                        },
                                        onClick = {
                                            iconTypeField = option.type
                                            showIconDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddDialog = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    val price = priceField.toDoubleOrNull() ?: 0.0
                                    if (nameField.isNotBlank() && price > 0.0) {
                                        provider.addProduct(nameField, price, iconTypeField)
                                        showAddDialog = false
                                    }
                                },
                                enabled = nameField.isNotBlank() && priceField.isNotBlank()
                            ) {
                                Text("Create")
                            }
                        }
                    }
                }
            }
        }

        // 2. EDIT PRODUCT DIALOG
        editingProduct?.let { product ->
            Dialog(onDismissRequest = { editingProduct = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Edit Product Information",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = nameField,
                            onValueChange = { nameField = it },
                            label = { Text("Product Name") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = priceField,
                            onValueChange = { priceField = it },
                            label = { Text("Price (৳)") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Icon picker dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = iconTypeField,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Visual Icon Theme") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showIconDropdown = true },
                                enabled = false,
                                colors = TextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledContainerColor = Color.Transparent,
                                    disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { showIconDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                }
                            )

                            DropdownMenu(
                                expanded = showIconDropdown,
                                onDismissRequest = { showIconDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.55f)
                            ) {
                                IconUtils.availableIconTypes.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = option.icon,
                                                    contentDescription = option.name,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(option.name)
                                            }
                                        },
                                        onClick = {
                                            iconTypeField = option.type
                                            showIconDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { editingProduct = null }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    val price = priceField.toDoubleOrNull() ?: 0.0
                                    if (nameField.isNotBlank() && price > 0.0) {
                                        provider.updateProduct(
                                            product.copy(
                                                name = nameField,
                                                price = price,
                                                iconType = iconTypeField
                                            )
                                        )
                                        editingProduct = null
                                    }
                                },
                                enabled = nameField.isNotBlank() && priceField.isNotBlank()
                            ) {
                                Text("Save Updates")
                            }
                        }
                    }
                }
            }
        }

        // 3. DELETE CONFIRMATION DIALOG
        deletingProduct?.let { product ->
            AlertDialog(
                onDismissRequest = { deletingProduct = null },
                title = { Text("Delete Product?") },
                text = { Text("Are you sure you want to delete '${product.name}'? This action cannot be undone and will affect active logs.") },
                confirmButton = {
                    Button(
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            provider.deleteProduct(product)
                            deletingProduct = null
                        }
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingProduct = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
