package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_records")
data class BillRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val totalAmount: Double,
    val status: String, // "DUE" (বাকী) or "PAID" (পরিশোধ)
    val itemsSummary: String = "", // e.g. "বেনসন x2, লাল চা x1"
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = "",
    val formattedTime: String = "",
    val monthKey: String = "" // "YYYY-MM" e.g. "2026-10"
)
