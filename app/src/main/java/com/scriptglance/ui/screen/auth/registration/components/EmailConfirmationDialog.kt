package com.scriptglance.ui.screen.auth.registration.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.scriptglance.R
import com.scriptglance.ui.theme.Black21
import com.scriptglance.ui.theme.Green3959
import com.scriptglance.ui.theme.Green45
import com.scriptglance.ui.theme.RedEA
import com.scriptglance.ui.theme.WhiteE0


@Composable
fun EmailConfirmationDialog(
    email: String,
    isOpen: Boolean,
    code: List<String>,
    onCodeChange: (List<String>) -> Unit,
    isCodeIncorrect: Boolean,
    secondsLeft: Int,
    isResending: Boolean,
    isVerifying: Boolean,
    onResend: () -> Unit,
    onVerify: () -> Unit,
    onChangeEmail: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!isOpen) return

    val codeLength = 6
    val focusRequesters = remember { List(codeLength) { FocusRequester() } }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            focusRequesters.firstOrNull()?.requestFocus()
        }
    }

    val minutes = (secondsLeft / 60).toString().padStart(2, '0')
    val seconds = (secondsLeft % 60).toString().padStart(2, '0')
    val timerFormatted = "$minutes:$seconds"

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        containerColor = Color.White,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        title = null,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.email_confirmation),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black21,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = buildAnnotatedString {
                        val fullText = stringResource(R.string.enter_email_confirmation_code, email)
                        val emailStartIndex = fullText.indexOf(email)
                        if (emailStartIndex != -1) {
                            append(fullText.substring(0, emailStartIndex))
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Black21)) {
                                append(email)
                            }
                            append(fullText.substring(emailStartIndex + email.length))
                        } else {
                            append(fullText)
                        }
                    },
                    fontSize = 16.sp,
                    color = Green45,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                    lineHeight = 22.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until codeLength) {
                        val char = code.getOrNull(i) ?: ""
                        BasicTextField(
                            value = char,
                            onValueChange = { value ->
                                val newCode = code.toMutableList()
                                if (value.isEmpty()) {
                                    newCode[i] = ""
                                    onCodeChange(newCode)
                                } else if (value.all { it.isDigit() }) {
                                    if (value.length == 1) {
                                        newCode[i] = value
                                        onCodeChange(newCode)
                                        if (i < codeLength - 1) {
                                            focusRequesters[i + 1].requestFocus()
                                        } else if (newCode.all { it.isNotEmpty() }) {
                                            onVerify()
                                        }
                                    } else {
                                        var currentPastedIdx = 0
                                        for (fillIdx in i until codeLength) {
                                            if (currentPastedIdx < value.length) {
                                                newCode[fillIdx] = value[currentPastedIdx].toString()
                                                currentPastedIdx++
                                            } else break
                                        }
                                        onCodeChange(newCode)

                                        val nextFocusIdx = i + currentPastedIdx
                                        if (nextFocusIdx < codeLength) {
                                            focusRequesters[nextFocusIdx].requestFocus()
                                        } else {
                                            focusRequesters.last().requestFocus()
                                        }
                                        if (newCode.all { it.isNotEmpty() }) {
                                            onVerify()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (isCodeIncorrect) RedEA else WhiteE0,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                )
                                .background(
                                    Color.White,
                                    androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                )
                                .focusRequester(focusRequesters[i])
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace) {
                                        if (code[i].isEmpty() && i > 0) {
                                            val newCode = code.toMutableList()
                                            newCode[i-1] = ""
                                            onCodeChange(newCode)
                                            focusRequesters[i - 1].requestFocus()
                                            true
                                        } else {
                                            false
                                        }
                                    } else {
                                        false
                                    }
                                },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = if (i == codeLength - 1) ImeAction.Done else ImeAction.Next
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = if (isCodeIncorrect) RedEA else Black21
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                if (isCodeIncorrect) {
                    Text(
                        stringResource(R.string.incorrect_code_and_retry),
                        color = RedEA,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (secondsLeft > 0) {
                    Text(
                        text = stringResource(R.string.retry_after, timerFormatted),
                        color = Green3959,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.resend),
                        color = Green3959,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .clickable(enabled = !isResending && !isVerifying) {
                                onResend()
                                focusRequesters
                                    .firstOrNull()
                                    ?.requestFocus()
                            }
                    )
                }
                Text(
                    text = stringResource(R.string.change_email),
                    color = Green3959,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable(enabled = !isVerifying && !isResending) { onChangeEmail() }
                )
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}