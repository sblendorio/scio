/*
 * Copyright 2017 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.scio.coders

import org.joda.time.{LocalDate, LocalDateTime}
import org.scalacheck._
import org.scalatest._
import org.scalatest.prop.Checkers

import scala.util.Try

class JodaSerializerTest extends FlatSpec with Checkers {

  // TODO: remove this once https://github.com/scalatest/scalatest/issues/1090 is addressed
  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 100)

  implicit val localDateTimeArb = Arbitrary {
    for {
      year <- Gen.choose(-292275054, 292278993)
      month <- Gen.choose(1, 12)
      maxDayOfMonth <- Try {
        Gen.const(new LocalDateTime(year, month, 1, 0, 0).dayOfMonth().getMaximumValue)
      }.getOrElse(Gen.fail)
      day <- Gen.choose(1, maxDayOfMonth)
      hour <- Gen.choose(0, 23)
      minute <- Gen.choose(0, 59)
      second <- Gen.choose(0, 59)
      ms <- Gen.choose(0, 999)
      attempt <- Try {
        val ldt = new LocalDateTime(year, month, day, hour, minute, second, ms)
        Gen.const(ldt)
      }.getOrElse(Gen.fail)
    } yield attempt
  }

  implicit val localDateArb = Arbitrary {
    Arbitrary.arbitrary[LocalDateTime].map(_.toLocalDate)
  }

  val coder = new KryoAtomicCoder[Any](KryoOptions())

  def roundTripProp[T](value: T): Prop = Prop.secure {
    CoderTestUtils.testRoundTrip(coder, value)
  }

  "KryoAtomicCoder" should "roundtrip LocalDate" in {
    check(roundTripProp[LocalDate] _)
  }

  it should "roundtrip LocalDateTime" in {
    check(roundTripProp[LocalDateTime] _)
  }

}
