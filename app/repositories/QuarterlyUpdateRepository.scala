package repositories

import domain.QuarterlyUpdate
import scala.concurrent.Future
trait QuarterlyUpdateRepository {

    def save(
        update: QuarterlyUpdate
    ): Future[QuarterlyUpdate]

    def findById(
        id: String
    ): Future[Option[QuarterlyUpdate]]
}
