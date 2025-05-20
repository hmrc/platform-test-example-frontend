package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class PayYourDepositFormProviderSpec extends StringFieldBehaviours {

  val form = new PayYourDepositFormProvider()()

  ".AccountName" - {

    val fieldName = "AccountName"
    val requiredKey = "payYourDeposit.error.AccountName.required"
    val lengthKey = "payYourDeposit.error.AccountName.length"
    val maxLength = 50

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".SortCode" - {

    val fieldName = "SortCode"
    val requiredKey = "payYourDeposit.error.SortCode.required"
    val lengthKey = "payYourDeposit.error.SortCode.length"
    val maxLength = 6

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".AccountNumber" - {

    val fieldName = "AccountNumber"
    val requiredKey = "payYourDeposit.error.AccountNumber.required"
    val lengthKey = "payYourDeposit.error.AccountNumber.length"
    val maxLength = 8

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".RollNumber" - {

    val fieldName = "RollNumber"
    val requiredKey = "payYourDeposit.error.RollNumber.required"
    val lengthKey = "payYourDeposit.error.RollNumber.length"
    val maxLength = 8

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
