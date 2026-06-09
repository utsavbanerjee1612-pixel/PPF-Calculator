package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.ui.PpfViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // A pure background function to compute your compounding layout math matrix
    private fun calculatePPFCore(contributions: Map<Int, List<Int>>): Triple<Double, Double, Double> {
        var currentBalance = 0.0
        var absoluteInvestment = 0.0
        var absoluteInterest = 0.0
        val defaultInterestRate = 7.1 / 100.0 // Standard default PPF baseline
        val totalPlannedYears = 15 // Matches the standard 15-year baseline matrix

        for (year in 1..totalPlannedYears) {
            val monthlyInvestments = contributions[year] ?: List(12) { 0 }
            var yearInvestment = 0.0
            var interestEarnedThisYear = 0.0

            for (monthIndex in 0..11) {
                val monthlyDeposit = monthlyInvestments[monthIndex].toDouble()
                yearInvestment += monthlyDeposit
                currentBalance += monthlyDeposit
                interestEarnedThisYear += (currentBalance * (defaultInterestRate / 12.0))
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
            // Using standard safe Compose inline initialization to clear type inferences
            val ppfViewModel: PpfViewModel = viewModel()

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

            // Calculation outcome tracking states
            var maturityAmount by remember { mutableStateOf(0.0) }
            var totalInvestment by remember { mutableStateOf(0.0) }
            var totalInterest by remember { mutableStateOf(0.0) }

            MyApplicationTheme(darkTheme = darkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PpfCalculatorScreen(
                        viewModel = ppfViewModel,
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
                        // Matches 'Function1' signature perfectly: accepts ONLY the map parameter
                        onCalculateClicked = { finalMap ->
                            val (mat, inv, int) = calculatePPFCore(finalMap)
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
