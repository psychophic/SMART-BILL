package com.example.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Category
import androidx.compose.ui.graphics.vector.ImageVector

object IconUtils {
    fun getIconForType(iconType: String): ImageVector {
        return when (iconType.uppercase()) {
            "SMOKE" -> Icons.Default.SmokingRooms
            "TEA" -> Icons.Default.LocalCafe
            "COFFEE" -> Icons.Default.Coffee
            "GAME" -> Icons.Default.SportsEsports
            "FOOD" -> Icons.Default.Restaurant
            else -> Icons.Default.Category
        }
    }

    val availableIconTypes = listOf(
        IconOption("SMOKE", "সিগারেট / বিড়ি", Icons.Default.SmokingRooms),
        IconOption("GAME", "ক্যারাম / খেলা", Icons.Default.SportsEsports),
        IconOption("TEA", "চা", Icons.Default.LocalCafe),
        IconOption("COFFEE", "কফি", Icons.Default.Coffee),
        IconOption("FOOD", "বিস্কুট / চানাচুর / খাবার", Icons.Default.Restaurant),
        IconOption("OTHER", "অন্যান্য", Icons.Default.Category)
    )
}

data class IconOption(
    val type: String,
    val name: String,
    val icon: ImageVector
)
