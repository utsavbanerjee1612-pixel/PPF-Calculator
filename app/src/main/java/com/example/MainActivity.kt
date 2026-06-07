package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.model.AppTheme
import com.example.ui.PpfCalculatorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PpfViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: PpfViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val appTheme by viewModel.appTheme.collectAsState()
      val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
      val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> systemDark
      }

      MyApplicationTheme(darkTheme = darkTheme) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          PpfCalculatorScreen(
            viewModel = viewModel,
            currentTheme = appTheme.name,
            onThemeSelected = { selectedName ->
              val enumTheme = try {
                AppTheme.valueOf(selectedName)
              } catch (e: Exception) {
                AppTheme.SYSTEM
              }
              viewModel.setAppTheme(enumTheme)
            },
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
