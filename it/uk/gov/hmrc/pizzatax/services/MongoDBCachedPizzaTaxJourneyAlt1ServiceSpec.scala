package uk.gov.hmrc.pizzatax.services

import uk.gov.hmrc.pizzatax.support.AppISpec
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID
import uk.gov.hmrc.pizzatax.models.QuestionnaireAnswers

class MongoDBCachedPizzaTaxJourneyAlt1ServiceSpec extends AppISpec {

  lazy val service: MongoDBCachedPizzaTaxJourneyAlt1Service =
    app.injector.instanceOf[MongoDBCachedPizzaTaxJourneyAlt1Service]

  import service.model.{State, Transitions}

  implicit val hc: HeaderCarrier =
    HeaderCarrier()
      .withExtraHeaders("PizzaTaxJourneyAlt1" -> UUID.randomUUID.toString)

  "MongoDBCachedPizzaTaxJourneyServiceAlt1" should {
    "apply start transition" in {
      await(service.apply(Transitions.start)) shouldBe (
        (
          State.HaveYouBeenHungryRecently(answers = QuestionnaireAnswers.empty),
          List(State.Start)
        )
      )
    }
  }

}
