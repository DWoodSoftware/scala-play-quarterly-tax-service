package domain

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class QuarterSpec extends AnyFunSpec with Matchers {

  describe("Quarter") {
    it("only exposes the supported quarter values") {
      Quarter.values.toList should contain theSameElementsAs List(Quarter.Q1, Quarter.Q2, Quarter.Q3, Quarter.Q4)
    }

    it("does not allow arbitrary integer or string values to be represented") {
      Quarter.values.toList should have size 4
      Quarter.values.exists(_ == Quarter.Q1) shouldBe true
      Quarter.values.exists(_ == Quarter.Q2) shouldBe true
      Quarter.values.exists(_ == Quarter.Q3) shouldBe true
      Quarter.values.exists(_ == Quarter.Q4) shouldBe true
    }
  }
}
