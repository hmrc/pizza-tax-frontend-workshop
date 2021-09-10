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

package uk.gov.hmrc.pizzatax.controllers

import play.api.Configuration
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pizzatax.config.AppConfig
import uk.gov.hmrc.pizzatax.connectors.FrontendAuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.fsm.JourneyController
import uk.gov.hmrc.play.fsm.JourneyIdSupport
import uk.gov.hmrc.play.fsm.JourneyService

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/** Base controller class for a journey. */
abstract class BaseJourneyController[S <: JourneyService[HeaderCarrier]](
  val journeyService: S,
  controllerComponents: MessagesControllerComponents,
  appConfig: AppConfig,
  val authConnector: FrontendAuthConnector,
  val env: Environment,
  val config: Configuration
) extends FrontendController(controllerComponents) with I18nSupport with AuthActions
    with JourneyController[HeaderCarrier] with JourneyIdSupport[HeaderCarrier] {

  final override val actionBuilder = controllerComponents.actionBuilder
  final override val messagesApi = controllerComponents.messagesApi

  implicit val ec: ExecutionContext = controllerComponents.executionContext

  /** Provide response when user have no required enrolment. */
  final override def toSubscriptionJourney(continueUrl: String): Result =
    NotImplemented

  /** AsUser authorisation request */
  final val AsUser: WithAuthorised[Option[String]] = { implicit request =>
    authorisedWithEnrolment(appConfig.authorisedServiceName, appConfig.authorisedIdentifierKey)
  }

  /** Base authorized action builder */
  final val whenAuthorisedAsUser =
    actions.whenAuthorised(AsUser)

  /** Dummy action to use only when developing to fill loose-ends. */
  final val actionNotYetImplemented = Action(NotImplemented)

  final val showWorkInProgress: Action[AnyContent] =
    action { implicit request =>
      Future.successful(NotImplemented)
    }

  // ------------------------------------
  // Retrieval of journeyId configuration
  // ------------------------------------

  private val journeyIdPathParamRegex = ".*?/journey/([A-Za-z0-9-]{36})/.*".r

  final override def journeyId(implicit rh: RequestHeader): Option[String] = {
    val journeyIdFromPath = rh.path match {
      case journeyIdPathParamRegex(id) => Some(id)
      case _                           => None
    }

    val idOpt = journeyIdFromPath
      .orElse(rh.session.get(journeyService.journeyKey))

    idOpt
  }

  final override implicit def context(implicit rh: RequestHeader): HeaderCarrier =
    appendJourneyId(super.hc)

  final override def amendContext(headerCarrier: HeaderCarrier)(key: String, value: String): HeaderCarrier =
    headerCarrier.withExtraHeaders(key -> value)
}
