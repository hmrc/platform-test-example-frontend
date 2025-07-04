/*
 * Copyright 2025 HM Revenue & Customs
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

package generators

import models._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

trait ModelGenerators {

  implicit lazy val arbitraryPayYourDeposit: Arbitrary[PayYourDeposit] =
    Arbitrary {
      for {
        AccountName <- arbitrary[String]
        SortCode <- arbitrary[String]
        AccountNumber <- arbitrary[String]
        RollNumber <- arbitrary[Option[String]]
      } yield PayYourDeposit(AccountName, SortCode, AccountNumber, RollNumber)
    }

  implicit lazy val arbitraryWhatPetLookingFor: Arbitrary[WhatPetLookingFor] =
    Arbitrary {
      Gen.oneOf(WhatPetLookingFor.values)
    }
}
