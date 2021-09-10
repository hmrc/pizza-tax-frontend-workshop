package uk.gov.hmrc.pizzatax.support

import uk.gov.hmrc.pizzatax.config.AppConfig
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

case class TestAppConfig(
  wireMockBaseUrl: String,
  wireMockPort: Int
) extends AppConfig {

  override val authBaseUrl: String = wireMockBaseUrl
  override val authorisedServiceName: String = "HMRC-XYZ"
  override val authorisedIdentifierKey: String = "Foo"
  override val mongoSessionExpiration: Duration = 1.hour
  override val traceFSM: Boolean = false
  override val exitSurveyUrl: String = wireMockBaseUrl + "/dummy-survey-url"
  override val signOutUrl: String = wireMockBaseUrl + "/dummy-sign-out-url"
  override val researchBannerUrl: String = wireMockBaseUrl + "dummy-research-banner-url"
  override val baseExternalCallbackUrl: String = wireMockBaseUrl
  override val contactHost: String = wireMockBaseUrl
  override val contactFormServiceIdentifier: String = "dummy"
  override val timeout: Int = 10
  override val countdown: Int = 2
}
