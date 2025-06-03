package com.scriptglance.utils.constants

import androidx.annotation.StringRes
import com.scriptglance.R

enum class ErrorCode(val code: Int, @StringRes val messageResId: Int) {
    InvalidCredentials(0, R.string.error_invalid_credentials),
    EmailDuplicate(1, R.string.error_email_duplicate),
    InvalidResetPasswordToken(2, R.string.error_invalid_reset_password_token),
    InvalidEmailVerificationCode(3, R.string.error_invalid_email_verification_code),
    EmailNotVerified(4, R.string.error_email_not_verified),
    EmailAlreadyVerified(5, R.string.error_email_already_verified),
    EmailVerificationCodeNotExpired(6, R.string.error_email_verification_code_not_expired),
    PasswordResetTokenNotExpired(7, R.string.error_password_reset_token_not_expired),;

    companion object {
        fun fromCode(code: Int?): ErrorCode? = entries.find { it.code == code }
    }
}
