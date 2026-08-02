package domain

object FinancialCalculator {
    def totalIncome(
        entries: List[IncomeEntry]
    ): BigDecimal = 
        entries.map(_.amount).sum
}
