package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TaxYearSpec extends AnyWordSpec with Matchers {
    
    "TaxYear" should {
        
        "create a tax year when the end year immediately follows the start year" in {
            val result = TaxYear.create(2026, 2027)

            result shouldBe Right(TaxYear(2026, 2027))
        }

        "reject a tax year where both years are the same" in {
            val result = TaxYear.create(2026, 2026)

            result.isLeft shouldBe true
        }

        "reject a tax year where the end year skips a year" in {
            val result = TaxYear(2026, 2028)

            result.isLeft shouldBe true
        }

        "reject a tax year where the end year is before the start year" in {
            val result = TaxYear.create(2026, 2025)

            result.isLeft shouldBe true
        }
    }
}