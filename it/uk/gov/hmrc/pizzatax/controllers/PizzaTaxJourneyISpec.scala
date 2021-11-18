package uk.gov.hmrc.pizzatax.controllers

import akka.actor.ActorSystem
import play.api.libs.json.Format
import play.api.libs.ws.DefaultWSCookie
import play.api.libs.ws.StandaloneWSRequest
import play.api.mvc.AnyContent
import play.api.mvc.Call
import play.api.mvc.Cookie
import play.api.mvc.Request
import play.api.mvc.Session
import play.api.test.FakeRequest
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

  import journey.model._

  implicit val journeyId: JourneyId = JourneyId()

  "PizzaTaxJourneyController" when {
    "GET /" should {
      "show the start page" in {
        journey.setState(State.Start)
        givenAuthorisedForEnrolment(Enrolment("HMRC-XYZ", "Foo", "foo"))
        val result = await(request("/").get())
        journey.getState shouldBe State.HaveYouBeenHungryRecently
        result.status shouldBe 200
        result.body should include(htmlEscapedPageTitle("view.haveYouBeenHungryRecently.title"))
      }
    }

    "GET /have-you-been-hungry-recently" should {
      "show [Have you been hungry recently?] page" in {
        journey.setState(State.HaveYouBeenHungryRecently)
        givenAuthorisedForEnrolment(Enrolment("HMRC-XYZ", "Foo", "foo"))
        val result = await(request("/have-you-been-hungry-recently").get())
        journey.getState shouldBe State.HaveYouBeenHungryRecently
        result.status shouldBe 200
        result.body should include(htmlEscapedPageTitle("view.haveYouBeenHungryRecently.title"))
      }
    }

    "POST /have-you-been-hungry-recently" should {
      "if selected YES then show [What you did to address the hunger?]" in {
        journey.setState(State.HaveYouBeenHungryRecently)
        givenAuthorisedForEnrolment(Enrolment("HMRC-XYZ", "Foo", "foo"))
        val result = await(
          request("/have-you-been-hungry-recently")
            .post(Map("haveYouBeenHungryRecently" -> "yes"))
        )
        journey.getState shouldBe State.WhatYouDidToAddressHunger
        result.status shouldBe 200
        result.body should include(htmlEscapedPageTitle("view.whatYouDidToAddressHunger.title"))
      }

      "if selected NO then show [Did you order pizza anyway?]" in {
        journey.setState(State.HaveYouBeenHungryRecently)
        givenAuthorisedForEnrolment(Enrolment("HMRC-XYZ", "Foo", "foo"))
        val result = await(
          request("/have-you-been-hungry-recently")
            .post(Map("haveYouBeenHungryRecently" -> "no"))
        )
        journey.getState shouldBe State.DidYouOrderPizzaAnyway
        result.status shouldBe 200
        result.body should include(htmlEscapedPageTitle("view.didYouOrderPizzaAnyway.title"))
      }
    }

    "GET /did-you-order-pizza-anyway" should {
      "show [Did you order pizza anyway?] page" in {
        journey.setState(State.DidYouOrderPizzaAnyway)
        givenAuthorisedForEnrolment(Enrolment("HMRC-XYZ", "Foo", "foo"))
        val result = await(request("/did-you-order-pizza-anyway").get())
        journey.getState shouldBe State.DidYouOrderPizzaAnyway
        result.status shouldBe 200
        result.body should include(htmlEscapedPageTitle("view.didYouOrderPizzaAnyway.title"))
      }
    }

    "POST /did-you-order-pizza-anyway" should {
      "if selected YES then show [HowManyPizzasDidYouOrder]" in {
        journey.setState(State.DidYouOrderPizzaAnyway)
        givenAuthorisedForEnrolment(Enrolment("HMRC-XYZ", "Foo", "foo"))
        val result = await(
          request("/did-you-order-pizza-anyway")
            .post(Map("didYouOrderPizzaAnyway" -> "yes"))
        )
        journey.getState shouldBe State.HowManyPizzasDidYouOrder
        //result.status shouldBe 200
        //result.body should include(htmlEscapedPageTitle("view.whatYouDidToAddressHunger.title"))
      }

      "if selected NO then show [NotEligibleForPizzaTax]" in {
        journey.setState(State.DidYouOrderPizzaAnyway)
        givenAuthorisedForEnrolment(Enrolment("HMRC-XYZ", "Foo", "foo"))
        val result = await(
          request("/did-you-order-pizza-anyway")
            .post(Map("didYouOrderPizzaAnyway" -> "no"))
        )
        journey.getState shouldBe State.NotEligibleForPizzaTax
        //result.status shouldBe 200
        //result.body should include(htmlEscapedPageTitle("view.didYouOrderPizzaAnyway.title"))
      }
    }

    "GET /work-in-progress" should {
      "show the NotImplemented error" in {
        journey.setState(State.Start)
        givenAuthorisedForEnrolment(Enrolment("HMRC-XYZ", "Foo", "foo"))
        val result = await(request("/work-in-progress").get())
        result.status shouldBe 501
        journey.getState shouldBe State.Start
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

  final def fakeRequest(cookies: Cookie*)(implicit
    journeyId: JourneyId
  ): Request[AnyContent] =
    fakeRequest("GET", "/", cookies: _*)

  final def fakeRequest(method: String, path: String, cookies: Cookie*)(implicit
    journeyId: JourneyId
  ): Request[AnyContent] =
    FakeRequest(Call(method, path))
      .withCookies(cookies: _*)
      .withSession(journey.journeyKey -> journeyId.value)

  final def request(path: String)(implicit journeyId: JourneyId): StandaloneWSRequest = {
    val sessionCookie =
      sessionCookieBaker
        .encodeAsCookie(Session(Map(journey.journeyKey -> journeyId.value)))
    wsClient
      .url(s"$baseUrl$path")
      .withCookies(
        DefaultWSCookie(
          sessionCookie.name,
          sessionCookieCrypto.crypto.encrypt(PlainText(sessionCookie.value)).value
        )
      )
  }

  final def requestWithCookies(path: String, cookies: (String, String)*)(implicit
    journeyId: JourneyId
  ): StandaloneWSRequest = {
    val sessionCookie =
      sessionCookieBaker
        .encodeAsCookie(Session(Map(journey.journeyKey -> journeyId.value)))

    wsClient
      .url(s"$baseUrl$path")
      .withCookies(
        (cookies.map(c => DefaultWSCookie(c._1, c._2)) :+ DefaultWSCookie(
          sessionCookie.name,
          sessionCookieCrypto.crypto.encrypt(PlainText(sessionCookie.value)).value
        )): _*
      )
  }
}
