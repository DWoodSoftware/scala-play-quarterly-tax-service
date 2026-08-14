package support

import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

object HttpFixtures {

  def validQuarterlyUpdateJson(
      income: JsArray = Json.arr(
        Json.obj(
          "category" -> "SelfEmployment",
          "amount" -> 1500.00
        )
      ),
      expenses: JsArray = Json.arr(
        Json.obj(
          "category" -> "OfficeCosts",
          "amount" -> 250.00
        )
      )
  ): JsObject =
    Json.obj(
      "taxpayerReference" -> "TAX-12345678",
      "taxYear" -> Json.obj(
        "startYear" -> 2026,
        "endYear" -> 2027
      ),
      "quarter" -> "Q1",
      "income" -> income,
      "expenses" -> expenses
    )
    
    // Intentionally no generic "invalid" fixture:
    // validity belongs to the domain rules under test.

  def createQuarterlyUpdateRequest(
      body: JsObject = validQuarterlyUpdateJson()
  ) =
    FakeRequest(
      POST,
      "/api/v1/quarterly-updates"
    ).withJsonBody(body)

  def retrieveQuarterlyUpdateRequest(
      id: String
  ) =
    FakeRequest(
      GET,
      s"/api/v1/quarterly-updates/$id"
    )

  def submitQuarterlyUpdateRequest(
      id: String
  ) =
    FakeRequest(
      POST,
      s"/api/v1/quarterly-updates/$id/submit"
    )

  def malformedQuarterlyUpdateRequest =
    FakeRequest(
      POST,
      "/api/v1/quarterly-updates"
    )
      .withHeaders(
        CONTENT_TYPE -> "application/json"
      )
      .withBody("{ invalid-json}")
}