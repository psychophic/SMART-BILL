package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.providers.BillingProvider
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    provider: BillingProvider,
    paddingValues: PaddingValues
) {
    val darkModeSetting by provider.darkModeSetting.collectAsState()
    val context = LocalContext.current
    var showResetConfirm by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "সেটিংস",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppTheme.colors.accentYellow
                    )
                    Text(
                        text = "অ্যাপের থিম এবং নিয়ন্ত্রণ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary
                    )
                }
            }

            // Theme Mode
            item {
                Text(
                    text = "ডিসপ্লে ও থিম",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.accentYellow,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "থিম নির্বাচন করুন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = "ডার্ক, লাইট অথবা আপনার ফোনের সিস্টেম থিম বাছাই করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.textSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf(
                                "DARK" to "ডার্ক",
                                "LIGHT" to "লাইট",
                                "SYSTEM" to "সিস্টেম"
                            )
                            modes.forEach { (mode, label) ->
                                val isSelected = darkModeSetting == mode
                                Button(
                                    onClick = { provider.setDarkModeSetting(mode) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) AppTheme.colors.accentYellow else AppTheme.colors.elevatedBackground,
                                        contentColor = if (isSelected) Color.Black else AppTheme.colors.textPrimary
                                    ),
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reset Section
            item {
                Text(
                    text = "ডাটা রিসেট",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.accentYellow,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ডিফল্ট ডাটা রিস্টোর",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = "সকল প্রোডাক্ট এবং ১৬ জন ডিফল্ট কাস্টমার পুনরায় লোড করতে চাইলে রিসেট বাটনে চাপুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.textSecondary
                        )
                        Button(
                            onClick = { showResetConfirm = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.accentRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("সব ডাটা রিসেট করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Developer Credit & App Info
            item {
                Text(
                    text = "অ্যাপ পরিচিতি",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.accentYellow,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.accentYellow.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, AppTheme.colors.accentYellow, RoundedCornerShape(16.dp))
                                .background(AppTheme.colors.elevatedBackground)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_icon_asset),
                                contentDescription = "App Icon",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Text(
                            text = "বিড়িখোর বিল",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppTheme.colors.accentYellow
                        )
                        Text(
                            text = "ভার্সন ১.৩.০ (Instant & Due-Payment Edition)",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.textSecondary
                        )
                        Divider(color = AppTheme.colors.border, modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "Developed by Shanewas Shan",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.accentRed
                        )
                    }
                }
            }
        }

        // Reset Confirmation Dialog
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("রিসেট নিশ্চিতকরণ", color = AppTheme.colors.accentRed, fontWeight = FontWeight.Bold) },
                text = { Text("আপনি কি নিশ্চিতভাবে সব প্রোডাক্ট এবং ক্রেতার খাতা ডিফল্ট অবস্থায় ফিরিয়ে নিতে চান?", color = AppTheme.colors.textPrimary) },
                confirmButton = {
                    Button(
                        onClick = {
                            provider.resetToDefaults()
                            showResetConfirm = false
                            Toast.makeText(context, "ডিফল্ট ডাটা রিস্টোর সম্পন্ন হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.accentRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text("হ্যাঁ, রিসেট করুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) {
                        Text("বাতিল", color = AppTheme.colors.textMuted)
                    }
                },
                containerColor = AppTheme.colors.cardBackground
            )
        }
    }
}
