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

package controllers

import base.SpecBase
import models.{UserAnswers, WhatWouldYouLikeToDo}
import pages.WhatWouldYouLikeToDoPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ConfirmationView

class ConfirmationControllerSpec extends SpecBase {

  "Confirmation Controller" - {

    "must show the pet request confirmation for that journey" in {
      val userAnswers =
        UserAnswers(userAnswersId).set(WhatWouldYouLikeToDoPage, WhatWouldYouLikeToDo.Requestapet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(WhatWouldYouLikeToDo.Requestapet)(request, messages(application)).toString
      }
    }

    "must show the account creation confirmation for that journey" in {
      val userAnswers =
        UserAnswers(userAnswersId).set(WhatWouldYouLikeToDoPage, WhatWouldYouLikeToDo.Createanaccount).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ConfirmationController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[ConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(WhatWouldYouLikeToDo.Createanaccount)(request, messages(application)).toString
      }
    }

    "must redirect to the journey selection page when no journey has been selected" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ConfirmationController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.WhatWouldYouLikeToDoController.onPageLoad(models.NormalMode).url
      }
    }
  }
}
