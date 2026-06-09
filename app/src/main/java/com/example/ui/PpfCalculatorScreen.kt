package com.example.ppfcalculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ppfcalculator.viewmodel.PpfViewModel

@Composable
fun PpfCalculatorScreen(
    viewModel: PpfViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "PPF Calculator",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Yearly Investment Input Field
        OutlinedTextField(
            value = viewModel.yearlyInvestment,
            onValueChange = { viewModel.yearlyInvestment = it },
            label = { Text("Yearly Investment Amount (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Interest Rate Input Field
        OutlinedTextField(
            value = viewModel.interestRate,
            onValueChange = { viewModel.interestRate = it },
            label = { Text("Interest Rate (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Duration Input Field
        OutlinedTextField(
            value = viewModel.timePeriodYears,
            onValueChange = { viewModel.timePeriodYears = it },
            label = { Text("Time Period (Years)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Calculate Trigger Button
        Button(
            onClick = { viewModel.calculatePpf() },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(text = "Calculate", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Results Presentation Block
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Total Investment: ₹${String.format("%.2f", viewModel.totalInvestment)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Total Interest Earned: ₹${String.format("%.2f", viewModel.totalInterestEarned)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Maturity Amount: ₹${String.format("%.2f", viewModel.maturityAmount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
