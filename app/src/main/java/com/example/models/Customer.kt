package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val billItemsJson: String = "{}", // JSON map of productId (Int) -> quantity (Int)
    val expensesJson: String = "[]",   // JSON array of Double amounts
    val totalBill: Double = 0.0,       // Cached total bill amount for quick display and sorting
    val lastUpdated: Long = System.currentTimeMillis()
)
