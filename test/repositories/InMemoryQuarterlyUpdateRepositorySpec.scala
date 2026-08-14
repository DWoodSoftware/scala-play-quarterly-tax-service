package repositories

import domain.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import support.DomainFixtures.*

class InMemoryQuarterlyUpdateRepositorySpec extends AsyncWordSpec with Matchers {

  "InMemoryQuarterlyUpdateRepository" should {

    "save and retrieve a quarterly update by id" in {
      val repository: QuarterlyUpdateRepository =
        new InMemoryQuarterlyUpdateRepository

      val update = QuarterlyUpdate.create(
        validQuarterlyUpdateInput()
      )

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
}
