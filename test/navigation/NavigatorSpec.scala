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

package navigation

import base.SpecBase
import controllers.routes
import models.WhatPetLookingFor._
import models.WhatWouldYouLikeToDo._
import pages._
import models._

import java.time.LocalDate


class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from What would you like to do selecting create an account in the route map to What is your name page" in {
        val userAnswers = UserAnswers(userAnswersId).set(WhatWouldYouLikeToDoPage, Createanaccount).success.value

        navigator.nextPage(WhatWouldYouLikeToDoPage, NormalMode, userAnswers) mustBe routes.WhatIsYourNameController.onPageLoad(NormalMode)
      }

      "must go from What would you like to do selecting create an account in the route map to What pet looking for page" in {
        val userAnswers = UserAnswers(userAnswersId).set(WhatWouldYouLikeToDoPage, Requestapet).success.value

        navigator.nextPage(WhatWouldYouLikeToDoPage, NormalMode, userAnswers) mustBe routes.WhatPetLookingForController.onPageLoad(NormalMode)
      }

      "must go from What is your name page in the route map to What is your email page" in {
        val userAnswers = UserAnswers("id").set(WhatIsYourNamePage,"PersonName").success.value

        navigator.nextPage(WhatIsYourNamePage, NormalMode,userAnswers) mustBe routes.WhatIsYourEmailController.onPageLoad(NormalMode)
      }

      "must go from What is your Email page in the route map to Choose Location page" in {
        val userAnswers = emptyUserAnswers.set(WhatIsYourEmailPage, "EmailAddress").success.value

        navigator.nextPage(WhatIsYourEmailPage, NormalMode, userAnswers) mustBe routes.ChooseLocationController.onPageLoad(NormalMode)
      }

      "must go from Choose location page in the route map to check your answers page" in {
        val userAnswers = emptyUserAnswers.set(ChooseLocationPage, "DE-Germany").success.value
        navigator.nextPage(ChooseLocationPage, NormalMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from WhatPetLookingForPage in the route map to WillPetBeAroundChildrenPage" in {

        val userAnswers = emptyUserAnswers.set(WhatPetLookingForPage, Cat).success.value
        navigator.nextPage(WhatPetLookingForPage, NormalMode, userAnswers) mustBe routes.WillPetBeAroundChildrenController.onPageLoad(NormalMode)
      }

      "must go from WillPetBeAroundChildrenPage in the route map to WhenWantPetFromPage" in {
        val userAnswers = emptyUserAnswers.set(WillPetBeAroundChildrenPage, true).success.value
        navigator.nextPage(WillPetBeAroundChildrenPage, NormalMode, userAnswers) mustBe routes.WhenWantPetFromController.onPageLoad(NormalMode)
      }

      "must go from WhenWantPetFromPage in the route map to WhenWantPetUntilPage" in {
        val startDate = LocalDate.now()
        val userAnswers = emptyUserAnswers.set(WhenWantPetFromPage, startDate).success.value

        navigator.nextPage(WhenWantPetFromPage, NormalMode, userAnswers) mustBe routes.WhenWantPetUntilController.onPageLoad(NormalMode)
      }

      "must go from WhenWantPetUntilPage in the route map to CheckYourAnswersPage" in {
        val endDate = LocalDate.now()
        val userAnswers = emptyUserAnswers.set(WhenWantPetUntilPage, endDate).success.value

        navigator.nextPage(WhenWantPetUntilPage, NormalMode, userAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
