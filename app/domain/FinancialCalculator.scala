package domain

object FinancialCalculator {
    def totalIncome(
        entries: List[IncomeEntry]
    ): BigDecimal = 
        entries.map(_.amount).sum

    def totalExpenses(
        entries: List[ExpenseEntry]
    ): BigDecimal =
        entries.map(_.amount).sum

    def netAmount(
        totalIncome: BigDecimal,
        totalExpenses: BigDecimal
    ): BigDecimal =
        totalIncome - totalExpenses
}
