package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.PpfCalculatorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PpfViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: PpfViewModel by viewModels()

  /**
   * Explicitly defined calculate function in MainActivity accepting the multi-year map
   * data structure. This ensures type safety and a single source of truth for triggering math.
   * Internally, the core compounding engine iterates through each year's key-value pairs in 
   * this map to accurately compute overall accumulated maturity wealth.
   */
  fun calculatePPF(contributions: Map<Int, List<Int>>) {
    viewModel.setMultiYearContributions(contributions)
    viewModel.calculatePPF()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
      var selectedTheme by remember {
        mutableStateOf(sharedPref.getString("selected_theme", "System") ?: "System")
      }

      val systemDark = isSystemInDarkTheme()
      val darkTheme = when (selectedTheme) {
        "Light" -> false
        "Dark" -> true
        else -> systemDark
      }

      // Track multi-year state container inside onCreate utilizing the required state declaration
      val multiYearContributions = remember {
        mutableStateOf(mapOf(1 to List(12) { 0 }))
      }

      MyApplicationTheme(darkTheme = darkTheme) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          PpfCalculatorScreen(
            viewModel = viewModel,
            selectedTheme = selectedTheme,
            onThemeSelected = { newTheme ->
              selectedTheme = newTheme
              sharedPref.edit().putString("selected_theme", newTheme).apply()
            },
            multiYearContributions = multiYearContributions.value,
            onContributionChanged = { yearIndex, monthIndex, updatedValue ->
              val currentList = multiYearContributions.value[yearIndex] ?: List(12) { 0 }
              val updatedList = currentList.toMutableList().apply {
                if (monthIndex in 0..11) {
                  set(monthIndex, updatedValue)
                }
              }
              multiYearContributions.value = multiYearContributions.value.toMutableMap().apply {
                put(yearIndex, updatedList)
              }.toMap()
            },
            onCalculateClicked = { contributionsMap ->
              // Compounding calculation cycles through each year entry inside this map to compute the overall wealth accurately
              calculatePPF(contributionsMap)
            },
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
