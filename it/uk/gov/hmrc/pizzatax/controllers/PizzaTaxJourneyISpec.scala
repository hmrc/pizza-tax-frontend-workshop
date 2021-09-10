package uk.gov.hmrc.pizzatax.controllers

import akka.actor.ActorSystem
import play.api.libs.json.Format
import play.api.libs.ws.DefaultWSCookie
import play.api.mvc.Session
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.pizzatax.journeys.PizzaTaxJourneyModelFormats
import uk.gov.hmrc.pizzatax.repository.CacheRepository
import uk.gov.hmrc.pizzatax.services.MongoDBCachedJourneyService
import uk.gov.hmrc.pizzatax.services.PizzaTaxJourneyService
import uk.gov.hmrc.pizzatax.support.ServerISpec
import uk.gov.hmrc.pizzatax.support.TestJourneyService

import scala.concurrent.ExecutionContext.Implicits.global

class PizzaTaxJourneyISpec extends PizzaTaxJourneyISpecSetup {

  import journey.model.State._

  "PizzaTaxJourneyController" when {
    "GET /" should {
      "show the start page" in {
        implicit val journeyId: JourneyId = JourneyId()
        journey.setState(Start)
        givenAuthorisedForEnrolment(Enrolment("HMRC-XYZ", "Foo", "foo"))

        val result = await(request("/").get())

        result.status shouldBe 501
        journey.getState shouldBe Start
      }
    }

    "GET /work-in-progress" should {
      "show the NotImplemented error" in {
        implicit val journeyId: JourneyId = JourneyId()
        journey.setState(Start)
        givenAuthorisedForEnrolment(Enrolment("HMRC-XYZ", "Foo", "foo"))

        val result = await(request("/work-in-progress").get())

        result.status shouldBe 501
        journey.getState shouldBe Start
      }
    }
  }
}

trait PizzaTaxJourneyISpecSetup extends ServerISpec {

  lazy val journey = new TestJourneyService[JourneyId]
    with PizzaTaxJourneyService[JourneyId] with MongoDBCachedJourneyService[JourneyId] {

    override lazy val actorSystem: ActorSystem = app.injector.instanceOf[ActorSystem]
    override lazy val cacheRepository = app.injector.instanceOf[CacheRepository]
    override lazy val applicationCrypto = app.injector.instanceOf[ApplicationCrypto]

    override val stateFormats: Format[model.State] =
      PizzaTaxJourneyModelFormats.formats

    override def getJourneyId(journeyId: JourneyId): Option[String] = Some(journeyId.value)
  }

  final def request(path: String)(implicit journeyId: JourneyId) = {
    val sessionCookie = sessionCookieBaker.encodeAsCookie(Session(Map(journey.journeyKey -> journeyId.value)))

    wsClient
      .url(s"$baseUrl$path")
      .withCookies(
        DefaultWSCookie(sessionCookie.name, sessionCookieCrypto.crypto.encrypt(PlainText(sessionCookie.value)).value)
      )
  }
}
