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

package uk.gov.hmrc.pizzatax.journey

import uk.gov.hmrc.pizzatax.journeys.PizzaTaxJourneyModel
import uk.gov.hmrc.pizzatax.models.QuestionnaireAnswers
import uk.gov.hmrc.pizzatax.support._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PizzaTaxJourneyModelSpec extends AnyWordSpec with Matchers with JourneyModelSpec {

  override val model = PizzaTaxJourneyModel

  import model.{State, Transitions}

  "PizzaTaxJourneyModel" when {

    "at state Start" should {
      "goto HaveYouBeenHungryRecently when start" in {
        given(State.Start) when Transitions.start should thenGo(
          State.HaveYouBeenHungryRecently(QuestionnaireAnswers.empty)
        )
      }
      "goto Start when askHaveYouBeenHungryRecently" in {
        given(State.Start) when Transitions.askHaveYouBeenHungryRecently should thenGo(State.Start)
      }
    }

    "at state HaveYouBeenHungryRecently" should {
      "stay when askHaveYouBeenHungryRecently" in {
        val thatState = State.HaveYouBeenHungryRecently(QuestionnaireAnswers(haveYouBeenHungryRecently = Some(true)))
        given(thatState) when Transitions.askHaveYouBeenHungryRecently should thenGo(thatState)
      }

      "goto WorkInProgressDeadEnd when submitted haveYouBeenHungryRecently=true" in {
        given(State.HaveYouBeenHungryRecently(QuestionnaireAnswers.empty)) when Transitions
          .submittedHaveYouBeenHungryRecently(true) should thenGo(State.WorkInProgressDeadEnd)
      }

      "goto WorkInProgressDeadEnd when submitted haveYouBeenHungryRecently=false" in {
        given(State.HaveYouBeenHungryRecently(QuestionnaireAnswers.empty)) when Transitions
          .submittedHaveYouBeenHungryRecently(false) should thenGo(State.WorkInProgressDeadEnd)
      }
    }
  }
}
