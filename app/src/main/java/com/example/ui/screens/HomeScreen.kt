package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.models.Customer
import com.example.providers.BillingProvider
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    provider: BillingProvider,
    paddingValues: PaddingValues,
    onNavigateSummary: () -> Unit
) {
    val activeCustomer by provider.activeCustomer.collectAsState()

    // If an individual customer's billing is active, display CustomerBillScreen
    if (activeCustomer != null) {
        CustomerBillScreen(
            customer = activeCustomer!!,
            provider = provider,
            onBack = { provider.closeCustomerBilling() }
        )
        return
    }

    val customers by provider.customers.collectAsState()
    val products by provider.products.collectAsState()

    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "DUE", "PAID"
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var newCustomerName by remember { mutableStateOf("") }

    // Instant Counter Bill sheet/mode toggle
    var showInstantCounterBill by remember { mutableStateOf(false) }

    // Filtered customer list
    val filteredCustomers = remember(customers, searchQuery, selectedFilter) {
        customers.filter { cust ->
            val matchesSearch = cust.name.contains(searchQuery.trim(), ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "DUE" -> cust.totalBill > 0
                "PAID" -> cust.totalBill == 0.0
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    // Ledger Summary Stats
    val totalDueLedger = remember(customers) {
        customers.sumOf { it.totalBill }
    }
    val dueCustomersCount = remember(customers) {
        customers.count { it.totalBill > 0 }
    }

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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
        ) {
            // Dashboard Header with ALWAYS DISPLAYED APP ICON
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BiriBlackCard),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, BiriYellow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // The App Icon permanently displayed on the Dashboard
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(2.dp, BiriYellow, RoundedCornerShape(16.dp))
                                    .background(BiriBlackElevated)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_icon_asset),
                                    contentDescription = "বিড়িখোর বিল লোগো",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "বিড়িখোর বিল",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BiriYellow,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "দোকানের খাতা ও ডিজিটাল ক্যাশ মেমো",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = "Developed by Shanewas Shan",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BiriRed,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = BiriBorderDark)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Totals Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "মোট বকেয়া খাতা",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "৳ ${String.format("%.0f", totalDueLedger)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (totalDueLedger > 0) BiriRed else BiriGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "বকেয়া কাস্টমার",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "$dueCustomersCount / ${customers.size} জন",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BiriYellow
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions: Add Customer & Instant Counter Bill
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showAddCustomerDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BiriYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ নতুন ক্রেতা",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showInstantCounterBill = !showInstantCounterBill },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (showInstantCounterBill) BiriRed else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (showInstantCounterBill) BiriRed else BiriBorderDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showInstantCounterBill) "খাতায় ফিরুন" else "তাৎক্ষণিক বিল",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // If Instant Counter Bill Mode is open, render instant product counter
            if (showInstantCounterBill) {
                item {
                    InstantCounterBillCard(
                        provider = provider,
                        onNavigateSummary = onNavigateSummary
                    )
                }
            }

            // Section Header: Customer Ledger Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ক্রেতার খাতা ও হিসাব তালিকা",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BiriYellow
                        )
                        Text(
                            text = "নামে ক্লিক করে পণ্য ও খরচ এড/এডিট করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                }
            }

            // Search and Filter Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ক্রেতার নাম দিয়ে খুঁজুন...", color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = BiriYellow)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BiriYellow,
                            unfocusedBorderColor = BiriBorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Filter chips: All, Due, Paid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("সবাই (${customers.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BiriYellow,
                                selectedLabelColor = Color.Black,
                                containerColor = BiriBlackElevated,
                                labelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "DUE",
                            onClick = { selectedFilter = "DUE" },
                            label = { Text("বকেয়া ($dueCustomersCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BiriRed,
                                selectedLabelColor = Color.White,
                                containerColor = BiriBlackElevated,
                                labelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "PAID",
                            onClick = { selectedFilter = "PAID" },
                            label = { Text("ক্লিয়ার (${customers.size - dueCustomersCount})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BiriGreen,
                                selectedLabelColor = Color.White,
                                containerColor = BiriBlackElevated,
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Customer Cards List
            if (filteredCustomers.isEmpty()) {
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
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোন ক্রেতা খুঁজে পাওয়া যায়নি",
                                color = Color.LightGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                items(filteredCustomers, key = { it.id }) { customer ->
                    val hasDue = customer.totalBill > 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                provider.openCustomerBilling(customer)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasDue) BiriBlackElevated else BiriBlackCard
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (hasDue) BiriRed.copy(alpha = 0.6f) else BiriBorderDark
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left side: Avatar + Customer Name
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (hasDue) BiriRed else Color(0xFF333333)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.name.take(1),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = if (hasDue) Color.White else BiriYellow
                                    )
                                }

                                Column {
                                    Text(
                                        text = customer.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (hasDue) "বকেয়া বিল আছে" else "হিসাব পরিশোধিত",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (hasDue) BiriYellow else BiriGreen
                                    )
                                }
                            }

                            // Right side: Total Bill Card Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = if (hasDue) BiriYellow else Color(0xFF222222),
                                    shape = RoundedCornerShape(10.dp),
                                    border = if (hasDue) null else androidx.compose.foundation.BorderStroke(1.dp, BiriBorderDark)
                                ) {
                                    Text(
                                        text = "৳ ${String.format("%.0f", customer.totalBill)}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = if (hasDue) Color.Black else Color.LightGray
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "বিল দেখুন",
                                    tint = if (hasDue) BiriYellow else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Customer Dialog
        if (showAddCustomerDialog) {
            Dialog(onDismissRequest = { showAddCustomerDialog = false }) {
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
                            text = "নতুন ক্রেতা যোগ করুন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BiriYellow
                        )

                        OutlinedTextField(
                            value = newCustomerName,
                            onValueChange = { newCustomerName = it },
                            label = { Text("ক্রেতার নাম") },
                            placeholder = { Text("যেমন: রফিক, কালাম...") },
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    showAddCustomerDialog = false
                                    newCustomerName = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("বাতিল", color = Color.Gray)
                            }

                            Button(
                                onClick = {
                                    if (newCustomerName.isNotBlank()) {
                                        provider.addCustomer(newCustomerName)
                                        newCustomerName = ""
                                        showAddCustomerDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BiriYellow,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("যোগ করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sub-composable for Instant Walk-In Counter Billing
@Composable
fun InstantCounterBillCard(
    provider: BillingProvider,
    onNavigateSummary: () -> Unit
) {
    val products by provider.products.collectAsState()
    val quantities by provider.quantities.collectAsState()
    val expenses by provider.expenses.collectAsState()

    val totalCounter = remember(quantities, expenses, products) {
        val prodMap = products.associate { it.id to it.price }
        var sum = 0.0
        quantities.forEach { (pId, qty) ->
            sum += (prodMap[pId] ?: 0.0) * qty
        }
        expenses.forEach { sum += it.amount }
        sum
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BiriBlackElevated),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BiriRed)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "তাৎক্ষণিক কাউন্টার ক্যাশ মেমো",
                        fontWeight = FontWeight.Bold,
                        color = BiriYellow,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "হাতে হাতে বিল করে ক্লিয়ার করার জন্য",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "৳ ${String.format("%.0f", totalCounter)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = BiriRed
                )
            }

            // Quick Product increments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                products.take(4).forEach { prod ->
                    val q = quantities[prod.id] ?: 0
                    OutlinedButton(
                        onClick = { provider.incrementQuantity(prod.id) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (q > 0) BiriYellow else BiriBorderDark
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (q > 0) BiriYellow.copy(alpha = 0.15f) else Color.Transparent
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(prod.name.take(6), fontSize = 11.sp, color = Color.White, maxLines = 1)
                            Text(
                                if (q > 0) "($q)" else "+ ৳${prod.price.toInt()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BiriYellow
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { provider.resetQuantities() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                ) {
                    Text("রিসেট", fontSize = 13.sp)
                }
                Button(
                    onClick = onNavigateSummary,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiriRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("মেমো দেখুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
