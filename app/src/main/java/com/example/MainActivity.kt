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

    // Move calculation logic entirely outside the Compose lifecycle scope
    private fun calculatePPFCore(
        contributions: Map<Int, List<Int>>, 
        totalYears: Int, 
        interestRate: Double
    ): Triple<Double, Double, Double> {
        var currentBalance = 0.0
        var absoluteInvestment = 0.0
        var absoluteInterest = 0.0
        val r = interestRate / 100.0

        for (year in 1..totalYears) {
            val monthlyInvestments = contributions[year] ?: List(12) { 0 }
            var yearInvestment = 0.0
            var interestEarnedThisYear = 0.0

            for (monthIndex in 0..11) {
                val monthlyDeposit = monthlyInvestments[monthIndex].toDouble()
                yearInvestment += monthlyDeposit
                currentBalance += monthlyDeposit
                interestEarnedThisYear += (currentBalance * (r / 12.0))
            }

            currentBalance += interestEarnedThisYear
            absoluteInvestment += yearInvestment
            absoluteInterest += interestEarnedThisYear
        }
        return Triple(currentBalance, absoluteInvestment, absoluteInterest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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

            // Results states tracked safely inside Compose
            var maturityAmount by remember { mutableStateOf(0.0) }
            var totalInvestment by remember { mutableStateOf(0.0) }
            var totalInterest by remember { mutableStateOf(0.0) }

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
                            val (mat, inv, int) = calculatePPFCore(finalMap, periodYears, currentRate)
                            maturityAmount = mat
                            totalInvestment = inv
                            totalInterest = int
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
