package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FinancialCalculatorSpec extends AnyWordSpec with Matchers {

  "FinancialCalculator" should {
    "calculate total income from all income entries" in {
      val first =
        IncomeEntry.create(
          IncomeCategory.SelfEmployment,
          BigDecimal("1500.00")
        ) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val second =
        IncomeEntry.create(
          IncomeCategory.Dividends,
          BigDecimal("250.00")
        ) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      FinancialCalculator.totalIncome(
        List(first, second)
      ) shouldEqual BigDecimal("1750.00")
    }

    "return zero when there are no income entries" in {
      FinancialCalculator.totalIncome(
        List.empty
      ) shouldBe BigDecimal("0")
    }

    "calculate total expenses from all expense entries" in {
      val first =
        ExpenseEntry.create(
          ExpenseCategory.OfficeCosts,
          BigDecimal("250.00")
        ) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val second =
        ExpenseEntry.create(
          ExpenseCategory.Travel,
          BigDecimal("80.00")
        ) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      FinancialCalculator.totalExpenses(
        List(first, second)
      ) shouldEqual BigDecimal("330.00")
    }

    "return zero when there are no expense entries" in {
      FinancialCalculator.totalExpenses(
        List.empty
      ) shouldBe BigDecimal("0")
    }

    "calculate net amount from total income and total expenses" in {
      FinancialCalculator.netAmount(
        totalIncome = BigDecimal("1750.00"),
        totalExpenses = BigDecimal("330.00")
      ) shouldEqual BigDecimal("1420.00")
    }

    "allow a negative net amount when expenses exceed income" in {
      FinancialCalculator.netAmount(
        totalIncome = BigDecimal("500.00"),
        totalExpenses = BigDecimal("750.00")
      ) shouldEqual BigDecimal("-250.00")
    }
  }
}
