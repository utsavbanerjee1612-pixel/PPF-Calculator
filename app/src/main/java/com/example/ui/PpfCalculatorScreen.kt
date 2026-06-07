package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ContributionType
import com.example.model.PpfResult
import com.example.model.YearlyBreakdown
import com.example.model.AppTheme
import com.example.viewmodel.PpfViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PpfCalculatorScreen(
    viewModel: PpfViewModel,
    modifier: Modifier = Modifier
) {
    val contributionType by viewModel.contributionType.collectAsState()
    val yearsInput by viewModel.yearsInput.collectAsState()
    val interestRateInput by viewModel.interestRateInput.collectAsState()
    
    // Mode specific Inputs
    val lumpsumAmountInput by viewModel.lumpsumAmountInput.collectAsState()
    val flatMonthlyInput by viewModel.flatMonthlyInput.collectAsState()
    val monthlyContributions by viewModel.monthlyContributions.collectAsState()
    val isCustomMonthlyEnabled by viewModel.isCustomMonthlyEnabled.collectAsState()
    
    // Result & Errors
    val ppfResult by viewModel.ppfResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val appTheme by viewModel.appTheme.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val isDark = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> systemDark
    }

    val focusManager = LocalFocusManager.current
    var isBreakdownCollapsed by remember { mutableStateOf(false) }
    var showValidationErrorDialog by remember { mutableStateOf(false) }
    var showThemeSelectorDialog by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }
    var validationErrorTitle by remember { mutableStateOf("Invalid Amount") }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // App Bar (Top Header) - styled according to Design HTML
            PpfAppBarView(
                isDark = isDark,
                onToggleTheme = { showThemeSelectorDialog = true }
            )

            // Scrollable Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("year_breakdown_list")
                        .widthIn(max = 600.dp)
                        .align(Alignment.TopCenter),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    // 1. Contribution Mode Toggle
                    item {
                        ContributionSelectorCard(
                            selectedType = contributionType,
                            onTypeSelected = { viewModel.setContributionType(it) }
                        )
                    }

                    // 2. Primary Parameters Input Grid & Slider
                    item {
                        FinancialParametersCard(
                            years = yearsInput,
                            interestRate = interestRateInput,
                            onYearsChange = { viewModel.setYearsInput(it) },
                            onInterestRateChange = { viewModel.setInterestRateInput(it) }
                        )
                    }

                    // 3. Dynamic Investment Input Card Based on Mode
                    item {
                        DynamicInvestmentCard(
                            contributionType = contributionType,
                            lumpsumAmount = lumpsumAmountInput,
                            flatMonthlyAmount = flatMonthlyInput,
                            isCustomMonthly = isCustomMonthlyEnabled,
                            monthlyContributions = monthlyContributions,
                            onLumpsumChange = { viewModel.setLumpsumAmountInput(it) },
                            onFlatMonthlyChange = { viewModel.setFlatMonthlyInput(it) },
                            onCustomMonthlyToggle = { viewModel.setCustomMonthlyEnabled(it) },
                            onMonthlyValueChange = { index, value -> 
                                viewModel.updateMonthlyContribution(index, value)
                            }
                        )
                    }

                    // 4. Action Button and Error Banner
                    item {
                        ActionButtonRow(
                            errorMessage = errorMessage,
                            onCalculate = {
                                if (contributionType == ContributionType.LUMPSUM) {
                                    val amount = lumpsumAmountInput.toIntOrNull()
                                    if (amount == null || amount < 500 || amount > 150000 || amount % 100 != 0) {
                                        validationErrorTitle = "Invalid Amount"
                                        validationErrorMessage = "Total annual amount in PPF need to be between ₹500 and ₹150000. Please renter the value."
                                        showValidationErrorDialog = true
                                    } else {
                                        focusManager.clearFocus()
                                        viewModel.calculatePPF()
                                    }
                                } else {
                                    if (!isCustomMonthlyEnabled) {
                                        val amount = flatMonthlyInput.toIntOrNull()
                                        if (amount == null || amount < 100 || amount > 12500 || amount % 100 != 0) {
                                            validationErrorTitle = "Invalid Amount"
                                            validationErrorMessage = "Total monthly amount in PPF need to be between ₹100 and ₹12500. Please re-enter the value."
                                            showValidationErrorDialog = true
                                        } else {
                                            focusManager.clearFocus()
                                            viewModel.calculatePPF()
                                        }
                                    } else {
                                        // First, round all individual monthly investments to nearest 100
                                        val roundedContributions = monthlyContributions.map { contrib ->
                                            (Math.round(contrib / 100.0) * 100.0)
                                        }
                                        // Update the viewmodel so the UI instantly updates to the rounded numbers
                                        roundedContributions.forEachIndexed { idx, rv ->
                                            if (rv != monthlyContributions[idx]) {
                                                viewModel.updateMonthlyContribution(idx, rv)
                                            }
                                        }

                                        // Perform validations on the rounded values
                                        val individualValid = roundedContributions.all { contrib ->
                                            contrib >= 0.0 && contrib <= 150000.0
                                        }
                                        val totalSum = roundedContributions.sum()
                                        val totalValid = totalSum >= 500.0 && totalSum <= 150000.0

                                        if (!individualValid || !totalValid) {
                                            validationErrorTitle = "Invalid Distribution"
                                            validationErrorMessage = "Total monthly amount in PPF in a single month needs to be between ₹100 and ₹150000 and total annual amount between ₹500 and ₹150000. Please re-enter the value."
                                            showValidationErrorDialog = true
                                        } else {
                                            focusManager.clearFocus()
                                            viewModel.calculatePPF()
                                        }
                                    }
                                }
                            }
                        )
                    }

                    // 5. Result Summary Section (Pastel Summary Cards)
                    ppfResult?.let { result ->
                        item {
                            PpfResultSummarySection(result = result)
                        }

                        // 6. Growth Ledger container card (Year-by-Year breakdown in nice styled borders)
                        item {
                            BreakdownContainerTitle(
                                isCollapsed = isBreakdownCollapsed,
                                onToggleCollapse = { isBreakdownCollapsed = !isBreakdownCollapsed }
                            )
                        }

                        if (!isBreakdownCollapsed) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .border(
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .clip(RoundedCornerShape(24.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Header Row
                                        LedgerHeaderRow()
                                        
                                        // LedgerRows
                                        result.yearlyBreakdown.forEach { row ->
                                            YearlyLedgerRow(row = row)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Bar (Material 3 Mock)
            PpfBottomNavigationBar()
        }
    }

    if (showValidationErrorDialog) {
        AlertDialog(
            onDismissRequest = { showValidationErrorDialog = false },
            title = {
                Text(text = validationErrorTitle)
            },
            text = {
                Text(text = validationErrorMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = { showValidationErrorDialog = false },
                    modifier = Modifier.testTag("dialog_ok_button")
                ) {
                    Text("Ok")
                }
            },
            modifier = Modifier.testTag("validation_alert_dialog")
        )
    }

    if (showThemeSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showThemeSelectorDialog = false },
            title = {
                Text(
                    text = "Choose Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val options = listOf(
                        AppTheme.LIGHT to "Light Mode",
                        AppTheme.DARK to "Dark Mode",
                        AppTheme.SYSTEM to "System Default"
                    )
                    options.forEach { (optionTheme, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setAppTheme(optionTheme)
                                    showThemeSelectorDialog = false
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (appTheme == optionTheme),
                                onClick = {
                                    viewModel.setAppTheme(optionTheme)
                                    showThemeSelectorDialog = false
                                },
                                modifier = Modifier.testTag("theme_radio_$label")
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showThemeSelectorDialog = false },
                    modifier = Modifier.testTag("theme_dialog_close_button")
                ) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.testTag("theme_selection_dialog")
        )
    }
}

@Composable
fun PpfAppBarView(
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PPF Wealth",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier.testTag("theme_toggle_button")
        ) {
            Icon(
                imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Overflow options",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun ContributionSelectorCard(
    selectedType: ContributionType,
    onTypeSelected: (ContributionType) -> Unit
) {
    // Elegant fully rounded pill toggle BG (EADDFF for Light)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Monthly option
        val isMonthly = selectedType == ContributionType.MONTHLY
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(CircleShape)
                .background(
                    if (isMonthly) MaterialTheme.colorScheme.primary else Color.Transparent
                )
                .clickable { onTypeSelected(ContributionType.MONTHLY) }
                .testTag("contribution_type_monthly"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Monthly",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isMonthly) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Lumpsum option
        val isLumpsum = selectedType == ContributionType.LUMPSUM
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(CircleShape)
                .background(
                    if (isLumpsum) MaterialTheme.colorScheme.primary else Color.Transparent
                )
                .clickable { onTypeSelected(ContributionType.LUMPSUM) }
                .testTag("contribution_type_lumpsum"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lumpsum",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isLumpsum) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FinancialParametersCard(
    years: String,
    interestRate: String,
    onYearsChange: (String) -> Unit,
    onInterestRateChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Grid inputs (Inputs Section in HTML)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Period (Years) classic M3 filled style input
            TextField(
                value = years,
                onValueChange = onYearsChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("duration_input"),
                label = { 
                    Text(
                        "PERIOD (YEARS)", 
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                placeholder = { Text("15") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
            )

            // Interest rate input
            TextField(
                value = interestRate,
                onValueChange = onInterestRateChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("interest_rate_input"),
                label = { 
                    Text(
                        "INTEREST RATE (%)", 
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                placeholder = { Text("7.1") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
            )
        }

        // Years slider companion
        val currentYearsVal = years.toFloatOrNull() ?: 15f
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duration Planner",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = "${currentYearsVal.toInt()} Years",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = currentYearsVal.coerceIn(1f, 50f),
                onValueChange = { onYearsChange(it.toInt().toString()) },
                valueRange = 1f..50f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("duration_slider")
            )
        }
    }
}

@Composable
fun DynamicInvestmentCard(
    contributionType: ContributionType,
    lumpsumAmount: String,
    flatMonthlyAmount: String,
    isCustomMonthly: Boolean,
    monthlyContributions: List<Double>,
    onLumpsumChange: (String) -> Unit,
    onFlatMonthlyChange: (String) -> Unit,
    onCustomMonthlyToggle: (Boolean) -> Unit,
    onMonthlyValueChange: (Int, Double) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (contributionType) {
            ContributionType.LUMPSUM -> {
                TextField(
                    value = lumpsumAmount,
                    onValueChange = onLumpsumChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lumpsum_amount_input"),
                    label = { 
                        Text(
                            "INVESTMENT AMOUNT (ANNUAL)", 
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    placeholder = { Text("150000") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                )
                Text(
                    text = "Standard limits for PPF: ₹500 to ₹1,50,000 annually.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            ContributionType.MONTHLY -> {
                TextField(
                    value = flatMonthlyAmount,
                    onValueChange = onFlatMonthlyChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flat_monthly_amount_input"),
                    label = { 
                        Text(
                            "INVESTMENT AMOUNT (MONTHLY)", 
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    placeholder = { Text("12500") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    enabled = !isCustomMonthly,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                )

                // Customizable monthly toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Customize Month-by-Month",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Enable variable month-by-month cashflows",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = isCustomMonthly,
                        onCheckedChange = onCustomMonthlyToggle
                    )
                }

                // Custom sliders scroll vertical
                AnimatedVisibility(
                    visible = isCustomMonthly,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val monthsLabel = listOf(
                        "April (Apr)", "May", "June (Jun)", "July (Jul)",
                        "August (Aug)", "September (Sep)", "October (Oct)", "November (Nov)",
                        "December (Dec)", "January (Jan)", "February (Feb)", "March (Mar)"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Financial Year Contributions (Apr to Mar)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        monthsLabel.forEachIndexed { idx, label ->
                            val currentVal = monthlyContributions.getOrElse(idx) { 0.0 }
                            
                            // A local state to track direct typing cleanly, keyed on currentVal
                            var textState by remember(currentVal) {
                                val initialText = if (currentVal == 0.0) "" else currentVal.toInt().toString()
                                mutableStateOf(initialText)
                            }
                            // Visual hint if current user typed value is not a multiple of 100
                            val isMultipleOf100 = (Math.round(currentVal) % 100L == 0L)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (!isMultipleOf100 && textState.isNotEmpty()) {
                                            Text(
                                                text = "Will be rounded to nearest 100",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedTextField(
                                        value = textState,
                                        onValueChange = { newVal ->
                                            val digits = newVal.filter { it.isDigit() }
                                            textState = digits
                                            val parsed = digits.toDoubleOrNull() ?: 0.0
                                            onMonthlyValueChange(idx, parsed)
                                        },
                                        modifier = Modifier
                                            .width(130.dp)
                                            .height(52.dp)
                                            .testTag("monthly_input_$label"),
                                        placeholder = { Text("0") },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Next
                                        ),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))

                                Slider(
                                    value = currentVal.toFloat().coerceIn(0f, 150000f),
                                    onValueChange = { sliderVal ->
                                        val snapped = (Math.round(sliderVal / 100.0) * 100.0)
                                        onMonthlyValueChange(idx, snapped)
                                    },
                                    valueRange = 0f..150000f,
                                    modifier = Modifier.fillMaxWidth().testTag("monthly_slider_$label")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtonRow(
    errorMessage: String?,
    onCalculate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Error Display
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            errorMessage?.let { errorText ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error message icon",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Beautiful Action Calculate button (rounded pill)
        Button(
            onClick = onCalculate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("calculate_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = CircleShape,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Calculate, 
                    contentDescription = "Calculator icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calculate PPF Wealth",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PpfResultSummarySection(
    result: PpfResult
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Pastel Summary Cards (Grid row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Total Invested (background E8DEF8 in light theme)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("total_invested_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TOTAL INVESTED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(result.totalInvested),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Card 2: Total Interest (background FFD8E4 in light theme)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("total_interest_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TOTAL INTEREST",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(result.totalInterest),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Full width Maturity worth Card (background D0BCFF)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("maturity_worth_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFD0BCFF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "TOTAL MATURITY VALUE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatCurrency(result.maturityAmount),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Visual ratios composition graph bar
        val totalAmount = result.maturityAmount
        val investRatio = if (totalAmount > 0) (result.totalInvested / totalAmount).toFloat() else 0f
        val interestRatio = if (totalAmount > 0) (result.totalInterest / totalAmount).toFloat() else 0f

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Composition Profile",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(investRatio.coerceAtLeast(0.01f))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(interestRatio.coerceAtLeast(0.01f))
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Principal (${String.format("%.1f", investRatio * 100)}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Interest (${String.format("%.1f", interestRatio * 100)}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun BreakdownContainerTitle(
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Year-by-Year Growth",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Detailed cashflow compounding projections",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        IconButton(onClick = onToggleCollapse) {
            Icon(
                imageVector = if (isCollapsed) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                contentDescription = "Toggle breakdown"
            )
        }
    }
}

@Composable
fun LedgerHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Year",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Opening Bal",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Deposited",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Interest",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Closing Bal",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun YearlyLedgerRow(row: YearlyBreakdown) {
    val isEven = row.year % 2 == 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isEven) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Yr index
        Text(
            text = "Yr ${row.year}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Opening Balance
        Text(
            text = formatCurrency(row.openingBalance),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Deposited
        Text(
            text = formatCurrency(row.investment),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Interest
        Text(
            text = formatCurrency(row.interestEarned),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )

        // Closing Balance
        Text(
            text = formatCurrency(row.closingBalance),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun PpfBottomNavigationBar() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Column {
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calculator tab (Active Indicator Pill)
                Column(
                    modifier = Modifier.clickable { },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Calculate,
                            contentDescription = "Calculator icon",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "Calculator",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Savings tab (Muted/Inactive opacity 0.6)
                Column(
                    modifier = Modifier.clickable { },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = "Savings icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "Savings",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    return try {
        val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN"))
        "₹" + formatter.format(Math.round(amount))
    } catch (e: Exception) {
        "₹" + String.format("%,.0f", amount)
    }
}
