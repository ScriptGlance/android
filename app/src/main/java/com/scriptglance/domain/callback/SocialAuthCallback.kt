package com.scriptglance.domain.callback

import android.content.Context

interface SocialAuthCallback {
    fun socialLogin(
        context: Context,
        provider: String,
        token: String,
        onSuccess: () -> Unit
    )
}