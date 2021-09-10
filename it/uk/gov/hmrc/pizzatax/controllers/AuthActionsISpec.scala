package uk.gov.hmrc.pizzatax.controllers

import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.pizzatax.support.AppISpec
import uk.gov.hmrc.pizzatax.stubs._

import scala.concurrent.Future

class AuthActionsISpec extends AuthActionISpecSetup {

  "authorisedWithEnrolment" should {

    "authorize when enrolment granted" in {
      givenAuthorisedForEnrolment(Enrolment("serviceName", "serviceKey", "serviceIdentifierFoo"))
      val result = TestController.testAuthorizedWithEnrolment("serviceName", "serviceKey")
      status(result) shouldBe 200
      bodyOf(result) should be("serviceIdentifierFoo")
    }

    "redirect to subscription journey when insufficient enrollments" in {
      givenRequestIsNotAuthorised("InsufficientEnrolments")
      val result = TestController.testAuthorizedWithEnrolment("serviceName", "serviceKey")
      status(result) shouldBe 303
      redirectLocation(result).get should include("/subscription")
    }

    "redirect to government gateway login when authorization fails" in {
      givenRequestIsNotAuthorised("IncorrectCredentialStrength")
      val result = TestController.testAuthorizedWithEnrolment("serviceName", "serviceKey")
      status(result) shouldBe 303
      redirectLocation(result).get should include(
        "/bas-gateway/sign-in?continue_url=%2F&origin=pizza-tax-frontend"
      )
    }
  }

  "authorisedWithoutEnrolment" should {

    "authorize even when insufficient enrollments" in {
      givenAuthorised
      val result = TestController.testAuhorizedWithoutEnrolment
      status(result) shouldBe 200
      bodyOf(result) should be("none")
    }

    "redirect to government gateway login when authorization fails" in {
      givenRequestIsNotAuthorised("IncorrectCredentialStrength")
      val result = TestController.testAuhorizedWithoutEnrolment
      status(result) shouldBe 303
      redirectLocation(result).get should include(
        "/bas-gateway/sign-in?continue_url=%2F&origin=pizza-tax-frontend"
      )
    }
  }
}

trait AuthActionISpecSetup extends AppISpec with AuthStubs with DataStreamStubs {

  override def fakeApplication: Application = appBuilder.build()

  override def commonStubs(): Unit =
    givenAuditConnector()

  object TestController extends AuthActions {

    override def authConnector: AuthConnector = app.injector.instanceOf[AuthConnector]

    override def config: Configuration = app.injector.instanceOf[Configuration]

    override def env: Environment = app.injector.instanceOf[Environment]

    import scala.concurrent.ExecutionContext.Implicits.global

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val request = FakeRequest().withSession(SessionKeys.authToken -> "Bearer XYZ")

    def testAuthorizedWithEnrolment[A](serviceName: String, identifierKey: String): Result =
      await(super.authorisedWithEnrolment(serviceName, identifierKey) { res =>
        Future.successful(Ok(res.getOrElse("none")))
      })

    def testAuhorizedWithoutEnrolment[A]: Result =
      await(super.authorisedWithoutEnrolment { res =>
        Future.successful(Ok(res.getOrElse("none")))
      })

    override def toSubscriptionJourney(continueUrl: String): Result = Redirect("/subscription")
  }

}
