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
import models.{CheckMode, NormalMode, UserAnswers}
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.UploadFilePage
import play.api.inject.bind
import play.api.libs.Files.TemporaryFileCreator
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository

import java.io.RandomAccessFile
import java.nio.file.Files
import scala.concurrent.Future

class UploadFileControllerSpec extends SpecBase with MockitoSugar {

  private val boundary = "upload-test-boundary"
  private val expectedMaxFileSizeBytes = 100000000L

  private def uploadRequest(url: String, filename: String = "photo-id.txt", contents: String = "synthetic test file, not real ID") =
    FakeRequest(POST, url)
      .withHeaders(CONTENT_TYPE -> s"multipart/form-data; boundary=$boundary")
      .withBody(ByteString(
        s"--$boundary\r\nContent-Disposition: form-data; name=\"file-upload-1\"; filename=\"$filename\"\r\n" +
          s"Content-Type: application/octet-stream\r\n\r\n$contents\r\n--$boundary--\r\n"
      ))

  "UploadFile Controller" - {

    "must return OK and render the upload page for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.UploadFileController.onPageLoad(NormalMode).url)
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("Upload your photo ID")
        contentAsString(result) must include("enctype=\"multipart/form-data\"")
        contentAsString(result) must include("method=\"POST\"")
        contentAsString(result) must include(s"""action="${routes.UploadFileController.onSubmit(NormalMode).url}"""")
      }
    }

