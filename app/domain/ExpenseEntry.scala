package domain

final case class ExpenseEntry(
    category: ExpenseCategory, 
    amount: BigDecimal
)

object ExpenseEntry {
  
  def create(
    category: ExpenseCategory, 
    amount: BigDecimal
    ): Either[String, ExpenseEntry] = 
        if amount >= 0 then
            Right(ExpenseEntry(category, amount))
        else
            Left("Expense amount must be non-negative")
}
