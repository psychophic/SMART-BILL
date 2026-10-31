package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.providers.BillingProvider
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SummaryScreen
import com.example.ui.theme.MyApplicationTheme

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
            NavigationBar {
              NavigationBarItem(
                selected = currentTab == "home",
                onClick = { provider.selectTab("home") },
                icon = { Icon(Icons.Default.Calculate, contentDescription = "Home") },
                label = { Text("Home") }
              )
              NavigationBarItem(
                selected = currentTab == "products",
                onClick = { provider.selectTab("products") },
                icon = { Icon(Icons.Default.Category, contentDescription = "Products") },
                label = { Text("Products") }
              )
              NavigationBarItem(
                selected = currentTab == "settings",
                onClick = { provider.selectTab("settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") }
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
