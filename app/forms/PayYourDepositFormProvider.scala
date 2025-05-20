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

package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.PayYourDeposit

class PayYourDepositFormProvider @Inject() extends Mappings {

   def apply(): Form[PayYourDeposit] = Form(
     mapping(
      "AccountName" -> text("payYourDeposit.error.AccountName.required")
        .verifying(maxLength(50, "payYourDeposit.error.AccountName.length")),
       "SortCode" -> text("payYourDeposit.error.SortCode.required")
         .verifying(maxLength(6, "payYourDeposit.error.SortCode.length")),
       "AccountNumber" -> text("payYourDeposit.error.AccountNumber.required")
         .verifying(maxLength(8, "payYourDeposit.error.AccountNumber.length")),
       "RollNumber" -> optional(text("payYourDeposit.error.RollNumber.required")
         .verifying(maxLength(8, "payYourDeposit.error.RollNumber.length")))
    )(PayYourDeposit.apply)(x => Some((x.AccountName, x.SortCode, x.AccountNumber, x.RollNumber)))
   )
 }
