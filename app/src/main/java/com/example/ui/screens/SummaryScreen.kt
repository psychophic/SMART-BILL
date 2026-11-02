package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Product
import com.example.providers.BillingProvider
import com.example.ui.theme.*
import com.example.utils.DateUtils
import java.util.*

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    provider: BillingProvider,
    onDismiss: () -> Unit
) {
    BackHandler {
        onDismiss()
    }

    val products by provider.products.collectAsState()
    val expenses by provider.expenses.collectAsState()
    val quantities by provider.quantities.collectAsState()

    val context = LocalContext.current
    var customerNameInput by remember { mutableStateOf("") }

    // Calculate totals
    var totalProductsCost = 0.0
    val activeItems = remember(products, quantities) {
        val list = mutableListOf<Triple<Product, Int, Double>>()
        products.forEach { p ->
            val qty = quantities[p.id] ?: 0
            if (qty > 0) {
                val subtotal = p.price * qty
                totalProductsCost += subtotal
                list.add(Triple(p, qty, subtotal))
            }
        }
        list
    }

    val totalExpensesCost = remember(expenses) {
        expenses.sumOf { it.amount }
    }

    val grandTotal = totalProductsCost + totalExpensesCost

    val timestampFormatted = remember {
        DateUtils.formatDateTime(System.currentTimeMillis())
    }

    val receiptText = remember(activeItems, expenses, grandTotal, timestampFormatted) {
        val sb = java.lang.StringBuilder()
        sb.append("============================\n")
        sb.append("   বিড়িখোর বিল - ক্যাশ মেমো\n")
        sb.append("============================\n")
        sb.append("তারিখ ও সময়: $timestampFormatted\n")
        sb.append("----------------------------\n")
        sb.append("পণ্যের বিবরণ:\n")
        activeItems.forEach { (p, qty, sub) ->
            sb.append("- ${p.name} (x${DateUtils.toBengaliDigits(qty)}): ৳ ${DateUtils.toBengaliDigits(sub)} টাকা\n")
        }
        if (expenses.isNotEmpty()) {
            sb.append("----------------------------\n")
            sb.append("বিবিধ / অন্যান্য খরচ:\n")
            expenses.forEachIndexed { i, exp ->
                sb.append("- এন্ট্রি #${DateUtils.toBengaliDigits(i + 1)}: ৳ ${DateUtils.toBengaliDigits(exp.amount)} টাকা\n")
            }
        }
        sb.append("============================\n")
        sb.append("পণ্য সামগ্রী মোট : ৳ ${DateUtils.toBengaliDigits(totalProductsCost)} টাকা\n")
        sb.append("অন্যান্য খরচ   : ৳ ${DateUtils.toBengaliDigits(totalExpensesCost)} টাকা\n")
        sb.append("সর্বমোট বিল    : ৳ ${DateUtils.toBengaliDigits(grandTotal)} টাকা\n")
        sb.append("============================\n")
        sb.append("Developed by Shanewas Shan\n")
        sb.append("ধন্যবাদ, আবার আসবেন!\n")
        sb.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ক্যাশ মেমো ও রশিদ",
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.accentYellow
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = AppTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("BiriKhor Receipt", receiptText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "রশিদ কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "কপি", tint = AppTheme.colors.accentYellow)
                    }
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                            }
                            context.startActivity(Intent.createChooser(intent, "রশিদ শেয়ার করুন"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "শেয়ার", tint = AppTheme.colors.accentYellow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.cardBackground)
            )
        },
        bottomBar = {
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
                    // Customer Name Input Field (ঐচ্ছিক)
                    OutlinedTextField(
                        value = customerNameInput,
                        onValueChange = { customerNameInput = it },
                        placeholder = { Text("ক্রেতার নাম লিখুন (ঐচ্ছিক)", color = AppTheme.colors.textMuted, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AppTheme.colors.accentYellow)
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

                    // Two Primary Action Buttons: "বাকী" and "পরিশোধ"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // "বাকী" Button (Red)
                        Button(
                            onClick = {
                                if (grandTotal > 0) {
                                    provider.saveInstantBill(
                                        nameInput = customerNameInput,
                                        isDue = true,
                                        onComplete = { savedName ->
                                            Toast.makeText(context, "$savedName এর জন্য ৳ ${DateUtils.toBengaliDigits(grandTotal)} বাকী হিসেবে জমা হয়েছে", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        }
                                    )
                                } else {
                                    Toast.makeText(context, "কোন পণ্য সিলেক্ট করা নেই", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.accentRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("বাকী", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        // "পরিশোধ" Button (Green)
                        Button(
                            onClick = {
                                if (grandTotal > 0) {
                                    provider.saveInstantBill(
                                        nameInput = customerNameInput,
                                        isDue = false,
                                        onComplete = { savedName ->
                                            Toast.makeText(context, "$savedName এর জন্য ৳ ${DateUtils.toBengaliDigits(grandTotal)} পরিশোধ সম্পন্ন হয়েছে", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        }
                                    )
                                } else {
                                    Toast.makeText(context, "কোন পণ্য সিলেক্ট করা নেই", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.accentGreen,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("পরিশোধ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                .padding(16.dp)
        ) {
            // Receipt Preview Card
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.accentYellow)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "বিড়িখোর বিল",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppTheme.colors.accentYellow
                            )
                            Text(
                                text = "ডিজিটাল ক্যাশ মেমো রশিদ",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.textSecondary
                            )
                            Text(
                                text = "তারিখ: $timestampFormatted",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppTheme.colors.textMuted,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            if (customerNameInput.isNotBlank()) {
                                Text(
                                    text = "ক্রেতা: $customerNameInput",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.accentYellow,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            HorizontalDivider(
                                color = AppTheme.colors.border,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }

                    if (activeItems.isEmpty() && expenses.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "বিল করার মতো কোন পণ্য সিলেক্ট করা নেই",
                                    color = AppTheme.colors.textMuted
                                )
                            }
                        }
                    }

                    // Product items
                    items(activeItems) { (product, qty, subtotal) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = "৳ ${DateUtils.toBengaliDigits(product.price)} × ${DateUtils.toBengaliDigits(qty)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                            Text(
                                text = "৳ ${DateUtils.toBengaliDigits(subtotal)}",
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.accentYellow
                            )
                        }
                    }

                    // Expenses
                    if (expenses.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                color = AppTheme.colors.border,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = "অন্যান্য / বিবিধ খরচ:",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                        items(expenses) { exp ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "অতিরিক্ত খরচ",
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = "৳ ${DateUtils.toBengaliDigits(exp.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.accentRed
                                )
                            }
                        }
                    }

                    item {
                        HorizontalDivider(
                            color = AppTheme.colors.border,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        // Totals summary
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("পণ্যের মোট:", color = AppTheme.colors.textSecondary)
                                Text("৳ ${DateUtils.toBengaliDigits(totalProductsCost)}", color = AppTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            if (totalExpensesCost > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("অন্যান্য খরচ:", color = AppTheme.colors.textSecondary)
                                    Text("৳ ${DateUtils.toBengaliDigits(totalExpensesCost)}", color = AppTheme.colors.accentRed, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            HorizontalDivider(
                                color = AppTheme.colors.border,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "সর্বমোট প্রদেয় বিল:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = "৳ ${DateUtils.toBengaliDigits(grandTotal)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppTheme.colors.accentYellow
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Developed by Shanewas Shan",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.accentRed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "ধন্যবাদ, আবার আসবেন!",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.textMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
