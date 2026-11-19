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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.UploadFilePage
import play.api.data.Form
import play.api.data.Forms.text
import play.api.i18n.I18nSupport
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, MultipartFormData}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UploadFileView

import java.nio.file.Files
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

object UploadFileController {
  val MaxFileSize: Long = 100000000L
  val FileField: String = "file-upload-1"
}

class UploadFileController @Inject()(
                                       val controllerComponents: MessagesControllerComponents,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       view: UploadFileView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  import UploadFileController._

  private val form = Form(FileField -> text)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    Ok(view(form, mode))
  }

  def onSubmit(mode: Mode): Action[MultipartFormData[TemporaryFile]] =
    Action.async(parse.multipartFormData(maxLength = MaxFileSize + 1024 * 1024)) { request =>
      Future.unit.flatMap { _ =>
        submit(mode).apply(request)
      }.transform { outcome =>
        val errors = request.body.files.flatMap { file =>
          Try(Files.deleteIfExists(file.ref.path)).failed.toOption
        }
        errors.headOption match {
          case Some(error) =>
            errors.tail.foreach(error.addSuppressed)
            outcome.failed.foreach(error.addSuppressed)
            Failure(error)
          case None =>
            outcome
        }
      }
    }

  private def submit(mode: Mode): Action[MultipartFormData[TemporaryFile]] =
    (identify andThen getData andThen requireData)
      .async(parse.multipartFormData(maxLength = MaxFileSize + 1024 * 1024)) { implicit request =>
        def invalid(key: String) =
          Future.successful(BadRequest(view(form.withError(FileField, key), mode)))

        request.body.file(FileField) match {
          case None =>
            invalid("uploadFile.error.required")

          case Some(file) =>
            val size = Files.size(file.ref.path)
            val filename = file.filename.replace('\\', '/').split('/').lastOption.filter(_.nonEmpty)

            filename match {
              case None =>
                invalid("uploadFile.error.required")
              case Some(_) if size == 0 =>
                invalid("uploadFile.error.required")
              case Some(_) if size >= MaxFileSize =>
                invalid("uploadFile.error.size")
              case Some(name) =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(UploadFilePage, name))
                  saved <- sessionRepository.set(updatedAnswers)
                  _ <- if (saved) Future.successful(()) else Future.failed(new IllegalStateException("Unable to save upload answer"))
                } yield Redirect(navigator.nextPage(UploadFilePage, mode, updatedAnswers))
            }
        }
      }
}
