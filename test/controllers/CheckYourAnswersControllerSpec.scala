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
import models.NormalMode
import models.WhatPetLike.Cats
import models.WhatPetLookingFor.Cat
import models.WhatWouldYouLikeToDo.{Createanaccount, Requestapet}
import pages.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.*
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import java.time.LocalDate

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val allAnswers = emptyUserAnswers
    .set(WhatPetLookingForPage, Cat).success.value
    .set(WillPetBeAroundChildrenPage, true).success.value
    .set(WhenWantPetFromPage, LocalDate.of(2027, 1, 1)).success.value
    .set(WhenWantPetUntilPage, LocalDate.of(2027, 1, 7)).success.value
    .set(WhatIsYourNamePage, "Example Name").success.value
    .set(WhatIsYourEmailPage, "example@example.com").success.value
    .set(ChooseLocationPage, "GB-United Kingdom").success.value
    .set(WhatIsYourDOBPage, LocalDate.of(2007, 11, 27)).success.value
    .set(WhatPetLikePage, Cats).success.value

  "Check Your Answers Controller" - {

    "must show only pet request answers when both journeys have saved answers" in {

      val answers = allAnswers.set(WhatWouldYouLikeToDoPage, Requestapet).success.value
      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        implicit val msgs: Messages = messages(application)
        val rows = Seq(
          WhatPetLookingForSummary.row(answers),
          WillPetBeAroundChildrenSummary.row(answers),
          WhenWantPetFromSummary.row(answers),
          WhenWantPetUntilSummary.row(answers)
        ).flatten
        val list = SummaryListViewModel(rows)

        rows.size mustEqual 4
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "must show only account creation answers when both journeys have saved answers" in {
      val answers = allAnswers.set(WhatWouldYouLikeToDoPage, Createanaccount).success.value
      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        implicit val msgs: Messages = messages(application)
        val rows = Seq(
          WhatIsYourNameSummary.row(answers),
          WhatIsYourEmailSummary.row(answers),
          ChooseLocationSummary.row(answers),
          WhatIsYourDOBSummary.row(answers),
          WhatPetLikeSummary.row(answers)
        ).flatten
        val list = SummaryListViewModel(rows)

        rows.size mustEqual 5
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "must redirect to journey selection for a GET if no journey is selected" in {
      val application = applicationBuilder(userAnswers = Some(allAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.WhatWouldYouLikeToDoController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to Index Page for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
      }
    }

    "must redirect to journey selection for a POST if no journey is selected" in {
      val application = applicationBuilder(userAnswers = Some(allAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.WhatWouldYouLikeToDoController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to Index Page for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
      }
    }
  }
}
