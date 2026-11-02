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
import com.example.utils.DateUtils
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
            .background(AppTheme.colors.background)
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
                            color = AppTheme.colors.accentYellow
                        )
                        Text(
                            text = "পণ্যের মূল্য ও আইকন নিয়ন্ত্রণ করুন",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.colors.textSecondary
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
                            containerColor = AppTheme.colors.accentYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "নতুন পণ্য",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "মোট পণ্য (${DateUtils.toBengaliDigits(products.size)} টি)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.accentYellow,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (products.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
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
                                tint = AppTheme.colors.textMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোন পণ্য নেই। ডিফল্ট পণ্য লোড করুন বা নতুন যোগ করুন।",
                                color = AppTheme.colors.textSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                items(products, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
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
                                        .background(AppTheme.colors.elevatedBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconUtils.getIconForType(product.iconType),
                                        contentDescription = null,
                                        tint = AppTheme.colors.accentYellow,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.textPrimary
                                    )
                                    Text(
                                        text = "মূল্য: ৳ ${DateUtils.toBengaliDigits(product.price)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppTheme.colors.accentYellow,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        editingProduct = product
                                        nameField = product.name
                                        priceField = if (product.price % 1.0 == 0.0) product.price.toInt().toString() else product.price.toString()
                                        iconTypeField = product.iconType
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = AppTheme.colors.accentYellow
                                    )
                                }

                                IconButton(
                                    onClick = { deletingProduct = product }
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = AppTheme.colors.accentRed
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
                onNameChange = { nameField = it },
                price = priceField,
                onPriceChange = { priceField = it },
                iconType = iconTypeField,
                onIconTypeChange = { iconTypeField = it },
                onDismiss = { showAddDialog = false },
                onConfirm = {
                    val p = priceField.toDoubleOrNull() ?: 0.0
                    if (nameField.isNotBlank() && p >= 0) {
                        provider.addProduct(nameField.trim(), p, iconTypeField)
                        showAddDialog = false
                    }
                }
            )
        }

        // Edit Product Dialog
        if (editingProduct != null) {
            ProductEditDialog(
                title = "পণ্য এডিট করুন",
                name = nameField,
                onNameChange = { nameField = it },
                price = priceField,
                onPriceChange = { priceField = it },
                iconType = iconTypeField,
                onIconTypeChange = { iconTypeField = it },
                onDismiss = { editingProduct = null },
                onConfirm = {
                    val p = priceField.toDoubleOrNull() ?: 0.0
                    if (nameField.isNotBlank() && p >= 0) {
                        editingProduct?.let {
                            provider.updateProduct(
                                it.copy(name = nameField.trim(), price = p, iconType = iconTypeField)
                            )
                        }
                        editingProduct = null
                    }
                }
            )
        }

        // Delete Product Dialog
        if (deletingProduct != null) {
            AlertDialog(
                onDismissRequest = { deletingProduct = null },
                title = { Text("পণ্য মুছুন", color = AppTheme.colors.accentRed, fontWeight = FontWeight.Bold) },
                text = { Text("আপনি কি নিশ্চিতভাবে '${deletingProduct?.name}' পণ্যটি তালিকা থেকে মুছে ফেলতে চান?", color = AppTheme.colors.textPrimary) },
                confirmButton = {
                    Button(
                        onClick = {
                            deletingProduct?.let { provider.deleteProduct(it) }
                            deletingProduct = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentRed, contentColor = Color.White)
                    ) {
                        Text("হ্যাঁ, মুছুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingProduct = null }) {
                        Text("বাতিল", color = AppTheme.colors.textMuted)
                    }
                },
                containerColor = AppTheme.colors.cardBackground
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditDialog(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    iconType: String,
    onIconTypeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val icons = listOf("FOOD", "TEA", "COFFEE", "SMOKE", "GAME", "WATER", "SNACK", "ITEM")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.accentYellow)
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
                    color = AppTheme.colors.accentYellow
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("পণ্যের নাম") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.colors.accentYellow,
                        unfocusedBorderColor = AppTheme.colors.border,
                        focusedLabelColor = AppTheme.colors.accentYellow,
                        focusedTextColor = AppTheme.colors.textPrimary,
                        unfocusedTextColor = AppTheme.colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text("মূল্য (টাকা)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.colors.accentYellow,
                        unfocusedBorderColor = AppTheme.colors.border,
                        focusedLabelColor = AppTheme.colors.accentYellow,
                        focusedTextColor = AppTheme.colors.textPrimary,
                        unfocusedTextColor = AppTheme.colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "আইকন নির্বাচন করুন",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.textSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    icons.take(4).forEach { ic ->
                        val isSelected = iconType == ic
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AppTheme.colors.accentYellow else AppTheme.colors.elevatedBackground)
                                .clickable { onIconTypeChange(ic) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconUtils.getIconForType(ic),
                                contentDescription = ic,
                                tint = if (isSelected) Color.Black else AppTheme.colors.textSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    icons.drop(4).forEach { ic ->
                        val isSelected = iconType == ic
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AppTheme.colors.accentYellow else AppTheme.colors.elevatedBackground)
                                .clickable { onIconTypeChange(ic) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconUtils.getIconForType(ic),
                                contentDescription = ic,
                                tint = if (isSelected) Color.Black else AppTheme.colors.textSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("বাতিল", color = AppTheme.colors.textMuted)
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.accentYellow,
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
