package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TaxYearSpec extends AnyWordSpec with Matchers {

  "TaxYear" should {

    "create a tax year when the end year immediately follows the start year" in {
      TaxYear.create(2026, 2027) match {
        case Right(taxYear) =>
          taxYear.startYear shouldBe 2026
          taxYear.endYear shouldBe 2027

        case Left(error) =>
          fail(s"Expected a valid TaxYear, but got an error: $error")
      }
    }

    "reject a tax year where both years are the same" in {
      TaxYear.create(2026, 2026).isLeft shouldBe true
    }

    "reject a tax year where the end year skips a year" in {
      TaxYear.create(2026, 2028).isLeft shouldBe true
    }

    "reject a tax year where the end year is before the start year" in {
      TaxYear.create(2026, 2025).isLeft shouldBe true
    }
  }
}
