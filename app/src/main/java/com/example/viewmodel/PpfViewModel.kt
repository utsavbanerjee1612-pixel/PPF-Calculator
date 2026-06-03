package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.ContributionType
import com.example.model.PpfResult
import com.example.engine.PpfCalculatorEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PpfViewModel : ViewModel() {

    // Theme Management Flow (null means follow system)
    private val _isDarkTheme = MutableStateFlow<Boolean?>(null)
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme(systemDarkDefault: Boolean) {
        val current = _isDarkTheme.value ?: systemDarkDefault
        _isDarkTheme.value = !current
    }

    // UI Input States
    private val _contributionType = MutableStateFlow(ContributionType.LUMPSUM)
    val contributionType: StateFlow<ContributionType> = _contributionType.asStateFlow()

    private val _yearsInput = MutableStateFlow("15")
    val yearsInput: StateFlow<String> = _yearsInput.asStateFlow()

    private val _interestRateInput = MutableStateFlow("7.1")
    val interestRateInput: StateFlow<String> = _interestRateInput.asStateFlow()

    // Lumpsum specific inputs
    private val _lumpsumAmountInput = MutableStateFlow("150000")
    val lumpsumAmountInput: StateFlow<String> = _lumpsumAmountInput.asStateFlow()

    // Monthly specific inputs
    private val _flatMonthlyInput = MutableStateFlow("12500")
    val flatMonthlyInput: StateFlow<String> = _flatMonthlyInput.asStateFlow()

    // List of 12 month values corresponding to Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar
    private val _monthlyContributions = MutableStateFlow(List(12) { 12500.0 })
    val monthlyContributions: StateFlow<List<Double>> = _monthlyContributions.asStateFlow()

    private val _isCustomMonthlyEnabled = MutableStateFlow(false)
    val isCustomMonthlyEnabled: StateFlow<Boolean> = _isCustomMonthlyEnabled.asStateFlow()

    // Calculation Result
    private val _ppfResult = MutableStateFlow<PpfResult?>(null)
    val ppfResult: StateFlow<PpfResult?> = _ppfResult.asStateFlow()

    // Validation/Error States
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Trigger initial calculation so that the user starts with a populated, beautiful visual dashboard
        calculatePPF()
    }

    fun setContributionType(type: ContributionType) {
        _contributionType.value = type
        calculatePPF()
    }

    fun setYearsInput(input: String) {
        // Keep numeric only
        val filtered = input.filter { it.isDigit() }
        _yearsInput.value = filtered
    }

    fun setInterestRateInput(input: String) {
        // Allow digit and single decimal dot
        val filtered = input.filter { it.isDigit() || it == '.' }
        _interestRateInput.value = filtered
    }

    fun setLumpsumAmountInput(input: String) {
        val filtered = input.filter { it.isDigit() }
        _lumpsumAmountInput.value = filtered
    }

    fun setFlatMonthlyInput(input: String) {
        val filtered = input.filter { it.isDigit() }
        _flatMonthlyInput.value = filtered
        
        // If they update the flat amount, automatically populate the monthly template if custom is not locked or if we want to sync
        if (!_isCustomMonthlyEnabled.value) {
            val amount = filtered.toDoubleOrNull() ?: 0.0
            _monthlyContributions.value = List(12) { amount }
        }
    }

    fun setCustomMonthlyEnabled(enabled: Boolean) {
        _isCustomMonthlyEnabled.value = enabled
        if (!enabled) {
            // Re-sync with flat amount when custom is disabled
            val amount = _flatMonthlyInput.value.toDoubleOrNull() ?: 0.0
            _monthlyContributions.value = List(12) { amount }
        }
    }

    fun updateMonthlyContribution(index: Int, amount: Double) {
        if (index in 0..11) {
            val currentList = _monthlyContributions.value.toMutableList()
            currentList[index] = amount
            _monthlyContributions.value = currentList
        }
    }

    fun calculatePPF() {
        _errorMessage.value = null

        val years = _yearsInput.value.toIntOrNull()
        if (years == null || years <= 0) {
            _errorMessage.value = "Please enter a valid duration (e.g., 15 years)."
            return
        }
        if (years > 50) {
            _errorMessage.value = "The maximum duration for calculation is 50 years."
            return
        }

        val rate = _interestRateInput.value.toDoubleOrNull()
        if (rate == null || rate <= 0 || rate > 25) {
            _errorMessage.value = "Please enter a realistic annual interest rate (e.g., 7.1%)."
            return
        }

        val tempResult = when (_contributionType.value) {
            ContributionType.LUMPSUM -> {
                val lumpsum = _lumpsumAmountInput.value.toDoubleOrNull() ?: 0.0
                if (lumpsum <= 0) {
                    _errorMessage.value = "Please enter a valid investment amount."
                    return
                }
                PpfCalculatorEngine.calculateLumpsum(
                    years = years,
                    annualInvestment = lumpsum,
                    interestRate = rate
                )
            }
            ContributionType.MONTHLY -> {
                // If custom is enabled, calculate on individual months. Otherwise, use flat monthly synced contributions.
                val contributionsList = _monthlyContributions.value
                val totalYearly = contributionsList.sum()
                if (totalYearly <= 0) {
                    _errorMessage.value = "Please enter a non-zero monthly contribution value."
                    return
                }
                PpfCalculatorEngine.calculateMonthly(
                    years = years,
                    monthlyContributions = contributionsList,
                    interestRate = rate
                )
            }
        }

        _ppfResult.value = tempResult
    }
}
