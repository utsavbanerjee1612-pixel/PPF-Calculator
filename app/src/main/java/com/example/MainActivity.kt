package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

    // Safely delegate the dynamic architecture instance initialization 
    private val ppfViewModel: PpfViewModel by viewModels()

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

            // Initializes the variable structure tracking matching what your layout uses
            val multiYearContributions by remember {
                mutableStateOf(mapOf(1 to List(12) { 0 }))
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Fully maps parameters to exactly match what PpfCalculatorScreen expects
                    PpfCalculatorScreen(
                        viewModel = ppfViewModel,
                        selectedTheme = selectedTheme,
                        onThemeSelected = { newTheme ->
                            selectedTheme = newTheme
                            sharedPref.edit().putString("selected_theme", newTheme).apply()
                        },
                        multiYearContributions = multiYearContributions,
                        onContributionChanged = { yearIndex, monthIndex, updatedValue ->
                            // This matches the structural callback expected by your screen file
                        },
                        onCalculateClicked = { finalMap ->
                            // Triggers calculation execution routines inside your architecture model
                            ppfViewModel.calculatePpf() 
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
