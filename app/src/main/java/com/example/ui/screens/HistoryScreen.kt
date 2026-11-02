package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.models.BillRecord
import com.example.providers.BillingProvider
import com.example.ui.theme.*
import com.example.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var sourceFilter by remember { mutableStateOf("ALL") } // "ALL", "KHATA", "INSTANT"
    var statusFilter by remember { mutableStateOf("ALL") } // "ALL", "DUE", "PAID"
    var selectedMonthKey by remember { mutableStateOf(DateUtils.getMonthKey(System.currentTimeMillis())) }
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    val currentYear = remember { SimpleDateFormat("yyyy", Locale.US).format(Date()) }
    val all12MonthKeys = remember(currentYear) {
        (1..12).map { String.format(Locale.US, "%s-%02d", currentYear, it) }
    }

    // Dialog states
    var recordToDelete by remember { mutableStateOf<BillRecord?>(null) }
    var recordToEdit by remember { mutableStateOf<BillRecord?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showConfirmResetTransactionsDialog by remember { mutableStateOf(false) }
    var showConfirmFactoryResetDialog by remember { mutableStateOf(false) }

    // Expandable customer in monthly tab
    var expandedCustomerName by remember { mutableStateOf<String?>(null) }

    // Filtered records for "ALL" tab
    val filteredRecords = remember(billRecords, searchQuery, sourceFilter, statusFilter) {
        billRecords.filter { record ->
            val matchesSearch = record.customerName.contains(searchQuery.trim(), ignoreCase = true) ||
                    record.itemsSummary.contains(searchQuery.trim(), ignoreCase = true)
            val isInstant = record.customerId == 0L || record.customerName.contains("কাউন্টার")
            val matchesSource = when (sourceFilter) {
                "KHATA" -> !isInstant
                "INSTANT" -> isInstant
                else -> true
            }
            val matchesStatus = when (statusFilter) {
                "DUE" -> record.status == "DUE"
                "PAID" -> record.status == "PAID"
                else -> true
            }
            matchesSearch && matchesSource && matchesStatus
        }
    }

    // Overall Totals across all records (তাৎক্ষণিক + খাতা উভয় জায়গার টোটাল বিল)
    val overallTotalBill = remember(billRecords) { billRecords.sumOf { it.totalAmount } }
    val overallTotalDue = remember(billRecords) { billRecords.filter { it.status == "DUE" }.sumOf { it.totalAmount } }
    val overallTotalPaid = remember(billRecords) { billRecords.filter { it.status == "PAID" }.sumOf { it.totalAmount } }

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
            val isInstant = records.any { it.customerId == 0L }
            object {
                val name = custName
                val billList = records
                val total = totalBill
                val due = dueTotal
                val paid = paidTotal
                val instant = isInstant
            }
        }.sortedByDescending { it.total }
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
            // Header with Reset button
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "হিসাব ও বিস্তারিত রিপোর্ট",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppTheme.colors.accentYellow
                            )
                            Text(
                                text = "তাৎক্ষণিক ও খাতার সকল মাসিক ও মোট বিল",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.colors.textSecondary
                            )
                        }

                        // "রিসেট" Action Button
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppTheme.colors.accentRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.accentRed),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AppTheme.colors.accentRed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "রিসেট",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AppTheme.colors.accentRed
                            )
                        }
                    }

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
                    // Summary Totals Row (তাৎক্ষণিক + খাতা উভয় জায়গার টোটাল বিল)
                    Surface(
                        color = AppTheme.colors.cardBackground,
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("সর্বমোট বিল", fontSize = 11.sp, color = AppTheme.colors.textMuted)
                                Text(
                                    text = "৳ ${DateUtils.toBengaliDigits(overallTotalBill)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = AppTheme.colors.accentYellow
                                )
                            }
                            Column {
                                Text("মোট বকেয়া (বাকী)", fontSize = 11.sp, color = AppTheme.colors.textMuted)
                                Text(
                                    text = "৳ ${DateUtils.toBengaliDigits(overallTotalDue)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = AppTheme.colors.accentRed
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("মোট আদায় (নগদ)", fontSize = 11.sp, color = AppTheme.colors.textMuted)
                                Text(
                                    text = "৳ ${DateUtils.toBengaliDigits(overallTotalPaid)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = AppTheme.colors.accentGreen
                                )
                            }
                        }
                    }

                    // Search and Filter row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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

                        // Filters: Source and Status
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = sourceFilter == "ALL",
                                    onClick = { sourceFilter = "ALL" },
                                    label = { Text("সব উৎস") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppTheme.colors.accentYellow,
                                        selectedLabelColor = Color.Black,
                                        containerColor = AppTheme.colors.cardBackground,
                                        labelColor = AppTheme.colors.textPrimary
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = sourceFilter == "KHATA",
                                    onClick = { sourceFilter = "KHATA" },
                                    label = { Text("খাতা") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppTheme.colors.accentYellow,
                                        selectedLabelColor = Color.Black,
                                        containerColor = AppTheme.colors.cardBackground,
                                        labelColor = AppTheme.colors.textPrimary
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = sourceFilter == "INSTANT",
                                    onClick = { sourceFilter = "INSTANT" },
                                    label = { Text("তাৎক্ষণিক") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppTheme.colors.accentYellow,
                                        selectedLabelColor = Color.Black,
                                        containerColor = AppTheme.colors.cardBackground,
                                        labelColor = AppTheme.colors.textPrimary
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = statusFilter == "DUE",
                                    onClick = { statusFilter = if (statusFilter == "DUE") "ALL" else "DUE" },
                                    label = { Text("বাকী") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppTheme.colors.accentRed,
                                        selectedLabelColor = Color.White,
                                        containerColor = AppTheme.colors.cardBackground,
                                        labelColor = AppTheme.colors.textPrimary
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = statusFilter == "PAID",
                                    onClick = { statusFilter = if (statusFilter == "PAID") "ALL" else "PAID" },
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
                                    text = "কোন হিসাব বা লেনদেনের তথ্য পাওয়া যায়নি",
                                    color = AppTheme.colors.textSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "তাৎক্ষণিক বা ক্রেতার বিল সেভ করলে এখানে তালিকা তৈরি হবে",
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
                                val isInstant = record.customerId == 0L || record.customerName.contains("কাউন্টার")

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
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
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

                                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = record.customerName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp,
                                                            color = AppTheme.colors.textPrimary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Surface(
                                                            color = if (isInstant) AppTheme.colors.accentYellow.copy(alpha = 0.15f) else Color(0xFF2196F3).copy(alpha = 0.15f),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(
                                                                text = if (isInstant) "তাৎক্ষণিক" else "খাতা",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isInstant) AppTheme.colors.accentYellow else Color(0xFF42A5F5),
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "${record.formattedDate} • ${record.formattedTime}",
                                                        fontSize = 11.sp,
                                                        color = AppTheme.colors.textMuted,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    color = if (isDue) AppTheme.colors.accentRed.copy(alpha = 0.15f) else AppTheme.colors.accentGreen.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = if (isDue) "বাকী" else "পরিশোধ",
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (isDue) AppTheme.colors.accentRed else AppTheme.colors.accentGreen,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }

                                                Text(
                                                    text = "৳ ${DateUtils.toBengaliDigits(record.totalAmount)}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 16.sp,
                                                    color = if (isDue) AppTheme.colors.accentRed else AppTheme.colors.accentGreen,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
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

                                        // Action buttons row: এডিট (Edit) & ডিলিট (Delete)
                                        Divider(color = AppTheme.colors.border)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // "এডিট" Button
                                            TextButton(
                                                onClick = { recordToEdit = record },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "এডিট",
                                                    tint = AppTheme.colors.accentYellow,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "এডিট",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppTheme.colors.accentYellow
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // "ডিলিট" Button
                                            TextButton(
                                                onClick = { recordToDelete = record },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteOutline,
                                                    contentDescription = "ডিলিট",
                                                    tint = AppTheme.colors.accentRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "ডিলিট",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppTheme.colors.accentRed
                                                )
                                            }
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
                    // Month Selector Dropdown (১২টি মাসের ড্রপডাউন মেনু)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মাস নির্বাচন করুন:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.accentYellow
                        )

                        Box {
                            Surface(
                                onClick = { monthDropdownExpanded = true },
                                color = AppTheme.colors.cardBackground,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.accentYellow)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = AppTheme.colors.accentYellow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = DateUtils.getMonthDisplayName(selectedMonthKey),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AppTheme.colors.textPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = AppTheme.colors.accentYellow
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = monthDropdownExpanded,
                                onDismissRequest = { monthDropdownExpanded = false },
                                modifier = Modifier
                                    .background(AppTheme.colors.cardBackground)
                                    .widthIn(min = 190.dp)
                            ) {
                                all12MonthKeys.forEach { mKey ->
                                    val isSelected = mKey == selectedMonthKey
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = DateUtils.getMonthDisplayName(mKey),
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                color = if (isSelected) AppTheme.colors.accentYellow else AppTheme.colors.textPrimary
                                            )
                                        },
                                        trailingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = AppTheme.colors.accentYellow,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null,
                                        onClick = {
                                            selectedMonthKey = mKey
                                            monthDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Monthly Aggregate Overview Card (তাৎক্ষণিক ও খাতা উভয় জায়গার মাসিক বিল)
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
                                text = "${DateUtils.getMonthDisplayName(selectedMonthKey)} এর মোট হিসাব",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.accentYellow
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("মাসের মোট বিল", style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textMuted)
                                    Text(
                                        text = "৳ ${DateUtils.toBengaliDigits(monthTotalBill)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = AppTheme.colors.textPrimary
                                    )
                                }
                                Column {
                                    Text("মাসের বাকী", style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textMuted)
                                    Text(
                                        text = "৳ ${DateUtils.toBengaliDigits(monthTotalDue)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = AppTheme.colors.accentRed
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("মাসের পরিশোধ", style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textMuted)
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
                        text = "ক্রেতার মাসিক বিল (${DateUtils.toBengaliDigits(monthlyCustomerData.size)} জন)",
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
                                text = "এই মাসে কোন ক্রেতা বা কাউন্টারের হিসাব নেই",
                                color = AppTheme.colors.textMuted
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 30.dp)
                        ) {
                            items(monthlyCustomerData, key = { it.name }) { custItem ->
                                val isExpanded = expandedCustomerName == custItem.name

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedCustomerName = if (isExpanded) null else custItem.name
                                        },
                                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
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
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(CircleShape)
                                                        .background(AppTheme.colors.accentYellow.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = custItem.name.take(1),
                                                        fontWeight = FontWeight.Bold,
                                                        color = AppTheme.colors.accentYellow
                                                    )
                                                }

                                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text(
                                                            text = custItem.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp,
                                                            color = AppTheme.colors.textPrimary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        if (custItem.instant) {
                                                            Surface(
                                                                color = AppTheme.colors.accentYellow.copy(alpha = 0.15f),
                                                                shape = RoundedCornerShape(6.dp)
                                                            ) {
                                                                Text(
                                                                    text = "কাউন্টার",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = AppTheme.colors.accentYellow,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = "মোট মেমো: ${DateUtils.toBengaliDigits(custItem.billList.size)} টি • বিস্তারিত দেখতে ক্লিক করুন",
                                                        fontSize = 11.sp,
                                                        color = AppTheme.colors.textMuted,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            // Monthly total bill on a single line
                                            Text(
                                                text = "মোট বিল: ৳ ${DateUtils.toBengaliDigits(custItem.total)}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = AppTheme.colors.accentYellow,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }

                                        Divider(color = AppTheme.colors.border, modifier = Modifier.padding(vertical = 2.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "বাকী: ৳ ${DateUtils.toBengaliDigits(custItem.due)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppTheme.colors.accentRed,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Text(
                                                text = "পরিশোধ: ৳ ${DateUtils.toBengaliDigits(custItem.paid)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppTheme.colors.accentGreen,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }

                                        // Expanded individual bills with Edit and Delete options
                                        if (isExpanded) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Divider(color = AppTheme.colors.accentYellow.copy(alpha = 0.5f))
                                            Text(
                                                text = "${custItem.name} এর এই মাসের লেনদেন সমূহ:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppTheme.colors.accentYellow,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )

                                            custItem.billList.forEach { rec ->
                                                Surface(
                                                    color = AppTheme.colors.elevatedBackground,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(10.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = "৳ ${DateUtils.toBengaliDigits(rec.totalAmount)}",
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 14.sp,
                                                                    color = if (rec.status == "DUE") AppTheme.colors.accentRed else AppTheme.colors.accentGreen
                                                                )
                                                                Text(
                                                                    text = "(${if (rec.status == "DUE") "বাকী" else "পরিশোধ"})",
                                                                    fontSize = 11.sp,
                                                                    color = if (rec.status == "DUE") AppTheme.colors.accentRed else AppTheme.colors.accentGreen
                                                                )
                                                            }
                                                            Text(
                                                                text = "${rec.formattedDate} • ${rec.formattedTime}",
                                                                fontSize = 10.sp,
                                                                color = AppTheme.colors.textMuted
                                                            )
                                                            if (rec.itemsSummary.isNotBlank()) {
                                                                Text(
                                                                    text = rec.itemsSummary,
                                                                    fontSize = 11.sp,
                                                                    color = AppTheme.colors.textSecondary
                                                                )
                                                            }
                                                        }

                                                        Row {
                                                            IconButton(
                                                                onClick = { recordToEdit = rec },
                                                                modifier = Modifier.size(30.dp)
                                                            ) {
                                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AppTheme.colors.accentYellow, modifier = Modifier.size(16.dp))
                                                            }
                                                            IconButton(
                                                                onClick = { recordToDelete = rec },
                                                                modifier = Modifier.size(30.dp)
                                                            ) {
                                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AppTheme.colors.accentRed, modifier = Modifier.size(16.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= DIALOGS =================

        // 1. Edit Record Dialog (এডিট অপশন)
        if (recordToEdit != null) {
            val rec = recordToEdit!!
            var editName by remember { mutableStateOf(rec.customerName) }
            var editAmountText by remember { mutableStateOf(rec.totalAmount.toString()) }
            var editStatus by remember { mutableStateOf(rec.status) }
            var editSummary by remember { mutableStateOf(rec.itemsSummary) }

            Dialog(onDismissRequest = { recordToEdit = null }) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.accentYellow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "বিল রেকর্ড এডিট করুন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.accentYellow
                        )

                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("ক্রেতা / কাউন্টারের নাম") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppTheme.colors.accentYellow,
                                unfocusedBorderColor = AppTheme.colors.border,
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editAmountText,
                            onValueChange = { editAmountText = it },
                            label = { Text("বিলের টাকার পরিমাণ") },
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

                        // Status radio buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = editStatus == "DUE",
                                onClick = { editStatus = "DUE" },
                                label = { Text("বাকী") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppTheme.colors.accentRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = AppTheme.colors.cardBackground,
                                    labelColor = AppTheme.colors.textPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = editStatus == "PAID",
                                onClick = { editStatus = "PAID" },
                                label = { Text("পরিশোধ") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppTheme.colors.accentGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = AppTheme.colors.cardBackground,
                                    labelColor = AppTheme.colors.textPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = editSummary,
                            onValueChange = { editSummary = it },
                            label = { Text("পণ্যের বিবরণ / নোট") },
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
                                onClick = { recordToEdit = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("বাতিল", color = AppTheme.colors.textMuted)
                            }

                            Button(
                                onClick = {
                                    val parsedAmt = editAmountText.toDoubleOrNull() ?: rec.totalAmount
                                    val updated = rec.copy(
                                        customerName = editName.trim(),
                                        totalAmount = parsedAmt,
                                        status = editStatus,
                                        itemsSummary = editSummary.trim()
                                    )
                                    provider.updateBillRecord(updated)
                                    recordToEdit = null
                                    Toast.makeText(context, "বিল রেকর্ড আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppTheme.colors.accentYellow,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("সেভ করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 2. Delete Record Confirmation Dialog (ডিলিট অপশন)
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

        // 3. Reset Options Dialog (সবকিছু রিসেট করার অপশন)
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AppTheme.colors.accentRed)
                        Text("হিসাব ও ডাটা রিসেট", color = AppTheme.colors.accentRed, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "আপনি কোন ধরনের রিসেট করতে চান?",
                            color = AppTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Option 1: Transactions & Dues Reset Only
                        Button(
                            onClick = {
                                showResetDialog = false
                                showConfirmResetTransactionsDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.accentYellow,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("১। শুধুমাত্র লেনদেন ও বকেয়া রিসেট", fontWeight = FontWeight.Bold)
                        }

                        // Option 2: Factory Reset Everything
                        Button(
                            onClick = {
                                showResetDialog = false
                                showConfirmFactoryResetDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.accentRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("২। সবকিছু ফ্যাক্টরি রিসেট (সকল ডাটা)", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("বাতিল", color = AppTheme.colors.textMuted)
                    }
                },
                containerColor = AppTheme.colors.cardBackground
            )
        }

        // Confirm Reset Transactions Dialog
        if (showConfirmResetTransactionsDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmResetTransactionsDialog = false },
                title = { Text("লেনদেন ও বকেয়া রিসেট", color = AppTheme.colors.accentRed, fontWeight = FontWeight.Bold) },
                text = { Text("সকল পুরনো বিল, মেমো ও খাতার বকেয়া শূন্য করা হবে। ক্রেতা ও পণ্যের তালিকা অক্ষুণ্ণ থাকবে। আপনি কি নিশ্চিত?", color = AppTheme.colors.textPrimary) },
                confirmButton = {
                    Button(
                        onClick = {
                            provider.resetTransactionsOnly()
                            showConfirmResetTransactionsDialog = false
                            Toast.makeText(context, "সকল লেনদেন ও বকেয়া সফলভাবে রিসেট করা হয়েছে", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentRed, contentColor = Color.White)
                    ) {
                        Text("হ্যাঁ, রিসেট করুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmResetTransactionsDialog = false }) {
                        Text("বাতিল", color = AppTheme.colors.textMuted)
                    }
                },
                containerColor = AppTheme.colors.cardBackground
            )
        }

        // Confirm Factory Reset Dialog
        if (showConfirmFactoryResetDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmFactoryResetDialog = false },
                title = { Text("ফ্যাক্টরি রিসেট", color = AppTheme.colors.accentRed, fontWeight = FontWeight.Bold) },
                text = { Text("অ্যাপের সকল ডাটা মুছে প্রাথমিক ডিফল্ট অবস্থায় ফিরিয়ে নেওয়া হবে। আপনি কি নিশ্চিত?", color = AppTheme.colors.textPrimary) },
                confirmButton = {
                    Button(
                        onClick = {
                            provider.resetToDefaults()
                            showConfirmFactoryResetDialog = false
                            Toast.makeText(context, "অ্যাপ প্রাথমিক অবস্থায় রিসেট করা হয়েছে", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accentRed, contentColor = Color.White)
                    ) {
                        Text("হ্যাঁ, ফ্যাক্টরি রিসেট করুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmFactoryResetDialog = false }) {
                        Text("বাতিল", color = AppTheme.colors.textMuted)
                    }
                },
                containerColor = AppTheme.colors.cardBackground
            )
        }
    }
}
