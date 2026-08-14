package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class DomainErrorSpec extends AnyWordSpec with Matchers {

  "DomainError" should {

    "represent validation failures with their validation errors" in {
      val error: DomainError =
        DomainError.ValidationFailed(
          List(ValidationError.MissingIncome)
        )

      error match {
        case DomainError.ValidationFailed(errors) =>
          errors shouldBe List(ValidationError.MissingIncome)

        case _ =>
          fail("Expected ValidationFailed")
      }
    }

    "represent a missing quarterly update by identifier" in {
      val error: DomainError =
        DomainError.UpdateNotFound("missing-id")

      error match {
        case DomainError.UpdateNotFound(id) =>
          id shouldBe "missing-id"

        case _ =>
          fail("Expected UpdateNotFound")
      }
    }

    "represent an invalid submission state transition" in {
      val error: DomainError =
        DomainError.InvalidStateTransition(
          current = SubmissionStatus.Submitted,
          requested = SubmissionStatus.Draft
        )

      error match {
        case DomainError.InvalidStateTransition(current, requested) =>
          current shouldBe SubmissionStatus.Submitted
          requested shouldBe SubmissionStatus.Draft

        case _ =>
          fail("Expected InvalidStateTransition")
      }
    }
  }
}
