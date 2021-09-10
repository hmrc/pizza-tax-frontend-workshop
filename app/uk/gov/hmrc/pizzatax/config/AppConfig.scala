/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.pizzatax.config

import com.google.inject.ImplementedBy
import javax.inject.Inject
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import scala.concurrent.duration.Duration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

@ImplementedBy(classOf[AppConfigImpl])
trait AppConfig {

  val authBaseUrl: String
  val signOutUrl: String
  val baseExternalCallbackUrl: String
  val exitSurveyUrl: String
  val researchBannerUrl: String
  val authorisedServiceName: String
  val authorisedIdentifierKey: String
  val contactHost: String
  val contactFormServiceIdentifier: String
  val mongoSessionExpiration: Duration
  val timeout: Int
  val countdown: Int
  val traceFSM: Boolean

  val languageMap: Map[String, Lang] =
    Map(
      "english" -> Lang("en"),
      "cymraeg" -> Lang("cy")
    )

  def requestUri(implicit request: RequestHeader): String =
    SafeRedirectUrl(baseExternalCallbackUrl + request.uri).encodedUrl

  def betaFeedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=$requestUri"
}

class AppConfigImpl @Inject() (config: ServicesConfig) extends AppConfig {

  override val authBaseUrl: String =
    config.baseUrl("auth")

  override val signOutUrl: String =
    config.getString("urls.signOut")

  override val baseExternalCallbackUrl: String =
    config.getString("urls.callback.external")

  private val exitSurveyBaseUrl =
    config.getString("feedback-frontend.host") +
      config.getString("feedback-frontend.url")

  override val researchBannerUrl: String =
    config.getString("urls.researchBanner")

  override val authorisedServiceName: String =
    config.getString("authorisedServiceName")

  override val authorisedIdentifierKey: String =
    config.getString("authorisedIdentifierKey")

  override val contactHost: String =
    config.getString("contact-frontend.host")

  override val contactFormServiceIdentifier: String =
    config.getString("feedback-frontend.formIdentifier")

  override val mongoSessionExpiration: Duration =
    config.getDuration("mongodb.session.expiration")

  override val timeout: Int =
    config.getInt("session.timeoutSeconds")

  override val countdown: Int =
    config.getInt("session.countdownInSeconds")

  override val exitSurveyUrl =
    s"$exitSurveyBaseUrl/$contactFormServiceIdentifier"

  override val traceFSM: Boolean =
    config.getBoolean("trace.fsm")

}
