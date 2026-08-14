package controllers

import domain.*
import repositories.QuarterlyUpdateRepository
import services.QuarterlyUpdateService

import scala.concurrent.{ExecutionContext, Future}

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.Injecting

import support.DomainFixtures.*
import support.HttpFixtures.*

class QuarterlyUpdateControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "POST /api/v1/quarterly-updates" should {

    "return 422 Unprocessable Entity when domain validation fails" in {
      val requestBody =
        validQuarterlyUpdateJson(
          income = Json.arr()
        )

      val request =
        createQuarterlyUpdateRequest(requestBody)

      val result = route(app, request).get

      status(result) mustBe UNPROCESSABLE_ENTITY

      contentAsJson(result) mustBe Json.obj(
        "error" -> Json.obj(
          "code"    -> "VALIDATION_FAILED",
          "message" -> "Quarterly update validation failed.",
          "details" -> Json.arr(
            "MissingIncome"
          )
        )
      )
    }

    "return 201 Created for a valid quarterly update request" in {
      val result =
        route(
          app,
          createQuarterlyUpdateRequest()
        ).get

      status(result) mustBe CREATED
    }

    "return 400 Bad Request for malformed JSON" in {
      val result = route(
        app,
        malformedQuarterlyUpdateRequest
      ).get

      status(result) mustBe BAD_REQUEST
    }
  }

  "GET /api/v1/quarterly-updates/:id" should {
    "return 500 Internal Server Error when the repository fails unexpectedly" in {
      val repository = new QuarterlyUpdateRepository {

        override def save(
          update: QuarterlyUpdate
        ): Future[QuarterlyUpdate] =
          Future.successful(update)

        override def findById(
          id: String
        ): Future[Option[QuarterlyUpdate]] =
          Future.failed(
            new RuntimeException("repository unavailable")
          )
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
        retrieveQuarterlyUpdateRequest("failing-id")

      val result =
        controller.findById("failing-id")(request)

      status(result) mustBe INTERNAL_SERVER_ERROR

      contentAsJson(result) mustBe Json.obj(
        "error" -> Json.obj(
          "code"    -> "INTERNAL_SERVER_ERROR",
          "message" -> "An unexpected error occurred"
        )
      )
    }

    "return 200 OK for an existing quarterly update" in {
      val createResult =
        route(
          app,
          createQuarterlyUpdateRequest(
            validQuarterlyUpdateJson(
              expenses = Json.arr()
            )
          )
        ).get

      status(createResult) mustBe CREATED

      val id =
        (contentAsJson(createResult) \ "id").as[String]

      val retrieveResult =
        route(
          app,
          retrieveQuarterlyUpdateRequest(id)
        ).get

      status(retrieveResult) mustBe OK
    }

    "return 404 Not Found when the quarterly update does not exist" in {
      val result =
        route(
          app,
          retrieveQuarterlyUpdateRequest("missing-id")
        ).get

      status(result) mustBe NOT_FOUND

      contentAsJson(result) mustBe Json.obj(
        "error" -> Json.obj(
          "code"    -> "UPDATE_NOT_FOUND",
          "message" -> "Quarterly update was not found"
        )
      )
    }
  }

  "POST /api/v1/quarterly-updates/:id/submit" should {

    "return 200 OK when submitting an existing valid draft" in {
      val createResult =
        route(
          app,
          createQuarterlyUpdateRequest(
            validQuarterlyUpdateJson(
              expenses = Json.arr()
            )
          )
        ).get

      status(createResult) mustBe CREATED

      val id =
        (contentAsJson(createResult) \ "id").as[String]

      val submitResult =
        route(
          app,
          submitQuarterlyUpdateRequest(id)
        ).get

      status(submitResult) mustBe OK
    }

    "return 404 Not Found when submitting a missing quarterly update" in {
      val result =
        route(
          app,
          submitQuarterlyUpdateRequest("missing-id")
        ).get

      status(result) mustBe NOT_FOUND
    }

    "return 422 Unprocessable Entity when submission validation fails" in {
      val invalidInput =
        validQuarterlyUpdateInput(
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
        submitQuarterlyUpdateRequest(invalidDraft.id)

      val result =
        controller.submit(invalidDraft.id)(request)

      status(result) mustBe UNPROCESSABLE_ENTITY
    }

    "return 409 Conflict when submitting an already submitted update" in {
      val createResult =
        route(
          app,
          createQuarterlyUpdateRequest(
            validQuarterlyUpdateJson(
              expenses = Json.arr()
            )
          )
        ).get

      status(createResult) mustBe CREATED

      val id =
        (contentAsJson(createResult) \ "id").as[String]

      val firstSubmission =
        route(
          app,
          submitQuarterlyUpdateRequest(id)
        ).get

      status(firstSubmission) mustBe OK

      val secondSubmission =
        route(
          app,
          submitQuarterlyUpdateRequest(id)
        ).get

      status(secondSubmission) mustBe CONFLICT

      contentAsJson(secondSubmission) mustBe Json.obj(
        "error" -> Json.obj(
          "code"    -> "INVALID_STATE_TRANSITION",
          "message" -> "Quarterly update cannot transition from its current state"
        )
      )
    }
  }
}
