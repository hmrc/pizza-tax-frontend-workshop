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
import uk.gov.hmrc.pizzatax.journeys.PizzaTaxJourneyModel.State
import uk.gov.hmrc.pizzatax.journeys.PizzaTaxJourneyModelFormats
import uk.gov.hmrc.pizzatax.support.JsonFormatTest
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.pizzatax.models.PizzaOrdersDeclaration
import uk.gov.hmrc.pizzatax.models.PizzaAllowance
import uk.gov.hmrc.pizzatax.models.ITRole

class PizzaTaxJourneyModelFormatsSpec extends AnyWordSpec with Matchers {

  implicit val formats: Format[State] = PizzaTaxJourneyModelFormats.formats

  "PizzaTaxJourneyStateFormats" should {
    "serialize and deserialize state" in new JsonFormatTest[State](info) {
      validateJsonFormat("""{"state":"Start"}""", State.Start)
      validateJsonFormat("""{"state":"WorkInProgressDeadEnd"}""", State.WorkInProgressDeadEnd)
      validateJsonFormat("""{"state":"HaveYouBeenHungryRecently"}""", State.HaveYouBeenHungryRecently)
      validateJsonFormat("""{"state":"WhatYouDidToAddressHunger"}""", State.WhatYouDidToAddressHunger)
      validateJsonFormat("""{"state":"DidYouOrderPizzaAnyway"}""", State.DidYouOrderPizzaAnyway)
      validateJsonFormat("""{"state":"NotEligibleForPizzaTax"}""", State.NotEligibleForPizzaTax)
      validateJsonFormat("""{"state":"HowManyPizzasDidYouOrder"}""", State.HowManyPizzasDidYouOrder)
      validateJsonFormat(
        """{"state":"AreYouEligibleForSpecialAllowance","properties":{"pizzaOrders":{"totalNumberOfPizzas":12}}}""",
        State.AreYouEligibleForSpecialAllowance(PizzaOrdersDeclaration(totalNumberOfPizzas = 12))
      )
      validateJsonFormat(
        """{"state":"WhatIsYourITRole","properties":{"pizzaOrders":{"totalNumberOfPizzas":3}}}""",
        State.WhatIsYourITRole(PizzaOrdersDeclaration(totalNumberOfPizzas = 3))
      )
      validateJsonFormat(
        """{"state":"QuestionnaireSummary","properties":{"pizzaOrders":{"totalNumberOfPizzas":4},"pizzaAllowance":"BrokenHeart","itRoleOpt":"Architect"}}""",
        State.QuestionnaireSummary(
          PizzaOrdersDeclaration(totalNumberOfPizzas = 4),
          PizzaAllowance.BrokenHeart,
          itRoleOpt = Some(ITRole.Architect)
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
