package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val iconType: String, // "SMOKE", "TEA", "COFFEE", "GAME", "FOOD", "OTHER"
    val isDefault: Boolean = false
)
