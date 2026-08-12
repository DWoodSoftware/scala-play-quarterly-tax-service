package integration

import domain.QuarterlyUpdate
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Injecting}
import repositories.QuarterlyUpdateRepository

import scala.concurrent.Future

class QuarterlyUpdateLifecycleSpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with Injecting {

  final class RecordingQuarterlyUpdateRepository
      extends QuarterlyUpdateRepository {

    var saveCalled: Boolean = false

    override def save(
        update: QuarterlyUpdate
    ): Future[QuarterlyUpdate] = {
      saveCalled = true
      Future.successful(update)
    }

    override def findById(
        id: String
    ): Future[Option[QuarterlyUpdate]] =
      Future.successful(None)
  }

  "Quarterly update lifecycle" should {

    "create and then retrieve the same quarterly update" in {
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

      val createResult =
        route(
          app,
          FakeRequest(
            POST,
            "/api/v1/quarterly-updates"
          ).withJsonBody(requestBody)
        ).get

      status(createResult) mustBe CREATED

      val created =
        contentAsJson(createResult)

      val id =
        (created \ "id").as[String]

      val retrieveResult =
        route(
          app,
          FakeRequest(
            GET,
            s"/api/v1/quarterly-updates/$id"
          )
        ).get

      status(retrieveResult) mustBe OK

      val retrieved =
        contentAsJson(retrieveResult)

      (retrieved \ "id").as[String] mustBe id
      (retrieved \ "status").as[String] mustBe "Draft"
      (retrieved \ "totalIncome").as[BigDecimal] mustBe BigDecimal("1500.00")
      (retrieved \ "totalExpenses").as[BigDecimal] mustBe BigDecimal("250.00")
      (retrieved \ "netAmount").as[BigDecimal] mustBe BigDecimal("1250.00")
    }

    "create and then submit a quarterly update" in {
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
            "expenses" -> Json.arr()
        )

        val createResult =
            route(
            app,
            FakeRequest(
                POST,
                "/api/v1/quarterly-updates"
            ).withJsonBody(requestBody)
            ).get

        status(createResult) mustBe CREATED

        val id =
            (contentAsJson(createResult) \ "id").as[String]

        val submitResult =
            route(
            app,
            FakeRequest(
                POST,
                s"/api/v1/quarterly-updates/$id/submit"
            )
            ).get

        status(submitResult) mustBe OK

        val submitted =
            contentAsJson(submitResult)

        (submitted \ "id").as[String] mustBe id
        (submitted \ "status").as[String] mustBe "Submitted"
        (submitted \ "submittedAt").asOpt[String] mustBe defined
    }

    "reject duplicate submission without changing the submitted update" in {
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
        "expenses" -> Json.arr()
      )

      val createResult =
        route(
          app,
          FakeRequest(
            POST,
            "/api/v1/quarterly-updates"
          ).withJsonBody(requestBody)
        ).get

      status(createResult) mustBe CREATED

      val id =
        (contentAsJson(createResult) \ "id").as[String]

      val firstSubmitResult =
        route(
          app,
          FakeRequest(
            POST,
            s"/api/v1/quarterly-updates/$id/submit"
          )
        ).get

      status(firstSubmitResult) mustBe OK
      (contentAsJson(firstSubmitResult) \ "status").as[String] mustBe "Submitted"

      val secondSubmitResult =
        route(
          app,
          FakeRequest(
            POST,
            s"/api/v1/quarterly-updates/$id/submit"
          )
        ).get

      status(secondSubmitResult) mustBe CONFLICT

      val retrieveResult =
        route(
          app,
          FakeRequest(
            GET,
            s"/api/v1/quarterly-updates/$id"
          )
        ).get

      status(retrieveResult) mustBe OK
      (contentAsJson(retrieveResult) \ "status").as[String] mustBe "Submitted"
    }

    "not persist an invalid quarterly update" in {
      val repository =
        new RecordingQuarterlyUpdateRepository

      val testApp: Application =
        new GuiceApplicationBuilder()
          .overrides(
            bind[QuarterlyUpdateRepository]
              .toInstance(repository)
          )
          .build()

      val requestBody = Json.obj(
        "taxpayerReference" -> "TAX-12345678",
        "taxYear" -> Json.obj(
          "startYear" -> 2026,
          "endYear" -> 2027
        ),
        "quarter" -> "Q1",
        "income" -> Json.arr(),
        "expenses" -> Json.arr()
      )

      val result =
        route(
          testApp,
          FakeRequest(
            POST,
            "/api/v1/quarterly-updates"
          ).withJsonBody(requestBody)
        ).get

      status(result) mustBe UNPROCESSABLE_ENTITY
      repository.saveCalled mustBe false
    }

    "calculate derived financial values server-side" in {
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
            "amount" -> 2000.00
          ),
          Json.obj(
            "category" -> "Other",
            "amount" -> 500.00
          )
        ),
        "expenses" -> Json.arr(
          Json.obj(
            "category" -> "OfficeCosts",
            "amount" -> 300.00
          )
        ),

        // Malicious/incorrect client-supplied derived values
        "totalIncome" -> 999999.00,
        "totalExpenses" -> 999999.00,
        "netAmount" -> 999999.00
      )

      val createResult =
        route(
          app,
          FakeRequest(
            POST,
            "/api/v1/quarterly-updates"
          ).withJsonBody(requestBody)
        ).get

      status(createResult) mustBe CREATED

      val id =
        (contentAsJson(createResult) \ "id").as[String]

      val retrieveResult =
        route(
          app,
          FakeRequest(
            GET,
            s"/api/v1/quarterly-updates/$id"
          )
        ).get

      status(retrieveResult) mustBe OK

      val retrieved =
        contentAsJson(retrieveResult)

      (retrieved \ "totalIncome").as[BigDecimal] mustBe
        BigDecimal("2500.00")

      (retrieved \ "totalExpenses").as[BigDecimal] mustBe
        BigDecimal("300.00")

      (retrieved \ "netAmount").as[BigDecimal] mustBe
        BigDecimal("2200.00")
    }
  }
}