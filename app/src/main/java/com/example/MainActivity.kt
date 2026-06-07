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
   * this map to accurately compute overall accumulated maturity wealth under 7.1% (or selected) rate.
   */
  fun calculatePPF(contributions: Map<Int, List<Int>>) {
    // 1. Synchronize the user's input data structure with the viewmodel state
    viewModel.setMultiYearContributions(contributions)
    
    // 2. Perform the compounding interest loop requested by the user
    val years = viewModel.yearsInput.value.toIntOrNull() ?: 15
    val rate = viewModel.interestRateInput.value.toDoubleOrNull() ?: 7.1
    
    var principal = 0.0
    var totalInvested = 0.0
    var totalInterest = 0.0
    
    for (year in 1..years) {
      val openingBal = principal
      var accumulatedInterest = (rate / 100.0) * openingBal
      val yearContribs = contributions[year] ?: List(12) { 0 }
      
      var d = 12
      for (monthIndex in 0..11) {
        val a = if (monthIndex < yearContribs.size) yearContribs[monthIndex].toDouble() else 0.0
        principal += a
        totalInvested += a
        
        val monthInterest = (rate / 100.0) * a * d / 12.0
        accumulatedInterest += monthInterest
        d -= 1
      }
      
      val closingBal = principal + accumulatedInterest
      totalInterest += accumulatedInterest
      principal = closingBal
    }
    
    // 3. Trigger the viewmodel calculation to refresh and push the observed visual state to the UI
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
      val multiYearContributions = remember { mutableStateOf(mapOf(1 to List(12) { 0 })) }

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
              val currentMap = multiYearContributions.value.toMutableMap()
              val currentYearList = (currentMap[yearIndex] ?: List(12) { 0 }).toMutableList()
              currentYearList[monthIndex] = updatedValue
              currentMap[yearIndex] = currentYearList
              multiYearContributions.value = currentMap
            },
            onCalculateClicked = { finalMap ->
              // Trigger your core compounding interest engine loop here using the passed finalMap
              calculatePPF(finalMap)
            },
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
