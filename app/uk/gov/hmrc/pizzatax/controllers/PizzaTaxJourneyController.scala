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
import play.api.data.Form
import play.api.mvc._
import uk.gov.hmrc.pizzatax.config.AppConfig
import uk.gov.hmrc.pizzatax.connectors.FrontendAuthConnector
import uk.gov.hmrc.pizzatax.services.PizzaTaxJourneyServiceWithHeaderCarrier

import javax.inject.Inject
import javax.inject.Singleton
import uk.gov.hmrc.pizzatax.models.HungerSolution

@Singleton
class PizzaTaxJourneyController @Inject() (
  appConfig: AppConfig,
  authConnector: FrontendAuthConnector,
  environment: Environment,
  configuration: Configuration,
  controllerComponents: MessagesControllerComponents,
  views: uk.gov.hmrc.pizzatax.views.Views,
  pizzaTaxJourneyService: PizzaTaxJourneyServiceWithHeaderCarrier
) extends BaseJourneyController(
      pizzaTaxJourneyService,
      controllerComponents,
      appConfig,
      authConnector,
      environment,
      configuration
    ) {

  final val controller = routes.PizzaTaxJourneyController

  import journeyService.model._
  import PizzaTaxJourneyController._

  // YOUR ACTIONS

  final val showStart: Action[AnyContent] =
    actions.apply(Transitions.start)

  final val showHaveYouBeenHungryRecently: Action[AnyContent] =
    actions.show[State.HaveYouBeenHungryRecently.type]

  final val submittedHaveYouBeenHungryRecently: Action[AnyContent] =
    actions
      .bindForm(Forms.haveYouBeenHungryRecentlyForm)
      .apply(Transitions.submittedHaveYouBeenHungryRecently)

  final val showWhatYouDidToAddressHunger: Action[AnyContent] =
    actions.show[State.WhatYouDidToAddressHunger.type]

  /**
    * Function from the `State` to the `Call` (route),
    * used by play-fsm internally to create redirects.
    */
  final override def getCallFor(state: State)(implicit request: Request[_]): Call =
    state match {
      case State.Start                     => controller.showStart
      case State.HaveYouBeenHungryRecently => controller.showHaveYouBeenHungryRecently
      case State.WhatYouDidToAddressHunger => controller.showWhatYouDidToAddressHunger
      case _                               => controller.showWorkInProgress
    }

  import uk.gov.hmrc.play.fsm.OptionalFormOps._

  /**
    * Function from the `State` to the `Result`,
    * used by play-fsm internally to render the actual content.
    */
  final override def renderState(state: State, breadcrumbs: List[State], formWithErrors: Option[Form[_]])(implicit
    request: Request[_]
  ): Result =
    state match {
      case State.Start => Redirect(controller.showHaveYouBeenHungryRecently)
      case State.HaveYouBeenHungryRecently =>
        Ok(
          views.haveYouBeenHungryRecentlyView(
            formWithErrors.or(Forms.haveYouBeenHungryRecentlyForm),
            controller.submittedHaveYouBeenHungryRecently,
            None
          )
        )
      case State.WhatYouDidToAddressHunger =>
        Ok(
          views.whatYouDidToAddressHungerView(
            formWithErrors.or(Forms.whatYouDidToAddressHunger),
            controller.showWorkInProgress,
            Some(backLinkFor(breadcrumbs))
          )
        )
      case _ => NotImplemented
    }
}

object PizzaTaxJourneyController {

  object Forms {

    import play.api.data.Form
    import play.api.data.Forms._
    import uk.gov.hmrc.pizzatax.controllers.FormFieldMappings._

    val haveYouBeenHungryRecentlyForm = Form[Boolean](
      mapping("haveYouBeenHungryRecently" -> booleanMapping("haveYouBeenHungryRecently", "yes", "no"))(identity)(
        Option.apply
      )
    )

    val whatYouDidToAddressHunger = Form[HungerSolution](
      mapping("whatYouDidToAddressHunger" -> enumMapping[HungerSolution]("whatYouDidToAddressHunger"))(identity)(
        Option.apply
      )
    )
  }

}
