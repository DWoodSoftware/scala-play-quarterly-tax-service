package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class QuarterlyUpdateValidatorSpec extends AnyWordSpec with Matchers {

    "QuarterlyUpdateValidator" should {

        "return the input when the quarterly update is valid" in {
            val input = validInput()

            QuarterlyUpdateValidator.validate(input) shouldEqual Right(input)
        }

        "reject a quarterly update with no income" in {
            val input = validInput().copy(income = List.empty)

            QuarterlyUpdateValidator.validate(input) shouldEqual Left(List(ValidationError.MissingIncome))
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
        
        val income =
            IncomeEntry.create(
                IncomeCategory.SelfEmployment, 
                BigDecimal("1500.00")
                ) match {
                    case Right(value) => value
                    case Left(error) => fail(error)
            }

        QuarterlyUpdateInput(
            taxpayerReference = taxpayerReference,
            taxYear = taxYear,
            quarter = Quarter.Q1,
            income = List(income),
            expenses = List.empty
        )
    }
}
