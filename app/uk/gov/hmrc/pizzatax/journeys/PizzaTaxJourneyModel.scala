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

import uk.gov.hmrc.pizzatax.models._
import uk.gov.hmrc.play.fsm.JourneyModel

object PizzaTaxJourneyModel extends JourneyModel {

  sealed trait State

  override val root: State = State.Start

  /** All the possible states the journey can take. */
  object State {

    /** Root state of the journey. */
    case object Start extends State

    /** State intended to use only in the development of the model to fill loose ends. */
    case object WorkInProgressDeadEnd extends State

    // Base trait of states finishing the journey
    trait EndState extends State

    case object HaveYouBeenHungryRecently extends State

    case object WhatYouDidToAddressHunger extends State

    case object DidYouOrderPizzaAnyway extends State

    case object NotEligibleForPizzaTax extends EndState

  }

  /** This is where things happen a.k.a bussiness logic of the service. */
  object Transitions {
    import State._

    // Transition (re)starting the journey.
    final val start =
      Transition {
        case _ => goto(HaveYouBeenHungryRecently)
      }

    final def submittedHaveYouBeenHungryRecently(confirmed: Boolean) =
      Transition {
        case HaveYouBeenHungryRecently =>
          if (confirmed)
            goto(WhatYouDidToAddressHunger)
          else
            goto(DidYouOrderPizzaAnyway)
      }

    final def submittedWhatYouDidToAddressHunger(solution: HungerSolution) =
      Transition {
        case WhatYouDidToAddressHunger =>
          solution match {
            case HungerSolution.OrderPizza =>
              goto(WorkInProgressDeadEnd)
            case _ =>
              goto(DidYouOrderPizzaAnyway)
          }
      }

    final def submittedDidYouOrderPizzaAnyway(confirmed: Boolean) =
      Transition {
        case DidYouOrderPizzaAnyway =>
          if (confirmed)
            goto(WorkInProgressDeadEnd)
          else
            goto(NotEligibleForPizzaTax)
      }
  }
}
