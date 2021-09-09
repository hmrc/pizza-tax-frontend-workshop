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

import play.api.libs.json.Format
import play.api.libs.json.JsResultException
import play.api.libs.json.Json
import uk.gov.hmrc.pizzatax.journeys.PizzaTaxJourneyModelAlt1.State
import uk.gov.hmrc.pizzatax.journeys.PizzaTaxJourneyModelAlt1Formats
import uk.gov.hmrc.pizzatax.support.JsonFormatTest
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.pizzatax.models.PizzaOrdersDeclaration
import uk.gov.hmrc.pizzatax.models.PizzaAllowance
import uk.gov.hmrc.pizzatax.models.ITRole
import uk.gov.hmrc.pizzatax.models.QuestionnaireAnswers
import uk.gov.hmrc.pizzatax.models.HungerSolution

class PizzaTaxJourneyModelAlt1FormatsSpec extends AnyWordSpec with Matchers {

  implicit val formats: Format[State] = PizzaTaxJourneyModelAlt1Formats.formats

  "PizzaTaxJourneyStateFormats" should {
    "serialize and deserialize state" in new JsonFormatTest[State](info) {
      validateJsonFormat("""{"state":"Start"}""", State.Start)
      validateJsonFormat("""{"state":"WorkInProgressDeadEnd"}""", State.WorkInProgressDeadEnd)
      validateJsonFormat(
        """{"state":"HaveYouBeenHungryRecently","properties":{"answers":{}}}""",
        State.HaveYouBeenHungryRecently(QuestionnaireAnswers.empty)
      )
      validateJsonFormat(
        """{"state":"WhatYouDidToAddressHunger","properties":{"answers":{"haveYouBeenHungryRecently":true}}}""",
        State.WhatYouDidToAddressHunger(QuestionnaireAnswers.empty.withHaveYouBeenHungryRecently(true))
      )
      validateJsonFormat(
        """{"state":"DidYouOrderPizzaAnyway","properties":{"answers":{"haveYouBeenHungryRecently":false}}}""",
        State.DidYouOrderPizzaAnyway(QuestionnaireAnswers.empty.withHaveYouBeenHungryRecently(false))
      )
      validateJsonFormat(
        """{"state":"NotEligibleForPizzaTax","properties":{"answers":{"haveYouBeenHungryRecently":false,"didYouOrderPizzaAnyway":false}}}""",
        State.NotEligibleForPizzaTax(
          QuestionnaireAnswers.empty.withHaveYouBeenHungryRecently(false).withDidYouOrderPizzaAnyway(false)
        )
      )
      validateJsonFormat(
        """{"state":"HowManyPizzasDidYouOrder","properties":{"answers":{"haveYouBeenHungryRecently":true,"whatYouDidToAddressHunger":"OrderPizza"}}}""",
        State.HowManyPizzasDidYouOrder(
          QuestionnaireAnswers.empty
            .withHaveYouBeenHungryRecently(true)
            .withWhatYouDidToAddressHunger(HungerSolution.OrderPizza)
        )
      )
      validateJsonFormat(
        """{"state":"AreYouEligibleForSpecialAllowance","properties":{"answers":{"haveYouBeenHungryRecently":true,"whatYouDidToAddressHunger":"OrderPizza","pizzaOrders":{"totalNumberOfPizzas":7}}}}""",
        State.AreYouEligibleForSpecialAllowance(
          QuestionnaireAnswers.empty
            .withHaveYouBeenHungryRecently(true)
            .withWhatYouDidToAddressHunger(HungerSolution.OrderPizza)
            .withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = 7))
        )
      )
      validateJsonFormat(
        """{"state":"WhatIsYourITRole","properties":{"answers":{"haveYouBeenHungryRecently":true,"whatYouDidToAddressHunger":"OrderPizza","pizzaOrders":{"totalNumberOfPizzas":7},"pizzaAllowance":"ITWorker"}}}""",
        State.WhatIsYourITRole(
          QuestionnaireAnswers.empty
            .withHaveYouBeenHungryRecently(true)
            .withWhatYouDidToAddressHunger(HungerSolution.OrderPizza)
            .withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = 7))
            .withPizzaAllowance(PizzaAllowance.ITWorker)
        )
      )
      validateJsonFormat(
        """{"state":"QuestionnaireSummary","properties":{"answers":{"haveYouBeenHungryRecently":true,"whatYouDidToAddressHunger":"OrderPizza","pizzaOrders":{"totalNumberOfPizzas":7},"pizzaAllowance":"ITWorker","itRoleOpt":"Designer"}}}""",
        State.QuestionnaireSummary(
          QuestionnaireAnswers.empty
            .withHaveYouBeenHungryRecently(true)
            .withWhatYouDidToAddressHunger(HungerSolution.OrderPizza)
            .withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = 7))
            .withPizzaAllowance(PizzaAllowance.ITWorker)
            .withITRole(ITRole.Designer)
        )
      )
      validateJsonFormat(
        """{"state":"QuestionnaireSummary","properties":{"answers":{"haveYouBeenHungryRecently":false,"didYouOrderPizzaAnyway":true,"pizzaOrders":{"totalNumberOfPizzas":3},"pizzaAllowance":"Basic"}}}""",
        State.QuestionnaireSummary(
          QuestionnaireAnswers.empty
            .withHaveYouBeenHungryRecently(false)
            .withDidYouOrderPizzaAnyway(true)
            .withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = 3))
            .withPizzaAllowance(PizzaAllowance.Basic)
        )
      )
      validateJsonFormat(
        """{"state":"QuestionnaireSummary","properties":{"answers":{"haveYouBeenHungryRecently":true,"whatYouDidToAddressHunger":"RansackFridge","didYouOrderPizzaAnyway":true,"pizzaOrders":{"totalNumberOfPizzas":1},"pizzaAllowance":"Firefighter"}}}""",
        State.QuestionnaireSummary(
          QuestionnaireAnswers.empty
            .withHaveYouBeenHungryRecently(true)
            .withWhatYouDidToAddressHunger(HungerSolution.RansackFridge)
            .withDidYouOrderPizzaAnyway(true)
            .withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = 1))
            .withPizzaAllowance(PizzaAllowance.Firefighter)
        )
      )
      validateJsonFormat(
        """{"state":"TaxStatementConfirmation","properties":{"pizzaOrders":{"totalNumberOfPizzas":4},"pizzaAllowance":"BrokenHeart","itRoleOpt":"Architect","correlationId":"foo","amountOfTaxDue":12345}}""",
        State.TaxStatementConfirmation(
          PizzaOrdersDeclaration(totalNumberOfPizzas = 4),
          PizzaAllowance.BrokenHeart,
          itRoleOpt = Some(ITRole.Architect),
          "foo",
          12345
        )
      )

    }

    "throw an exception when unknown state" in {
      val json = Json.parse("""{"state":"StrangeState","properties":{}}""")
      an[JsResultException] shouldBe thrownBy {
        json.as[State]
      }
    }

  }
}
