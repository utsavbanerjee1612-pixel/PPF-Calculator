package com.example.ppfcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ppfcalculator.ui.PpfCalculatorScreen
import com.example.ppfcalculator.ui.theme.PPFCalculatorTheme // Replace with your actual theme package if different
import com.example.ppfcalculator.viewmodel.PpfViewModel

class MainActivity : ComponentActivity() {

    // Initialize the ViewModel using the activity viewModels delegate
    private val ppfViewModel: PpfViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            PPFCalculatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Pass the active, non-null viewModel instance and modifier to the screen
                    PpfCalculatorScreen(
                        viewModel = ppfViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
