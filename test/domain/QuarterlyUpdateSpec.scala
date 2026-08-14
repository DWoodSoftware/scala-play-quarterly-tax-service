package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import support.DomainFixtures.*

class QuarterlyUpdateSpec extends AnyWordSpec with Matchers {

  "QuarterlyUpdate" should {

    "create a draft quarterly update from validated input" in {
      val input = populatedQuarterlyUpdateInput

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
      val first  = QuarterlyUpdate.create(populatedQuarterlyUpdateInput)
      val second = QuarterlyUpdate.create(populatedQuarterlyUpdateInput)

      first.id should not be empty
      second.id should not be empty
      first.id should not equal second.id
    }

    "populate derived financial totals from the supplied entries" in {
      val update = QuarterlyUpdate.create(populatedQuarterlyUpdateInput)

      update.totalIncome shouldBe BigDecimal("1750.00")
      update.totalExpenses shouldBe BigDecimal("330.00")
      update.netAmount shouldBe BigDecimal("1420.00")
    }

    "never set submittedAt when initially created" in {
      QuarterlyUpdate.create(populatedQuarterlyUpdateInput).submittedAt shouldBe None
    }

    "preserve entity invariants after creation" in {
      val update = QuarterlyUpdate.create(populatedQuarterlyUpdateInput)

      update.totalIncome shouldBe FinancialCalculator.totalIncome(update.income)
      update.totalExpenses shouldBe FinancialCalculator.totalExpenses(update.expenses)
      update.netAmount shouldBe update.totalIncome - update.totalExpenses
      update.status shouldBe SubmissionStatus.Draft
      update.submittedAt shouldBe None
    }
  }
}
