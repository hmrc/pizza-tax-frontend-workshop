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
import uk.gov.hmrc.pizzatax.utils.OptionOps._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

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
    case class HowManyPizzasDidYouOrder(answers: QuestionnaireAnswers) extends HaveAnswers with State
    case class AreYouEligibleForSpecialAllowance(answers: QuestionnaireAnswers) extends HaveAnswers with State
    case class WhatIsYourITRole(answers: QuestionnaireAnswers) extends HaveAnswers with State
    case class QuestionnaireSummary(answers: QuestionnaireAnswers) extends HaveAnswers with State

    case class TaxStatementConfirmation(
      pizzaOrders: PizzaOrdersDeclaration,
      pizzaAllowance: PizzaAllowance,
      itRoleOpt: Option[ITRole],
      correlationId: String,
      amountOfTaxDue: Int
    ) extends EndState
  }

  type PizzaTaxAssessmentAPI =
    PizzaTaxAssessmentRequest => Future[PizzaTaxAssessmentResponse]

  /** Validate entity, apply and go to the new state, otherwise stay. */
  final def gotoIfValid[B <: CanValidate](f: B => State)(b: B): Future[State] =
    if (b.isValid) goto(f(b)) else stay

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
            if (q.haveYouBeenHungryRecently.isTrue)
              goto(WhatYouDidToAddressHunger(q)) // no change
            else
              gotoIfValid(WhatYouDidToAddressHunger)(q.withHaveYouBeenHungryRecently(true))
          else if (q.haveYouBeenHungryRecently.isFalse)
            goto(DidYouOrderPizzaAnyway(q)) // no change
          else
            gotoIfValid(DidYouOrderPizzaAnyway)(
              q.empty.withHaveYouBeenHungryRecently(false)
            )
      }

    final def submittedWhatYouDidToAddressHunger(solution: HungerSolution) =
      Transition {
        case WhatYouDidToAddressHunger(q) =>
          solution match {
            case HungerSolution.OrderPizza =>
              if (q.whatYouDidToAddressHunger.contains(solution))
                goto(HowManyPizzasDidYouOrder(q)) // no change
              else
                gotoIfValid(HowManyPizzasDidYouOrder)(q.withWhatYouDidToAddressHunger(HungerSolution.OrderPizza))
            case _ =>
              if (q.whatYouDidToAddressHunger.contains(solution))
                goto(DidYouOrderPizzaAnyway(q)) // no change
              else
                gotoIfValid(DidYouOrderPizzaAnyway)(q.withWhatYouDidToAddressHunger(solution))
          }
      }

    final def submittedDidYouOrderPizzaAnyway(confirmed: Boolean) =
      Transition {
        case DidYouOrderPizzaAnyway(q) =>
          if (confirmed)
            if (q.didYouOrderPizzaAnyway.isTrue)
              goto(HowManyPizzasDidYouOrder(q)) // no change
            else
              gotoIfValid(HowManyPizzasDidYouOrder)(q.withDidYouOrderPizzaAnyway(true))
          else if (q.didYouOrderPizzaAnyway.isFalse)
            goto(NotEligibleForPizzaTax(q)) // no change
          else
            gotoIfValid(NotEligibleForPizzaTax)(q.withDidYouOrderPizzaAnyway(false))
      }

    final def submittedHowManyPizzasDidYouOrder(
      limits: BasicPizzaAllowanceLimits
    )(pizzaOrders: PizzaOrdersDeclaration) =
      Transition {
        case HowManyPizzasDidYouOrder(q) =>
          if (limits.areNotExceededBy(pizzaOrders))
            if (q.pizzaOrders.contains(pizzaOrders))
              goto(QuestionnaireSummary(q)) // no change
            else
              gotoIfValid(QuestionnaireSummary)(q.withPizzaOrders(pizzaOrders).withPizzaAllowance(PizzaAllowance.Basic))
          else if (q.pizzaOrders.contains(pizzaOrders))
            goto(AreYouEligibleForSpecialAllowance(q)) // no change
          else
            gotoIfValid(AreYouEligibleForSpecialAllowance)(q.withPizzaOrders(pizzaOrders))
      }

    final def submittedAreYouEligibleForSpecialAllowance(pizzaAllowance: PizzaAllowance) =
      Transition {
        case AreYouEligibleForSpecialAllowance(q) =>
          pizzaAllowance match {
            case PizzaAllowance.ITWorker =>
              if (q.pizzaAllowance.contains(pizzaAllowance))
                goto(WhatIsYourITRole(q)) // no change
              else
                gotoIfValid(WhatIsYourITRole)(q.withPizzaAllowance(PizzaAllowance.ITWorker))
            case _ =>
              if (q.pizzaAllowance.contains(pizzaAllowance))
                goto(QuestionnaireSummary(q)) // no change
              else
                gotoIfValid(QuestionnaireSummary)(q.withPizzaAllowance(pizzaAllowance))
          }
      }

    final def submittedWhatIsYourITRole(itRole: ITRole) =
      Transition {
        case WhatIsYourITRole(q) =>
          gotoIfValid(QuestionnaireSummary)(q.withITRole(itRole))
      }

    final def submitPizzaTaxAssessment(pizzaTaxAssessmentAPI: PizzaTaxAssessmentAPI)(implicit ec: ExecutionContext) =
      Transition {
        case QuestionnaireSummary(PizzaTaxAssessmentRequest.create(request)) =>
          pizzaTaxAssessmentAPI(request)
            .map {
              case PizzaTaxAssessmentResponse(confirmationId, amountOfTaxDue) =>
                TaxStatementConfirmation(
                  request.pizzaOrders,
                  request.pizzaAllowance,
                  request.itRoleOpt,
                  confirmationId,
                  amountOfTaxDue
                )
            }
      }

    final val backToHaveYouBeenHungryRecently =
      Transition {
        case QuestionnaireSummary(q)                                          => goto(HaveYouBeenHungryRecently(q))
        case WhatYouDidToAddressHunger(q)                                     => goto(HaveYouBeenHungryRecently(q))
        case DidYouOrderPizzaAnyway(q) if q.whatYouDidToAddressHunger.isEmpty => goto(HaveYouBeenHungryRecently(q))
      }

    final val backToWhatYouDidToAddressHunger =
      Transition {
        case QuestionnaireSummary(q)                                            => goto(WhatYouDidToAddressHunger(q))
        case DidYouOrderPizzaAnyway(q) if q.whatYouDidToAddressHunger.isDefined => goto(WhatYouDidToAddressHunger(q))
        case HowManyPizzasDidYouOrder(q)                                        => goto(WhatYouDidToAddressHunger(q))
      }

    final val backToDidYouOrderPizzaAnyway =
      Transition {
        case QuestionnaireSummary(q)                                        => goto(DidYouOrderPizzaAnyway(q))
        case NotEligibleForPizzaTax(q)                                      => goto(DidYouOrderPizzaAnyway(q))
        case HowManyPizzasDidYouOrder(q) if q.didYouOrderPizzaAnyway.isTrue => goto(DidYouOrderPizzaAnyway(q))
      }

    final val backToHowManyPizzasDidYouOrder =
      Transition {
        case QuestionnaireSummary(q)              => goto(HowManyPizzasDidYouOrder(q))
        case AreYouEligibleForSpecialAllowance(q) => goto(HowManyPizzasDidYouOrder(q))
      }

    final val backToAreYouEligibleForSpecialAllowance =
      Transition {
        case QuestionnaireSummary(q) => goto(AreYouEligibleForSpecialAllowance(q))
        case WhatIsYourITRole(q)     => goto(AreYouEligibleForSpecialAllowance(q))
      }

    final val backToWhatIsYourITRole =
      Transition {
        case QuestionnaireSummary(q) => goto(WhatIsYourITRole(q))
      }

  }
}
