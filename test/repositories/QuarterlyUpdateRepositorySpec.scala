package repositories

import domain.QuarterlyUpdate
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Future

class QuarterlyUpdateRepositorySpec extends AnyWordSpec with Matchers {

  "QuarterlyUpdateRepository" should {

    "define an asynchronous save and lookup contract" in {
        class TestRepository extends QuarterlyUpdateRepository {

            override def save(
                update: QuarterlyUpdate
            ): Future[QuarterlyUpdate] =
                Future.successful(update)

            override def findById(
                id: String
            ): Future[Option[QuarterlyUpdate]] =
                Future.successful(None)
        }

        val repository = new TestRepository
        repository shouldBe a[QuarterlyUpdateRepository]
    }
  }
}