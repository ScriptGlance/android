package com.scriptglance.ui.screen.auth.forgotPassword

import AuthCard
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scriptglance.R
import com.scriptglance.ui.common.components.AppTextField
import com.scriptglance.ui.common.components.BeigeButton
import com.scriptglance.ui.theme.Black21
import com.scriptglance.ui.theme.Green3959
import com.scriptglance.ui.theme.Green45
import com.scriptglance.ui.theme.WhiteF3

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isEmailValid = remember(state.email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(WhiteF3)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = stringResource(R.string.logo),
            modifier = Modifier
                .padding(top = 15.dp)
                .fillMaxWidth()
                .height(60.dp)
        )

        AuthCard(
            title = stringResource(R.string.forgot_password).takeIf { !state.isSuccess },
            content = {
                if (!state.isSuccess) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.forgot_password_description),
                            fontSize = 18.sp,
                            color = Green45,
                            textAlign = TextAlign.Center,
                            lineHeight = 23.sp,
                            modifier = Modifier.padding(bottom = 22.dp)
                        )
                        AppTextField(
                            value = state.email,
                            onValueChange = { viewModel.onEmailChange(it) },
                            placeholder = stringResource(R.string.email),
                            keyboardType = KeyboardType.Email,
                            singleLine = true,
                            error = if (state.isError) stringResource(R.string.error) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                        )
                        BeigeButton(
                            label = stringResource(R.string.restore_password),
                            onClick = { viewModel.sendForgotPassword() },
                            enabled = state.email.isNotBlank() && isEmailValid && !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.return_to_login),
                            color = Green3959,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .clickable {
                                    viewModel.resetState()
                                    onBackToLogin()
                                }
                                .padding(top = 2.dp)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = stringResource(R.string.success),
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            stringResource(R.string.reset_email_sent),
                            fontSize = 22.sp,
                            color = Black21,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 18.dp, top = 6.dp)
                        )
                        Text(
                            text = stringResource(R.string.return_to_login),
                            color = Green3959,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    viewModel.resetState()
                                    onBackToLogin()
                                }
                                .padding(top = 6.dp)
                        )
                    }
                }
            }
        )
    }
}
