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

import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

import scala.concurrent.{ExecutionContext, Future}

/**
  * Basic in-memory store used to test journeys.
  */
trait InMemoryStore[S, HC] {

  private val state: AtomicReference[Option[S]] = new AtomicReference(None)

  def fetch: Future[Option[S]] =
    Future.successful(state.get())

  def save(newState: S)(implicit ec: ExecutionContext): Future[S] =
    Future {
      state
        .updateAndGet(new UnaryOperator[Option[S]] {
          override def apply(t: Option[S]): Option[S] = Some(newState)
        })
        .get
    }

  def clear(): Unit =
    state.set(None)

}