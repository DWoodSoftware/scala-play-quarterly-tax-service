package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FinancialCategorySpec extends AnyWordSpec with Matchers {

    "IncomeCategory" should {
        "expose only the supported income categories" in {
            IncomeCategory.values.toSet shouldBe Set(
                IncomeCategory.SelfEmployment,
                IncomeCategory.Property,
                IncomeCategory.Investment,
                IncomeCategory.Pension,
                IncomeCategory.StateBenefits,
                IncomeCategory.Dividends,
                IncomeCategory.Other
            )
        }
    }

    "ExpenseCategory" should {
        "expose only the supported expense categories" in {
            ExpenseCategory.values.toSet shouldBe Set(
                ExpenseCategory.Travel,
                ExpenseCategory.OfficeCosts,
                ExpenseCategory.ProfessionalFees,
                ExpenseCategory.Advertising,
                ExpenseCategory.Equipment,
                ExpenseCategory.Other
            )
        }
    }
}