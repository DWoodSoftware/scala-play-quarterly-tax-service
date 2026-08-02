package repositories

import domain.QuarterlyUpdate

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

final class InMemoryQuarterlyUpdateRepository extends QuarterlyUpdateRepository {
    
    private val storage =
        TrieMap.empty[String, QuarterlyUpdate]

    override def save(
        update: QuarterlyUpdate
    ): Future[QuarterlyUpdate] = {
        storage.put(update.id, update)
        Future.successful(update)
    }

    override def findById(
        id: String
    ): Future[Option[QuarterlyUpdate]] =
        Future.successful(storage.get(id))
}
