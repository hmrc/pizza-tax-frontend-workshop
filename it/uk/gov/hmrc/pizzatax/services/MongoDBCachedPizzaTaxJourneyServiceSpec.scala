package uk.gov.hmrc.pizzatax.services

import uk.gov.hmrc.pizzatax.support.AppISpec
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID

class MongoDBCachedPizzaTaxJourneyServiceSpec extends AppISpec {

  lazy val service: MongoDBCachedPizzaTaxJourneyService =
    app.injector.instanceOf[MongoDBCachedPizzaTaxJourneyService]

  import service.model.{State, Transitions}

  implicit val hc: HeaderCarrier =
    HeaderCarrier()
      .withExtraHeaders("PizzaTaxJourney" -> UUID.randomUUID.toString)

  "MongoDBCachedPizzaTaxJourneyService" should {
    "apply start transition" in {
      await(service.apply(Transitions.start)) shouldBe ((State.HaveYouBeenHungryRecently, List(State.Start)))
    }
  }

}
