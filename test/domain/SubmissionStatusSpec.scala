package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SubmissionStatusSpec extends AnyWordSpec with Matchers {

  "SubmissionStatus" should {

    "expose only recognised submission states" in {
      SubmissionStatus.values.toSet shouldBe Set(
        SubmissionStatus.Draft,
        SubmissionStatus.Validated,
        SubmissionStatus.Submitted
      )
    }

    "allow a draft update to transition to validated" in {
      SubmissionStatus.Draft.canTransitionTo(SubmissionStatus.Validated) shouldBe true
    }

    "allow a validated update to transition to submitted" in {
      SubmissionStatus.Validated.canTransitionTo(SubmissionStatus.Submitted) shouldBe true
    }

    "prevent draft from transitioning directly to submitted" in {
      SubmissionStatus.Draft.canTransitionTo(SubmissionStatus.Submitted) shouldBe false
    }

    "prevent a validated update from returning to draft" in {
      SubmissionStatus.Validated.canTransitionTo(SubmissionStatus.Draft) shouldBe false
    }

    "prevent submitted from returning to an earlier state" in {
      SubmissionStatus.Submitted.canTransitionTo(SubmissionStatus.Validated) shouldBe false
      SubmissionStatus.Submitted.canTransitionTo(SubmissionStatus.Draft) shouldBe false
    }

    "prevent transitioning to the current state" in
      SubmissionStatus.values.foreach { status =>
        status.canTransitionTo(status) shouldBe false
      }
  }
}
