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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.pizzatax.journeys.PizzaTaxJourneyModelAlt1
import uk.gov.hmrc.pizzatax.models._
import uk.gov.hmrc.pizzatax.support._
import uk.gov.hmrc.pizzatax.utils.OptionOps._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import java.util.UUID
import org.scalatest.BeforeAndAfterAll

class PizzaTaxJourneyModelAlt1Spec extends AnyWordSpec with Matchers with BeforeAndAfterAll with JourneyModelSpec {

  override val model = PizzaTaxJourneyModelAlt1

  import model.{State, Transitions}

  val q13e = QuestionnaireAnswers.empty

  "PizzaTaxJourneyModelAlt1" when {

    "at state Start" should {
      "ask an empty HaveYouBeenHungryRecently when start" in
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
      "ask WhatYouDidToAddressHunger when submitted true" in
        given(State.HaveYouBeenHungryRecently(q13e))
          .when(Transitions.submittedHaveYouBeenHungryRecently(true))
          .thenGoes(State.WhatYouDidToAddressHunger(q13e.withHaveYouBeenHungryRecently(true)))

      "ask WhatYouDidToAddressHunger again" in {
        for (
          q13e <- QuestionnaireAnswers.allValidQ13e
                    .filter(_.haveYouBeenHungryRecently.isTrue)
        )
          given(State.HaveYouBeenHungryRecently(q13e))
            .when(Transitions.submittedHaveYouBeenHungryRecently(true))
            .thenGoes(State.WhatYouDidToAddressHunger(q13e))
      }

      "ask DidYouOrderPizzaAnyway when submitted false" in
        given(State.HaveYouBeenHungryRecently(q13e))
          .when(Transitions.submittedHaveYouBeenHungryRecently(false))
          .thenGoes(State.DidYouOrderPizzaAnyway(q13e.withHaveYouBeenHungryRecently(false)))

      "ask DidYouOrderPizzaAnyway again" in {
        for (
          q13e <- QuestionnaireAnswers.allValidQ13e
                    .filter(_.haveYouBeenHungryRecently.isFalse)
        )
          given(State.HaveYouBeenHungryRecently(q13e))
            .when(Transitions.submittedHaveYouBeenHungryRecently(false))
            .thenGoes(State.DidYouOrderPizzaAnyway(q13e))
      }

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

      "ask HowManyPizzasDidYouOrder if pizza order selected" in
        givenWhatYouDidToAddressHunger
          .when(Transitions.submittedWhatYouDidToAddressHunger(HungerSolution.OrderPizza))
          .thenGoes(
            State.HowManyPizzasDidYouOrder(
              q13e
                .withHaveYouBeenHungryRecently(true)
                .withWhatYouDidToAddressHunger(HungerSolution.OrderPizza)
            )
          )

      "ask HowManyPizzasDidYouOrder again" in {
        for (
          q13e <- QuestionnaireAnswers.allValidQ13e
                    .filter(_.whatYouDidToAddressHunger.contains(HungerSolution.OrderPizza))
        )
          given(State.WhatYouDidToAddressHunger(q13e))
            .when(Transitions.submittedWhatYouDidToAddressHunger(HungerSolution.OrderPizza))
            .thenGoes(
              State.HowManyPizzasDidYouOrder(q13e)
            )
      }

      "ask DidYouOrderPizzaAnyway when any other option selected" in {
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

      "ask DidYouOrderPizzaAnyway again" in {
        for (
          q13e <- QuestionnaireAnswers.allValidQ13e
                    .filter(_.whatYouDidToAddressHunger.containsNot(HungerSolution.OrderPizza))
        )
          given(State.WhatYouDidToAddressHunger(q13e))
            .when(Transitions.submittedWhatYouDidToAddressHunger(q13e.whatYouDidToAddressHunger.get))
            .thenGoes(
              State.DidYouOrderPizzaAnyway(q13e)
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

      "finish journey again" in {
        for (
          q13e <- QuestionnaireAnswers.allValidQ13e
                    .filter(_.didYouOrderPizzaAnyway.isFalse)
        )
          given(State.DidYouOrderPizzaAnyway(q13e))
            .when(Transitions.submittedDidYouOrderPizzaAnyway(false))
            .thenGoes(
              State.NotEligibleForPizzaTax(q13e)
            )
      }

      "ask HowManyPizzasDidYouOrder if not hungry and answer is yes" in
        given(State.DidYouOrderPizzaAnyway(q13e.withHaveYouBeenHungryRecently(false)))
          .when(Transitions.submittedDidYouOrderPizzaAnyway(true))
          .thenGoes(
            State.HowManyPizzasDidYouOrder(
              q13e
                .withHaveYouBeenHungryRecently(false)
                .withDidYouOrderPizzaAnyway(true)
            )
          )

      "ask HowManyPizzasDidYouOrder again" in {
        for (
          q13e <- QuestionnaireAnswers.allValidQ13e
                    .filter(_.didYouOrderPizzaAnyway.isTrue)
        )
          given(State.DidYouOrderPizzaAnyway(q13e))
            .when(Transitions.submittedDidYouOrderPizzaAnyway(true))
            .thenGoes(
              State.HowManyPizzasDidYouOrder(q13e)
            )
      }

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

      "ask HowManyPizzasDidYouOrder if hungry, didn't order pizza and answer is yes" in
        given(
          State.DidYouOrderPizzaAnyway(
            q13e
              .withHaveYouBeenHungryRecently(true)
              .withWhatYouDidToAddressHunger(HungerSolution.BurnToasts)
          )
        )
          .when(Transitions.submittedDidYouOrderPizzaAnyway(true))
          .thenGoes(
            State.HowManyPizzasDidYouOrder(
              q13e
                .withHaveYouBeenHungryRecently(true)
                .withWhatYouDidToAddressHunger(HungerSolution.BurnToasts)
                .withDidYouOrderPizzaAnyway(true)
            )
          )
    }

    "at state HowManyPizzasDidYouOrder with hungry = yes and pizza order" should {

      val initialQ13e = q13e
        .withHaveYouBeenHungryRecently(true)
        .withWhatYouDidToAddressHunger(HungerSolution.OrderPizza)

      val givenHowManyPizzasDidYouOrder = given(State.HowManyPizzasDidYouOrder(initialQ13e))

      val basicPizzaAllowanceLimits = new BasicPizzaAllowanceLimits {
        override def areNotExceededBy(pizzaOrders: PizzaOrdersDeclaration): Boolean =
          pizzaOrders.totalNumberOfPizzas <= 4
      }

      "ask AreYouEligibleForSpecialAllowance when declaration submited and exceeds the basic allowance limits" in
        givenHowManyPizzasDidYouOrder
          .when(
            Transitions.submittedHowManyPizzasDidYouOrder(basicPizzaAllowanceLimits)(
              PizzaOrdersDeclaration(totalNumberOfPizzas = 12)
            )
          )
          .thenGoes(
            State.AreYouEligibleForSpecialAllowance(
              initialQ13e.withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = 12))
            )
          )

      "ask AreYouEligibleForSpecialAllowance again" in {
        for (
          q13e <- QuestionnaireAnswers.allValidQ13e
                    .filter(_.pizzaOrders.exists(_.totalNumberOfPizzas > 4))
        )
          given(State.HowManyPizzasDidYouOrder(q13e))
            .when(Transitions.submittedHowManyPizzasDidYouOrder(basicPizzaAllowanceLimits)(q13e.pizzaOrders.get))
            .thenGoes(State.AreYouEligibleForSpecialAllowance(q13e))
      }

      "show QuestionnaireSummary when declaration submited and exceeds the basic allowance limits" in
        givenHowManyPizzasDidYouOrder
          .when(
            Transitions.submittedHowManyPizzasDidYouOrder(basicPizzaAllowanceLimits)(
              PizzaOrdersDeclaration(totalNumberOfPizzas = 4)
            )
          )
          .thenGoes(
            State.QuestionnaireSummary(
              initialQ13e
                .withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = 4))
                .withPizzaAllowance(PizzaAllowance.Basic)
            )
          )

      "ask QuestionnaireSummary again" in {
        for (
          q13e <- QuestionnaireAnswers.allValidQ13e
                    .filter(q =>
                      q.pizzaOrders.exists(_.totalNumberOfPizzas <= 4) &&
                        q.pizzaAllowance.contains(PizzaAllowance.Basic)
                    )
        )
          given(State.HowManyPizzasDidYouOrder(q13e))
            .when(Transitions.submittedHowManyPizzasDidYouOrder(basicPizzaAllowanceLimits)(q13e.pizzaOrders.get))
            .thenGoes(State.QuestionnaireSummary(q13e))
      }
    }

