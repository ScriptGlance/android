package com.scriptglance.ui.screen.premium.management

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.scriptglance.R
import com.scriptglance.data.model.payment.PaymentCard
import com.scriptglance.data.model.payment.Transaction
import com.scriptglance.ui.screen.premium.components.PaymentWebView
import com.scriptglance.ui.theme.Gray59
import com.scriptglance.ui.theme.Green5E
import com.scriptglance.ui.theme.RedEA
import com.scriptglance.ui.theme.WhiteEA
import com.scriptglance.utils.constants.InvoiceStatus
import com.scriptglance.utils.constants.SubscriptionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PremiumManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: PremiumManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showWebView by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("PremiumManagementScreen", "Screen resumed, refreshing data...")
                    viewModel.refreshData()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                    viewModel.onCardUpdateCompleted()
                },
                onPaymentComplete = {
                    showWebView = false
                    viewModel.onCardUpdateCompleted()
                }
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                SubscriptionManagementHeader(
                    onNavigateBack = onNavigateBack
                )

                if (state.isLoading) {
                    LoadingContent()
                } else if (state.subscriptionData?.paymentCard == null && state.subscriptionData?.status != SubscriptionStatus.CANCELLED) {
                    CardLoadingContent()
                } else {
                    SubscriptionContent(
                        state = state,
                        onUpdateCard = viewModel::updateCard,
                        onCancelSubscription = viewModel::showCancelDialog
                    )
                }
            }
        }

        if (state.showCancelDialog) {
            CancelSubscriptionDialog(
                onConfirm = viewModel::cancelSubscription,
                onDismiss = viewModel::hideCancelDialog,
                isLoading = state.isCancelling
            )
        }

        state.error?.let { error ->
            LaunchedEffect(error) {
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun SubscriptionManagementHeader(
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
                text = stringResource(R.string.premium_subscription),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
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
private fun CardLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Green5E)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.loading_payment_card),
                color = Gray59,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun SubscriptionContent(
    state: PremiumManagementState,
    onUpdateCard: () -> Unit,
    onCancelSubscription: () -> Unit
) {
    val subscription = state.subscriptionData ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            SubscriptionStatusCard(
                status = subscription.status,
                nextPaymentDate = subscription.nextPaymentDate,
                onCancelSubscription = onCancelSubscription
            )
        }

        if (subscription.paymentCard != null) {
            item {
                PaymentCardSection(
                    paymentCard = subscription.paymentCard,
                    onUpdateCard = onUpdateCard,
                    isUpdating = state.isUpdatingCard
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.transactions),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }


        if (state.isLoadingTransactions) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green5E)
                }
            }
        } else if (state.transactions.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_transactions),
                    color = Gray59,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                )
            }
        } else {
            items(state.transactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
}

@Composable
private fun SubscriptionStatusCard(
    status: SubscriptionStatus,
    nextPaymentDate: String?,
    onCancelSubscription: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.status_label, getStatusText(status)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    if (nextPaymentDate != null && status == SubscriptionStatus.ACTIVE) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.next_payment_label, formatDate(nextPaymentDate)),
                            fontSize = 14.sp,
                            color = Gray59
                        )
                    }
                }

                if (status == SubscriptionStatus.ACTIVE) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = onCancelSubscription,
                        colors = ButtonDefaults.buttonColors(containerColor = RedEA),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel_subscription),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentCardSection(
    paymentCard: PaymentCard,
    onUpdateCard: () -> Unit,
    isUpdating: Boolean
) {
    Column {
        Text(
            text = stringResource(R.string.payment_card),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = getPaymentSystemIcon(paymentCard.paymentSystem)),
                        contentDescription = paymentCard.paymentSystem,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "•••• ${paymentCard.maskedNumber}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = paymentCard.paymentSystem.uppercase(),
                        fontSize = 12.sp,
                        color = Gray59
                    )
                }

                Button(
                    onClick = onUpdateCard,
                    enabled = !isUpdating,
                    colors = ButtonDefaults.outlinedButtonColors(),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(1.dp, Gray59, RoundedCornerShape(20.dp)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Green5E
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.change_card),
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = formatDate(transaction.date),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = getTransactionStatusText(transaction.status),
                    fontSize = 14.sp,
                    color = getTransactionStatusColor(transaction.status)
                )
            }

            Text(
                text = "${(transaction.amount / 100.0).toInt()}$",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun CancelSubscriptionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.cancel_subscription_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = stringResource(R.string.cancel_subscription_message),
                fontSize = 16.sp,
                color = Gray59
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = RedEA),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(
                    text = stringResource(R.string.keep_subscription),
                    color = Gray59,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun getStatusText(status: SubscriptionStatus): String {
    return when (status) {
        SubscriptionStatus.ACTIVE -> stringResource(R.string.status_active)
        SubscriptionStatus.PAST_DUE -> stringResource(R.string.status_past_due)
        SubscriptionStatus.CANCELLED -> stringResource(R.string.status_cancelled)
        SubscriptionStatus.CREATED -> stringResource(R.string.status_created)
    }
}

@Composable
private fun getTransactionStatusText(status: InvoiceStatus?): String {
    return when (status) {
        InvoiceStatus.SUCCESS -> stringResource(R.string.transaction_success)
        InvoiceStatus.PROCESSING -> stringResource(R.string.transaction_processing)
        InvoiceStatus.FAILURE -> stringResource(R.string.transaction_failure)
        null -> stringResource(R.string.transaction_unknown)
    }
}

@Composable
private fun getTransactionStatusColor(status: InvoiceStatus?): Color {
    return when (status) {
        InvoiceStatus.SUCCESS -> Green5E
        InvoiceStatus.PROCESSING -> Color(0xFFFFA500)
        InvoiceStatus.FAILURE -> RedEA
        null -> Gray59
    }
}

private fun getPaymentSystemIcon(paymentSystem: String): Int {
    return when (paymentSystem.lowercase()) {
        "visa" -> R.drawable.ic_visa
        else -> R.drawable.ic_mastercard
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}