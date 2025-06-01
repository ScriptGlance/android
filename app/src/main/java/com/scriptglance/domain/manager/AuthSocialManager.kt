package com.scriptglance.domain.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.scriptglance.R
import com.scriptglance.domain.callback.SocialAuthCallback

class AuthSocialManager(
    private val context: Context,
    private val handler: SocialAuthCallback
) {

    fun launchGoogleSignIn(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.google_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        launcher.launch(googleSignInClient.signInIntent)
    }

    fun handleGoogleResult(result: ActivityResult, onLoginSuccess: () -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                handler.socialLogin(context, "google", idToken, onLoginSuccess)
            }
        } catch (_: Exception) {
        }
    }

    fun launchFacebookSignIn(activity: Activity) {
        LoginManager.getInstance()
            .logInWithReadPermissions(activity, listOf("email", "public_profile"))
    }

    fun registerFacebookCallback(
        callbackManager: CallbackManager,
        onLoginSuccess: (String) -> Unit
    ) {
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val token = result.accessToken.token
                    onLoginSuccess(token)
                }

                override fun onCancel() {}
                override fun onError(error: FacebookException) {}
            }
        )
    }
}
