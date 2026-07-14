package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ProxmoxViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val pveViewModel: ProxmoxViewModel = viewModel()
      
      // Compute theme dynamically based on user options setting
      val darkTheme = when (pveViewModel.themeSetting) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
      }

      val primaryColor = when (pveViewModel.primaryColorSetting) {
        "Mavi" -> androidx.compose.ui.graphics.Color(0xFF0284C7) // Sky Blue
        "Yeşil" -> androidx.compose.ui.graphics.Color(0xFF059669) // Emerald Green
        "Mor" -> androidx.compose.ui.graphics.Color(0xFF7C3AED) // Purple
        else -> androidx.compose.ui.graphics.Color(0xFFEA580C) // "Turuncu" (Orange)
      }

      MyApplicationTheme(darkTheme = darkTheme, primaryColor = primaryColor, dynamicColor = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
          var isAppUnlocked by remember { mutableStateOf(false) }

          if (pveViewModel.appLockEnabledSetting && !isAppUnlocked) {
            LockScreen(
              onUnlock = { isAppUnlocked = true }
            )
          } else {
            if (pveViewModel.selectedServer == null) {
              WelcomeScreen(
                viewModel = pveViewModel,
                onServerSelected = { server ->
                  pveViewModel.selectServer(server)
                }
              )
            } else {
              DashboardScreen(
                viewModel = pveViewModel,
                onDisconnect = {
                  pveViewModel.selectServer(null)
                  // Reset unlock state upon explicit server disconnection so it locks again on next session
                  isAppUnlocked = false
                }
              )
            }
          }
        }
      }
    }
  }
}
