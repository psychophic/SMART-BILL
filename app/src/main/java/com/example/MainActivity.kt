package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.providers.BillingProvider
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InstantBillScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SummaryScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
  private val provider: BillingProvider by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val currentTab by provider.currentTab.collectAsState()
      val showSummary by provider.showSummary.collectAsState()
      val darkModeSetting by provider.darkModeSetting.collectAsState()

      MyApplicationTheme(userThemeMode = darkModeSetting) {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          containerColor = AppTheme.colors.background,
          bottomBar = {
            NavigationBar(
              containerColor = AppTheme.colors.cardBackground,
              tonalElevation = 4.dp
            ) {
              // 1. খাতা
              NavigationBarItem(
                selected = currentTab == "home",
                onClick = { provider.selectTab("home") },
                icon = { Icon(Icons.Default.Book, contentDescription = "খাতা") },
                label = { Text("খাতা", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = AppTheme.colors.accentYellow,
                  indicatorColor = AppTheme.colors.accentYellow,
                  unselectedIconColor = AppTheme.colors.textMuted,
                  unselectedTextColor = AppTheme.colors.textMuted
                )
              )
              // 2. তাৎক্ষণিক (Instant Bill)
              NavigationBarItem(
                selected = currentTab == "instant",
                onClick = { provider.selectTab("instant") },
                icon = { Icon(Icons.Default.PointOfSale, contentDescription = "তাৎক্ষণিক") },
                label = { Text("তাৎক্ষণিক", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = AppTheme.colors.accentYellow,
                  indicatorColor = AppTheme.colors.accentYellow,
                  unselectedIconColor = AppTheme.colors.textMuted,
                  unselectedTextColor = AppTheme.colors.textMuted
                )
              )
              // 3. পুরনো হিসাব (History & Monthly Reports)
              NavigationBarItem(
                selected = currentTab == "history",
                onClick = { provider.selectTab("history") },
                icon = { Icon(Icons.Default.HistoryEdu, contentDescription = "পুরনো হিসাব") },
                label = { Text("হিসাব", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = AppTheme.colors.accentYellow,
                  indicatorColor = AppTheme.colors.accentYellow,
                  unselectedIconColor = AppTheme.colors.textMuted,
                  unselectedTextColor = AppTheme.colors.textMuted
                )
              )
              // 4. পণ্য সমূহ
              NavigationBarItem(
                selected = currentTab == "products",
                onClick = { provider.selectTab("products") },
                icon = { Icon(Icons.Default.Category, contentDescription = "পণ্য সমূহ") },
                label = { Text("পণ্য", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = AppTheme.colors.accentYellow,
                  indicatorColor = AppTheme.colors.accentYellow,
                  unselectedIconColor = AppTheme.colors.textMuted,
                  unselectedTextColor = AppTheme.colors.textMuted
                )
              )
              // 5. সেটিংস
              NavigationBarItem(
                selected = currentTab == "settings",
                onClick = { provider.selectTab("settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = "সেটিংস") },
                label = { Text("সেটিংস", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = AppTheme.colors.accentYellow,
                  indicatorColor = AppTheme.colors.accentYellow,
                  unselectedIconColor = AppTheme.colors.textMuted,
                  unselectedTextColor = AppTheme.colors.textMuted
                )
              )
            }
          }
        ) { innerPadding ->
          if (showSummary) {
            SummaryScreen(
              provider = provider,
              onDismiss = { provider.setShowSummary(false) }
            )
          } else {
            when (currentTab) {
              "home" -> HomeScreen(
                provider = provider,
                paddingValues = innerPadding,
                onNavigateSummary = { provider.setShowSummary(true) }
              )
              "instant" -> InstantBillScreen(
                provider = provider,
                paddingValues = innerPadding,
                onNavigateSummary = { provider.setShowSummary(true) }
              )
              "history" -> HistoryScreen(
                provider = provider,
                paddingValues = innerPadding
              )
              "products" -> ProductsScreen(
                provider = provider,
                paddingValues = innerPadding
              )
              "settings" -> SettingsScreen(
                provider = provider,
                paddingValues = innerPadding
              )
            }
          }
        }
      }
    }
  }
}
