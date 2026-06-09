package com.example.ppfcalculator.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.pow

class PpfViewModel : ViewModel() {

    // UI Input States
    var yearlyInvestment by mutableStateOf("")
    var interestRate by mutableStateOf("7.1") // Default current PPF rate
    var timePeriodYears by mutableStateOf("15") // Minimum PPF lock-in period

    // UI Output States
    var totalInvestment by mutableStateOf(0.0)
    var totalInterestEarned by mutableStateOf(0.0)
    var maturityAmount by mutableStateOf(0.0)

    /**
     * Executes the PPF compounding calculation loop.
     * Accessible directly from the UI via viewModel.calculatePpf()
     */
    fun calculatePpf() {
        val p = yearlyInvestment.toDoubleOrNull() ?: 0.0
        val r = (interestRate.toDoubleOrNull() ?: 7.1) / 100.0
        val n = timePeriodYears.toIntOrNull() ?: 15

        if (p <= 0.0 || r <= 0.0 || n <= 0) {
            resetOutputs()
            return
        }

        var f = 0.0
        var totalInvestedSum = 0.0

        // PPF Compound Interest Formula applied per financial year
        for (i in 1..n) {
            totalInvestedSum += p
            f = (f + p) * (1 + r)
        }

        maturityAmount = f
        totalInvestment = totalInvestedSum
        totalInterestEarned = maturityAmount - totalInvestment
    }

    private fun resetOutputs() {
        totalInvestment = 0.0
        totalInterestEarned = 0.0
        maturityAmount = 0.0
    }
}
