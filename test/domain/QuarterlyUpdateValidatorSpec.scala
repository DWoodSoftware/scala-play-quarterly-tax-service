package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import support.DomainFixtures.*

class QuarterlyUpdateValidatorSpec extends AnyWordSpec with Matchers {

    "QuarterlyUpdateValidator" should {

        "return the input when the quarterly update is valid" in {
            val input = validQuarterlyUpdateInput()

            QuarterlyUpdateValidator.validate(input) shouldEqual Right(input)
        }

        "reject a quarterly update with no income" in {
            val input = validQuarterlyUpdateInput().copy(
                income = List.empty
            )

            QuarterlyUpdateValidator.validate(input) shouldEqual 
                Left(List(ValidationError.MissingIncome))
        }
    }
}