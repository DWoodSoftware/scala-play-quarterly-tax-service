package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}

class QuarterlyUpdateControllerSpec 
    extends PlaySpec 
    with GuiceOneAppPerTest
    with Injecting {

    "POST /api/v1/quarterly-updates" should {

        "return 422 Unprocessable Entity when domain validation fails" in {
            val requestBody = Json.obj(
                "taxpayerReference" -> "TAX-12345678",
                "taxYear" -> Json.obj(
                    "startYear" -> 2026,
                    "endYear" -> 2027
                ),
                "quarter" -> "Q1",
                "income" -> Json.arr(),
                "expenses" -> Json.arr(
                    Json.obj(
                        "category" -> "OfficeCosts",
                        "amount" -> 250.00
                    )
                )
            )

            val request =
                FakeRequest(
                    POST,
                    "/api/v1/quarterly-updates"
                ).withJsonBody(requestBody)

            val result = route(app, request).get

            status(result) mustBe UNPROCESSABLE_ENTITY
        }

        "return 201 Created for a valid quarterly update request" in {

            val requestBody = Json.obj(
                "taxpayerReference" -> "TAX-12345678",
                "taxYear" -> Json.obj(
                    "startYear" -> 2026,
                    "endYear" -> 2027
                ),
                "quarter" -> "Q1",
                "income" -> Json.arr(
                    Json.obj(
                        "category" -> "SelfEmployment",
                        "amount" -> 1500.00
                    )
                ),
                "expenses" -> Json.arr(
                    Json.obj(
                        "category" -> "OfficeCosts",
                        "amount" -> 250.00
                    )
                )
            )

            val request =
                FakeRequest(
                    POST,
                    "/api/v1/quarterly-updates"
                ).withJsonBody(requestBody)

            val result =
                route(app, request).get

            status(result) mustBe CREATED
        }

        "return 400 Bad Request for malformed JSON" in {
            val request = 
                FakeRequest(
                    POST,
                    "/api/v1/quarterly-updates"
                )
                    .withHeaders(CONTENT_TYPE -> "application/json")
                    .withBody("{ invalid-json}")

            val result = route(app, request).get

            status(result) mustBe BAD_REQUEST
        }
    }
}