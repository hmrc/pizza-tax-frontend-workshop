package uk.gov.hmrc.pizzatax.support

import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.mvc.SessionCookieBaker
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto
import uk.gov.hmrc.pizzatax.config.AppConfig

import java.util.UUID
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import uk.gov.hmrc.pizzatax.stubs.AuthStubs
import uk.gov.hmrc.pizzatax.stubs.DataStreamStubs

abstract class ServerISpec
    extends BaseISpec with GuiceOneServerPerSuite with AuthStubs with DataStreamStubs with MetricsTestSupport {

  override def fakeApplication: Application = appBuilder.build()

  override def commonStubs(): Unit = {
    givenAuditConnector()
    givenCleanMetricRegistry()
  }

  lazy val appConfig = fakeApplication.injector.instanceOf[AppConfig]
  lazy val sessionCookieBaker: SessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]
  lazy val sessionCookieCrypto: SessionCookieCrypto = app.injector.instanceOf[SessionCookieCrypto]

  def wsClient = {
    import play.shaded.ahc.org.asynchttpclient._
    val asyncHttpClientConfig = new DefaultAsyncHttpClientConfig.Builder()
      .setMaxRequestRetry(0)
      .setShutdownQuietPeriod(0)
      .setShutdownTimeout(0)
      .setFollowRedirect(true)
      .build
    val asyncHttpClient = new DefaultAsyncHttpClient(asyncHttpClientConfig)
    new StandaloneAhcWSClient(asyncHttpClient)
  }

  case class JourneyId(value: String = UUID.randomUUID().toString)

  val baseUrl: String = s"http://localhost:$port/pay-as-you-eat"

  def requestWithoutJourneyId(path: String) =
    wsClient
      .url(s"$baseUrl$path")

}
