package com.scriptglance.ui.screen.presentation.teleprompter

import android.Manifest
import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.scriptglance.R
import com.scriptglance.data.model.presentation.Participant
import com.scriptglance.data.model.teleprompter.PartReassignRequest
import com.scriptglance.data.model.teleprompter.PartWithWords
import com.scriptglance.data.model.teleprompter.PresentationActiveJoinedUser
import com.scriptglance.data.model.teleprompter.ReadingConfirmationRequest
import com.scriptglance.ui.common.components.UserAvatar
import com.scriptglance.ui.theme.*
import com.scriptglance.utils.constants.PartReassignReason
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val GRADIENT_HEIGHT_DP = 60
private const val HEADER_SHADOW_ELEVATION_DP = 2
private const val FOOTER_CARD_ELEVATION_DP = 4
private const val VERTICAL_PADDING_DP = 300
private const val HORIZONTAL_PADDING_DP = 16
private const val AVATAR_SIZE_DP = 40
private const val CROWN_SIZE_DP = 15
private const val BUTTON_SIZE_DP = 48
private const val ICON_SIZE_DP = 24
private const val SPACING_BETWEEN_AVATARS_DP = -8
private const val MAX_VISIBLE_AVATARS = 3

private const val DELAY_BEFORE_RECOGNITION_MS = 300L
private const val WORD_SCROLL_DELAY_MS = 150L
private const val SCROLL_RESET_DELAY_MS = 300L
private const val SNACKBAR_TIMEOUT_MS = 3000L
private const val COUNTDOWN_INTERVAL_MS = 1000L

private const val MIN_FONT_SIZE = 0.8f
private const val MAX_FONT_SIZE = 2.8f
private const val DEFAULT_FONT_SIZE = 2.4f
private const val ZOOM_STEP = 0.1f

private const val GRADIENT_START_ALPHA = 0.9f
private const val GRADIENT_END_ALPHA = 0f
private const val HIGHLIGHT_BACKGROUND_ALPHA = 0.8f
private const val SNACKBAR_BACKGROUND_ALPHA = 0.8f

val NEWLINE_WITH_SPACES_REGEX = "\n\\s{4,}".toRegex()

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TeleprompterScreen(
    goBack: () -> Unit,
    viewModel: TeleprompterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    HandleScreenBrightness(
        isPresentationActive = state.currentPresentationStartDate != null,
        activity = activity
    )

    InitializeSpeechRecognition(
        micPermissionState = micPermissionState,
        viewModel = viewModel
    )

    HandleSpeechRecognitionStart(
        state = state,
        viewModel = viewModel,
        micPermissionState = micPermissionState
    )

    when {
        state.isLoading || !state.initialLoadComplete -> {
            LoadingScreen()
        }

        viewModel.speechApiAvailable == false && viewModel.networkSpeechApiError != null -> {
            SpeechApiErrorScreen(
                errorType = viewModel.networkSpeechApiError ?: "unknown",
                onRetry = { viewModel.onCheckSpeechApiAvailability() },
                onBack = goBack
            )
        }

        state.error -> {
            ErrorScreen(onBack = goBack)
        }

        else -> {
            TeleprompterMainContent(
                state = state,
                viewModel = viewModel,
                goBack = goBack,
                micPermissionState = micPermissionState,
                coroutineScope = coroutineScope
            )
        }
    }

    ShowDialogs(state = state, viewModel = viewModel)
}

