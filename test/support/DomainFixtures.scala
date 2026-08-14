package support

import domain.*

object DomainFixtures {

  def validTaxpayerReference: TaxpayerReference =
    TaxpayerReference.create("TAX-12345678") match {
      case Right(value) => value
      case Left(error)  =>
        throw new IllegalStateException(
          s"Invalid test fixture: $error"
        )
    }

  def validTaxYear: TaxYear =
    TaxYear.create(2026, 2027) match {
      case Right(value) => value
      case Left(error)  =>
        throw new IllegalStateException(
          s"Invalid test fixture: $error"
        )
    }

  def validIncomeEntry(
    amount: BigDecimal = BigDecimal("1500.00"),
    category: IncomeCategory = IncomeCategory.SelfEmployment
  ): IncomeEntry =
    IncomeEntry.create(category, amount) match {
      case Right(value) => value
      case Left(error)  =>
        throw new IllegalStateException(
          s"Invalid test fixture: $error"
        )
    }

  def validQuarterlyUpdateInput(
    income: List[IncomeEntry] = List(validIncomeEntry()),
    expenses: List[ExpenseEntry] = List.empty
  ): QuarterlyUpdateInput =
    QuarterlyUpdateInput(
      taxpayerReference = validTaxpayerReference,
      taxYear = validTaxYear,
      quarter = Quarter.Q1,
      income = income,
      expenses = expenses
    )

  def populatedQuarterlyUpdateInput: QuarterlyUpdateInput = {
    val incomeOne =
      validIncomeEntry(
        amount = BigDecimal("1500.00"),
        category = IncomeCategory.SelfEmployment
      )

    val incomeTwo =
      validIncomeEntry(
        amount = BigDecimal("250.00"),
        category = IncomeCategory.Dividends
      )

    val expenseOne =
      ExpenseEntry.create(
        ExpenseCategory.OfficeCosts,
        BigDecimal("250.00")
      ) match {
        case Right(value) => value
        case Left(error)  =>
          throw new IllegalStateException(
            s"Invalid test fixture: $error"
          )
      }

    val expenseTwo =
      ExpenseEntry.create(
        ExpenseCategory.Travel,
        BigDecimal("80.00")
      ) match {
        case Right(value) => value
        case Left(error)  =>
          throw new IllegalStateException(
            s"Invalid test fixture: $error"
          )
      }

    validQuarterlyUpdateInput(
      income = List(incomeOne, incomeTwo),
      expenses = List(expenseOne, expenseTwo)
    )
  }
}
