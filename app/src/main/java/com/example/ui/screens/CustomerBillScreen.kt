package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.utils.DateUtils
import com.example.utils.IconUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerBillScreen(
    customer: Customer,
    provider: BillingProvider,
    onBack: () -> Unit
) {
    // Back navigation
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    val products by provider.products.collectAsState()
    val quantities by provider.customerQuantities.collectAsState()
    val expenses by provider.customerExpenses.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }
    var showDeleteCustomerDialog by remember { mutableStateOf(false) }

    // Direct quantity editor dialog
    var activeQtyEditProduct by remember { mutableStateOf<Product?>(null) }
    var activeQtyText by remember { mutableStateOf("") }

    // Custom expense input
    var expenseAmount by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Calculate current items in cart
    val cartItemsTotal = remember(quantities, expenses, products) {
        val prodMap = products.associate { it.id to it.price }
        var sum = 0.0
        quantities.forEach { (pId, qty) ->
            sum += (prodMap[pId] ?: 0.0) * qty
        }
        expenses.forEach { sum += it }
        sum
    }

    val totalWithPreviousDue = customer.totalBill + cartItemsTotal

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.accentYellow
                        )
                        Text(
                            text = "ব্যক্তিগত হিসাব ও খাতা",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = AppTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    // Delete customer
                    IconButton(
                        onClick = { showDeleteCustomerDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "ক্রেতা মুছুন",
                            tint = AppTheme.colors.textMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.cardBackground
                )
            )
        },
        bottomBar = {
            // Sticky Bottom Action Bar with Total, "বাকী" and "পরিশোধ" buttons
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Display Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (cartItemsTotal > 0) "নতুন সিলেক্টেড বিল" else "পূর্বের বকেয়া",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppTheme.colors.textMuted
                            )
                            Text(
                                text = "৳ ${DateUtils.toBengaliDigits(if (cartItemsTotal > 0) cartItemsTotal else customer.totalBill)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (cartItemsTotal > 0 || customer.totalBill > 0) AppTheme.colors.accentRed else AppTheme.colors.textPrimary
                            )
                        }

                        if (cartItemsTotal > 0 && customer.totalBill > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "আগের বকেয়াসহ মোট",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppTheme.colors.textMuted
                                )
                                Text(
                                    text = "৳ ${DateUtils.toBengaliDigits(totalWithPreviousDue)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.accentYellow
                                )
                            }
                        }
                    }

                    // Two Primary Action Buttons: "বাকী" and "পরিশোধ"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // "সংরক্ষণ" Button (Red - saves as due to Khata)
                        Button(
                            onClick = {
                                if (cartItemsTotal > 0 || customer.totalBill > 0) {
                                    provider.saveCustomerDueBill()
                                    Toast.makeText(context, "${customer.name} এর মোট বিল বাকী হিসেবে সংরক্ষণ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    onBack()
                                } else {
                                    Toast.makeText(context, "কোন পণ্য বা হিসাব সিলেক্ট করা নেই", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.accentRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "সংরক্ষণ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // "পরিশোধ" Button (Green)
                        Button(
                            onClick = {
                                if (cartItemsTotal > 0 || customer.totalBill > 0) {
                                    provider.settleCustomerPaidBill()
                                    Toast.makeText(context, "${customer.name} এর হিসাব পরিশোধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    onBack()
                                } else {
                                    Toast.makeText(context, "কোন বকেয়া বা বিল অবশিষ্ট নেই", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.accentGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "পরিশোধ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = AppTheme.colors.background
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
                contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
            ) {
                // PROMINENT LIVE TOTAL & "সংরক্ষণ" HEADER CARD AT THE TOP (বড় করে লাইভ হিসাব ও সংরক্ষণ বাটন)
                item {
                    val hasDue = customer.totalBill > 0
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (cartItemsTotal > 0) AppTheme.colors.elevatedBackground else AppTheme.colors.cardBackground
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (cartItemsTotal > 0 || hasDue) AppTheme.colors.accentRed else AppTheme.colors.accentYellow
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Top Row: Avatar + Name + Due Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(if (hasDue) AppTheme.colors.accentRed else AppTheme.colors.accentGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = customer.name.take(1),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = customer.name,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = AppTheme.colors.accentYellow
                                        )
                                        Text(
                                            text = if (hasDue) "পূর্বের বকেয়া: ৳ ${DateUtils.toBengaliDigits(customer.totalBill)}" else "কোন পূর্বের বকেয়া নেই",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (hasDue) AppTheme.colors.accentRed else AppTheme.colors.accentGreen
                                        )
                                    }
                                }

                                if (hasDue) {
                                    Surface(
                                        color = AppTheme.colors.accentRed.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "বাকী আছে",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppTheme.colors.accentRed
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = AppTheme.colors.border)

                            // Main Live Calculation & "সংরক্ষণ" Row (উপরে বড় করে লাইভ হিসাব ও সংরক্ষণ বাটন)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (cartItemsTotal > 0) "টোটাল হিসাব (লাইভ বিল)" else "টোটাল হিসাব",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.textMuted
                                    )
                                    Text(
                                        text = "৳ ${DateUtils.toBengaliDigits(if (cartItemsTotal > 0) cartItemsTotal else customer.totalBill)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 30.sp,
                                        color = if (cartItemsTotal > 0 || hasDue) AppTheme.colors.accentRed else AppTheme.colors.accentYellow
                                    )
                                    if (cartItemsTotal > 0 && hasDue) {
                                        Text(
                                            text = "বকেয়াসহ মোট: ৳ ${DateUtils.toBengaliDigits(totalWithPreviousDue)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AppTheme.colors.accentYellow
                                        )
                                    }
                                }

                                // "সংরক্ষণ" Button (Saves as due to Khata / Dashboard immediately)
                                Button(
                                    onClick = {
                                        if (cartItemsTotal > 0 || customer.totalBill > 0) {
                                            provider.saveCustomerDueBill()
                                            Toast.makeText(context, "${customer.name} এর মোট বিল বাকী হিসেবে সংরক্ষণ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        } else {
                                            Toast.makeText(context, "কোন পণ্য সিলেক্ট করা নেই", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppTheme.colors.accentRed,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "সংরক্ষণ",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
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
                            color = AppTheme.colors.accentYellow
                        )
                        Text(
                            text = "ট্যাপ করে বা + / - দিয়ে কোয়ান্টিটি যোগ করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.colors.textMuted
                        )
                    }
                }

                // Products list
                items(products, key = { it.id }) { product ->
                    val qty = quantities[product.id] ?: 0
                    val subtotal = qty * product.price

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                provider.incrementCustomerQuantity(product.id)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (qty > 0) AppTheme.colors.elevatedBackground else AppTheme.colors.cardBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            if (qty > 0) 2.dp else 1.dp,
                            if (qty > 0) AppTheme.colors.accentYellow else AppTheme.colors.border
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
                                        .size(44.dp)
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
                                            fontWeight = FontWeight.Bold,
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
                                // Minus button
                                IconButton(
                                    onClick = { provider.decrementCustomerQuantity(product.id) },
                                    enabled = qty > 0,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = if (qty > 0) AppTheme.colors.accentRed.copy(alpha = 0.15f) else AppTheme.colors.elevatedBackground,
                                        contentColor = if (qty > 0) AppTheme.colors.accentRed else AppTheme.colors.textMuted
                                    ),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "মাইনাস",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Clickable Quantity Counter Badge (opens dialog for manual input)
                                Surface(
                                    color = if (qty > 0) AppTheme.colors.accentYellow else AppTheme.colors.elevatedBackground,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (qty > 0) AppTheme.colors.accentYellow else AppTheme.colors.border),
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 42.dp, minHeight = 40.dp)
                                        .clickable {
                                            activeQtyEditProduct = product
                                            activeQtyText = if (qty > 0) qty.toString() else ""
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                        Text(
                                            text = DateUtils.toBengaliDigits(qty),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = if (qty > 0) Color.Black else AppTheme.colors.textMuted
                                        )
                                    }
                                }

                                // Plus button
                                IconButton(
                                    onClick = { provider.incrementCustomerQuantity(product.id) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = AppTheme.colors.accentYellow.copy(alpha = 0.25f),
                                        contentColor = AppTheme.colors.accentYellow
                                    ),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "প্লাস",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Other / Custom Expenses
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
                                text = "অন্যান্য / অতিরিক্ত খরচ যোগ করুন",
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
                                    placeholder = { Text("খরচের টাকা (যেমন ৫০)", color = AppTheme.colors.textMuted) },
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
                                            provider.addCustomerExpense(amt)
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

                            // Quick expense chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(10.0, 20.0, 50.0, 100.0).forEach { quickVal ->
                                    OutlinedButton(
                                        onClick = { provider.addCustomerExpense(quickVal) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = AppTheme.colors.textPrimary
                                        )
                                    ) {
                                        Text("+৳${quickVal.toInt()}", fontSize = 12.sp)
                                    }
                                }
                            }

                            // Added custom expenses list
                            if (expenses.isNotEmpty()) {
                                Divider(color = AppTheme.colors.border, modifier = Modifier.padding(vertical = 4.dp))
                                Text(
                                    text = "যুক্ত অতিরিক্ত খরচ সমূহ:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AppTheme.colors.textSecondary
                                )

                                expenses.forEachIndexed { index, exp ->
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
                                            text = "অতিরিক্ত খরচ #${index + 1}: ৳ ${DateUtils.toBengaliDigits(exp)}",
                                            color = AppTheme.colors.textPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        IconButton(
                                            onClick = { provider.removeCustomerExpense(index) },
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
        }

        // Direct Quantity Input Dialog
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
                                    provider.setCustomerQuantity(prod.id, parsed)
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

        // Delete Customer Dialog
        if (showDeleteCustomerDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteCustomerDialog = false },
                title = { Text("ক্রেতা মুছুন", color = AppTheme.colors.accentRed, fontWeight = FontWeight.Bold) },
                text = { Text("আপনি কি নিশ্চিতভাবে ${customer.name} কে খাতা থেকে মুছে ফেলতে চান?", color = AppTheme.colors.textPrimary) },
                confirmButton = {
                    Button(
                        onClick = {
                            provider.deleteCustomer(customer)
                            showDeleteCustomerDialog = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.accentRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text("হ্যাঁ, মুছুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteCustomerDialog = false }) {
                        Text("বাতিল", color = AppTheme.colors.textMuted)
                    }
                },
                containerColor = AppTheme.colors.cardBackground
            )
        }
    }
}
