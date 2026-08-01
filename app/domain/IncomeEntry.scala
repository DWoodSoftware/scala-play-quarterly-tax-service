package domain

final case class IncomeEntry(
    category: IncomeCategory, 
    amount: BigDecimal
)

object IncomeEntry {
  def create(
    category: IncomeCategory, 
    amount: BigDecimal
    ): Either[String, IncomeEntry] = 
        if amount >= 0 then
            Right(IncomeEntry(category, amount))
        else
            Left("Income amount must be non-negative")
  }
