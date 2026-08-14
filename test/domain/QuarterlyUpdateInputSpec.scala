package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class QuarterlyUpdateInputSpec extends AnyWordSpec with Matchers {

  "QuarterlyUpdateInput" should {

    "carry only trusted client-supplied quarterly reporting data" in {
      val taxYear =
        TaxYear.create(2026, 2027) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val incomeEntry =
        IncomeEntry.create(
          IncomeCategory.SelfEmployment,
          BigDecimal("1500.00")
        ) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val expenseEntry =
        ExpenseEntry.create(
          ExpenseCategory.OfficeCosts,
          BigDecimal("250.00")
        ) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val taxpayerReference =
        TaxpayerReference.create("TAX-12345678") match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val input = QuarterlyUpdateInput(
        taxpayerReference = taxpayerReference,
        taxYear = taxYear,
        quarter = Quarter.Q1,
        income = List(incomeEntry),
        expenses = List(expenseEntry)
      )

      input.taxpayerReference shouldEqual taxpayerReference
      input.taxYear shouldEqual taxYear
      input.quarter shouldEqual Quarter.Q1
      input.income shouldEqual List(incomeEntry)
      input.expenses shouldEqual List(expenseEntry)
    }

    "allow multiple income and expense entries" in {
      val taxYear =
        TaxYear.create(2026, 2027) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val selfEmployment =
        IncomeEntry.create(
          IncomeCategory.SelfEmployment,
          BigDecimal("1500.00")
        ) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val dividends =
        IncomeEntry.create(
          IncomeCategory.Dividends,
          BigDecimal("200.00")
        ) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val officeCosts =
        ExpenseEntry.create(
          ExpenseCategory.OfficeCosts,
          BigDecimal("250.00")
        ) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val travel =
        ExpenseEntry.create(
          ExpenseCategory.Travel,
          BigDecimal("80.00")
        ) match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val taxpayerReference =
        TaxpayerReference.create("TAX-12345678") match {
          case Right(value) => value
          case Left(error)  => fail(error)
        }

      val input = QuarterlyUpdateInput(
        taxpayerReference = taxpayerReference,
        taxYear = taxYear,
        quarter = Quarter.Q1,
        income = List(selfEmployment, dividends),
        expenses = List(officeCosts, travel)
      )

      input.income should contain theSameElementsAs List(selfEmployment, dividends)
      input.expenses should contain theSameElementsAs List(officeCosts, travel)
    }
  }
}
