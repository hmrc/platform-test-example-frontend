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
import pages._
import models._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from What is your name page in the route map to What is your email page" in {
        case object WhatIsYourNamePage extends Page
        navigator.nextPage(WhatIsYourNamePage, NormalMode, UserAnswers("id")) mustBe routes.WhatIsYourEmailController.onPageLoad(NormalMode)
      }

      "must go from What is your Email page in the route map to Choose Location page" in {
        case object WhatIsYourEmailPage extends Page
        navigator.nextPage(WhatIsYourEmailPage, NormalMode, UserAnswers("id")) mustBe routes.ChooseLocationController.onPageLoad(NormalMode)
      }

      "must go from WhatPetLookingForPage in the route map to WillPetBeAroundChildrenPage" in {

        case object WhatPetLookingForPage extends Page
        navigator.nextPage(WhatPetLookingForPage, NormalMode, UserAnswers("id")) mustBe routes.WillPetBeAroundChildrenController.onPageLoad(NormalMode)
      }

      "must go from WillPetBeAroundChildrenPage in the route map to WhenWantPetFromPage" in {

        case object WillPetBeAroundChildrenPage extends Page
        navigator.nextPage(WillPetBeAroundChildrenPage, NormalMode, UserAnswers("id")) mustBe routes.WhenWantPetFromController.onPageLoad(NormalMode)
      }

      "must go from WhenWantPetFromPage in the route map to WhenWantPetUntilPage" in {

        case object WhenWantPetFromPage extends Page
        navigator.nextPage(WhenWantPetFromPage, NormalMode, UserAnswers("id")) mustBe routes.WhenWantPetUntilController.onPageLoad(NormalMode)
      }

      "must go from WhenWantPetUntilPage in the route map to CheckYourAnswersPage" in {

        case object WhenWantPetUntilPage extends Page
        navigator.nextPage(WhenWantPetUntilPage, NormalMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad()
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
