package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FinancialEntrySpec extends AnyWordSpec with Matchers {

  "IncomeEntry" should {
    
    "create an income entry with a supported category and positive amount" in {
        IncomeEntry.create(
            IncomeCategory.SelfEmployment,
            BigDecimal("1500.00")
        ) match {
            case Right(entry) =>
                entry.category shouldBe IncomeCategory.SelfEmployment
                entry.amount shouldBe BigDecimal("1500.00")

            case Left(error) =>
                fail(s"Expected valid IncomeEntry, got an error: $error")
        }
    }

    "allow a zero amount" in {
        IncomeEntry.create(
            IncomeCategory.Other,
            BigDecimal("0.00")
        ).isRight shouldBe true
    }

    "reject a negative amount" in {
        IncomeEntry.create(
            IncomeCategory.SelfEmployment,
            BigDecimal("-1.00")
        ).isLeft shouldBe true
    }
  }

  "ExpenseEntry" should {
    
    "create an expense entry with a supported category and positive amount" in {
        ExpenseEntry.create(
            ExpenseCategory.OfficeCosts,
            BigDecimal("250.00")
        ) match {
            case Right(entry) =>
                entry.category shouldBe ExpenseCategory.OfficeCosts
                entry.amount shouldBe BigDecimal("250.00")

            case Left(error) =>
                fail(s"Expected valid ExpenseEntry, got an error: $error")
        }
    }

    "allow a zero amount" in {
        ExpenseEntry.create(
            ExpenseCategory.Other,
            BigDecimal("0.00")
        ).isRight shouldBe true
    }

    "reject a negative amount" in {
        ExpenseEntry.create(
            ExpenseCategory.Travel,
            BigDecimal("-1.00")
        ).isLeft shouldBe true
    }
  }
}
