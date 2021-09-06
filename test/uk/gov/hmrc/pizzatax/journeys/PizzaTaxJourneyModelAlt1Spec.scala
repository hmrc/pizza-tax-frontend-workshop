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

import uk.gov.hmrc.pizzatax.journeys.PizzaTaxJourneyModelAlt1
import uk.gov.hmrc.pizzatax.models._
import uk.gov.hmrc.pizzatax.support._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.pizzatax.journeys.PizzaTaxJourneyModel

class PizzaTaxJourneyModelAlt1Spec extends AnyWordSpec with Matchers with JourneyModelSpec {

  override val model = PizzaTaxJourneyModelAlt1

  import model.{State, Transitions}

  val q13e = QuestionnaireAnswers.empty

  "PizzaTaxJourneyModelAlt1" when {

    "at state Start" should {
      "go to an empty HaveYouBeenHungryRecently when start" in
        given(State.Start)
          .when(Transitions.start)
          .thenGoes(State.HaveYouBeenHungryRecently(q13e))

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
        given(State.HaveYouBeenHungryRecently(q13e))
          .when(Transitions.submittedHaveYouBeenHungryRecently(true))
          .thenGoes(State.WhatYouDidToAddressHunger(q13e.withHaveYouBeenHungryRecently(true)))

      "go to DidYouOrderPizzaAnyway when submitted false" in
        given(State.HaveYouBeenHungryRecently(q13e))
          .when(Transitions.submittedHaveYouBeenHungryRecently(false))
          .thenGoes(State.DidYouOrderPizzaAnyway(q13e.withHaveYouBeenHungryRecently(false)))

      "reset questionaire when start" in {
        for (b <- Set(true, false))
          given(State.HaveYouBeenHungryRecently(q13e.withHaveYouBeenHungryRecently(b)))
            .when(Transitions.start)
            .thenGoes(State.HaveYouBeenHungryRecently(q13e))
      }
    }

    "at state WhatYouDidToAddressHunger" should {

      val givenWhatYouDidToAddressHunger =
        given(State.WhatYouDidToAddressHunger(q13e.withHaveYouBeenHungryRecently(true)))

      "go to dead end when pizza has been ordered" in
        givenWhatYouDidToAddressHunger
          .when(Transitions.submittedWhatYouDidToAddressHunger(HungerSolution.OrderPizza))
          .thenGoes(State.WorkInProgressDeadEnd)

      "go to DidYouOrderPizzaAnyway when any other option selected" in {
        for (a <- HungerSolution.values - HungerSolution.OrderPizza)
          givenWhatYouDidToAddressHunger
            .when(Transitions.submittedWhatYouDidToAddressHunger(a))
            .thenGoes(
              State.DidYouOrderPizzaAnyway(
                q13e
                  .withHaveYouBeenHungryRecently(true)
                  .withWhatYouDidToAddressHunger(a)
              )
            )
      }
    }

    "at state DidYouOrderPizzaAnyway" should {
      "finish journey if not hungry and answer is no" in
        given(State.DidYouOrderPizzaAnyway(q13e.withHaveYouBeenHungryRecently(false)))
          .when(Transitions.submittedDidYouOrderPizzaAnyway(false))
          .thenGoes(
            State.NotEligibleForPizzaTax(
              q13e
                .withHaveYouBeenHungryRecently(false)
                .withDidYouOrderPizzaAnyway(false)
            )
          )

      "ask ??? if not hungry and answer is yes" in
        given(State.DidYouOrderPizzaAnyway(q13e.withHaveYouBeenHungryRecently(false)))
          .when(Transitions.submittedDidYouOrderPizzaAnyway(true))
          .thenGoes(State.WorkInProgressDeadEnd)

      "finish journey if hungry, didn't order pizza and answer is no" in
        given(
          State.DidYouOrderPizzaAnyway(
            q13e
              .withHaveYouBeenHungryRecently(true)
              .withWhatYouDidToAddressHunger(HungerSolution.Daydream)
          )
        )
          .when(Transitions.submittedDidYouOrderPizzaAnyway(false))
          .thenGoes(
            State.NotEligibleForPizzaTax(
              q13e
                .withHaveYouBeenHungryRecently(true)
                .withWhatYouDidToAddressHunger(HungerSolution.Daydream)
                .withDidYouOrderPizzaAnyway(false)
            )
          )

      "do nothing if initial state has an invalid questionnaire" in
        given(
          State.DidYouOrderPizzaAnyway(
            q13e
              .withHaveYouBeenHungryRecently(false)
              .withWhatYouDidToAddressHunger(HungerSolution.Daydream)
          )
        )
          .when(Transitions.submittedDidYouOrderPizzaAnyway(false))
          .thenNoChange

      "ask ??? if hungry, didn't order pizza and answer is yes" in
        given(
          State.DidYouOrderPizzaAnyway(
            q13e
              .withHaveYouBeenHungryRecently(true)
              .withWhatYouDidToAddressHunger(HungerSolution.BurnToasts)
          )
        )
          .when(Transitions.submittedDidYouOrderPizzaAnyway(true))
          .thenGoes(State.WorkInProgressDeadEnd)

    }

    // An example of testing backward transitions (back links)
    "transition backToHaveYouBeenHungryRecently" should {
      "ask HaveYouBeenHungryRecently again for all valid states" in {
        for {
          q <- QuestionnaireAnswers.allPossibleQ13e
          s <- Set(
                 State.HaveYouBeenHungryRecently(q),
                 State.WhatYouDidToAddressHunger(q),
                 State.DidYouOrderPizzaAnyway(q.clearWhatYouDidToAddressHunger())
               )
        } given(s)
          .when(Transitions.backToHaveYouBeenHungryRecently)
          .thenGoes(State.HaveYouBeenHungryRecently(s.answers))
      }

      "do nothing for all invalid states" in {
        for {
          q <- QuestionnaireAnswers.allPossibleQ13e
          s <- Set(
                 State.NotEligibleForPizzaTax(q)
               )
        } given(s)
          .when(Transitions.backToHaveYouBeenHungryRecently)
          .thenNoChange
      }
    }

    "transition backToWhatYouDidToAddressHunger" should {
      "ask WhatYouDidToAddressHunger again for all valid states" in {
        for {
          q <- QuestionnaireAnswers.allPossibleQ13e.filter(_.whatYouDidToAddressHunger.isDefined)
          s <- Set(
                 State.DidYouOrderPizzaAnyway(q)
               )
        } given(s)
          .when(Transitions.backToWhatYouDidToAddressHunger)
          .thenGoes(State.WhatYouDidToAddressHunger(s.answers))
      }

      "do nothing for all invalid states" in {
        for {
          q <- QuestionnaireAnswers.allPossibleQ13e
          s <- Set(
                 State.HaveYouBeenHungryRecently(q),
                 State.NotEligibleForPizzaTax(q)
               )
        } given(s)
          .when(Transitions.backToWhatYouDidToAddressHunger)
          .thenNoChange
      }
    }
  }
}
