package domain

import java.time.Instant
import java.util.UUID

final case class QuarterlyUpdate private(
    id: String,
    taxpayerReference: TaxpayerReference,
    taxYear: TaxYear,
    quarter: Quarter,
    income: List[IncomeEntry],
    expenses: List[ExpenseEntry],
    totalIncome: BigDecimal,
    totalExpenses: BigDecimal,
    netAmount: BigDecimal,
    status: SubmissionStatus,
    submittedAt: Option[Instant]
)

object QuarterlyUpdate {

    def create(input: QuarterlyUpdateInput): QuarterlyUpdate = {
        val totalIncome =
            FinancialCalculator.totalIncome(input.income)
        
        val totalExpenses =
            FinancialCalculator.totalExpenses(input.expenses)

        val netAmount =
            FinancialCalculator.netAmount(
                totalIncome, 
                totalExpenses
            )

        QuarterlyUpdate(
            id = UUID.randomUUID().toString,
            taxpayerReference = input.taxpayerReference,
            taxYear = input.taxYear,
            quarter = input.quarter,
            income = input.income,
            expenses = input.expenses,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netAmount = netAmount,
            status = SubmissionStatus.Draft,
            submittedAt = None
        )
    }
}