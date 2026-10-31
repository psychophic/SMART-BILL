package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.models.Customer
import com.example.models.Product
import com.example.providers.BillingProvider
import com.example.ui.theme.*
import com.example.utils.IconUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerBillScreen(
    customer: Customer,
    provider: BillingProvider,
    onBack: () -> Unit
) {
    // Hardware/gesture back handler
    BackHandler {
        onBack()
    }

    val products by provider.products.collectAsState()
    val quantities by provider.customerQuantities.collectAsState()
    val expenses by provider.customerExpenses.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }
    var showSavedSnackbar by remember { mutableStateOf(false) }
    var showDeleteCustomerDialog by remember { mutableStateOf(false) }

    // Direct quantity editor dialog
    var activeQtyEditProduct by remember { mutableStateOf<Product?>(null) }
    var activeQtyText by remember { mutableStateOf("") }

    // Custom expense input
    var expenseAmount by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Calculate total bill
    val currentTotal = remember(quantities, expenses, products) {
        val prodMap = products.associate { it.id to it.price }
        var sum = 0.0
        quantities.forEach { (pId, qty) ->
            sum += (prodMap[pId] ?: 0.0) * qty
        }
        expenses.forEach { sum += it }
        sum
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BiriYellow
                        )
                        Text(
                            text = "ব্যক্তিগত হিসাব ও খাতা",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Clear / Settle bill button
                    IconButton(
                        onClick = { showClearDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = BiriRed.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "হিসাব পরিশোধ / ক্লিয়ার",
                            tint = BiriRed
                        )
                    }
                    // Delete customer
                    IconButton(
                        onClick = { showDeleteCustomerDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "ক্রেতা মুছুন",
                            tint = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BiriBlack
                )
            )
        },
        bottomBar = {
            // Sticky Bottom Action Bar with Total and Save button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BiriBlackCard,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "সর্বমোট বকেয়া বিল",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "৳ ${String.format("%.0f", currentTotal)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (currentTotal > 0) BiriYellow else Color.White
                            )
                        }

                        Button(
                            onClick = {
                                provider.saveActiveCustomerBill()
                                showSavedSnackbar = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BiriYellow,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .defaultMinSize(minWidth = 140.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "বিল সেভ করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = BiriBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                // Customer Status Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BiriBlackCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (currentTotal > 0) BiriRed.copy(alpha = 0.5f) else BiriBorderDark
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (currentTotal > 0) BiriRed else BiriYellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = if (currentTotal > 0) Color.White else Color.Black
                                    )
                                }
                                Column {
                                    Text(
                                        text = "${customer.name} এর হিসাব",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (currentTotal > 0) "বকেয়া বিল আছে" else "হিসাব পরিশোধিত",
                                        fontSize = 12.sp,
                                        color = if (currentTotal > 0) BiriYellow else BiriGreen
                                    )
                                }
                            }

                            if (currentTotal > 0) {
                                OutlinedButton(
                                    onClick = { showClearDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = BiriRed
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BiriRed),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("পরিশোধ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Section Title: Products
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "পণ্য সমূহ (প্রোডাক্ট লিস্ট)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BiriYellow
                        )
                        Text(
                            text = "সংখ্যা যোগ বা বিয়োগ করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Products items
                items(products, key = { it.id }) { product ->
                    val qty = quantities[product.id] ?: 0
                    val subtotal = qty * product.price

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (qty > 0) BiriBlackElevated else BiriBlackCard
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (qty > 0) BiriYellow.copy(alpha = 0.6f) else BiriBorderDark
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon and Product info
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (qty > 0) BiriYellow.copy(alpha = 0.2f) else Color(0xFF222222)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconUtils.getIconForType(product.iconType),
                                        contentDescription = null,
                                        tint = if (qty > 0) BiriYellow else Color.LightGray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "৳ ${String.format("%.0f", product.price)} প্রতি পিস",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray
                                    )
                                    if (qty > 0) {
                                        Text(
                                            text = "সাবটোটাল: ৳ ${String.format("%.0f", subtotal)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BiriYellow
                                        )
                                    }
                                }
                            }

                            // Stepper Controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Minus button
                                IconButton(
                                    onClick = { provider.decrementCustomerQuantity(product.id) },
                                    enabled = qty > 0,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = if (qty > 0) BiriRedContainer else Color(0xFF222222),
                                        contentColor = if (qty > 0) BiriRed else Color.DarkGray
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "কমান",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Quantity badge (clickable for direct input)
                                Box(
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 38.dp, minHeight = 36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (qty > 0) BiriYellow else Color(0xFF2A2A2A))
                                        .clickable {
                                            activeQtyEditProduct = product
                                            activeQtyText = if (qty > 0) qty.toString() else ""
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = qty.toString(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (qty > 0) Color.Black else Color.Gray
                                    )
                                }

                                // Plus button
                                IconButton(
                                    onClick = { provider.incrementCustomerQuantity(product.id) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = BiriYellow.copy(alpha = 0.2f),
                                        contentColor = BiriYellow
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "বাড়ান",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Title: Other / Miscellaneous Expenses
                item {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "অন্যান্য / বিবিধ খরচ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BiriYellow
                        )
                        Text(
                            text = "প্রোডাক্ট ছাড়া অতিরিক্ত কোন পাওনা বা খরচ থাকলে যোগ করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Quick add expense input & chips
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BiriBlackCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BiriBorderDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = expenseAmount,
                                    onValueChange = { expenseAmount = it },
                                    label = { Text("টাকার পরিমাণ (৳)") },
                                    placeholder = { Text("যেমন: ২০") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BiriYellow,
                                        unfocusedBorderColor = BiriBorderDark,
                                        focusedLabelColor = BiriYellow
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        val amt = expenseAmount.toDoubleOrNull()
                                        if (amt != null && amt > 0) {
                                            provider.addCustomerExpense(amt)
                                            expenseAmount = ""
                                            keyboardController?.hide()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BiriRed,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(54.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("যোগ", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Quick preset chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(10.0, 20.0, 50.0, 100.0).forEach { preset ->
                                    AssistChip(
                                        onClick = {
                                            provider.addCustomerExpense(preset)
                                        },
                                        label = { Text("+ ৳ ${preset.toInt()}") },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = Color(0xFF222222),
                                            labelColor = BiriYellow
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BiriBorderDark)
                                    )
                                }
                            }

                            // List of current added expenses
                            if (expenses.isNotEmpty()) {
                                Divider(color = BiriBorderDark)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    expenses.forEachIndexed { idx, exp ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "অতিরিক্ত খরচ #${idx + 1}:  ৳ ${String.format("%.0f", exp)}",
                                                color = Color.LightGray,
                                                fontSize = 14.sp
                                            )
                                            IconButton(
                                                onClick = { provider.removeCustomerExpense(idx) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "মুছুন",
                                                    tint = BiriRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Direct Quantity Editor Dialog
            if (activeQtyEditProduct != null) {
                val prod = activeQtyEditProduct!!
                Dialog(onDismissRequest = { activeQtyEditProduct = null }) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BiriBlackCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BiriYellow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "${prod.name} এর পরিমাণ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BiriYellow
                            )

                            OutlinedTextField(
                                value = activeQtyText,
                                onValueChange = { activeQtyText = it },
                                label = { Text("পিস / সংখ্যা") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BiriYellow,
                                    unfocusedBorderColor = BiriBorderDark
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = { activeQtyEditProduct = null },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("বাতিল", color = Color.Gray)
                                }
                                Button(
                                    onClick = {
                                        val qty = activeQtyText.toIntOrNull() ?: 0
                                        provider.setCustomerQuantity(prod.id, qty)
                                        activeQtyEditProduct = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BiriYellow,
                                        contentColor = Color.Black
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("ঠিক আছে", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Clear Bill Confirmation Dialog
            if (showClearDialog) {
                AlertDialog(
                    onDismissRequest = { showClearDialog = false },
                    title = {
                        Text(
                            text = "বিল পরিশোধ নিশ্চিতকরণ",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Text(
                            text = "${customer.name} কি সম্পূর্ণ বিল পরিশোধ করেছেন? বিল ক্লিয়ার করতে চান?",
                            color = Color.LightGray
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                provider.clearActiveCustomerBill()
                                showClearDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BiriRed,
                                contentColor = Color.White
                            )
                        ) {
                            Text("হ্যাঁ, ক্লিয়ার করুন", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearDialog = false }) {
                            Text("না", color = Color.Gray)
                        }
                    },
                    containerColor = BiriBlackCard
                )
            }

            // Delete Customer Dialog
            if (showDeleteCustomerDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteCustomerDialog = false },
                    title = {
                        Text(
                            text = "ক্রেতা মুছে ফেলা",
                            fontWeight = FontWeight.Bold,
                            color = BiriRed
                        )
                    },
                    text = {
                        Text(
                            text = "আপনি কি নিশ্চিতভাবে ${customer.name}-কে তালিকা থেকে মুছে ফেলতে চান?",
                            color = Color.LightGray
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                provider.deleteCustomer(customer)
                                showDeleteCustomerDialog = false
                                onBack()
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
                        TextButton(onClick = { showDeleteCustomerDialog = false }) {
                            Text("বাতিল", color = Color.Gray)
                        }
                    },
                    containerColor = BiriBlackCard
                )
            }

            // Save Confirmation Snackbar
            if (showSavedSnackbar) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { showSavedSnackbar = false }) {
                            Text("ঠিক আছে", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = BiriYellow,
                    contentColor = Color.Black
                ) {
                    Text(
                        text = "${customer.name} এর বিল সফলভাবে সেভ হয়েছে!",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
