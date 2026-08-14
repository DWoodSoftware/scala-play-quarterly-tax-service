import com.google.inject.AbstractModule
import repositories.{InMemoryQuarterlyUpdateRepository, QuarterlyUpdateRepository}

final class Module extends AbstractModule {

  override def configure(): Unit =
    bind(classOf[QuarterlyUpdateRepository])
      .to(classOf[InMemoryQuarterlyUpdateRepository])
}
