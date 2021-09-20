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

package uk.gov.hmrc.pizzatax.support

import org.scalatest.Assertion
import play.api.libs.json.{Format, Json}
import org.scalatest.Informer
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.play.fsm.PlayFsmUtils

abstract class JsonFormatTest[A: Format](info: Informer) extends Matchers {

  case class TestEntity(entity: A)
  implicit val testFormat: Format[TestEntity] = Json.format[TestEntity]

  def validateJsonFormat(value: String, entity: A): Assertion = {
    info(PlayFsmUtils.identityOf(entity))
    val json = s"""{"entity":${if (value.startsWith("{")) value else s""""$value""""}}"""
    Json.parse(json).as[TestEntity].entity shouldBe entity
    Json.stringify(Json.toJson(TestEntity(entity))) shouldBe json.filter(_ >= ' ')
  }

  def validateJsonReads(value: String, entity: A): Assertion = {
    info(PlayFsmUtils.identityOf(entity))
    val json = s"""{"entity":${if (value.startsWith("{")) value else s""""$value""""}}"""
    Json.parse(json).as[TestEntity].entity shouldBe entity
  }

  def validateJsonWrites(value: String, entity: A): Assertion = {
    info(PlayFsmUtils.identityOf(entity))
    val json = s"""{"entity":${if (value.startsWith("{")) value else s""""$value""""}}"""
    Json.stringify(Json.toJson(TestEntity(entity))) shouldBe json.filter(_ >= ' ')
  }

}
