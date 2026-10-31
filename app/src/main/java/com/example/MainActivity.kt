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
import com.example.providers.BillingProvider
import com.example.ui.screens.HomeScreen
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
          bottomBar = {
            NavigationBar(
              containerColor = BiriBlackCard
            ) {
              NavigationBarItem(
                selected = currentTab == "home",
                onClick = { provider.selectTab("home") },
                icon = { Icon(Icons.Default.Book, contentDescription = "খাতা ও বিল") },
                label = { Text("খাতা ও বিল") },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = BiriYellow,
                  indicatorColor = BiriYellow,
                  unselectedIconColor = Color.LightGray,
                  unselectedTextColor = Color.Gray
                )
              )
              NavigationBarItem(
                selected = currentTab == "products",
                onClick = { provider.selectTab("products") },
                icon = { Icon(Icons.Default.Category, contentDescription = "পণ্য সমূহ") },
                label = { Text("পণ্য সমূহ") },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = BiriYellow,
                  indicatorColor = BiriYellow,
                  unselectedIconColor = Color.LightGray,
                  unselectedTextColor = Color.Gray
                )
              )
              NavigationBarItem(
                selected = currentTab == "settings",
                onClick = { provider.selectTab("settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = "সেটিংস") },
                label = { Text("সেটিংস") },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = Color.Black,
                  selectedTextColor = BiriYellow,
                  indicatorColor = BiriYellow,
                  unselectedIconColor = Color.LightGray,
                  unselectedTextColor = Color.Gray
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
