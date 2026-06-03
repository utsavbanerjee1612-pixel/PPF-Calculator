package com.example.engine

import com.example.model.PpfResult
import com.example.model.YearlyBreakdown

object PpfCalculatorEngine {

    /**
     * Calculates PPF Wealth using Lumpsum (Annual) contribution mode.
     * 
     * For each year from 1 to 'n':
     * - Running balance (t) = running balance + investment (a).
     * - Interest for the year = (r / 100) * t.
     * - End of year balance (t) = running balance + interest.
     */
    fun calculateLumpsum(
        years: Int,
        annualInvestment: Double,
        interestRate: Double
    ): PpfResult {
        var runningBalance = 0.0
        var totalInvested = 0.0
        var totalInterest = 0.0
        val breakdown = mutableListOf<YearlyBreakdown>()

        for (year in 1..years) {
            val openingBal = runningBalance
            val investment = annualInvestment
            
            // Running balance updates (t = running balance + investment)
            runningBalance += investment
            totalInvested += investment

            // Interest for the year
            val interest = (interestRate / 100.0) * runningBalance
            totalInterest += interest

            // End of year balance updates (t = running balance + interest)
            val endBalance = runningBalance + interest
            runningBalance = endBalance

            breakdown.add(
                YearlyBreakdown(
                    year = year,
                    openingBalance = openingBal,
                    investment = investment,
                    interestEarned = interest,
                    closingBalance = endBalance
                )
            )
        }

        return PpfResult(
            totalInvested = totalInvested,
            totalInterest = totalInterest,
            maturityAmount = runningBalance,
            yearlyBreakdown = breakdown
        )
    }

    /**
     * Calculates PPF Wealth using Monthly contribution mode.
     * 
     * For each year from 1 to 'n':
     * - Get the annual interest rate (r) for that year.
     * - Track a monthly diminishing multiplier (d = 12 down to 1).
     * - For each month 1 to 12 (corresponding to Apr through Mar):
     *   - Let user input the contribution (a) for that specific month.
     *   - Principal (c) updates: c = c + a.
     *   - Monthly compounding interest component: interest = interest + ((r / 100) * a * d / 12).
     *   - Decrement d by 1 for the next month.
     * - At the end of the 12 months: Total for year (t) = principal (c) + accumulated interest.
     * - Set principal (c) for the next year equal to this closing balance (t), reset d to 12, and clear the temporary interest bucket for the next year cycle.
     */
    fun calculateMonthly(
        years: Int,
        monthlyContributions: List<Double>, // 12 values corresponding to Apr to Mar
        interestRate: Double
    ): PpfResult {
        var principal = 0.0 // Updates each year, starting at 0.0
        var totalInvested = 0.0
        var totalInterest = 0.0
        val breakdown = mutableListOf<YearlyBreakdown>()

        for (year in 1..years) {
            val openingBal = principal
            
            // The opening balance (previous closing balance) is held for the full 12 months of the year,
            // so it earns full annual interest: (r / 100) * opening_balance.
            var accumulatedInterest = (interestRate / 100.0) * openingBal
            var yearInvestment = 0.0

            var d = 12
            for (monthIndex in 0..11) {
                // Get month's contribution (a) from template list
                val a = if (monthIndex < monthlyContributions.size) monthlyContributions[monthIndex] else 0.0
                
                // Update principal (c = c + a)
                principal += a
                yearInvestment += a
                totalInvested += a

                // Monthly interest component: interest = interest + ((r / 100) * a * d / 12)
                val monthInterest = (interestRate / 100.0) * a * d / 12.0
                accumulatedInterest += monthInterest
                
                d -= 1
            }

            // Total for year (t) = principal (c) + accumulated interest
            val closingBal = principal + accumulatedInterest
            totalInterest += accumulatedInterest

            breakdown.add(
                YearlyBreakdown(
                    year = year,
                    openingBalance = openingBal,
                    investment = yearInvestment,
                    interestEarned = accumulatedInterest,
                    closingBalance = closingBal
                )
            )

            // Set principal (c) to closing balance (t) for the next year
            principal = closingBal
        }

        return PpfResult(
            totalInvested = totalInvested,
            totalInterest = totalInterest,
            maturityAmount = principal,
            yearlyBreakdown = breakdown
        )
    }
}
