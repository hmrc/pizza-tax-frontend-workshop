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

package uk.gov.hmrc.pizzatax.journeys

import play.api.libs.json._
import uk.gov.hmrc.pizzatax.journeys.PizzaTaxJourneyModel.State
import uk.gov.hmrc.pizzatax.journeys.PizzaTaxJourneyModel.State._
import uk.gov.hmrc.play.fsm.JsonStateFormats

object PizzaTaxJourneyStateFormats extends JsonStateFormats[State] {

  val AreYouEligibleForSpecialAllowanceFormat = Json.format[AreYouEligibleForSpecialAllowance]
  val WhatIsYourITRoleFormat = Json.format[WhatIsYourITRole]
  val QuestionnaireSummaryFormat = Json.format[QuestionnaireSummary]
  val TaxStatementConfirmationFormat = Json.format[TaxStatementConfirmation]

  override val serializeStateProperties: PartialFunction[State, JsValue] = {
    case s: AreYouEligibleForSpecialAllowance => AreYouEligibleForSpecialAllowanceFormat.writes(s)
    case s: WhatIsYourITRole                  => WhatIsYourITRoleFormat.writes(s)
    case s: QuestionnaireSummary              => QuestionnaireSummaryFormat.writes(s)
    case s: TaxStatementConfirmation          => TaxStatementConfirmationFormat.writes(s)
  }

  override def deserializeState(stateName: String, properties: JsValue): JsResult[State] =
    stateName match {
      case "Start"                             => JsSuccess(Start)
      case "HaveYouBeenHungryRecently"         => JsSuccess(HaveYouBeenHungryRecently)
      case "WhatYouDidToAddressHunger"         => JsSuccess(WhatYouDidToAddressHunger)
      case "DidYouOrderPizzaAnyway"            => JsSuccess(DidYouOrderPizzaAnyway)
      case "HowManyPizzasDidYouOrder"          => JsSuccess(HowManyPizzasDidYouOrder)
      case "NotEligibleForPizzaTax"            => JsSuccess(NotEligibleForPizzaTax)
      case "AreYouEligibleForSpecialAllowance" => AreYouEligibleForSpecialAllowanceFormat.reads(properties)
      case "WhatIsYourITRole"                  => WhatIsYourITRoleFormat.reads(properties)
      case "QuestionnaireSummary"              => QuestionnaireSummaryFormat.reads(properties)
      case "TaxStatementConfirmation"          => TaxStatementConfirmationFormat.reads(properties)
      case "WorkInProgressDeadEnd"             => JsSuccess(WorkInProgressDeadEnd)
      case _                                   => JsError(s"Unknown state name $stateName")
    }
}