    "at state HowManyPizzasDidYouOrder with hungry = yes and pizza ordered anyway" should {

      val initialQ13e = q13e
        .withHaveYouBeenHungryRecently(true)
        .withWhatYouDidToAddressHunger(HungerSolution.BurnToasts)
        .withDidYouOrderPizzaAnyway(true)

      val givenHowManyPizzasDidYouOrder = given(State.HowManyPizzasDidYouOrder(initialQ13e))

      val basicPizzaAllowanceLimits = new BasicPizzaAllowanceLimits {
        override def areNotExceededBy(pizzaOrders: PizzaOrdersDeclaration): Boolean =
          pizzaOrders.totalNumberOfPizzas <= 4
      }

      "ask AreYouEligibleForSpecialAllowance when declaration submited and exceeds the basic allowance limits" in
        givenHowManyPizzasDidYouOrder
          .when(
            Transitions.submittedHowManyPizzasDidYouOrder(basicPizzaAllowanceLimits)(
              PizzaOrdersDeclaration(totalNumberOfPizzas = 12)
            )
          )
          .thenGoes(
            State.AreYouEligibleForSpecialAllowance(
              initialQ13e.withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = 12))
            )
          )

      "show QuestionnaireSummary when declaration submited and exceeds the basic allowance limits" in
        givenHowManyPizzasDidYouOrder
          .when(
            Transitions.submittedHowManyPizzasDidYouOrder(basicPizzaAllowanceLimits)(
              PizzaOrdersDeclaration(totalNumberOfPizzas = 4)
            )
          )
          .thenGoes(
            State.QuestionnaireSummary(
              initialQ13e
                .withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = 4))
                .withPizzaAllowance(PizzaAllowance.Basic)
            )
          )
    }

    "at state HowManyPizzasDidYouOrder with hungry = no and pizza ordered anyway" should {

      val initialQ13e = q13e
        .withHaveYouBeenHungryRecently(false)
        .withDidYouOrderPizzaAnyway(true)

      val givenHowManyPizzasDidYouOrder = given(State.HowManyPizzasDidYouOrder(initialQ13e))

      val basicPizzaAllowanceLimits = new BasicPizzaAllowanceLimits {
        override def areNotExceededBy(pizzaOrders: PizzaOrdersDeclaration): Boolean =
          pizzaOrders.totalNumberOfPizzas <= 4
      }

      "ask AreYouEligibleForSpecialAllowance when declaration submited and exceeds the basic allowance limits" in
        givenHowManyPizzasDidYouOrder
          .when(
            Transitions.submittedHowManyPizzasDidYouOrder(basicPizzaAllowanceLimits)(
              PizzaOrdersDeclaration(totalNumberOfPizzas = 12)
            )
          )
          .thenGoes(
            State.AreYouEligibleForSpecialAllowance(
              initialQ13e.withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = 12))
            )
          )

      "show QuestionnaireSummary when declaration submited and exceeds the basic allowance limits" in
        givenHowManyPizzasDidYouOrder
          .when(
            Transitions.submittedHowManyPizzasDidYouOrder(basicPizzaAllowanceLimits)(
              PizzaOrdersDeclaration(totalNumberOfPizzas = 4)
            )
          )
          .thenGoes(
            State.QuestionnaireSummary(
              initialQ13e
                .withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = 4))
                .withPizzaAllowance(PizzaAllowance.Basic)
            )
          )
    }

    "at state AreYouEligibleForSpecialAllowance" should {

      val initialQ13e = q13e
        .withHaveYouBeenHungryRecently(true)
        .withWhatYouDidToAddressHunger(HungerSolution.OrderPizza)

      "show QuestionnaireSummary when not an IT worker" in {
        for (allowance <- PizzaAllowance.values - PizzaAllowance.ITWorker; pizzas <- 4 to 40 by 4)
          given(
            State.AreYouEligibleForSpecialAllowance(
              initialQ13e.withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = pizzas))
            )
          )
            .when(Transitions.submittedAreYouEligibleForSpecialAllowance(allowance))
            .thenGoes(
              State.QuestionnaireSummary(
                initialQ13e
                  .withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = pizzas))
                  .withPizzaAllowance(allowance)
              )
            )
      }

      "show QuestionnaireSummary again" in {
        for (
          q13e <- QuestionnaireAnswers.allValidQ13e
                    .filter(_.pizzaAllowance.containsNot(PizzaAllowance.ITWorker))
        )
          given(State.AreYouEligibleForSpecialAllowance(q13e))
            .when(Transitions.submittedAreYouEligibleForSpecialAllowance(q13e.pizzaAllowance.get))
            .thenGoes(State.QuestionnaireSummary(q13e))
      }

      "ask WhatIsYourITRole when an IT worker" in {
        for (pizzas <- 4 to 40 by 4)
          given(
            State.AreYouEligibleForSpecialAllowance(
              initialQ13e.withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = pizzas))
            )
          )
            .when(Transitions.submittedAreYouEligibleForSpecialAllowance(PizzaAllowance.ITWorker))
            .thenGoes(
              State.WhatIsYourITRole(
                initialQ13e
                  .withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = pizzas))
                  .withPizzaAllowance(PizzaAllowance.ITWorker)
              )
            )
      }

      "ask WhatIsYourITRole again" in {
        for (
          q13e <- QuestionnaireAnswers.allValidQ13e
                    .filter(_.pizzaAllowance.contains(PizzaAllowance.ITWorker))
        )
          given(State.AreYouEligibleForSpecialAllowance(q13e))
            .when(Transitions.submittedAreYouEligibleForSpecialAllowance(PizzaAllowance.ITWorker))
            .thenGoes(State.WhatIsYourITRole(q13e))
      }
    }

    "at state WhatIsYourITRole" should {

      val initialQ13e = q13e
        .withHaveYouBeenHungryRecently(true)
        .withWhatYouDidToAddressHunger(HungerSolution.OrderPizza)
        .withPizzaAllowance(PizzaAllowance.ITWorker)

      "show QuestionnaireSummary when submitted an answer" in {
        for (itRole <- ITRole.values; pizzas <- 4 to 40 by 4)
          given(
            State.WhatIsYourITRole(initialQ13e.withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = pizzas)))
          )
            .when(Transitions.submittedWhatIsYourITRole(itRole))
            .thenGoes(
              State.QuestionnaireSummary(
                initialQ13e
                  .withPizzaOrders(PizzaOrdersDeclaration(totalNumberOfPizzas = pizzas))
                  .withITRole(itRole)
              )
            )
      }

      "show QuestionnaireSummary again" in {
        for (
          q13e <- QuestionnaireAnswers.allValidQ13e
                    .filter(_.itRoleOpt.isDefined)
        )
          given(State.WhatIsYourITRole(q13e))
            .when(Transitions.submittedWhatIsYourITRole(q13e.itRoleOpt.get))
            .thenGoes(State.QuestionnaireSummary(q13e))
      }
    }

    "at state QuestionnaireSummary" should {

      val initialQ13e = q13e
        .withHaveYouBeenHungryRecently(true)
        .withWhatYouDidToAddressHunger(HungerSolution.OrderPizza)

      "ask TaxStatementConfirmation when submited assesment and API call returns the calculation" in {
        val amountOfTaxDue = Random.nextInt
        val confirmationId = UUID.randomUUID().toString()
        val pizzaTaxAssessmentAPI: model.PizzaTaxAssessmentAPI =
          (r: PizzaTaxAssessmentRequest) =>
            Future.successful(PizzaTaxAssessmentResponse(confirmationId, amountOfTaxDue))

        for {
          pizzaOrders    <- (1 to 10).map(PizzaOrdersDeclaration.apply)
          pizzaAllowance <- PizzaAllowance.values
          itRoleOpt <- pizzaAllowance match {
                         case PizzaAllowance.ITWorker => ITRole.values.map(Option.apply)
                         case _                       => Set(None)
                       }
        } given(
          State.QuestionnaireSummary(
            initialQ13e
              .withPizzaOrders(pizzaOrders)
              .withPizzaAllowance(pizzaAllowance)
              .withITRoleOpt(itRoleOpt)
          )
        ).when(Transitions.submitPizzaTaxAssessment(pizzaTaxAssessmentAPI))
          .thenGoes(
            State.TaxStatementConfirmation(
              pizzaOrders,
              pizzaAllowance,
              itRoleOpt,
              confirmationId,
              amountOfTaxDue
            )
          )
      }

      "pass an exception when API call fails" in {
        {
          val expectedException = new Exception()
          val pizzaTaxAssessmentAPI: model.PizzaTaxAssessmentAPI =
            (r: PizzaTaxAssessmentRequest) => Future.failed(expectedException)

          for {
            pizzaOrders    <- (1 to 10).map(PizzaOrdersDeclaration.apply)
            pizzaAllowance <- PizzaAllowance.values
            itRoleOpt <- pizzaAllowance match {
                           case PizzaAllowance.ITWorker => ITRole.values.map(Option.apply)
                           case _                       => Set(None)
                         }
          } given(
            State.QuestionnaireSummary(
              initialQ13e
                .withPizzaOrders(pizzaOrders)
                .withPizzaAllowance(pizzaAllowance)
                .withITRoleOpt(itRoleOpt)
            )
          )
            .when(Transitions.submitPizzaTaxAssessment(pizzaTaxAssessmentAPI))
            .thenFailsWith[expectedException.type]
        }
      }

      "do nothing if questionnaire not complete" in {
        {
          val pizzaTaxAssessmentAPI: model.PizzaTaxAssessmentAPI =
            (r: PizzaTaxAssessmentRequest) => Future.successful(PizzaTaxAssessmentResponse("foo", 1))

          for {
            pizzaOrders <- (1 to 10).map(PizzaOrdersDeclaration.apply)
          } given(
            State.QuestionnaireSummary(
              initialQ13e
                .withPizzaOrders(pizzaOrders)
            )
          )
            .when(Transitions.submitPizzaTaxAssessment(pizzaTaxAssessmentAPI))
            .thenNoChange
        }
      }
    }

    // An example of testing backward transitions (back links)
    "transition backToHaveYouBeenHungryRecently" should {
      "ask HaveYouBeenHungryRecently again for all valid states" in {
        for {
          q <- QuestionnaireAnswers.allValidQ13e
          s <- Set(
                 State.QuestionnaireSummary(q),
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
          q <- QuestionnaireAnswers.allValidQ13e
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
          q <- QuestionnaireAnswers.allValidQ13e
                 .filter(_.whatYouDidToAddressHunger.isDefined)
          s <- Set(
                 State.QuestionnaireSummary(q),
                 State.DidYouOrderPizzaAnyway(q),
                 State.HowManyPizzasDidYouOrder(q)
               )
        } given(s)
          .when(Transitions.backToWhatYouDidToAddressHunger)
          .thenGoes(State.WhatYouDidToAddressHunger(s.answers))
      }

      "do nothing for all invalid states" in {
        for {
          q <- QuestionnaireAnswers.allValidQ13e
          s <- Set(
                 State.HaveYouBeenHungryRecently(q),
                 State.NotEligibleForPizzaTax(q)
               )
        } given(s)
          .when(Transitions.backToWhatYouDidToAddressHunger)
          .thenNoChange
      }
    }

    "transition backToDidYouOrderPizzaAnyway" should {
      "ask DidYouOrderPizzaAnyway again for all valid states" in {
        for {
          q <- QuestionnaireAnswers.allValidQ13e
                 .filter(_.didYouOrderPizzaAnyway.isDefined)
          s <- Set(
                 State.QuestionnaireSummary(q),
                 State.NotEligibleForPizzaTax(q),
                 State.HowManyPizzasDidYouOrder(q)
               ).filter {
                 case State.HowManyPizzasDidYouOrder(_) => q.didYouOrderPizzaAnyway.isTrue
                 case _                                 => true
               }
        } given(s)
          .when(Transitions.backToDidYouOrderPizzaAnyway)
          .thenGoes(State.DidYouOrderPizzaAnyway(s.answers))
      }
    }

    "transition backToHowManyPizzasDidYouOrder" should {
      "ask HowManyPizzasDidYouOrder again for all valid states" in {
        for {
          q <- QuestionnaireAnswers.allValidQ13e
                 .filter(_.pizzaOrders.isDefined)
          s <- Set(
                 State.QuestionnaireSummary(q),
                 State.AreYouEligibleForSpecialAllowance(q)
               )
        } given(s)
          .when(Transitions.backToHowManyPizzasDidYouOrder)
          .thenGoes(State.HowManyPizzasDidYouOrder(s.answers))
      }
    }

    "transition backToAreYouEligibleForSpecialAllowance" should {
      "ask AreYouEligibleForSpecialAllowance again for all valid states" in {
        for {
          q <- QuestionnaireAnswers.allValidQ13e
                 .filter(_.pizzaAllowance.isDefined)
          s <- Set(
                 State.QuestionnaireSummary(q),
                 State.WhatIsYourITRole(q)
               )
        } given(s)
          .when(Transitions.backToAreYouEligibleForSpecialAllowance)
          .thenGoes(State.AreYouEligibleForSpecialAllowance(s.answers))
      }
    }

    "transition backToWhatIsYourITRole" should {
      "ask AreYouEligibleForSpecialAllowance again for all valid states" in {
        for {
          q <- QuestionnaireAnswers.allValidQ13e
                 .filter(_.itRoleOpt.isDefined)
          s <- Set(
                 State.QuestionnaireSummary(q)
               )
        } given(s)
          .when(Transitions.backToWhatIsYourITRole)
          .thenGoes(State.WhatIsYourITRole(s.answers))
      }
    }
  }
}
