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
import uk.gov.hmrc.pizzatax.support._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PizzaTaxJourneyModelSpec extends AnyWordSpec with Matchers with JourneyModelSpec {

  override val model = PizzaTaxJourneyModel

  import model.{State, Transitions}

  "PizzaTaxJourneyModel" when {

    "at state Start" should {
      "goto HaveYouBeenHungryRecently when start" in
        given(State.Start)
          .when(Transitions.start)
          .thenGoes(State.HaveYouBeenHungryRecently)

      "do nothing when strange transition" in {
        for (b <- Set(true, false))
          given(State.Start)
            .when(Transitions.submittedHaveYouBeenHungryRecently(b))
            .thenNoChange
      }

    }

    "at state HaveYouBeenHungryRecently" should {
      "goto an empty HaveYouBeenHungryRecently when start" in
        given(State.HaveYouBeenHungryRecently)
          .when(Transitions.start)
          .thenGoes(State.HaveYouBeenHungryRecently)

      "goto WorkInProgressDeadEnd when submitted true" in
        given(State.HaveYouBeenHungryRecently)
          .when(Transitions.submittedHaveYouBeenHungryRecently(true))
          .thenGoes(State.WorkInProgressDeadEnd)

      "goto WorkInProgressDeadEnd when submitted false" in
        given(State.HaveYouBeenHungryRecently)
          .when(Transitions.submittedHaveYouBeenHungryRecently(false))
          .thenGoes(State.WorkInProgressDeadEnd)

    }
  }
}
