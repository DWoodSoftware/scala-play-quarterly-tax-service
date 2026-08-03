package services

import domain.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import repositories.QuarterlyUpdateRepository

import scala.concurrent.Future

class QuarterlyUpdateServiceSpec extends AsyncWordSpec with Matchers {

  "QuarterlyUpdateService.submit" should {

    "submit a valid draft quarterly update" in {
        val draft = QuarterlyUpdate.create(validInput())

        var persisted: Option[QuarterlyUpdate] = None

        val repository = new QuarterlyUpdateRepository {

            override def save(
                update: QuarterlyUpdate
            ): Future[QuarterlyUpdate] = {
                persisted = Some(update)
                Future.successful(update)
            }

            override def findById(
                id: String
            ): Future[Option[QuarterlyUpdate]] =
                Future.successful(Some(draft))
        }

        val service = new QuarterlyUpdateService(repository)

        service.submit(draft.id).map { result =>
            result match {
                case Right(submitted) =>
                    submitted.status shouldBe SubmissionStatus.Submitted
                    submitted.submittedAt shouldBe defined
                    persisted shouldBe Some(submitted)

                case Left(error) =>
                    fail(s"Expected successful submission, got: $error ")
            }
        }
    }

    "return UpdateNotFound when submitting a missing quarterly update" in {
        val repository = new QuarterlyUpdateRepository {

            override def save(
                update: QuarterlyUpdate
            ): Future[QuarterlyUpdate] =
                Future.successful(update)

            override def findById(
                id: String
            ): Future[Option[QuarterlyUpdate]] =
                Future.successful(None)
        }

        val service = new QuarterlyUpdateService(repository)

        service.submit("missing-id").map { result =>
            result shouldBe Left(
                DomainError.UpdateNotFound("missing-id")
            )
        }
    }
  }

  "QuarterlyUpdateService.findById" should {
    
    "return an existing quarterly update" in {
        val update = QuarterlyUpdate.create(validInput())

        val repository = new QuarterlyUpdateRepository {
            
            override def save(
                update: QuarterlyUpdate
            ): Future[QuarterlyUpdate] =
                Future.successful(update)

            override def findById(
                id: String
            ): Future[Option[QuarterlyUpdate]] =
                Future.successful(Some(update))
        }

        val service = new QuarterlyUpdateService(repository)

        service.findById(update.id).map { result =>
            result shouldBe Right(update)
        }
    }

    "return UpdateNotFound when the quarterly update does not exist" in {
        val repository = new QuarterlyUpdateRepository {

            override def save(
                update: QuarterlyUpdate
            ): Future[QuarterlyUpdate] =
                Future.successful(update)

            override def findById(
                id: String
            ): Future[Option[QuarterlyUpdate]] =
                Future.successful(None)
        }

        val service = new QuarterlyUpdateService(repository)

        service.findById("missing-id").map { result =>
            result shouldBe Left(
                DomainError.UpdateNotFound("missing-id")
            )
        }
    }
  }

  "QuarterlyUpdateService.create" should {

    "validate, create, and persist a draft quarterly update" in {
      var persisted: Option[QuarterlyUpdate] = None

      val repository = new QuarterlyUpdateRepository {

        override def save(
            update: QuarterlyUpdate
        ): Future[QuarterlyUpdate] = {
          persisted = Some(update)
          Future.successful(update)
        }

        override def findById(
            id: String
        ): Future[Option[QuarterlyUpdate]] =
          Future.successful(None)
      }

      val service = new QuarterlyUpdateService(repository)

      val input = validInput()

      service.create(input).map { result =>
        result match {
          case Right(update) =>
            update.status shouldBe SubmissionStatus.Draft
            persisted shouldBe Some(update)

          case Left(error) =>
            fail(s"Expected successful creation, got: $error")
        }
      }
    }

    "return a validation failure without persisting invalid input" in {
      var saveCalled = false

      val repository = new QuarterlyUpdateRepository {

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

      val service = new QuarterlyUpdateService(repository)

      val input =
        validInput().copy(income = List.empty)

      service.create(input).map { result =>
        result shouldBe Left(
          DomainError.ValidationFailed(
            List(ValidationError.MissingIncome)
          )
        )

        saveCalled shouldBe false
      }
    }
  }

  private def validInput(): QuarterlyUpdateInput = {
    val taxpayerReference =
      TaxpayerReference.create("TAX-12345678") match {
        case Right(value) => value
        case Left(error)  => fail(error)
      }

    val taxYear =
      TaxYear.create(2026, 2027) match {
        case Right(value) => value
        case Left(error)  => fail(error)
      }

    val income =
      IncomeEntry.create(
        IncomeCategory.SelfEmployment,
        BigDecimal("1500.00")
      ) match {
        case Right(value) => value
        case Left(error)  => fail(error)
      }

    QuarterlyUpdateInput(
      taxpayerReference = taxpayerReference,
      taxYear = taxYear,
      quarter = Quarter.Q1,
      income = List(income),
      expenses = List.empty
    )
  }
}