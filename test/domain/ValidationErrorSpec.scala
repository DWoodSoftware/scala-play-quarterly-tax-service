package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ValidationErrorSpec extends AnyWordSpec with Matchers {

  "ValidationError" should {

    "expose only recognised domain validation failures" in {
      ValidationError.values.toSet shouldBe Set(
        ValidationError.MissingIncome
      )
    }
  }
}
