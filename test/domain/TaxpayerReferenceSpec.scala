package domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TaxPayerReferenceSpec extends AnyWordSpec with Matchers {

  "TaxpayerReference" should {

    "create a taxpayer reference with the supported format" in {
      TaxpayerReference.create("TAX-12345678") match {
        case Right(reference) =>
          reference.value shouldBe "TAX-12345678"

        case Left(error) =>
          fail(s"Expected a valid taxpayer reference, but got error: $error")
      }
    }

    "reject a reference without the TAX prefix" in {
      TaxpayerReference.create("12345678").isLeft shouldBe true
    }

    "reject a reference with too few digits" in {
      TaxpayerReference.create("TAX-1234567").isLeft shouldBe true
    }

    "reject a reference with too many digits" in {
      TaxpayerReference.create("TAX-123456789").isLeft shouldBe true
    }

    "reject a reference with non-digit characters" in {
      TaxpayerReference.create("TAX-1234ABCD").isLeft shouldBe true
    }

    "reject an empty reference" in {
      TaxpayerReference.create("").isLeft shouldBe true
    }
  }
}
