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

package uk.gov.hmrc.pizzatax.views.helpers

import javax.inject.Singleton
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.pizzatax.models.HungerSolution

@Singleton
class EnumsHelper extends RadioItemsHelper with CheckboxItemsHelper {

  def haveYouBeenHungryRecently(form: Form[_])(implicit messages: Messages): Seq[RadioItem] =
    Seq(
      RadioItem(
        value = Some("yes"),
        content = Text(messages(s"form.haveYouBeenHungryRecently.yes")),
        checked = form("haveYouBeenHungryRecently").value.contains("yes")
      ),
      RadioItem(
        value = Some("no"),
        content = Text(messages(s"form.haveYouBeenHungryRecently.no")),
        checked = form("haveYouBeenHungryRecently").value.contains("no")
      )
    )

  def whatYouDidToAddressHunger(form: Form[_])(implicit messages: Messages): Seq[RadioItem] =
    radioItems[HungerSolution](
      "",
      "whatYouDidToAddressHunger",
      Seq(
        HungerSolution.BurnToasts,
        HungerSolution.Daydream,
        HungerSolution.EatOut,
        HungerSolution.GetAngry,
        HungerSolution.OrderPizza,
        HungerSolution.RansackFridge
      ),
      form
    )

}
