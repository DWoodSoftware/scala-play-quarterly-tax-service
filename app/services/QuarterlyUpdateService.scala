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
}