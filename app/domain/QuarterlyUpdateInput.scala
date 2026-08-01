package domain

final case class QuarterlyUpdateInput(
    taxpayerReference: TaxpayerReference,
    taxYear: TaxYear,
    quarter: Quarter,
    income: List[IncomeEntry],
    expenses: List[ExpenseEntry]
)
