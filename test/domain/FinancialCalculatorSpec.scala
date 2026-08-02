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
                    case Left(error) => fail(error)
                }

            val second =
                IncomeEntry.create(
                    IncomeCategory.Dividends,
                    BigDecimal("250.00")
                ) match {
                    case Right(value) => value
                    case Left(error) => fail(error)
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
    }
}
