package com.scriptglance.ui.screen.premium.purachase

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scriptglance.R
import com.scriptglance.ui.screen.premium.components.PaymentWebView
import com.scriptglance.ui.theme.Gray59
import com.scriptglance.ui.theme.Green5E
import com.scriptglance.ui.theme.RedEA
import com.scriptglance.ui.theme.WhiteEA

@Composable
fun PremiumPurchaseScreen(
    onNavigateBack: () -> Unit,
    viewModel: PremiumPurchaseViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showWebView by remember { mutableStateOf(false) }

    LaunchedEffect(state.purchaseCompleted) {
        if (state.purchaseCompleted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.checkoutUrl) {
        if (state.checkoutUrl != null) {
            showWebView = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(WhiteEA)
    ) {
        if (showWebView && state.checkoutUrl != null) {
            PaymentWebView(
                url = state.checkoutUrl!!,
                onNavigateBack = {
                    showWebView = false
                    viewModel.resetState()
                },
                onPaymentComplete = {
                    showWebView = false
                    viewModel.onPaymentCompleted()
                }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                PremiumPurchaseHeader(
                    onNavigateBack = onNavigateBack
                )

                if (state.isLoading) {
                    LoadingContent()
                } else {
                    PremiumPurchaseContent(
                        state = state,
                        onPurchase = viewModel::purchasePremium
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Green5E)
    }
}

@Composable
private fun PremiumPurchaseContent(
    state: PremiumPurchaseState,
    onPurchase: () -> Unit
) {
    val config = state.config
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.get_scriptglance_premium),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.subscribe_to_unlock_all_features),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Gray59
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.free),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray59
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (config != null) {
                    FeatureItem(
                        text = stringResource(
                            R.string.video_duration_limit_dynamic,
                            formatTime(context, config.premiumConfig.maxFreeRecordingTimeSeconds)
                        ),
                        isIncluded = false
                    )
                    FeatureItem(
                        text = stringResource(
                            R.string.max_participants_dynamic,
                            config.premiumConfig.maxFreeParticipantsCount
                        ),
                        isIncluded = false
                    )
                    FeatureItem(
                        text = stringResource(
                            R.string.max_video_recordings_dynamic,
                            config.premiumConfig.maxFreeVideoCount
                        ),
                        isIncluded = false
                    )
                }

                FeatureItem(
                    text = stringResource(R.string.watermark),
                    isIncluded = false
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.premium_version),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Green5E
                )

                Spacer(modifier = Modifier.height(20.dp))

                FeatureItem(
                    text = stringResource(R.string.recorded_video_editing),
                    isIncluded = true
                )
                FeatureItem(
                    text = stringResource(R.string.no_restrictions),
                    isIncluded = true
                )
                FeatureItem(
                    text = stringResource(R.string.no_watermark),
                    isIncluded = true
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = config?.let {
                stringResource(
                    R.string.price_per_month_dynamic,
                    formatPrice(it.premiumConfig.premiumPriceCents)
                )
            } ?: "",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Green5E
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onPurchase,
            enabled = !state.isPurchasing,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green5E),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (state.isPurchasing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.buy_premium),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        state.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = RedEA,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun formatTime(context: Context, seconds: Int): String {
    val minutes = seconds / 60
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        if (remainingMinutes == 0) {
            context.getString(R.string.hours, hours)
        } else {
            context.getString(R.string.hours_minutes, hours, remainingMinutes)
        }
    } else {
        context.getString(R.string.minutes, minutes)
    }
}

private fun formatPrice(cents: Int): String {
    val dollars = cents / 100.0
    return if (dollars == dollars.toInt().toDouble()) {
        "${dollars.toInt()}$"
    } else {
        "${"%.2f".format(dollars)}$"
    }
}

@Composable
private fun PremiumPurchaseHeader(
    onNavigateBack: () -> Unit
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Green5E
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.premium_purchase_title),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FeatureItem(
    text: String,
    isIncluded: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = if (isIncluded) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (isIncluded) Green5E else RedEA,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}