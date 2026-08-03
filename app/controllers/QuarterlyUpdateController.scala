package controllers

import domain.* 
import play.api.libs.json.*
import play.api.mvc.* 
import services.QuarterlyUpdateService

import javax.inject.* 
import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class QuarterlyUpdateController @Inject() (
    cc: ControllerComponents,
    service: QuarterlyUpdateService
)(using ev: ExecutionContext)
    extends AbstractController(cc) {

    def create : Action[JsValue] =
        Action.async(parse.json) { request =>
            parseInput(request.body) match{
                case Left(error) =>
                    Future.successful(
                        BadRequest(Json.obj("error" -> error))
                    )

                case Right(input) =>
                    service.create(input).map {
                        case Right(update) =>
                            Created(
                                Json.obj(
                                    "id" -> update.id,
                                    "status" -> update.status.toString
                                )
                            )

                        case Left(DomainError.ValidationFailed(errors)) =>
                            UnprocessableEntity(
                                Json.obj(
                                    "errors" -> errors.map(_.toString)
                                )
                            )

                        case Left(_) =>
                            InternalServerError
                    }
            }
        }

    private def parseInput(
        json: JsValue
    ): Either[String, QuarterlyUpdateInput] =
        for {
            taxpayerReferenceValue <-
                (json \ "taxpayerReference")
                    .asOpt[String]
                    .toRight("Invalid taxpayerReference")

            taxpayerReference <-
                TaxpayerReference.create(taxpayerReferenceValue)

            startYear <-
                (json \ "taxYear" \ "startYear")
                    .asOpt[Int]
                    .toRight("Invalid taxYear.startYear")

            endYear <-
                (json \ "taxYear" \ "endYear")
                    .asOpt[Int]
                    .toRight("Invalid taxYear.endYear")

            taxYear <-
                TaxYear.create(startYear, endYear)

            quarterValue <-
                (json \ "quarter")
                    .asOpt[String]
                    .toRight("Invalid quarter")

            quarter <-
                parseQuarter(quarterValue)

            income <-
                parseIncome(json \ "income")

            expenses <-
                parseExpenses(json \ "expenses")
        } yield QuarterlyUpdateInput(
            taxpayerReference = taxpayerReference,
            taxYear = taxYear,
            quarter = quarter,
            income = income,
            expenses = expenses
        )

    private def parseQuarter(
        value: String
    ): Either[String, Quarter] =
        Quarter.values
            .find(_.toString == value)
            .toRight("Invalid quarter")

    private def parseIncome(
        value: JsLookupResult
    ): Either[String, List[IncomeEntry]] =
        value.asOpt[JsArray] match {
            case None =>
                Left("Invalid income")

            case Some(array) =>
                sequence(
                    array.value.toList.map { json =>
                        for {
                            categoryValue <-
                                (json \ "category")
                                    .asOpt[String]
                                    .toRight("Invalid income category")

                            category <-
                                IncomeCategory.values
                                    .find(_.toString == categoryValue)
                                    .toRight("Invalid income category")

                            amount <-
                                (json \ "amount")
                                    .asOpt[BigDecimal]
                                    .toRight("Invalid income amount")

                            entry <-
                                IncomeEntry.create(category, amount)
                        } yield entry
                    }
                )
        }

    private def parseExpenses(
        value: JsLookupResult
    ): Either[String, List[ExpenseEntry]] =
        value.asOpt[JsArray] match {
            case None =>
                Left("Invalid expenses")

            case Some(array) =>
                sequence(
                    array.value.toList.map { json =>
                        for {
                            categoryValue <-
                                (json \ "category")
                                    .asOpt[String]
                                    .toRight("Invalid expense category")
                            
                            category <-
                                ExpenseCategory.values
                                    .find(_.toString == categoryValue)
                                    .toRight("Invalid expense category")

                            amount <-
                                (json \ "amount")
                                    .asOpt[BigDecimal]
                                    .toRight("Invalid expense amount")

                            entry <-
                                ExpenseEntry.create(category, amount)
                        } yield entry
                    }
                )
        }

    private def sequence[A](
        values: List[Either[String, A]]
    ): Either[String, List[A]] =
        values.foldRight(Right(Nil): Either[String, List[A]]) {
            case (Right(value), Right(rest)) =>
                Right(value :: rest)

            case (Left(error), _) =>
                Left(error)
            
            case (_, Left(error)) =>
                Left(error)
        }
    }