package com.example.model

enum class ContributionType {
    LUMPSUM,
    MONTHLY
}

data class YearlyBreakdown(
    val year: Int,
    val openingBalance: Double,
    val investment: Double,
    val interestEarned: Double,
    val closingBalance: Double
)

data class PpfResult(
    val totalInvested: Double,
    val totalInterest: Double,
    val maturityAmount: Double,
    val yearlyBreakdown: List<YearlyBreakdown>
)
