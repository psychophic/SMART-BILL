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
        IconOption("GAME", "Game / Carrom", Icons.Default.SportsEsports),
        IconOption("SMOKE", "Smoking Room", Icons.Default.SmokingRooms),
        IconOption("TEA", "Tea Hot", Icons.Default.LocalCafe),
        IconOption("COFFEE", "Coffee Brewing", Icons.Default.Coffee),
        IconOption("FOOD", "Restaurant Food", Icons.Default.Restaurant),
        IconOption("OTHER", "Other Categories", Icons.Default.Category)
    )
}

data class IconOption(
    val type: String,
    val name: String,
    val icon: ImageVector
)
