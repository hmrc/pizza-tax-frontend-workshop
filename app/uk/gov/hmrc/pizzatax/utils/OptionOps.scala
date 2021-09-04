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

object OptionOps {

  implicit class OptionBooleanExt(val o: Option[Boolean]) extends AnyVal {
    def isTrue: Boolean = o.exists(b => b == true)
    def isFalse: Boolean = o.exists(b => b == false)
  }

  implicit class OptionExt[A](val o1: Option[A]) extends AnyVal {
    def isJoint(o2: Option[Any]): Boolean = checkJoint(o1, o2)
    def isDisjoint(o2: Option[Any]): Boolean = checkDisjoint(o1, o2)
    def isEmptyOr(f: A => Boolean): Boolean = checkIfDefined(o1, f)
    def isEmptyOr(f: => Boolean): Boolean = checkIfDefined(o1, (_: Any) => f)
  }

  def options[A](set: Set[A]): Iterable[Option[A]] =
    set.map(Option.apply(_)) + None

  def options[A](values: A*): Iterable[Option[A]] =
    None :: values.toList.map(Some.apply)

  def checkIfDefined[A](a: Option[A], f: A => Boolean): Boolean =
    a match {
      case None    => true
      case Some(v) => f(v)
    }

  def checkJoint(a: Option[Any], b: Option[Any]): Boolean =
    a match {
      case None => b.isEmpty
      case _    => b.isDefined
    }

  def checkDisjoint(a: Option[Any], b: Option[Any]): Boolean =
    a match {
      case None => b.isDefined
      case _    => b.isEmpty
    }
}
