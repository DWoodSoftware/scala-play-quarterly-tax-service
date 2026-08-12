package controllers

import domain.* 
import repositories.QuarterlyUpdateRepository
import services.QuarterlyUpdateService

import scala.concurrent.{ExecutionContext, Future}

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

            contentAsJson(result) mustBe Json.obj(
                "error" -> Json.obj(
                    "code" -> "VALIDATION_FAILED",
                    "message" -> "Quarterly update validation failed.",
                    "details" -> Json.arr(
                        "MissingIncome"
                    )
                )
            )
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

    "GET /api/v1/quarterly-updates/:id" should {

        "return 200 OK for an existing quarterly update" in {
            val createBody = Json.obj(
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
            "expenses" -> Json.arr()
            )

            val createRequest =
            FakeRequest(
                POST,
                "/api/v1/quarterly-updates"
            ).withJsonBody(createBody)

            val createResult = route(app, createRequest).get

            status(createResult) mustBe CREATED

            val id =
            (contentAsJson(createResult) \ "id").as[String]

            val retrieveRequest =
            FakeRequest(
                GET,
                s"/api/v1/quarterly-updates/$id"
            )

            val retrieveResult =
            route(app, retrieveRequest).get

            status(retrieveResult) mustBe OK
        }

        "return 404 Not Found when the quarterly update does not exist" in {
            val request =
                FakeRequest(
                GET,
                "/api/v1/quarterly-updates/missing-id"
                )

            val result = route(app, request).get

            status(result) mustBe NOT_FOUND

            contentAsJson(result) mustBe Json.obj(
                "error" -> Json.obj(
                    "code" -> "UPDATE_NOT_FOUND",
                    "message" -> "Quarterly update was not found"
                )
            )
        }
    }

    "POST /api/v1/quarterly-updates/:id/submit" should {

        "return 200 OK when submitting an existing valid draft" in {
            val createBody = Json.obj(
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
            "expenses" -> Json.arr()
            )

            val createRequest =
            FakeRequest(
                POST,
                "/api/v1/quarterly-updates"
            ).withJsonBody(createBody)

            val createResult =
            route(app, createRequest).get

            status(createResult) mustBe CREATED

            val id =
            (contentAsJson(createResult) \ "id").as[String]

            val submitRequest =
            FakeRequest(
                POST,
                s"/api/v1/quarterly-updates/$id/submit"
            )

            val submitResult =
            route(app, submitRequest).get

            status(submitResult) mustBe OK
        }

        "return 404 Not Found when submitting a missing quarterly update" in {
            val request =
                FakeRequest(
                POST,
                "/api/v1/quarterly-updates/missing-id/submit"
                )

            val result = route(app, request).get

            status(result) mustBe NOT_FOUND
        }

        "return 422 Unprocessable Entity when submission validation fails" in {
            val taxpayerReference =
                TaxpayerReference.create("TAX-12345678") match {
                    case Right(value) => value
                    case Left(error) => fail(error)
                }

            val taxYear =
                TaxYear.create(2026, 2027) match {
                    case Right(value) => value
                    case Left(error) => fail(error)
                }

            val invalidInput =
                QuarterlyUpdateInput(
                    taxpayerReference = taxpayerReference,
                    taxYear = taxYear,
                    quarter = Quarter.Q1,
                    income = List.empty,
                    expenses = List.empty
                )

            val invalidDraft =
                QuarterlyUpdate.create(invalidInput)

            val repository = new QuarterlyUpdateRepository {

                override def save(
                    update: QuarterlyUpdate
                ): Future[QuarterlyUpdate] =
                    Future.successful(update)

                override def findById(
                    id: String
                ): Future[Option[QuarterlyUpdate]] =
                    Future.successful(Some(invalidDraft))
            }

            val service =
                new QuarterlyUpdateService(
                    repository,
                    ExecutionContext.global
                )

            val controller =
                new QuarterlyUpdateController(
                    stubControllerComponents(),
                    service
                )(using ExecutionContext.global)

            val request =
                FakeRequest(
                    POST,
                    s"/api/v1/quarterly-updates/${invalidDraft.id}/submit"
                )

            val result =
                controller.submit(invalidDraft.id)(request)

            status(result) mustBe UNPROCESSABLE_ENTITY
        }

        "return 409 Conflict when submitting an already submitted update" in {
            val createBody = Json.obj(
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
                "expenses" -> Json.arr()
            )

            val createRequest =
                FakeRequest(
                    POST,
                    "/api/v1/quarterly-updates"
                ).withJsonBody(createBody)

            val createResult =
                route(app, createRequest).get

            status(createResult) mustBe CREATED

            val id =
                (contentAsJson(createResult) \ "id").as[String]

            val firstSubmission =
                route(
                    app,
                    FakeRequest(
                        POST,
                        s"/api/v1/quarterly-updates/$id/submit"
                    )
                ).get

            status(firstSubmission) mustBe OK

            val secondSubmission =
                route(
                    app,
                    FakeRequest(
                        POST,
                        s"/api/v1/quarterly-updates/$id/submit"
                    )
                ).get

            status(secondSubmission) mustBe CONFLICT

            contentAsJson(secondSubmission) mustBe Json.obj(
                "error" -> Json.obj(
                    "code" -> "INVALID_STATE_TRANSITION",
                    "message" -> "Quarterly update cannot transition from its current state"
                )
            )
        }
    }
}