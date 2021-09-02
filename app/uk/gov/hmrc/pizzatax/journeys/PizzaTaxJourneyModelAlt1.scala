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
import uk.gov.hmrc.pizzatax.utils.OptionOps._
import uk.gov.hmrc.play.fsm.JourneyModel

object PizzaTaxJourneyModelAlt1 extends JourneyModel {

  sealed trait State

  override val root: State = State.Start

  /** All the possible states the journey can take. */
  object State {

    /** Root state of the journey. */
    case object Start extends State

    /** State intended to use only in the development of the model to fill loose ends. */
    case object WorkInProgressDeadEnd extends State

    // Base trait of states carrying questionnaire answers
    trait HaveAnswers extends State {
      def answers: QuestionnaireAnswers
    }

    // Base trait of states finishing the journey
    trait EndState extends State

    case class HaveYouBeenHungryRecently(answers: QuestionnaireAnswers) extends HaveAnswers

    case class WhatYouDidToAddressHunger(answers: QuestionnaireAnswers) extends HaveAnswers

    case class DidYouOrderPizzaAnyway(answers: QuestionnaireAnswers) extends HaveAnswers

    case class NotEligibleForPizzaTax(answers: QuestionnaireAnswers) extends HaveAnswers with EndState

  }

  /** This is where things happen a.k.a bussiness logic of the service. */
  object Transitions {
    import State._

    // Transition (re)starting the journey.
    final val start =
      Transition {
        case _ => goto(HaveYouBeenHungryRecently(QuestionnaireAnswers.empty))
      }

    final def submittedHaveYouBeenHungryRecently(confirmed: Boolean) =
      Transition {
        case HaveYouBeenHungryRecently(q) =>
          if (confirmed)
            goto(WhatYouDidToAddressHunger(q.withHaveYouBeenHungryRecently(true)))
          else
            goto(DidYouOrderPizzaAnyway(q.withHaveYouBeenHungryRecently(false)))
      }

    final val backToHaveYouBeenHungryRecently =
      Transition {
        case WhatYouDidToAddressHunger(q) =>
          goto(HaveYouBeenHungryRecently(q))
        case DidYouOrderPizzaAnyway(q) if q.whatYouDidToAddressHunger.isEmpty =>
          goto(HaveYouBeenHungryRecently(q))
      }

    final def submittedWhatYouDidToAddressHunger(solution: HungerSolution) =
      Transition {
        case WhatYouDidToAddressHunger(q) =>
          solution match {
            case HungerSolution.OrderPizza =>
              goto(WorkInProgressDeadEnd)
            case _ =>
              goto(DidYouOrderPizzaAnyway(q.withWhatYouDidToAddressHunger(solution)))
          }
      }

    final val backToWhatYouDidToAddressHunger =
      Transition {
        case DidYouOrderPizzaAnyway(q) if q.whatYouDidToAddressHunger.isDefined =>
          goto(WhatYouDidToAddressHunger(q))
      }

    final def submittedDidYouOrderPizzaAnyway(confirmed: Boolean) =
      Transition {
        case DidYouOrderPizzaAnyway(q) =>
          if (confirmed)
            goto(WorkInProgressDeadEnd)
          else
            goto(NotEligibleForPizzaTax(q.withDidYouOrderPizzaAnyway(false)))
      }
  }
}
