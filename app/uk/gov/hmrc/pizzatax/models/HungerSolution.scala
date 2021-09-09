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

import uk.gov.hmrc.pizzatax.utils.EnumerationFormats

sealed trait HungerSolution

object HungerSolution extends EnumerationFormats[HungerSolution] {

  case object GetAngry extends HungerSolution
  case object Daydream extends HungerSolution
  case object EatOut extends HungerSolution
  case object RansackFridge extends HungerSolution
  case object BurnToasts extends HungerSolution
  case object OrderPizza extends HungerSolution

  final val values: Set[HungerSolution] =
    Set(GetAngry, Daydream, EatOut, RansackFridge, BurnToasts, OrderPizza)
}
