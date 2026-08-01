package domain

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class QuarterSpec extends AnyFunSpec with Matchers {

  describe("Quarter") {
    it("exposes the four supported quarter values") {
      Quarter.all shouldBe List(Quarter.Q1, Quarter.Q2, Quarter.Q3, Quarter.Q4)
    }

    it("does not expose arbitrary values beyond the supported quarter set") {
      Quarter.all should have size 4
      Quarter.all.map(_.toString) shouldBe List("Q1", "Q2", "Q3", "Q4")
    }
  }
}
