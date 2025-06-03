package com.scriptglance.ui.screen.auth.registration

import AuthCard
import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scriptglance.R
import com.scriptglance.domain.manager.auth.AuthSocialManager
import com.scriptglance.ui.activity.MainActivity
import com.scriptglance.ui.common.components.AppTextField
import com.scriptglance.ui.common.components.BeigeButton
import com.scriptglance.ui.common.components.ErrorDialog
import com.scriptglance.ui.common.components.SocialAuthButtons
import com.scriptglance.ui.screen.auth.components.DividerWithText
import com.scriptglance.ui.screen.auth.registration.components.EmailConfirmationDialog
import com.scriptglance.ui.theme.Gray59
import com.scriptglance.ui.theme.Green4D
import com.scriptglance.utils.constants.EMAIL_CONFIRMATION_TIME_SECONDS
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onLogin: () -> Unit,
) {
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var codeList by remember { mutableStateOf(List(6) { "" }) }
    var isCodeIncorrect by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(0) }
    var isResending by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val authDataStore = viewModel.authDataStore

    fun updateSecondsLeft(force: Boolean = false) {
        scope.launch {
            val s = authDataStore.getSecondsLeft(email, EMAIL_CONFIRMATION_TIME_SECONDS)
            if (secondsLeft != s || force) {
                secondsLeft = s
            }
        }
    }

    LaunchedEffect(email) {
        updateSecondsLeft(force = true)
    }

    LaunchedEffect(showConfirmationDialog, secondsLeft) {
        if (showConfirmationDialog && secondsLeft > 0) {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
        }
    }
    val isResendDisabled = secondsLeft > 0

    val activity = context as? Activity
    val mainActivity = context as? MainActivity
    val facebookCallbackManager = mainActivity?.facebookCallbackManager
    val authSocialManager = remember { AuthSocialManager(context, viewModel) }
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        authSocialManager.handleGoogleResult(result, onRegisterSuccess)
    }

    fun launchGoogleSignIn() {
        authSocialManager.launchGoogleSignIn(googleLauncher)
    }

    fun launchFacebookSignIn() {
        activity?.let { authSocialManager.launchFacebookSignIn(it) }
    }
    DisposableEffect(facebookCallbackManager) {
        facebookCallbackManager?.let {
            authSocialManager.registerFacebookCallback(
                it,
                onLoginSuccess = { token ->
                    viewModel.socialLogin(context, "facebook", token, onRegisterSuccess)
                }
            )
        }
        onDispose { }
    }

    fun sendVerificationCodeWithDataStore() {
        isResending = true
        isCodeIncorrect = false
        codeList = List(6) { "" }
        scope.launch {
            val s = authDataStore.getSecondsLeft(email, EMAIL_CONFIRMATION_TIME_SECONDS)
            if (s > 0) {
                secondsLeft = s
                showConfirmationDialog = true
                isResending = false
                return@launch
            }

            viewModel.sendVerificationEmail(
                context = context,
                email = email,
                onSuccess = {
                    scope.launch {
                        authDataStore.saveConfirmationInfo(email)
                    }
                    showConfirmationDialog = true
                    secondsLeft = EMAIL_CONFIRMATION_TIME_SECONDS
                },
                onError = { msg ->
                    emailError = msg
                },
                onFinally = {
                    isResending = false
                }
            )
        }
    }

    fun resendVerificationCodeWithDataStore() {
        scope.launch {
            val s = authDataStore.getSecondsLeft(email, EMAIL_CONFIRMATION_TIME_SECONDS)
            if (s > 0) {
                secondsLeft = s
                return@launch
            }
            sendVerificationCodeWithDataStore()
        }
    }

    fun registerWithCode() {
        isVerifying = true
        isCodeIncorrect = false
        viewModel.verifyEmailCode(
            context = context,
            email = email,
            code = codeList.joinToString(separator = ""),
            onSuccess = {
                viewModel.register(
                    context,
                    firstName,
                    lastName,
                    email,
                    password,
                    onSuccess = {
                        scope.launch { authDataStore.clearConfirmationInfo() }
                        showConfirmationDialog = false
                        onRegisterSuccess()
                    }
                )
            },
            onError = { msg ->
                isCodeIncorrect = true
            },
            onFinally = {
                isVerifying = false
            }
        )
    }

    fun validateAndSendCode() {
        var hasError = false
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = context.getString(R.string.email_error)
            hasError = true
        } else {
            emailError = null
        }
        if (password.length < 4) {
            passwordError = context.getString(R.string.password_too_short)
            hasError = true
        } else if (password != repeatPassword) {
            passwordError = context.getString(R.string.password_mismatch)
            hasError = true
        } else {
            passwordError = null
        }
        if (!hasError) {
            if (secondsLeft > 0) {
                showConfirmationDialog = true
                return
            }
            sendVerificationCodeWithDataStore()
        }
    }

    AuthCard(
        title = stringResource(R.string.registration),
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.already_registered),
                    fontSize = 16.sp,
                    color = Gray59
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(R.string.login),
                    fontSize = 16.sp,
                    color = Green4D,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onLogin() }
                )
            }
        },
        content = {
            AppTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = stringResource(R.string.name),
                isPassword = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )
            AppTextField(
                value = lastName,
                onValueChange = { lastName = it },
                placeholder = stringResource(R.string.surname),
                isPassword = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )
            AppTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                placeholder = stringResource(R.string.email),
                isPassword = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                error = emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )
            AppTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = stringResource(R.string.password),
                isPassword = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                error = passwordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )
            AppTextField(
                value = repeatPassword,
                onValueChange = { repeatPassword = it },
                placeholder = stringResource(R.string.repeat_password),
                isPassword = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = { validateAndSendCode() }
            )

            BeigeButton(
                label = stringResource(R.string.extract_account),
                onClick = { validateAndSendCode() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                enabled = !uiState.loading && !isResending
            )

            Spacer(Modifier.height(18.dp))
            DividerWithText(text = stringResource(R.string.or))
            Spacer(Modifier.height(16.dp))
            SocialAuthButtons(
                onGoogleClick = { launchGoogleSignIn() },
                onFacebookClick = { launchFacebookSignIn() }
            )
        }
    )

    EmailConfirmationDialog(
        email = email,
        isOpen = showConfirmationDialog,
        code = codeList,
        onCodeChange = {
            codeList = it
            isCodeIncorrect = false
        },
        isCodeIncorrect = isCodeIncorrect,
        secondsLeft = secondsLeft,
        isResending = isResending,
        isVerifying = isVerifying,
        onResend = {
            if (!isResendDisabled) {
                resendVerificationCodeWithDataStore()
            }
        },
        onVerify = { registerWithCode() },
        onChangeEmail = {
            showConfirmationDialog = false
            codeList = List(6) { "" }
            isCodeIncorrect = false
            secondsLeft = 0
        },
        onDismiss = {
            showConfirmationDialog = false
            codeList = List(6) { "" }
            isCodeIncorrect = false
        }
    )

    ErrorDialog(
        show = uiState.error != null,
        message = uiState.error ?: "",
        onDismiss = viewModel::dismissError
    )
}
