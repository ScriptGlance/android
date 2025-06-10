package com.scriptglance.ui.screen.premium.components

import android.content.Intent
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.scriptglance.R
import com.scriptglance.ui.theme.Green5E
import com.scriptglance.utils.constants.WEB_VIEW_REDIRECT_HOST

@Composable
fun PaymentWebView(
    url: String,
    onNavigateBack: () -> Unit,
    onPaymentComplete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Green5E
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.payment),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.setSupportMultipleWindows(false)

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            url?.let { currentUrl ->
                                if (currentUrl.contains("script-glance.pp.ua")) {
                                    onPaymentComplete()
                                    return true
                                }

                                if (currentUrl.startsWith("app://") ||
                                    currentUrl.startsWith("intent://") ||
                                    currentUrl.startsWith("android-app://") ||
                                    !currentUrl.startsWith("http://") &&
                                    !currentUrl.startsWith("https://")) {

                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, currentUrl.toUri())
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.w("WebView", "Cannot handle URL: $currentUrl", e)
                                    }
                                    return true
                                }

                                return false
                            }
                            return false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)

                            if (errorCode == ERROR_UNSUPPORTED_SCHEME ||
                                description?.contains("ERR_UNKNOWN_URL_SCHEME") == true) {

                                failingUrl?.let { url ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.w("WebView", "Cannot handle failing URL: $url", e)
                                    }
                                }
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            url?.let {
                                if (it.contains(WEB_VIEW_REDIRECT_HOST)) {
                                    onPaymentComplete()
                                }
                            }
                        }
                    }

                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}