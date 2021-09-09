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

package uk.gov.hmrc.pizzatax.models
import uk.gov.hmrc.pizzatax.utils.OptionOps._
import play.api.libs.json.Json

final case class QuestionnaireAnswers private (
  haveYouBeenHungryRecently: Option[Boolean],
  whatYouDidToAddressHunger: Option[HungerSolution],
  didYouOrderPizzaAnyway: Option[Boolean],
  pizzaOrders: Option[PizzaOrdersDeclaration],
  pizzaAllowance: Option[PizzaAllowance],
  itRoleOpt: Option[ITRole]
) extends CanValidate {

  override def isValid: Boolean =
    whatYouDidToAddressHunger.isEmptyOr(haveYouBeenHungryRecently.isTrue) &&
      didYouOrderPizzaAnyway.isEmptyOr(
        haveYouBeenHungryRecently.isFalse || whatYouDidToAddressHunger.exists(_ != HungerSolution.OrderPizza)
      ) &&
      pizzaOrders.isEmptyOr(
        (whatYouDidToAddressHunger.exists(_ == HungerSolution.OrderPizza) || didYouOrderPizzaAnyway.isTrue)
      ) &&
      pizzaAllowance.isEmptyOr(pizzaOrders.isDefined) &&
      itRoleOpt.isEmptyOr(pizzaAllowance.exists(_ == PizzaAllowance.ITWorker))

  def empty: QuestionnaireAnswers =
    QuestionnaireAnswers.empty

  def withHaveYouBeenHungryRecently(b: Boolean): QuestionnaireAnswers =
    copy(haveYouBeenHungryRecently = Some(b))

  def withWhatYouDidToAddressHunger(hs: HungerSolution): QuestionnaireAnswers =
    copy(whatYouDidToAddressHunger = Some(hs))

  def clearWhatYouDidToAddressHunger(): QuestionnaireAnswers =
    copy(whatYouDidToAddressHunger = None)

  def withDidYouOrderPizzaAnyway(b: Boolean): QuestionnaireAnswers =
    copy(didYouOrderPizzaAnyway = Some(b))

  def withPizzaOrders(pizzaOrders: PizzaOrdersDeclaration): QuestionnaireAnswers =
    copy(pizzaOrders = Some(pizzaOrders))

  def withPizzaAllowance(pizzaAllowance: PizzaAllowance): QuestionnaireAnswers =
    copy(pizzaAllowance = Some(pizzaAllowance))

  def withITRole(itRole: ITRole): QuestionnaireAnswers =
    copy(itRoleOpt = Some(itRole))

  def withITRoleOpt(itRoleOpt: Option[ITRole]): QuestionnaireAnswers =
    copy(itRoleOpt = itRoleOpt)
}

object QuestionnaireAnswers {

  implicit val format = Json.format[QuestionnaireAnswers]

  val empty =
    QuestionnaireAnswers(
      haveYouBeenHungryRecently = None,
      whatYouDidToAddressHunger = None,
      didYouOrderPizzaAnyway = None,
      pizzaOrders = None,
      pizzaAllowance = None,
      itRoleOpt = None
    )

  lazy val allValidQ13e: Set[QuestionnaireAnswers] =
    (for {
      haveYouBeenHungryRecently <- options(true, false)
      whatYouDidToAddressHunger <- options(HungerSolution.values)
      didYouOrderPizzaAnyway    <- options(true, false)
      pizzaOrders               <- options((1 to 10).map(PizzaOrdersDeclaration.apply).toSet)
      pizzaAllowance            <- options(PizzaAllowance.values)
      itRoleOpt                 <- options(ITRole.values)
    } yield QuestionnaireAnswers(
      haveYouBeenHungryRecently,
      whatYouDidToAddressHunger,
      didYouOrderPizzaAnyway,
      pizzaOrders,
      pizzaAllowance,
      itRoleOpt
    ))
      .filter(_.isValid)
      .toSet
}