    Seq(NormalMode, CheckMode).foreach { mode =>
      s"must receive a multipart upload, save its filename and redirect in $mode" in {
        val repository = mock[SessionRepository]
        when(repository.set(any())) thenReturn Future.successful(true)
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(repository))
          .build()

        running(application) {
          val result = route(application, uploadRequest(routes.UploadFileController.onSubmit(mode).url)).value

          status(result) mustEqual SEE_OTHER
          val expectedRoute = mode match {
            case NormalMode => routes.WhatPetLikeController.onPageLoad(NormalMode)
            case CheckMode => routes.CheckYourAnswersController.onPageLoad()
          }
          redirectLocation(result).value mustEqual expectedRoute.url
          val saved = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(repository).set(saved.capture())
          saved.getValue.get(UploadFilePage).value mustEqual "photo-id.txt"
        }
      }
    }

    "must hide the existing filename and allow replacement in Check mode" in {
      val repository = mock[SessionRepository]
      when(repository.set(any())) thenReturn Future.successful(true)
      val answers = emptyUserAnswers.set(UploadFilePage, "old.txt").success.value
      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[SessionRepository].toInstance(repository))
        .build()

      running(application) {
        val get = route(application, FakeRequest(GET, routes.UploadFileController.onPageLoad(CheckMode).url)).value
        status(get) mustEqual OK
        contentAsString(get) must not include "old.txt"
        contentAsString(get) must include(routes.UploadFileController.onSubmit(CheckMode).url)

        val result = route(application, uploadRequest(routes.UploadFileController.onSubmit(CheckMode).url, "new.txt")).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url
        val saved = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(repository).set(saved.capture())
        saved.getValue.get(UploadFilePage).value mustEqual "new.txt"
      }
    }

    "must reject a missing file without changing the existing answer" in {
      val repository = mock[SessionRepository]
      val answers = emptyUserAnswers.set(UploadFilePage, "old.txt").success.value
      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[SessionRepository].toInstance(repository))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.UploadFileController.onSubmit(CheckMode).url)
          .withHeaders(CONTENT_TYPE -> s"multipart/form-data; boundary=$boundary")
          .withBody(ByteString(s"--$boundary--\r\n"))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(messages(application)("uploadFile.error.required"))
        contentAsString(result) must not include "old.txt"
        contentAsString(result) must include("href=\"#file-upload-1\"")
        verifyNoInteractions(repository)
      }
    }

    "must reject an empty file" in {
      val repository = mock[SessionRepository]
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(repository))
        .build()

      running(application) {
        val result = route(application, uploadRequest(routes.UploadFileController.onSubmit(NormalMode).url, contents = "")).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(messages(application)("uploadFile.error.required"))
        verifyNoInteractions(repository)
      }
    }

    Seq(expectedMaxFileSizeBytes - 1, expectedMaxFileSizeBytes, expectedMaxFileSizeBytes + 1).foreach { size =>
      s"must enforce the size boundary for a file of $size bytes" in {
        val repository = mock[SessionRepository]
        when(repository.set(any())) thenReturn Future.successful(true)
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SessionRepository].toInstance(repository))
          .build()

        running(application) {
          val tempFile = application.injector.instanceOf[TemporaryFileCreator].create("upload-test", ".txt")
          try {
            val file = new RandomAccessFile(tempFile.path.toFile, "rw")
            try file.setLength(size) finally file.close()
            val body = MultipartFormData(
              Map.empty[String, Seq[String]],
              Seq(FilePart("file-upload-1", "photo-id.txt", Some("application/octet-stream"), tempFile)),
              Seq.empty
            )
            val request = FakeRequest(POST, routes.UploadFileController.onSubmit(NormalMode).url).withBody(body)
            val result = application.injector.instanceOf[UploadFileController].onSubmit(NormalMode)(request)

            if (size < expectedMaxFileSizeBytes) {
              status(result) mustEqual SEE_OTHER
              verify(repository).set(any())
            } else {
              status(result) mustEqual BAD_REQUEST
              contentAsString(result) must include(messages(application)("uploadFile.error.size"))
              verifyNoInteractions(repository)
            }
            Files.exists(tempFile.path) mustBe false
          } finally {
            tempFile.delete()
          }
        }
      }
    }

    Seq("success", "missing", "empty", "no-session", "save-false", "save-failed", "save-threw").foreach { scenario =>
      s"must delete all temporary file parts after $scenario" in {
        val repository = mock[SessionRepository]
        val saveError = new IllegalStateException("Save failed")
        scenario match {
          case "save-false" => when(repository.set(any())) thenReturn Future.successful(false)
          case "save-failed" => when(repository.set(any())) thenReturn Future.failed(saveError)
          case "save-threw" => when(repository.set(any())) thenThrow saveError
          case _ => when(repository.set(any())) thenReturn Future.successful(true)
        }
        val application = applicationBuilder(
          userAnswers = if (scenario == "no-session") None else Some(emptyUserAnswers)
        ).overrides(bind[SessionRepository].toInstance(repository)).build()

        running(application) {
          val creator = application.injector.instanceOf[TemporaryFileCreator]
          val selected = creator.create("upload-cleanup-test", ".txt")
          val extra = creator.create("upload-cleanup-extra", ".txt")
          try {
            if (scenario != "empty") {
              Files.write(selected.path, Array[Byte](1, 2, 3))
            }
            Files.write(extra.path, Array[Byte](4, 5, 6))
            val field = if (scenario == "missing") "unexpected-field" else UploadFileController.FileField
            val body = MultipartFormData(
              Map.empty[String, Seq[String]],
              Seq(
                FilePart(field, "photo-id.txt", Some("application/octet-stream"), selected),
                FilePart("extra", "extra.txt", Some("application/octet-stream"), extra)
              ),
              Seq.empty
            )
            val request = FakeRequest(POST, routes.UploadFileController.onSubmit(NormalMode).url).withBody(body)
            val result = application.injector.instanceOf[UploadFileController].onSubmit(NormalMode)(request)

            scenario match {
              case "save-false" =>
                val error = result.failed.futureValue
                error mustBe a[IllegalStateException]
                error.getMessage mustEqual "Unable to save upload answer"
              case "save-failed" | "save-threw" =>
                result.failed.futureValue mustBe saveError
              case "missing" | "empty" =>
                status(result) mustEqual BAD_REQUEST
                verifyNoInteractions(repository)
              case "no-session" =>
                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
                verifyNoInteractions(repository)
              case _ =>
                status(result) mustEqual SEE_OTHER
            }
            Files.exists(selected.path) mustBe false
            Files.exists(extra.path) mustBe false
          } finally {
            selected.delete()
            extra.delete()
          }
        }
      }
    }

    "must surface cleanup errors and still attempt to delete the remaining files" in {
      val repository = mock[SessionRepository]
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SessionRepository].toInstance(repository)).build()

      running(application) {
        val creator = application.injector.instanceOf[TemporaryFileCreator]
        val blocked = creator.create("upload-cleanup-blocked", ".tmp")
        val extra = creator.create("upload-cleanup-extra", ".txt")
        Files.delete(blocked.path)
        Files.createDirectory(blocked.path)
        val child = blocked.path.resolve("child.txt")
        try {
          Files.write(child, Array[Byte](1))
          val body = MultipartFormData(
            Map.empty[String, Seq[String]],
            Seq(
              FilePart("file-upload-1", "photo-id.txt", Some("application/octet-stream"), blocked),
              FilePart("extra", "extra.txt", Some("application/octet-stream"), extra)
            ),
            Seq.empty
          )
          val request = FakeRequest(POST, routes.UploadFileController.onSubmit(NormalMode).url).withBody(body)
          val result = application.injector.instanceOf[UploadFileController].onSubmit(NormalMode)(request)

          result.failed.futureValue mustBe a[java.nio.file.DirectoryNotEmptyException]
          Files.exists(extra.path) mustBe false
          verifyNoInteractions(repository)
        } finally {
          Files.deleteIfExists(child)
          Files.deleteIfExists(blocked.path)
          extra.delete()
        }
      }
    }

    Seq(GET, POST).foreach { method =>
      s"must redirect to Index for a $method without session answers" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            if (method == GET) {
              FakeRequest(GET, routes.UploadFileController.onPageLoad(NormalMode).url).withBody(ByteString.empty)
            } else {
              uploadRequest(routes.UploadFileController.onSubmit(NormalMode).url)
            }
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
        }
      }
    }
  }
}
