package domain

final case class QuarterlyUpdateInput(
    taxPayerReference: String,
    taxYear: TaxYear,
    quarter: Quarter,
    income: List[IncomeEntry],
    expenses: List[ExpenseEntry]
)
