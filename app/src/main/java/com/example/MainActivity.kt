package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

class MainActivity : ComponentActivity() {

    override class onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        setContent {
            var selectedTheme by remember {
                mutableStateOf(sharedPref.getString("selected_theme", "System") ?: "System")
            }

            val darkTheme = when (selectedTheme) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            val multiYearContributions = remember {
                mutableStateOf(mapOf(1 to List(12) { 0 }))
            }

            // Calculation result states to pass down to UI if needed
            var maturityAmount by remember { mutableStateOf(0.0) }
            var totalInterest by remember { mutableStateOf(0.0) }
            var totalInvestment by remember { mutableStateOf(0.0) }

            fun calculatePPF(contributions: Map<Int, List<Int>>, totalYears: Int, interestRate: Double) {
                var currentBalance = 0.0
                var absoluteInvestment = 0.0
                var absoluteInterest = 0.0
                val r = interestRate / 100.0

                for (year in 1..totalYears) {
                    val monthlyInvestments = contributions[year] ?: List(12) { 0 }
                    var yearInvestment = 0.0
                    var interestEarnedThisYear = 0.0

                    // Calculate month-by-month compounding logic for the year
                    for (monthIndex in 0..11) {
                        val monthlyDeposit = monthlyInvestments[monthIndex].toDouble()
                        yearInvestment += monthlyDeposit
                        
                        // PPF Interest is computed on the lowest balance between the 5th and last day of the month
                        // For basic monthly compounding simulation:
                        currentBalance += monthlyDeposit
                        interestEarnedThisYear += (currentBalance * (r / 12.0))
                    }

                    currentBalance += interestEarnedThisYear
                    absoluteInvestment += yearInvestment
                    absoluteInterest += interestEarnedThisYear
                }

                maturityAmount = currentBalance
                totalInvestment = absoluteInvestment
                totalInterest = absoluteInterest
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PpfCalculatorScreen(
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
                        onCalculateClicked = { finalMap, periodYears, currentRate ->
                            calculatePPF(finalMap, periodYears, currentRate)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
