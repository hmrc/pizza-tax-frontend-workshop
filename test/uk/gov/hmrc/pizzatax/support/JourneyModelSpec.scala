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

import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util.Try
import uk.gov.hmrc.play.fsm.JourneyModel
import org.scalatest.matchers.Matcher
import org.scalatest.matchers.MatchResult

trait JourneyModelSpec extends TestJourneyService[DummyContext] {
  self: Matchers =>

  val model: JourneyModel

  case class given[S <: model.State: ClassTag](initialState: S) {

    import scala.concurrent.ExecutionContext.Implicits.global

    private def await[A](future: Future[A])(implicit timeout: Duration): A =
      Await.result(future, timeout)

    implicit val context: DummyContext = DummyContext()

    implicit val defaultTimeout: FiniteDuration = 5 seconds

    Option(initialState) match {
      case Some(state) => await(save((state, Nil)))
      case None        => await(clear)
    }

    final def withBreadcrumbs(breadcrumbs: model.State*): this.type = {
      await(for {
        Some((s, _)) <- fetch
        _            <- save((s, breadcrumbs.toList))
      } yield ())
      this
    }

    final def when(transition: model.Transition): When = {
      val result = await(
        apply(transition)
          .recover { case model.TransitionNotAllowed(s, b, _) => (s, b) }
      )
      When(initialState, Right(result))
    }

    final def shouldFailWhen(transition: model.Transition) =
      Try(await(apply(transition))).isSuccess shouldBe false

    final def when(merger: model.Merger[S], state: model.State): When = {
      val result = await(modify { s: S => merger.apply((s, state)) })
      When(initialState, Right(result))
    }
  }

  case class When(
    initialState: model.State,
    result: Either[Throwable, (model.State, List[model.State])]
  )

  final def thenGo[S <: model.State: ClassTag](state: model.State): Matcher[When] =
    new Matcher[When] {
      override def apply(result: When): MatchResult =
        result match {
          case When(_, Left(exception)) =>
            MatchResult(false, s"Transition has been expected but got an exception $exception", s"")
          case When(_, Right((thisState, _))) if state != thisState =>
            MatchResult(false, s"State $state has been expected but got state $thisState", s"")
          case _ =>
            MatchResult(true, "", s"")
        }
    }

  final def thenMatch[S <: model.State: ClassTag](
    statePF: PartialFunction[model.State, Unit]
  ): Matcher[When] =
    new Matcher[When] {
      override def apply(result: When): MatchResult =
        result match {
          case When(_, Left(exception)) =>
            MatchResult(false, s"Transition has been expected but got an exception $exception", s"")
          case When(_, Right((thisState, _))) if !statePF.isDefinedAt(thisState) =>
            MatchResult(false, s"Matching state has been expected but got state $thisState", s"")
          case _ => MatchResult(true, "", s"")
        }
    }

  final def doNothing[S <: model.State: ClassTag]: Matcher[When] =
    new Matcher[When] {
      override def apply(result: When): MatchResult =
        result match {
          case When(_, Left(exception)) =>
            MatchResult(false, s"Transition has been expected but got an exception $exception", s"")
          case When(initialState, Right((thisState, _))) if thisState != initialState =>
            MatchResult(false, s"No state change has been expected but got state $thisState", s"")
          case _ =>
            MatchResult(true, "", s"")
        }
    }

}
