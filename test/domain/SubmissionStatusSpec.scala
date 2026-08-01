package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SubmissionStatusSpec extends AnyWordSpec with Matchers {

  "SubmissionStatus" should {

    "expose only recognised submission states" in {
      SubmissionStatus.values.toSet shouldBe Set(
        SubmissionStatus.Draft,
        SubmissionStatus.Submitted
      )
    }

    "allow a draft update to transition to submitted" in {
      SubmissionStatus.Draft.canTransitionTo(SubmissionStatus.Submitted) shouldBe true
    }

    "prevent a submitted update from returning to draft" in {
      SubmissionStatus.Submitted.canTransitionTo(SubmissionStatus.Draft) shouldBe false
    }

    "prevent transitioning to the current state" in {
      SubmissionStatus.Draft.canTransitionTo(SubmissionStatus.Draft) shouldBe false
      SubmissionStatus.Submitted.canTransitionTo(SubmissionStatus.Submitted) shouldBe false
    }
  }
}
