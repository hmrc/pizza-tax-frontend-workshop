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

package uk.gov.hmrc.pizzatax.utils

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class OptionOpsSpec extends AnyWordSpec with Matchers {

  import OptionOps._

  "OptionOps" should {
    "extend Option[Boolean] with isTrue" in {
      Some(false).isTrue shouldBe false
      None.isTrue shouldBe false
    }

    "extend Option[Boolean] with isFalse" in {
      Some(false).isFalse shouldBe true
      None.isFalse shouldBe false
    }

    "extend Option[A] with isJoint" in {
      Some(1).isJoint(None) shouldBe false
      Some(1).isJoint(Some(2)) shouldBe true
      None.isJoint(None) shouldBe true
      None.isJoint(Some(2)) shouldBe false
    }

    "extend Option[A] with isDisjoint" in {
      Some(1).isDisjoint(None) shouldBe true
      Some(1).isDisjoint(Some(2)) shouldBe false
      None.isDisjoint(None) shouldBe false
      None.isDisjoint(Some(2)) shouldBe true
    }

    "extend Option[A] with isEmptyOr" in {
      Some(1).isEmptyOr(true) shouldBe true
      Some(1).isEmptyOr(false) shouldBe false
      None.isEmptyOr(false) shouldBe true
      None.isEmptyOr(true) shouldBe true
      Some(1).isEmptyOr(_ == 1) shouldBe true
      Some(1).isEmptyOr(_ != 1) shouldBe false
      Some(1).isEmptyOr(_ == 2) shouldBe false
      Some(2).isEmptyOr(_ == 2) shouldBe true
      Option.empty[Int].isEmptyOr(_ == 1) shouldBe true
    }
  }
}