@Composable
private fun HandleScreenBrightness(
    isPresentationActive: Boolean,
    activity: Activity?
) {
    LaunchedEffect(isPresentationActive) {
        activity?.window?.let { window ->
            if (isPresentationActive) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun InitializeSpeechRecognition(
    micPermissionState: PermissionState,
    viewModel: TeleprompterViewModel
) {
    LaunchedEffect(Unit) {
        if (micPermissionState.status.isGranted) {
            viewModel.onCheckSpeechApiAvailability()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun HandleSpeechRecognitionStart(
    state: TeleprompterState,
    viewModel: TeleprompterViewModel,
    micPermissionState: PermissionState,
) {
    LaunchedEffect(
        state.processedTexts.currentHighlightedPartId,
        state.currentPresentationStartDate
    ) {
        if (micPermissionState.status.isGranted &&
            state.processedTexts.currentHighlightedPartId != null &&
            state.currentPresentationStartDate != null &&
            viewModel.isCurrentUserSpeakerOfCurrentPart() &&
            !state.isRecognizing
        ) {
            delay(DELAY_BEFORE_RECOGNITION_MS)
            viewModel.initAndStartRecognition()
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Green5E)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.teleprompter_loading), color = Gray59)
        }
    }
}

@Composable
private fun ErrorScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = RedEA,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.error_occurred),
                color = RedEA,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = BeigeE5)
            ) {
                Text(text = stringResource(R.string.go_back), color = Gray59)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun TeleprompterMainContent(
    state: TeleprompterState,
    viewModel: TeleprompterViewModel,
    goBack: () -> Unit,
    micPermissionState: PermissionState,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(WhiteEA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ParticipantsHeader(
                presentationName = state.presentation?.name.orEmpty(),
                participants = state.participants,
                activeUsers = state.teleprompterActiveUsers,
                ownerId = state.currentTeleprompterOwnerId,
                isOwner = viewModel.isCurrentUserOwner,
                isPresentationActive = state.currentPresentationStartDate != null,
                onPlayPauseClick = {
                    if (micPermissionState.status.isGranted) {
                        viewModel.handlePlayPauseClick()
                    } else {
                        coroutineScope.launch {
                            micPermissionState.launchPermissionRequest()
                        }
                    }
                },
                onBackClick = goBack
            )

            ConnectionStatusBar(isConnected = state.isSocketConnected)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                TeleprompterTextArea(
                    partsWithWords = state.partsWithWords,
                    processedWordsIndices = state.processedTexts.processedWordsIndices,
                    currentHighlightedPartId = state.processedTexts.currentHighlightedPartId,
                    fontSizeEm = viewModel.fontSizeEm,
                    isPresentationActive = state.currentPresentationStartDate != null
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GRADIENT_HEIGHT_DP.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    WhiteEA,
                                    WhiteEA.copy(alpha = GRADIENT_START_ALPHA),
                                    WhiteEA.copy(alpha = GRADIENT_END_ALPHA)
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GRADIENT_HEIGHT_DP.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    WhiteEA.copy(alpha = GRADIENT_END_ALPHA),
                                    WhiteEA.copy(alpha = GRADIENT_START_ALPHA),
                                    WhiteEA
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
            }

            TeleprompterFooter(
                onZoomIn = { viewModel.zoomIn() },
                onZoomOut = { viewModel.zoomOut() },
                onExit = {
                    viewModel.handleLeave()
                    goBack()
                },
                fontSizeEm = viewModel.fontSizeEm,
                minFontSize = MIN_FONT_SIZE,
                maxFontSize = MAX_FONT_SIZE
            )
        }

        viewModel.snackbarMessage?.let { message ->
            CustomSnackbarHost(
                message = message,
                onDismiss = { },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    }
}

@Composable
private fun ConnectionStatusBar(isConnected: Boolean) {
    AnimatedVisibility(
        visible = !isConnected,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = RedEA.copy(alpha = GRADIENT_START_ALPHA)
        ) {
            Text(
                text = stringResource(R.string.screen_connection_lost),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ShowDialogs(
    state: TeleprompterState,
    viewModel: TeleprompterViewModel
) {
    state.partReassignRequest?.let { request ->
        PartReassignDialog(
            request = request,
            onReassign = { userId ->
                request.part?.let { viewModel.onReassignPartToUser(it.partId, userId) }
            },
            onDismiss = { viewModel.onDismissPartReassignDialog() }
        )
    }

    state.readingConfirmationRequest?.let { request ->
        ReadingConfirmationDialog(
            request = request,
            currentUserId = state.userProfile?.userId ?: -1,
            onConfirm = { isFromStart ->
                viewModel.onConfirmReading(isFromStart)
            },
            onDismiss = { viewModel.onDismissReadingConfirmationDialog() }
        )
    }
}

@Composable
fun TeleprompterTextArea(
    partsWithWords: List<PartWithWords>,
    processedWordsIndices: Map<Int, Int>,
    currentHighlightedPartId: Int?,
    fontSizeEm: Float,
    isPresentationActive: Boolean
) {
    val scrollState = rememberScrollState()
    var lastScrolledWordIndex by remember { mutableStateOf(-1) }
    var containerHeight by remember { mutableStateOf(0) }

    LaunchedEffect(isPresentationActive) {
        if (!isPresentationActive && partsWithWords.isNotEmpty()) {
            delay(SCROLL_RESET_DELAY_MS)
            scrollState.animateScrollTo(0)
            lastScrolledWordIndex = -1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .onGloballyPositioned { coordinates ->
                containerHeight = coordinates.size.height
            }
            .verticalScroll(scrollState, enabled = false)
            .padding(
                vertical = VERTICAL_PADDING_DP.dp,
                horizontal = HORIZONTAL_PADDING_DP.dp
            )
    ) {
        Column {
            partsWithWords.forEach { part ->
                PartTitleItem(
                    title = part.partName,
                    isActive = part.partId == currentHighlightedPartId,
                    fontSizeEm = fontSizeEm,
                    assignedParticipant = part.assignedParticipant
                )
                PartTextItem(
                    words = part.wordsArray.toList(),
                    isActivePart = part.partId == currentHighlightedPartId,
                    currentWordIndex = processedWordsIndices[part.partId] ?: -1,
                    fontSizeEm = fontSizeEm,
                    lastScrolledWordIndex = lastScrolledWordIndex,
                    containerHeight = containerHeight,
                    isPresentationActive = isPresentationActive,
                    onWordScrolled = { wordIndex ->
                        lastScrolledWordIndex = wordIndex
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PartTextItem(
    words: List<String>,
    isActivePart: Boolean,
    currentWordIndex: Int,
    fontSizeEm: Float,
    lastScrolledWordIndex: Int,
    containerHeight: Int,
    isPresentationActive: Boolean,
    onWordScrolled: (Int) -> Unit
) {
    rememberCoroutineScope()
    LocalDensity.current

    FlowRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        words.forEachIndexed { wordIndex, word ->
            when {
                wordIndex == 0 || word.matches(NEWLINE_WITH_SPACES_REGEX) -> {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                }

                word.trim().isEmpty() -> {}
                else -> {
                    val bringIntoViewRequester = remember { BringIntoViewRequester() }
                    var wordHeight by remember { mutableStateOf(0f) }

                    WordItem(
                        word = word,
                        isCurrentWord = isActivePart && wordIndex == currentWordIndex,
                        isPastWord = isActivePart && wordIndex < currentWordIndex,
                        isActivePartWord = isActivePart,
                        fontSizeEm = fontSizeEm,
                        modifier = Modifier
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onGloballyPositioned { coordinates ->
                                wordHeight = coordinates.size.height.toFloat()
                            }
                    )

                    LaunchedEffect(
                        isActivePart,
                        currentWordIndex,
                        wordHeight,
                        containerHeight,
                        isPresentationActive
                    ) {
                        if (isPresentationActive &&
                            isActivePart &&
                            wordIndex == currentWordIndex &&
                            wordIndex != lastScrolledWordIndex &&
                            wordHeight > 0 &&
                            containerHeight > 0
                        ) {
                            delay(WORD_SCROLL_DELAY_MS)

                            val containerHeightFloat = containerHeight.toFloat()
                            val centerOffset = containerHeightFloat / 2
                            val halfWordHeight = wordHeight / 2

                            val rect = Rect(
                                offset = Offset(0f, -centerOffset + halfWordHeight),
                                size = Size(0f, containerHeightFloat - wordHeight)
                            )

                            bringIntoViewRequester.bringIntoView(rect)
                            onWordScrolled(wordIndex)
                        }
                    }

                    LaunchedEffect(isPresentationActive) {
                        if (!isPresentationActive) {
                            onWordScrolled(-1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartTitleItem(
    title: String,
    isActive: Boolean,
    fontSizeEm: Float,
    assignedParticipant: Participant?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp * fontSizeEm,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Green5E else Color.Gray,
            modifier = Modifier.fillMaxWidth()
        )

        assignedParticipant?.let { participant ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 5.dp)
            ) {
                UserAvatar(
                    selectedLocalUri = null,
                    avatarUrl = participant.user.avatar,
                    firstName = participant.user.firstName,
                    lastName = participant.user.lastName,
                    size = 32.dp,
                    defaultBackgroundColor = Color(participant.color.toColorInt()),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "${participant.user.firstName} ${participant.user.lastName}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp * fontSizeEm,
                    color = Gray59,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun WordItem(
    word: String,
    isCurrentWord: Boolean,
    isPastWord: Boolean,
    isActivePartWord: Boolean,
    fontSizeEm: Float,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$word ",
        fontSize = 18.sp * fontSizeEm,
        color = when {
            isCurrentWord -> Green5E
            isPastWord && isActivePartWord -> Gray59
            isActivePartWord -> Color.Black
            else -> Color.Gray
        },
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .background(
                color = if (isCurrentWord) BeigeE5.copy(alpha = HIGHLIGHT_BACKGROUND_ALPHA) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 2.dp, vertical = 1.dp)
    )
}

@Composable
fun SpeechApiErrorScreen(
    errorType: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteEA)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = RedEA,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        val (titleRes, descriptionRes) = when (errorType) {
            "network" -> R.string.speech_api_connection_failed to R.string.speech_api_network_description
            "not-allowed" -> R.string.speech_api_permission_denied to R.string.speech_api_permission_description
            else -> R.string.speech_api_not_available to R.string.speech_api_not_available_description
        }

        Text(
            text = stringResource(titleRes),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(descriptionRes),
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Green5E
                )
            ) {
                Text(stringResource(R.string.go_back))
            }

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green5E
                )
            ) {
                Text(stringResource(R.string.try_again))
            }
        }
    }
}

@Composable
fun ParticipantsHeader(
    presentationName: String,
    participants: List<Participant>,
    activeUsers: List<PresentationActiveJoinedUser>,
    ownerId: Int?,
    isOwner: Boolean,
    isPresentationActive: Boolean,
    onPlayPauseClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = HEADER_SHADOW_ELEVATION_DP.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HORIZONTAL_PADDING_DP.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(AVATAR_SIZE_DP.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Green5E
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = presentationName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.Black,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            if (isOwner) {
                IconButton(
                    onClick = onPlayPauseClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Green5E.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .size(AVATAR_SIZE_DP.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPresentationActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = stringResource(if (isPresentationActive) R.string.stop else R.string.start),
                        tint = Green5E,
                        modifier = Modifier.size(ICON_SIZE_DP.dp)
                    )
                }

                Spacer(modifier = Modifier.width(HORIZONTAL_PADDING_DP.dp))
            }

            ActiveParticipants(
                participants = participants,
                activeUsers = activeUsers,
                ownerId = ownerId
            )
        }
    }
}

@Composable
fun ActiveParticipants(
    participants: List<Participant>,
    activeUsers: List<PresentationActiveJoinedUser>,
    ownerId: Int?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SPACING_BETWEEN_AVATARS_DP.dp)
    ) {
        participants
            .filter { participant ->
                activeUsers.any { it.userId == participant.user.userId }
            }
            .sortedWith(
                compareByDescending<Participant> { it.user.userId == ownerId }
                    .thenBy { it.user.firstName }
            )
            .take(MAX_VISIBLE_AVATARS)
            .forEach { participant ->
                val isOwner = participant.user.userId == ownerId
                Box {
                    UserAvatar(
                        selectedLocalUri = null,
                        avatarUrl = participant.user.avatar,
                        firstName = participant.user.firstName,
                        lastName = participant.user.lastName,
                        size = AVATAR_SIZE_DP.dp,
                        defaultBackgroundColor = Color(participant.color.toColorInt())
                    )

                    if (isOwner) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = stringResource(R.string.owner),
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 5.dp, y = (-5).dp)
                                .background(YellowE3, CircleShape)
                                .padding(2.dp)
                                .size(CROWN_SIZE_DP.dp)
                        )
                    }
                }
            }

        val extraCount = participants.count { participant ->
            activeUsers.any { it.userId == participant.user.userId }
        } - MAX_VISIBLE_AVATARS

        if (extraCount > 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(AVATAR_SIZE_DP.dp)
                    .background(Gray59, CircleShape)
            ) {
                Text(
                    text = "+$extraCount",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TeleprompterFooter(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onExit: () -> Unit,
    fontSizeEm: Float,
    minFontSize: Float,
    maxFontSize: Float
) {
    Card(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(FOOTER_CARD_ELEVATION_DP.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HORIZONTAL_PADDING_DP.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FooterButton(
                icon = R.drawable.ic_zoom_out,
                contentDescription = stringResource(R.string.zoom_out),
                onClick = onZoomOut,
                enabled = fontSizeEm > minFontSize,
                modifier = Modifier.weight(1f)
            )

            FooterButton(
                icon = R.drawable.ic_zoom_in,
                contentDescription = stringResource(R.string.zoom_in),
                onClick = onZoomIn,
                enabled = fontSizeEm < maxFontSize,
                modifier = Modifier.weight(1f)
            )

            FooterButton(
                icon = R.drawable.ic_exit,
                contentDescription = stringResource(R.string.exit),
                onClick = onExit,
                isExitButton = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FooterButton(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isExitButton: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isExitButton) RedEA.copy(alpha = 0.1f) else BeigeE5
    val iconTint = when {
        isExitButton -> RedEA
        enabled -> Green5E
        else -> Gray59.copy(alpha = 0.5f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(BUTTON_SIZE_DP.dp)
                .background(
                    color = backgroundColor,
                    shape = CircleShape
                )
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(ICON_SIZE_DP.dp)
            )
        }
    }
}

@Composable
fun CustomSnackbarHost(
    modifier: Modifier = Modifier,
    message: String,
    onDismiss: () -> Unit,
    timeout: Long = SNACKBAR_TIMEOUT_MS,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = SNACKBAR_BACKGROUND_ALPHA)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING_DP.dp)
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .fillMaxWidth()
            )
        }
    }

    LaunchedEffect(message) {
        delay(timeout)
        onDismiss()
    }
}

@Composable
fun PartReassignDialog(
    request: PartReassignRequest,
    onReassign: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedUserId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(request.availableParticipants) {
        val isSelectedParticipantAvailable =
            request.availableParticipants.any { it.user.userId == selectedUserId }
        if (!isSelectedParticipantAvailable) {
            selectedUserId = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.presentation_paused),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                val reasonText = when (request.reason) {
                    PartReassignReason.MISSING_ASSIGNEE ->
                        stringResource(
                            R.string.participant_missing,
                            request.missingParticipant?.fullName.orEmpty()
                        )

                    PartReassignReason.ASSIGNEE_NOT_RESPONDING ->
                        stringResource(
                            R.string.participant_not_responding,
                            request.missingParticipant?.fullName.orEmpty()
                        )
                }
                Text(
                    text = "$reasonText ${
                        stringResource(
                            R.string.choose_another_reader,
                            request.part?.partName.orEmpty()
                        )
                    }",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(request.availableParticipants) { participant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedUserId = participant.user.userId }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedUserId == participant.user.userId,
                                onClick = { selectedUserId = participant.user.userId }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            UserAvatar(
                                selectedLocalUri = null,
                                avatarUrl = participant.user.avatar,
                                firstName = participant.user.firstName,
                                lastName = participant.user.lastName,
                                size = 32.dp,
                                defaultBackgroundColor = Color(participant.color.toColorInt())
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${participant.user.firstName} ${participant.user.lastName}",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedUserId?.let { onReassign(it) }
                },
                enabled = selectedUserId != null,
                colors = ButtonDefaults.buttonColors(containerColor = Green5E)
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}

@Composable
fun ReadingConfirmationDialog(
    currentUserId: Int,
    request: ReadingConfirmationRequest,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(request.timeToConfirmSeconds) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(COUNTDOWN_INTERVAL_MS)
            timeLeft--
        }
        if (timeLeft == 0) {
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.reading_confirmation_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                val titleStartText = if (request.part?.assigneeUserId == currentUserId) {
                    stringResource(
                        R.string.reading_confirmation_paused,
                        request.part.partName.orEmpty()
                    )
                } else {
                    stringResource(
                        R.string.reading_confirmation_assigned,
                        request.part?.partName.orEmpty()
                    )
                }
                Text(
                    text = titleStartText,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = stringResource(R.string.confirmation_time_left, timeLeft),
                    color = if (timeLeft <= 5) Color.Red else Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Column {
                Button(
                    onClick = { onConfirm(true) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Green5E)
                ) {
                    Text(stringResource(R.string.start_from_beginning))
                }

                if (request.canContinueFromLastPosition) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onConfirm(false) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.continue_reading))
                    }
                }
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}