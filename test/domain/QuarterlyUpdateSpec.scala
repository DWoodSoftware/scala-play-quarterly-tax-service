package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class QuarterlyUpdateSpec extends AnyWordSpec with Matchers {
  "QuarterlyUpdate" should {

    "create a draft quarterly update from validated input" in {
        val input = validInput()

        val update = QuarterlyUpdate.create(input)

        update.taxpayerReference shouldBe input.taxpayerReference
        update.taxYear shouldBe input.taxYear
        update.quarter shouldBe input.quarter
        update.income shouldBe input.income
        update.expenses shouldBe input.expenses
        update.status shouldBe SubmissionStatus.Draft
        update.submittedAt shouldBe None
    }

    "generate a server-controlled identifier" in { 
        val first = QuarterlyUpdate.create(validInput())
        val second = QuarterlyUpdate.create(validInput())

        first.id should not be empty
        second.id should not be empty
        first.id should not equal second.id
    }

    "populate derived financial totals from the supplied entries" in {
        val update = QuarterlyUpdate.create(validInput())

        update.totalIncome shouldBe BigDecimal("1750.00")
        update.totalExpenses shouldBe BigDecimal("330.00")
        update.netAmount shouldBe BigDecimal("1420.00")
    }

    "never set submittedAt when initially created" in {
        QuarterlyUpdate.create(validInput()).submittedAt shouldBe None
    }

    "preserve entity invariants after creation" in {
        val update = QuarterlyUpdate.create(validInput())

        update.totalIncome shouldBe FinancialCalculator.totalIncome(update.income)
        update.totalExpenses shouldBe FinancialCalculator.totalExpenses(update.expenses)
        update.netAmount shouldBe update.totalIncome - update.totalExpenses
        update.status shouldBe SubmissionStatus.Draft
        update.submittedAt shouldBe None
    }
  }

  private def validInput(): QuarterlyUpdateInput = {
    val taxpayerReference = 
        TaxpayerReference.create("TAX-12345678") match {
            case Right(value) => value
            case Left(error) => fail(error)
        }

    val taxYear = 
        TaxYear.create(2026, 2027) match {
            case Right(value) => value
            case Left(error) => fail(error)
        }

    val incomeOne = 
        IncomeEntry.create(
            IncomeCategory.SelfEmployment,
            BigDecimal("1500.00")
        ) match {
            case Right(value) => value
            case Left(error) => fail(error)
        }
    
    val incomeTwo = 
        IncomeEntry.create(
            IncomeCategory.Dividends,
            BigDecimal("250.00")
        ) match {
            case Right(value) => value
            case Left(error) => fail(error)
        }

    
    val expenseOne = 
        ExpenseEntry.create(
            ExpenseCategory.OfficeCosts,
            BigDecimal("250.00")
        ) match {
            case Right(value) => value
            case Left(error) => fail(error)
        }
    
    
    val expenseTwo = 
        ExpenseEntry.create(
            ExpenseCategory.Travel,
            BigDecimal("80.00")
        ) match {
            case Right(value) => value
            case Left(error) => fail(error)
        }

    QuarterlyUpdateInput(
        taxpayerReference = taxpayerReference,
        taxYear = taxYear,
        quarter = Quarter.Q1,
        income = List(incomeOne, incomeTwo),
        expenses = List(expenseOne, expenseTwo)
    )
  }
}
