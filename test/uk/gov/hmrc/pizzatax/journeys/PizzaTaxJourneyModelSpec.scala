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
import uk.gov.hmrc.pizzatax.models._
import uk.gov.hmrc.pizzatax.support._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PizzaTaxJourneyModelSpec extends AnyWordSpec with Matchers with JourneyModelSpec {

  override val model = PizzaTaxJourneyModel

  import model.{State, Transitions}

  "PizzaTaxJourneyModel" when {

    "at state Start" should {
      "go to an empty HaveYouBeenHungryRecently when start" in
        given(State.Start)
          .when(Transitions.start)
          .thenGoes(State.HaveYouBeenHungryRecently)

      "do nothing when strange transition" in {
        for (
          t <- Set(
                 Transitions.submittedHaveYouBeenHungryRecently(true),
                 Transitions.submittedHaveYouBeenHungryRecently(false)
               )
        )
          given(State.Start)
            .when(t)
            .thenNoChange
      }
    }

    "at state HaveYouBeenHungryRecently" should {
      "go to WhatYouDidToAddressHunger when submitted true" in
        given(State.HaveYouBeenHungryRecently)
          .when(Transitions.submittedHaveYouBeenHungryRecently(true))
          .thenGoes(State.WhatYouDidToAddressHunger)

      "go to DidYouOrderPizzaAnyway when submitted false" in
        given(State.HaveYouBeenHungryRecently)
          .when(Transitions.submittedHaveYouBeenHungryRecently(false))
          .thenGoes(State.DidYouOrderPizzaAnyway)

      "reset questionaire when start" in {
        for (b <- Set(true, false))
          given(State.HaveYouBeenHungryRecently)
            .when(Transitions.start)
            .thenGoes(State.HaveYouBeenHungryRecently)
      }
    }

    "at state WhatYouDidToAddressHunger" should {
      "go to dead end when pizza has been ordered" in
        given(State.WhatYouDidToAddressHunger)
          .when(Transitions.submittedWhatYouDidToAddressHunger(HungerSolution.OrderPizza))
          .thenGoes(State.WorkInProgressDeadEnd)

      "go to DidYouOrderPizzaAnyway when any other option selected" in {
        for (a <- HungerSolution.values - HungerSolution.OrderPizza)
          given(State.WhatYouDidToAddressHunger)
            .when(Transitions.submittedWhatYouDidToAddressHunger(a))
            .thenGoes(State.DidYouOrderPizzaAnyway)
      }
    }

    "at state DidYouOrderPizzaAnyway" should {
      "finish journey if not hungry and answer is no" in
        given(State.DidYouOrderPizzaAnyway)
          .when(Transitions.submittedDidYouOrderPizzaAnyway(false))
          .thenGoes(State.NotEligibleForPizzaTax)

      "ask ??? if not hungry and answer is yes" in
        given(State.DidYouOrderPizzaAnyway)
          .when(Transitions.submittedDidYouOrderPizzaAnyway(true))
          .thenGoes(State.WorkInProgressDeadEnd)

    }
  }
}
