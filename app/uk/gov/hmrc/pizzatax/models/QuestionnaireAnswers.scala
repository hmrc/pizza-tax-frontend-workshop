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

final case class QuestionnaireAnswers private (
  haveYouBeenHungryRecently: Option[Boolean],
  whatYouDidToAddressHunger: Option[HungerSolution],
  didYouOrderPizzaAnyway: Option[Boolean]
) extends CanValidate {

  override def isValid: Boolean =
    whatYouDidToAddressHunger.isEmptyOr(haveYouBeenHungryRecently.isTrue) &&
      didYouOrderPizzaAnyway.isEmptyOr(
        haveYouBeenHungryRecently.isFalse || whatYouDidToAddressHunger.exists(_ != HungerSolution.OrderPizza)
      )

  def withHaveYouBeenHungryRecently(b: Boolean): QuestionnaireAnswers =
    copy(haveYouBeenHungryRecently = Some(b))

  def withWhatYouDidToAddressHunger(hs: HungerSolution): QuestionnaireAnswers =
    copy(whatYouDidToAddressHunger = Some(hs))

  def clearWhatYouDidToAddressHunger(): QuestionnaireAnswers =
    copy(whatYouDidToAddressHunger = None)

  def withDidYouOrderPizzaAnyway(b: Boolean): QuestionnaireAnswers =
    copy(didYouOrderPizzaAnyway = Some(b))
}

object QuestionnaireAnswers {

  val empty =
    QuestionnaireAnswers(
      haveYouBeenHungryRecently = None,
      whatYouDidToAddressHunger = None,
      didYouOrderPizzaAnyway = None
    )

  val allPossibleQ13e: Set[QuestionnaireAnswers] =
    (for {
      haveYouBeenHungryRecently <- options(true, false)
      whatYouDidToAddressHunger <- options(HungerSolution.values)
      didYouOrderPizzaAnyway    <- options(true, false)
    } yield QuestionnaireAnswers(haveYouBeenHungryRecently, whatYouDidToAddressHunger, didYouOrderPizzaAnyway))
      .filter(_.isValid)
      .toSet
}
