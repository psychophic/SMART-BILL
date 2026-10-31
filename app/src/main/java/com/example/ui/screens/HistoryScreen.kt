package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.BillRecord
import com.example.providers.BillingProvider
import com.example.ui.theme.*
import com.example.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    provider: BillingProvider,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val billRecords by provider.billRecords.collectAsState()

    var activeTab by remember { mutableStateOf("ALL") } // "ALL" or "MONTHLY"
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") } // "ALL", "DUE", "PAID"
    var selectedMonthKey by remember { mutableStateOf<String?>(null) }
    var recordToDelete by remember { mutableStateOf<BillRecord?>(null) }

    // Derive all available months from records or default to current month
    val availableMonths = remember(billRecords) {
        val keys = billRecords.map { it.monthKey }.filter { it.isNotBlank() }.distinct()
        if (keys.isEmpty()) {
            listOf(DateUtils.getMonthKey(System.currentTimeMillis()))
        } else {
            keys.sortedDescending()
        }
    }

    // Set default selected month if null
    LaunchedEffect(availableMonths) {
        if (selectedMonthKey == null && availableMonths.isNotEmpty()) {
            selectedMonthKey = availableMonths.first()
        }
    }

    // Filtered records for "ALL" tab
    val filteredRecords = remember(billRecords, searchQuery, statusFilter) {
        billRecords.filter { record ->
            val matchesSearch = record.customerName.contains(searchQuery.trim(), ignoreCase = true) ||
                    record.itemsSummary.contains(searchQuery.trim(), ignoreCase = true)
            val matchesStatus = when (statusFilter) {
                "DUE" -> record.status == "DUE"
                "PAID" -> record.status == "PAID"
                else -> true
            }
            matchesSearch && matchesStatus
        }
    }

    // Records for selected month
    val monthlyRecords = remember(billRecords, selectedMonthKey) {
        if (selectedMonthKey == null) emptyList()
        else billRecords.filter { it.monthKey == selectedMonthKey }
    }

    // Group monthly records by customer
    val monthlyCustomerData = remember(monthlyRecords) {
        monthlyRecords.groupBy { it.customerName }.map { (custName, records) ->
            val totalBill = records.sumOf { it.totalAmount }
            val dueTotal = records.filter { it.status == "DUE" }.sumOf { it.totalAmount }
            val paidTotal = records.filter { it.status == "PAID" }.sumOf { it.totalAmount }
            Triple(custName, records, Triple(totalBill, dueTotal, paidTotal))
        }.sortedByDescending { it.third.first }
    }

    // Monthly aggregates
    val monthTotalBill = remember(monthlyRecords) { monthlyRecords.sumOf { it.totalAmount } }
    val monthTotalDue = remember(monthlyRecords) { monthlyRecords.filter { it.status == "DUE" }.sumOf { it.totalAmount } }
    val monthTotalPaid = remember(monthlyRecords) { monthlyRecords.filter { it.status == "PAID" }.sumOf { it.totalAmount } }

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
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "পুরনো হিসাব ও রিপোর্ট",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppTheme.colors.accentYellow
                    )

                    // Tab Selector: "সকল লেনদেন" vs "মাসিক রিপোর্ট"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { activeTab = "ALL" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeTab == "ALL") AppTheme.colors.accentYellow else AppTheme.colors.elevatedBackground,
                                contentColor = if (activeTab == "ALL") Color.Black else AppTheme.colors.textPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সকল লেনদেন (${DateUtils.toBengaliDigits(billRecords.size)})", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { activeTab = "MONTHLY" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeTab == "MONTHLY") AppTheme.colors.accentYellow else AppTheme.colors.elevatedBackground,
                                contentColor = if (activeTab == "MONTHLY") Color.Black else AppTheme.colors.textPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("মাসিক হিসাব", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tab Content
            if (activeTab == "ALL") {
                // "সকল লেনদেন" View
                Column(modifier = Modifier.fillMaxSize()) {
                    // Filters row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("ক্রেতার নাম বা পণ্যের নাম দিয়ে খুঁজুন...", color = AppTheme.colors.textMuted) },
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
                            shape = RoundedCornerShape(10.dp),
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = statusFilter == "ALL",
                                onClick = { statusFilter = "ALL" },
                                label = { Text("সব হিসাব") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppTheme.colors.accentYellow,
                                    selectedLabelColor = Color.Black,
                                    containerColor = AppTheme.colors.cardBackground,
                                    labelColor = AppTheme.colors.textPrimary
                                )
                            )
                            FilterChip(
                                selected = statusFilter == "DUE",
                                onClick = { statusFilter = "DUE" },
                                label = { Text("বাকী") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppTheme.colors.accentRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = AppTheme.colors.cardBackground,
                                    labelColor = AppTheme.colors.textPrimary
                                )
                            )
                            FilterChip(
                                selected = statusFilter == "PAID",
                                onClick = { statusFilter = "PAID" },
                                label = { Text("পরিশোধ") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppTheme.colors.accentGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = AppTheme.colors.cardBackground,
                                    labelColor = AppTheme.colors.textPrimary
                                )
                            )
                        }
                    }

                    // Records List
                    if (filteredRecords.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = AppTheme.colors.textMuted, modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "কোন পুরনো হিসাবের তথ্য পাওয়া যায়নি",
                                    color = AppTheme.colors.textSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ক্রেতার বিলে 'বাকী' বা 'পরিশোধ' চাপলে এখানে রেকর্ড জমা হবে",
                                    color = AppTheme.colors.textMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 30.dp)
                        ) {
                            items(filteredRecords, key = { it.id }) { record ->
                                val isDue = record.status == "DUE"
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isDue) AppTheme.colors.accentRed.copy(alpha = 0.5f) else AppTheme.colors.border
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isDue) AppTheme.colors.accentRed else AppTheme.colors.accentGreen),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = record.customerName.take(1),
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }

                                                Column {
                                                    Text(
                                                        text = record.customerName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = AppTheme.colors.textPrimary
                                                    )
                                                    Text(
                                                        text = "${record.formattedDate} • ${record.formattedTime}",
                                                        fontSize = 11.sp,
                                                        color = AppTheme.colors.textMuted
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Surface(
                                                    color = if (isDue) AppTheme.colors.accentRed.copy(alpha = 0.15f) else AppTheme.colors.accentGreen.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = if (isDue) "বাকী" else "পরিশোধ",
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (isDue) AppTheme.colors.accentRed else AppTheme.colors.accentGreen
                                                    )
                                                }

                                                Text(
                                                    text = "৳ ${DateUtils.toBengaliDigits(record.totalAmount)}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 16.sp,
                                                    color = if (isDue) AppTheme.colors.accentRed else AppTheme.colors.accentGreen
                                                )

                                                IconButton(
                                                    onClick = { recordToDelete = record },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete record",
                                                        tint = AppTheme.colors.textMuted,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (record.itemsSummary.isNotBlank()) {
                                            Divider(color = AppTheme.colors.border)
                                            Text(
                                                text = "পণ্যের বিবরণ: ${record.itemsSummary}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppTheme.colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // "মাসিক হিসাব" View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Month Selector Chips
                    Text(
                        text = "মাস নির্বাচন করুন:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.accentYellow,
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableMonths) { monthKey ->
                            val isSelected = monthKey == selectedMonthKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMonthKey = monthKey },
                                label = { Text(DateUtils.getMonthDisplayName(monthKey)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppTheme.colors.accentYellow,
                                    selectedLabelColor = Color.Black,
                                    containerColor = AppTheme.colors.cardBackground,
                                    labelColor = AppTheme.colors.textPrimary
                                )
                            )
                        }
                    }

                    // Monthly Aggregate Overview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.accentYellow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "${selectedMonthKey?.let { DateUtils.getMonthDisplayName(it) }} এর সারসংক্ষেপ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.accentYellow
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("মোট লেনদেন বিল", style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textMuted)
                                    Text(
                                        text = "৳ ${DateUtils.toBengaliDigits(monthTotalBill)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = AppTheme.colors.textPrimary
                                    )
                                }
                                Column {
                                    Text("মোট বাকী", style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textMuted)
                                    Text(
                                        text = "৳ ${DateUtils.toBengaliDigits(monthTotalDue)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = AppTheme.colors.accentRed
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("মোট আদায়", style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textMuted)
                                    Text(
                                        text = "৳ ${DateUtils.toBengaliDigits(monthTotalPaid)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = AppTheme.colors.accentGreen
                                    )
                                }
                            }
                        }
                    }

                    // Customer Monthly Totals List
                    Text(
                        text = "ক্রেতাভিত্তিক এই মাসের মোট বিল (${DateUtils.toBengaliDigits(monthlyCustomerData.size)} জন)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.accentYellow
                    )

                    if (monthlyCustomerData.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "এই মাসে কোন ক্রেতার হিসাব নেই",
                                color = AppTheme.colors.textMuted
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 30.dp)
                        ) {
                            items(monthlyCustomerData, key = { it.first }) { (custName, records, totals) ->
                                val (totalAmt, dueAmt, paidAmt) = totals

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(AppTheme.colors.accentYellow.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = custName.take(1),
                                                        fontWeight = FontWeight.Bold,
                                                        color = AppTheme.colors.accentYellow
                                                    )
                                                }

                                                Column {
                                                    Text(
                                                        text = custName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = AppTheme.colors.textPrimary
                                                    )
                                                    Text(
                                                        text = "মোট লেনদেন: ${DateUtils.toBengaliDigits(records.size)} টি",
                                                        fontSize = 11.sp,
                                                        color = AppTheme.colors.textMuted
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "মাসের মোট বিল",
                                                    fontSize = 10.sp,
                                                    color = AppTheme.colors.textMuted
                                                )
                                                Text(
                                                    text = "৳ ${DateUtils.toBengaliDigits(totalAmt)}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 17.sp,
                                                    color = AppTheme.colors.textPrimary
                                                )
                                            }
                                        }

                                        Divider(color = AppTheme.colors.border, modifier = Modifier.padding(vertical = 2.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "বাকী: ৳ ${DateUtils.toBengaliDigits(dueAmt)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppTheme.colors.accentRed
                                            )
                                            Text(
                                                text = "পরিশোধ: ৳ ${DateUtils.toBengaliDigits(paidAmt)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppTheme.colors.accentGreen
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

        // Delete Record Confirmation Dialog
        if (recordToDelete != null) {
            val rec = recordToDelete!!
            AlertDialog(
                onDismissRequest = { recordToDelete = null },
                title = { Text("রেকর্ড মুছে ফেলুন", color = AppTheme.colors.accentRed, fontWeight = FontWeight.Bold) },
                text = { Text("আপনি কি নিশ্চিতভাবে এই লেনদেনের রেকর্ডটি মুছে ফেলতে চান?", color = AppTheme.colors.textPrimary) },
                confirmButton = {
                    Button(
                        onClick = {
                            provider.deleteBillRecord(rec)
                            recordToDelete = null
                            Toast.makeText(context, "রেকর্ড মোছা হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentRed, contentColor = Color.White)
                    ) {
                        Text("মুছুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { recordToDelete = null }) {
                        Text("বাতিল", color = AppTheme.colors.textMuted)
                    }
                },
                containerColor = AppTheme.colors.cardBackground
            )
        }
    }
}
