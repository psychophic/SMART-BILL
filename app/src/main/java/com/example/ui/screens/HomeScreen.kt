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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.providers.BillingProvider
import com.example.ui.theme.*
import com.example.utils.DateUtils

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

    val context = LocalContext.current
    val customers by provider.customers.collectAsState()
    val products by provider.products.collectAsState()

    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "DUE", "PAID"
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var newCustomerName by remember { mutableStateOf("") }

    // Sort customers: Customers with dues are sorted first and highlighted
    val sortedCustomers = remember(customers) {
        customers.sortedWith(
            compareByDescending<com.example.models.Customer> { it.totalBill > 0 }
                .thenByDescending { it.totalBill }
                .thenBy { it.id }
        )
    }

    // Filtered customer list
    val filteredCustomers = remember(sortedCustomers, searchQuery, selectedFilter) {
        sortedCustomers.filter { cust ->
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
            .background(AppTheme.colors.background)
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
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, AppTheme.colors.accentYellow)
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
                                    .border(2.dp, AppTheme.colors.accentYellow, RoundedCornerShape(16.dp))
                                    .background(AppTheme.colors.elevatedBackground)
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
                                    color = AppTheme.colors.accentYellow,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "দোকানের খাতা ও ডিজিটাল ক্যাশ মেমো",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.colors.textSecondary
                                )
                                Text(
                                    text = "Developed by Shanewas Shan",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.accentRed,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = AppTheme.colors.border)
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
                                    color = AppTheme.colors.textMuted
                                )
                                Text(
                                    text = "৳ ${DateUtils.toBengaliDigits(totalDueLedger)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (totalDueLedger > 0) AppTheme.colors.accentRed else AppTheme.colors.accentGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "বকেয়া কাস্টমার",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppTheme.colors.textMuted
                                )
                                Text(
                                    text = "${DateUtils.toBengaliDigits(dueCustomersCount)} / ${DateUtils.toBengaliDigits(customers.size)} জন",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.accentYellow
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions: "+ নতুন ক্রেতা" & "পুরনো হিসাব"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showAddCustomerDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.accentYellow,
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

                    // "পুরনো হিসাব" Button (as requested by user)
                    Button(
                        onClick = { provider.selectTab("history") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.cardBackground,
                            contentColor = AppTheme.colors.textPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.accentYellow),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = AppTheme.colors.accentYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "পুরনো হিসাব",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // HIGHLIGHTED SECTION: Due Customers (যে সকল ক্রেতার বিল বাকী সেগুলো ড্যাশবোর্ডে আলাদা করে হাইলাইট)
            if (dueCustomersCount > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (AppTheme.colors.isDark) Color(0xFF261212) else Color(0xFFFFECEC)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, AppTheme.colors.accentRed)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = AppTheme.colors.accentRed,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "বকেয়া বিল",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "বকেয়া ক্রেতাদের তালিকা (বাকী আছে)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = AppTheme.colors.accentRed
                                    )
                                }
                                Text(
                                    text = "${DateUtils.toBengaliDigits(dueCustomersCount)} জন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AppTheme.colors.accentRed
                                )
                            }

                            val dueList = customers.filter { it.totalBill > 0 }.sortedByDescending { it.totalBill }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                dueList.forEach { dueCust ->
                                    Surface(
                                        color = AppTheme.colors.cardBackground,
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.accentRed.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { provider.openCustomerBilling(dueCust) }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(AppTheme.colors.accentRed),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = dueCust.name.take(1),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = Color.White
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = dueCust.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = AppTheme.colors.textPrimary
                                                    )
                                                    Text(
                                                        text = "টোটাল বিল: ৳ ${DateUtils.toBengaliDigits(dueCust.totalBill)} (বাকী)",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = AppTheme.colors.accentRed
                                                    )
                                                }
                                            }

                                            // Direct "পরিশোধ" Button right on the highlighted due item
                                            Button(
                                                onClick = {
                                                    provider.settleCustomerDirectly(dueCust)
                                                    Toast.makeText(context, "${dueCust.name} এর বিল সম্পূর্ণ পরিশোধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = AppTheme.colors.accentGreen,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("পরিশোধ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                            color = AppTheme.colors.accentYellow
                        )
                        Text(
                            text = "নামে ক্লিক করে পণ্য ও খরচ এড বা পরিশোধ করুন",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.colors.textSecondary
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
                        placeholder = { Text("ক্রেতার নাম দিয়ে খুঁজুন...", color = AppTheme.colors.textMuted) },
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

                    // Filter chips: All, Due, Paid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("সবাই (${DateUtils.toBengaliDigits(customers.size)})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppTheme.colors.accentYellow,
                                selectedLabelColor = Color.Black,
                                containerColor = AppTheme.colors.cardBackground,
                                labelColor = AppTheme.colors.textPrimary
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "DUE",
                            onClick = { selectedFilter = "DUE" },
                            label = { Text("বকেয়া (${DateUtils.toBengaliDigits(dueCustomersCount)})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppTheme.colors.accentRed,
                                selectedLabelColor = Color.White,
                                containerColor = AppTheme.colors.cardBackground,
                                labelColor = AppTheme.colors.textPrimary
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "PAID",
                            onClick = { selectedFilter = "PAID" },
                            label = { Text("পরিশোধ (${DateUtils.toBengaliDigits(customers.size - dueCustomersCount)})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppTheme.colors.accentGreen,
                                selectedLabelColor = Color.White,
                                containerColor = AppTheme.colors.cardBackground,
                                labelColor = AppTheme.colors.textPrimary
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
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = AppTheme.colors.textMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোন ক্রেতা খুঁজে পাওয়া যায়নি",
                                color = AppTheme.colors.textSecondary,
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
                            containerColor = if (hasDue) {
                                if (AppTheme.colors.isDark) AppTheme.colors.elevatedBackground else Color(0xFFFFF5F5)
                            } else {
                                AppTheme.colors.cardBackground
                            }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            if (hasDue) 2.dp else 1.dp,
                            if (hasDue) AppTheme.colors.accentRed else AppTheme.colors.border
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
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (hasDue) AppTheme.colors.accentRed else AppTheme.colors.accentGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.name.take(1),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        color = Color.White
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = customer.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AppTheme.colors.textPrimary
                                        )
                                        if (hasDue) {
                                            Surface(
                                                color = AppTheme.colors.accentRed.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "বাকী",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppTheme.colors.accentRed,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    // Total bill next to/under customer name shown in RED if due, else GREEN
                                    Text(
                                        text = if (hasDue) {
                                            "টোটাল বিল: ৳ ${DateUtils.toBengaliDigits(customer.totalBill)} (বাকী)"
                                        } else {
                                            "হিসাব: ৳ ০ (পরিশোধিত)"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasDue) AppTheme.colors.accentRed else AppTheme.colors.accentGreen
                                    )
                                }
                            }

                            // Right side: Red Due badge + "পরিশোধ" Button (or Paid badge)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (hasDue) {
                                    // Direct "পরিশোধ" Button beside customer's name
                                    Button(
                                        onClick = {
                                            provider.settleCustomerDirectly(customer)
                                            Toast.makeText(context, "${customer.name} এর বিল সম্পূর্ণ পরিশোধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppTheme.colors.accentGreen,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("পরিশোধ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                } else {
                                    Surface(
                                        color = AppTheme.colors.accentGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.accentGreen)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = AppTheme.colors.accentGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "পরিশোধিত",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = AppTheme.colors.accentGreen
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "বিল দেখুন",
                                    tint = if (hasDue) AppTheme.colors.accentRed else AppTheme.colors.textMuted,
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
                            text = "নতুন ক্রেতা যোগ করুন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.accentYellow
                        )

                        OutlinedTextField(
                            value = newCustomerName,
                            onValueChange = { newCustomerName = it },
                            label = { Text("ক্রেতার নাম") },
                            placeholder = { Text("যেমন: রফিক, কালাম...") },
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
                                Text("বাতিল", color = AppTheme.colors.textMuted)
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
                                    containerColor = AppTheme.colors.accentYellow,
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
