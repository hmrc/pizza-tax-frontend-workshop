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

sealed trait ITRole

object ITRole {

  case object Manager extends ITRole
  case object Architect extends ITRole
  case object Developer extends ITRole
  case object Tester extends ITRole
  case object Scrummaster extends ITRole
  case object Analyst extends ITRole
  case object Designer extends ITRole
  case object Researcher extends ITRole
  case object Apprentice extends ITRole

  final val values: Set[ITRole] =
    Set(Manager, Architect, Developer, Tester, Scrummaster, Analyst, Designer, Researcher, Apprentice)
}
