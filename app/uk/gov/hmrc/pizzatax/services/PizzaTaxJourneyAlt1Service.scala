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

package uk.gov.hmrc.pizzatax.services

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Format
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.pizzatax.journeys.{PizzaTaxJourneyModelAlt1, PizzaTaxJourneyModelAlt1Formats}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.fsm.PersistentJourneyService
import uk.gov.hmrc.pizzatax.config.AppConfig
import uk.gov.hmrc.pizzatax.repository.CacheRepository
import akka.actor.ActorSystem

trait PizzaTaxJourneyAlt1Service[RequestContext] extends PersistentJourneyService[RequestContext] {

  val journeyKey = "PizzaTaxJourneyAlt1"

  override val model = PizzaTaxJourneyModelAlt1

  // do not keep errors or transient states in the journey history
  override val breadcrumbsRetentionStrategy: Breadcrumbs => Breadcrumbs =
    _.take(10) // retain last 10 states as a breadcrumbs
}

trait PizzaTaxJourneyServiceAlt1WithHeaderCarrier extends PizzaTaxJourneyAlt1Service[HeaderCarrier]

@Singleton
case class MongoDBCachedPizzaTaxJourneyAlt1Service @Inject() (
  cacheRepository: CacheRepository,
  applicationCrypto: ApplicationCrypto,
  appConfig: AppConfig,
  actorSystem: ActorSystem
) extends MongoDBCachedJourneyService[HeaderCarrier] with PizzaTaxJourneyServiceAlt1WithHeaderCarrier {

  override val stateFormats: Format[model.State] =
    PizzaTaxJourneyModelAlt1Formats.formats

  override def getJourneyId(hc: HeaderCarrier): Option[String] =
    hc.extraHeaders.find(_._1 == journeyKey).map(_._2)

  override val traceFSM: Boolean = appConfig.traceFSM
}