package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}

class HealthControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "GET /health" should {
    "return status UP as JSON" in {
      val request = FakeRequest(GET, "/health")
      val result  = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsString(result) must include("\"status\"")
      contentAsString(result) must include("\"UP\"")
    }
  }
}
