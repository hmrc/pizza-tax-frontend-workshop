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

import uk.gov.hmrc.pizzatax.models.QuestionnaireAnswers
import uk.gov.hmrc.play.fsm.JourneyModel

object PizzaTaxJourneyModel extends JourneyModel {

  sealed trait State
  sealed trait IsError
  sealed trait IsTransient

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

    case class HaveYouBeenHungryRecently(answers: QuestionnaireAnswers) extends HaveAnswers

  }

  /** This is where things happen a.k.a bussiness logic of the service. */
  object Transitions {
    import State._

    // Transition (re)starting the journey.
    final val start =
      Transition {
        case _ => goto(HaveYouBeenHungryRecently(QuestionnaireAnswers.empty))
      }

    final val askHaveYouBeenHungryRecently =
      Transition {
        case s: HaveAnswers => goto(HaveYouBeenHungryRecently(s.answers))
      }

    final def submittedHaveYouBeenHungryRecently(b: Boolean) =
      Transition {
        case s: HaveAnswers => goto(WorkInProgressDeadEnd)
      }
  }
}
