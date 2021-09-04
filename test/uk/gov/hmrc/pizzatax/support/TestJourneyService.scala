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

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import uk.gov.hmrc.play.fsm.PersistentJourneyService

trait TestJourneyService[HC] extends PersistentJourneyService[HC] {

  override val journeyKey: String = "TestJourney"

  val storage = new InMemoryStore[(model.State, List[model.State])] {}

  override def fetch(implicit
    hc: HC,
    ec: ExecutionContext
  ): Future[Option[(model.State, List[model.State])]] = storage.fetch

  override def save(
    state: (model.State, List[model.State])
  )(implicit hc: HC, ec: ExecutionContext): Future[(model.State, List[model.State])] =
    storage.save(state)

  def set(state: model.State, breadcrumbs: List[model.State])(implicit
    headerCarrier: HC,
    timeout: Duration,
    ec: ExecutionContext
  ): Unit =
    Await.result(save((state, breadcrumbs)), timeout)

  def get(implicit
    headerCarrier: HC,
    timeout: Duration,
    ec: ExecutionContext
  ): Option[StateAndBreadcrumbs] =
    Await.result(fetch, timeout)

  override def clear(implicit hc: HC, ec: ExecutionContext): Future[Unit] =
    Future.successful(storage.clear())

}
