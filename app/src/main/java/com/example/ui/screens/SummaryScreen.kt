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
import java.text.SimpleDateFormat
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

    val timestamp = remember {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date())
    }

    val receiptText = remember(activeItems, expenses, grandTotal, timestamp) {
        val sb = java.lang.StringBuilder()
        sb.append("============================\n")
        sb.append("   বিড়িখোর বিল - ক্যাশ মেমো\n")
        sb.append("============================\n")
        sb.append("তারিখ ও সময়: $timestamp\n")
        sb.append("----------------------------\n")
        sb.append("পণ্যের বিবরণ:\n")
        activeItems.forEach { (p, qty, sub) ->
            sb.append("- ${p.name} (x$qty): ৳ ${sub.toInt()} টাকা\n")
        }
        if (expenses.isNotEmpty()) {
            sb.append("----------------------------\n")
            sb.append("বিবিধ / অন্যান্য খরচ:\n")
            expenses.forEachIndexed { i, exp ->
                sb.append("- এন্ট্রি #${i + 1}: ৳ ${exp.amount.toInt()} টাকা\n")
            }
        }
        sb.append("============================\n")
        sb.append("পণ্য সামগ্রী মোট : ৳ ${totalProductsCost.toInt()} টাকা\n")
        sb.append("অন্যান্য খরচ   : ৳ ${totalExpensesCost.toInt()} টাকা\n")
        sb.append("সর্বমোট বিল    : ৳ ${grandTotal.toInt()} টাকা\n")
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
                        color = BiriYellow
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Color.White
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
                        Icon(Icons.Default.ContentCopy, contentDescription = "কপি", tint = BiriYellow)
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
                        Icon(Icons.Default.Share, contentDescription = "শেয়ার", tint = BiriYellow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BiriBlack)
            )
        },
        containerColor = BiriBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Receipt Preview Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BiriBlackCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BiriYellow)
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
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = BiriYellow
                            )
                            Text(
                                text = "ক্যাশ মেমো",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.LightGray
                            )
                            Text(
                                text = timestamp,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Divider(color = BiriBorderDark, modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }

                    if (activeItems.isEmpty() && expenses.isEmpty()) {
                        item {
                            Text(
                                text = "কোন পণ্য বা খরচ যুক্ত করা হয়নি",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(activeItems) { (product, qty, subtotal) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${product.name} (x$qty)",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "৳ ${subtotal.toInt()}",
                                    color = BiriYellow,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (expenses.isNotEmpty()) {
                            item {
                                Divider(color = BiriBorderDark, modifier = Modifier.padding(vertical = 6.dp))
                                Text("বিবিধ খরচ:", color = Color.LightGray, fontSize = 13.sp)
                            }
                            items(expenses) { exp ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "অন্যান্য পাওনা",
                                        color = Color.LightGray,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "৳ ${exp.amount.toInt()}",
                                        color = BiriYellowLight,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        item {
                            Divider(color = BiriYellow, thickness = 2.dp, modifier = Modifier.padding(vertical = 10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "সর্বমোট বিল",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "৳ ${grandTotal.toInt()} টাকা",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BiriRed
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Developed by Shanewas Shan",
                                style = MaterialTheme.typography.labelSmall,
                                color = BiriYellow,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Bottom Clear / Finish Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        provider.resetAllBill()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BiriRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BiriRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ক্লিয়ার ও শেষ", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiriYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সম্পন্ন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
