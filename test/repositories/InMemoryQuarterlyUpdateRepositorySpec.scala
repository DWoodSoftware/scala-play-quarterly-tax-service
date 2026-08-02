
package repositories

import domain.* 
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class InMemoryQuarterlyUpdateRepositorySpec extends AsyncWordSpec with Matchers {

  "InMemoryQuarterlyUpdateRepository" should {

    "save and retrieve a quarterly update by id" in {
        val repository: QuarterlyUpdateRepository =
            new InMemoryQuarterlyUpdateRepository

        val update = QuarterlyUpdate.create(validInput())

        repository.save(update).flatMap { saved =>
            repository.findById(saved.id).map { result =>
                result shouldBe Some(saved)
            }
        }
    }

    "return None when a quarterly update does not exist" in {
        val repository: QuarterlyUpdateRepository =
            new InMemoryQuarterlyUpdateRepository

        repository.findById("missing-id").map { result =>
            result shouldBe None
        }
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