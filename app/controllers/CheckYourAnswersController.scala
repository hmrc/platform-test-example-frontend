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

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.WhatWouldYouLikeToDo.{Createanaccount, Requestapet}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{ChooseLocationSummary, WhatIsYourDOBSummary, WhatIsYourEmailSummary, WhatIsYourNameSummary, WhatPetLikeSummary, WhatPetLookingForSummary, WhenWantPetFromSummary, WhenWantPetUntilSummary, WillPetBeAroundChildrenSummary}
import viewmodels.govuk.summarylist.*
import views.html.CheckYourAnswersView
import models.NormalMode
import pages.WhatWouldYouLikeToDoPage

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>

        val answers = request.userAnswers

        answers.get(WhatWouldYouLikeToDoPage) match {
          case Some(Requestapet) =>
            val list = SummaryListViewModel(
              Seq(
                WhatPetLookingForSummary.row(answers),
                WillPetBeAroundChildrenSummary.row(answers),
                WhenWantPetFromSummary.row(answers),
                WhenWantPetUntilSummary.row(answers)
              ).flatten
            )
            Ok(view(list))

          case Some(Createanaccount) =>
            val list = SummaryListViewModel(
              Seq(
                WhatIsYourNameSummary.row(answers),
                WhatIsYourEmailSummary.row(answers),
                ChooseLocationSummary.row(answers),
                WhatIsYourDOBSummary.row(answers),
                WhatPetLikeSummary.row(answers)
              ).flatten
            )
            Ok(view(list))

          case None =>
            Redirect(
              routes.WhatWouldYouLikeToDoController.onPageLoad(NormalMode)
            )
        }
    }

  def onSubmit: Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>

        request.userAnswers.get(WhatWouldYouLikeToDoPage) match {
          case Some(Requestapet) =>
            Redirect(routes.PayYourDepositController.onPageLoad(NormalMode))

          case Some(Createanaccount) =>
            Redirect(routes.ConfirmationController.onPageLoad())

          case None =>
            Redirect(routes.WhatWouldYouLikeToDoController.onPageLoad(NormalMode))
        }
    }
}
