package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.models.Expense
import com.example.models.Product
import com.example.providers.BillingProvider
import com.example.ui.theme.*
import com.example.utils.DateUtils
import com.example.utils.IconUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstantBillScreen(
    provider: BillingProvider,
    paddingValues: PaddingValues,
    onNavigateSummary: () -> Unit
) {
    val context = LocalContext.current
    val products by provider.products.collectAsState()
    val quantities by provider.quantities.collectAsState()
    val expenses by provider.expenses.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var activeQtyEditProduct by remember { mutableStateOf<Product?>(null) }
    var activeQtyText by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    // Calculate total instant counter bill
    val currentTotal = remember(quantities, expenses, products) {
        val prodMap = products.associate { it.id to it.price }
        var sum = 0.0
        quantities.forEach { (pId, qty) ->
            sum += (prodMap[pId] ?: 0.0) * qty
        }
        expenses.forEach { sum += it.amount }
        sum
    }

    // Total item count
    val totalItemCount = remember(quantities) {
        quantities.values.sum()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                color = AppTheme.colors.cardBackground,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "তাৎক্ষণিক কাউন্টার বিল",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppTheme.colors.accentYellow
                            )
                            Text(
                                text = "হাতে হাতে বিক্রি ও ক্যাশ মেমো তৈরির জন্য",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.colors.textSecondary
                            )
                        }

                        Surface(
                            color = if (currentTotal > 0) AppTheme.colors.accentYellow else AppTheme.colors.elevatedBackground,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "৳ ${DateUtils.toBengaliDigits(currentTotal)}",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = if (currentTotal > 0) Color.Black else AppTheme.colors.textMuted
                            )
                        }
                    }

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("পণ্য খুঁজুন...", color = AppTheme.colors.textMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = AppTheme.colors.accentYellow)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = AppTheme.colors.textMuted)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.accentYellow,
                            unfocusedBorderColor = AppTheme.colors.border,
                            focusedTextColor = AppTheme.colors.textPrimary,
                            unfocusedTextColor = AppTheme.colors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Products List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    val qty = quantities[product.id] ?: 0
                    val subtotal = qty * product.price

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (qty > 0) AppTheme.colors.elevatedBackground else AppTheme.colors.cardBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (qty > 0) AppTheme.colors.accentYellow.copy(alpha = 0.8f) else AppTheme.colors.border
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon and details
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (qty > 0) AppTheme.colors.accentYellow.copy(alpha = 0.2f) else AppTheme.colors.elevatedBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconUtils.getIconForType(product.iconType),
                                        contentDescription = null,
                                        tint = if (qty > 0) AppTheme.colors.accentYellow else AppTheme.colors.textSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.textPrimary
                                    )
                                    Text(
                                        text = "৳ ${DateUtils.toBengaliDigits(product.price)} প্রতি পিস",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppTheme.colors.textSecondary
                                    )
                                    if (qty > 0) {
                                        Text(
                                            text = "সাবটোটাল: ৳ ${DateUtils.toBengaliDigits(subtotal)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppTheme.colors.accentYellow
                                        )
                                    }
                                }
                            }

                            // Stepper Controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                IconButton(
                                    onClick = { provider.decrementQuantity(product.id) },
                                    enabled = qty > 0,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = if (qty > 0) AppTheme.colors.accentRed.copy(alpha = 0.15f) else AppTheme.colors.elevatedBackground,
                                        contentColor = if (qty > 0) AppTheme.colors.accentRed else AppTheme.colors.textMuted
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "মাইনাস",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Surface(
                                    color = if (qty > 0) AppTheme.colors.accentYellow else AppTheme.colors.elevatedBackground,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border),
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 38.dp, minHeight = 36.dp)
                                        .clickable {
                                            activeQtyEditProduct = product
                                            activeQtyText = if (qty > 0) qty.toString() else ""
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                        Text(
                                            text = DateUtils.toBengaliDigits(qty),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (qty > 0) Color.Black else AppTheme.colors.textMuted
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { provider.incrementQuantity(product.id) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = AppTheme.colors.accentYellow.copy(alpha = 0.2f),
                                        contentColor = AppTheme.colors.accentYellow
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "প্লাস",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom Expense Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "অন্যান্য / অতিরিক্ত খরচ যোগ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.accentYellow
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = expenseAmount,
                                    onValueChange = { expenseAmount = it },
                                    placeholder = { Text("টাকার পরিমাণ (যেমন ৫০)", color = AppTheme.colors.textMuted) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppTheme.colors.accentYellow,
                                        unfocusedBorderColor = AppTheme.colors.border,
                                        focusedTextColor = AppTheme.colors.textPrimary,
                                        unfocusedTextColor = AppTheme.colors.textPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        val amt = expenseAmount.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            provider.addExpense(amt)
                                            expenseAmount = ""
                                            keyboardController?.hide()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppTheme.colors.accentYellow,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Text("+ যোগ", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Expenses List
                            if (expenses.isNotEmpty()) {
                                Divider(color = AppTheme.colors.border, modifier = Modifier.padding(vertical = 4.dp))
                                expenses.forEach { exp ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppTheme.colors.elevatedBackground)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "অতিরিক্ত খরচ: ৳ ${DateUtils.toBengaliDigits(exp.amount)}",
                                            color = AppTheme.colors.textPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        IconButton(
                                            onClick = { provider.deleteExpense(exp) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = AppTheme.colors.accentRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sticky Bottom Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppTheme.colors.cardBackground,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "মোট পণ্য: ${DateUtils.toBengaliDigits(totalItemCount)} টি",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.colors.textMuted
                            )
                            Text(
                                text = "৳ ${DateUtils.toBengaliDigits(currentTotal)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (currentTotal > 0) AppTheme.colors.accentYellow else AppTheme.colors.textPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Reset button
                            OutlinedButton(
                                onClick = { provider.resetAllBill() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = AppTheme.colors.textMuted
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Text("রিসেট", fontSize = 13.sp)
                            }

                            // Cash Memo button
                            Button(
                                onClick = onNavigateSummary,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppTheme.colors.accentYellow,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ক্যাশ মেমো", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            // Settle instant sale
                            Button(
                                onClick = {
                                    if (currentTotal > 0) {
                                        // Build summary
                                        val prodMap = products.associate { it.id to it.name }
                                        val itemsText = quantities.filter { it.value > 0 }.map { (id, q) ->
                                            "${prodMap[id] ?: "পণ্য"} (${DateUtils.toBengaliDigits(q)})"
                                        }.joinToString(", ")
                                        val fullSummary = if (itemsText.isBlank()) "কাউন্টার নগদ বিক্রি" else itemsText

                                        provider.recordInstantCounterSale(
                                            amount = currentTotal,
                                            summary = fullSummary,
                                            isDue = false,
                                            customerName = "তাৎক্ষণিক কাউন্টার"
                                        )
                                        Toast.makeText(context, "বিক্রি সম্পন্ন ও ইতিহাসে সেভ হয়েছে", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "বিল খালি", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppTheme.colors.accentGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("পরিশোধ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Direct Quantity Dialog
        if (activeQtyEditProduct != null) {
            val prod = activeQtyEditProduct!!
            Dialog(onDismissRequest = { activeQtyEditProduct = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
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
                            text = "${prod.name} এর সংখ্যা লিখুন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.accentYellow
                        )

                        OutlinedTextField(
                            value = activeQtyText,
                            onValueChange = { activeQtyText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppTheme.colors.accentYellow,
                                unfocusedBorderColor = AppTheme.colors.border,
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TextButton(
                                onClick = { activeQtyEditProduct = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("বাতিল", color = AppTheme.colors.textMuted)
                            }

                            Button(
                                onClick = {
                                    val parsed = activeQtyText.toIntOrNull() ?: 0
                                    provider.setQuantity(prod.id, parsed)
                                    activeQtyEditProduct = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppTheme.colors.accentYellow,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("সেট করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
