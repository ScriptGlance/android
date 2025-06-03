import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.scriptglance.ui.screen.auth.login.LoginViewModel
import com.scriptglance.ui.theme.Gray59
import com.scriptglance.ui.theme.Green4D
import com.scriptglance.ui.theme.WhiteF3

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val activity = context as? Activity
    val mainActivity = context as? MainActivity
    val facebookCallbackManager = mainActivity?.facebookCallbackManager

    val authSocialManager = remember { AuthSocialManager(context, viewModel) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        authSocialManager.handleGoogleResult(result, onLoginSuccess)
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
                    viewModel.socialLogin(context, "facebook", token, onLoginSuccess)
                }
            )
        }
        onDispose { }
    }

    fun validateAndLogin() {
        var hasError = false
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = context.getString(R.string.email_error)
            hasError = true
        } else {
            emailError = null
        }
        if (!hasError) {
            viewModel.login(context, email, password, onLoginSuccess)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteF3)
            .padding(15.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = stringResource(R.string.logo),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )

        AuthCard(
            title = stringResource(R.string.login),
            content = {
                AppTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    placeholder = "Email",
                    isPassword = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    error = emailError,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    onImeAction = { passwordFocusRequester.requestFocus() }
                )

                AppTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    placeholder = stringResource(R.string.password),
                    isPassword = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        validateAndLogin()
                    }
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        stringResource(R.string.forgot_password),
                        fontSize = 15.sp,
                        color = Green4D,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onForgotPassword() }
                    )
                }

                BeigeButton(
                    label = stringResource(R.string.login),
                    onClick = { validateAndLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    enabled = !uiState.loading
                )

                Spacer(Modifier.height(18.dp))
                DividerWithText(text = stringResource(R.string.or))
                Spacer(Modifier.height(16.dp))

                SocialAuthButtons(
                    onGoogleClick = { launchGoogleSignIn() },
                    onFacebookClick = { launchFacebookSignIn() }
                )
            },
            footer = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        stringResource(R.string.no_account),
                        fontSize = 16.sp,
                        color = Gray59
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.registration),
                        fontSize = 16.sp,
                        color = Green4D,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onRegister() }
                    )
                }
            }
        )

        ErrorDialog(
            show = uiState.error != null,
            message = uiState.error ?: "",
            onDismiss = viewModel::dismissError
        )
    }
}
