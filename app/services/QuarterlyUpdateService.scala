package services

import domain.* 
import repositories.QuarterlyUpdateRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

final class QuarterlyUpdateService(
    repository: QuarterlyUpdateRepository
)(using ec: ExecutionContext) {

    def create(
        input: QuarterlyUpdateInput
    ): Future[Either[DomainError, QuarterlyUpdate]] = {

        QuarterlyUpdateValidator.validate(input) match {
            case Left(errors) =>
                Future.successful(
                    Left(DomainError.ValidationFailed(errors))
                )
            
            case Right(validInput) =>
                val update =
                    QuarterlyUpdate.create(validInput)

                repository
                    .save(update)
                    .map(saved => Right(saved))
        }
    }

    def findById(
        id: String
    ): Future[Either[DomainError, QuarterlyUpdate]] =
        repository
            .findById(id)
            .map {
                case Some(update) =>
                    Right(update)
            
                case None =>
                    Left(DomainError.UpdateNotFound(id))
            }
    
    def submit(
        id: String
    ): Future[Either[DomainError, QuarterlyUpdate]] =
        repository.findById(id).flatMap {
            case None =>
                Future.successful(
                    Left(DomainError.UpdateNotFound(id))
                )

            case Some(draft) =>
                val input = QuarterlyUpdateInput(
                    taxpayerReference = draft.taxpayerReference,
                    taxYear = draft.taxYear,
                    quarter = draft.quarter,
                    income = draft.income,
                    expenses = draft.expenses
                ) 

                QuarterlyUpdateValidator.validate(input) match {
                    case Left(errors) =>
                        Future.successful(
                            Left(DomainError.ValidationFailed(errors))
                        )

                    case Right(_) =>
                        draft
                            .transitionTo(SubmissionStatus.Validated)
                            .flatMap(
                                _.markSubmitted(java.time.Instant.now())
                            ) match {
                                case Left(error) =>
                                    Future.successful(Left(error))

                                case Right(submitted) =>
                                    repository
                                        .save(submitted)
                                        .map(saved => Right(saved))
                            }
                }
        }
}