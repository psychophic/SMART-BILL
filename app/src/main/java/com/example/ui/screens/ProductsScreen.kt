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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.models.Product
import com.example.providers.BillingProvider
import com.example.ui.theme.*
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
    var iconTypeField by remember { mutableStateOf("FOOD") }
    var showIconDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BiriBlack)
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
                            text = "পণ্য তালিকা",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = BiriYellow
                        )
                        Text(
                            text = "পণ্যের মূল্য ও আইকন নিয়ন্ত্রণ করুন",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                    }

                    Button(
                        onClick = {
                            nameField = ""
                            priceField = ""
                            iconTypeField = "FOOD"
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BiriYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন পণ্য", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Text(
                    text = "মোট পণ্য (${products.size} টি)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BiriYellow,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (products.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BiriBlackCard),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোন পণ্য নেই। ডিফল্ট পণ্য লোড করুন বা নতুন যোগ করুন।",
                                color = Color.LightGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(products, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BiriBlackCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BiriBorderDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF252528)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconUtils.getIconForType(product.iconType),
                                        contentDescription = null,
                                        tint = BiriYellow,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "৳ ${String.format("%.0f", product.price)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BiriYellow
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        editingProduct = product
                                        nameField = product.name
                                        priceField = product.price.toString()
                                        iconTypeField = product.iconType
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = BiriYellowLight
                                    )
                                }

                                IconButton(
                                    onClick = { deletingProduct = product }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = BiriRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Product Dialog
        if (showAddDialog) {
            ProductEditDialog(
                title = "নতুন পণ্য যোগ করুন",
                name = nameField,
                price = priceField,
                iconType = iconTypeField,
                onNameChange = { nameField = it },
                onPriceChange = { priceField = it },
                onIconTypeChange = { iconTypeField = it },
                onDismiss = { showAddDialog = false },
                onConfirm = {
                    val p = priceField.toDoubleOrNull() ?: 0.0
                    if (nameField.isNotBlank() && p > 0) {
                        provider.addProduct(nameField.trim(), p, iconTypeField)
                        showAddDialog = false
                    }
                }
            )
        }

        // Edit Product Dialog
        if (editingProduct != null) {
            val prod = editingProduct!!
            ProductEditDialog(
                title = "পণ্য সম্পাদনা করুন",
                name = nameField,
                price = priceField,
                iconType = iconTypeField,
                onNameChange = { nameField = it },
                onPriceChange = { priceField = it },
                onIconTypeChange = { iconTypeField = it },
                onDismiss = { editingProduct = null },
                onConfirm = {
                    val p = priceField.toDoubleOrNull() ?: prod.price
                    if (nameField.isNotBlank()) {
                        provider.updateProduct(
                            prod.copy(
                                name = nameField.trim(),
                                price = p,
                                iconType = iconTypeField
                            )
                        )
                        editingProduct = null
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (deletingProduct != null) {
            val prod = deletingProduct!!
            AlertDialog(
                onDismissRequest = { deletingProduct = null },
                title = { Text("পণ্য মুছে ফেলা", color = BiriRed, fontWeight = FontWeight.Bold) },
                text = { Text("আপনি কি নিশ্চিতভাবে '${prod.name}' তালিকা থেকে মুছে ফেলতে চান?", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            provider.deleteProduct(prod)
                            deletingProduct = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BiriRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text("মুছে ফেলুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingProduct = null }) {
                        Text("বাতিল", color = Color.Gray)
                    }
                },
                containerColor = BiriBlackCard
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditDialog(
    title: String,
    name: String,
    price: String,
    iconType: String,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onIconTypeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BiriBlackCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BiriYellow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BiriYellow
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("পণ্যের নাম") },
                    placeholder = { Text("যেমন: বেনসন, চা...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiriYellow,
                        unfocusedBorderColor = BiriBorderDark,
                        focusedLabelColor = BiriYellow,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text("মূল্য (৳)") },
                    placeholder = { Text("যেমন: ২০") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiriYellow,
                        unfocusedBorderColor = BiriBorderDark,
                        focusedLabelColor = BiriYellow,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Icon Selector
                Column {
                    Text("আইকন নির্বাচন করুন", style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconUtils.availableIconTypes.take(4).forEach { opt ->
                            val isSelected = iconType == opt.type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) BiriYellow else Color(0xFF2A2A2A))
                                    .clickable { onIconTypeChange(opt.type) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = opt.icon,
                                    contentDescription = opt.name,
                                    tint = if (isSelected) Color.Black else Color.LightGray
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাতিল", color = Color.Gray)
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BiriYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("সংরক্ষণ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
