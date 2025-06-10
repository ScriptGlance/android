package com.scriptglance.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scriptglance.R
import com.scriptglance.data.model.chat.ChatMessage
import com.scriptglance.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    var messageText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.initializeChat()
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty() && !state.isLoadingMore) {
            delay(100)
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    LaunchedEffect(listState.canScrollBackward) {
        snapshotFlow {
            listState.firstVisibleItemIndex == 0 && !state.isLoadingMore && state.hasMore
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && state.messages.isNotEmpty()) {
                viewModel.loadMessages(state.messages.size, true)
            }
        }
    }

    if (state.error) {
        LaunchedEffect(state.error) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(4000)
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.ime)
            .background(Color(0xFFFBFAF6))
    ) {
        ChatHeader(onNavigateBack = onNavigateBack)

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = false
            ) {
                if (state.isLoadingMore) {
                    item {
                        LoadingIndicator()
                    }
                }

                if (state.isLoading && state.messages.isEmpty()) {
                    item {
                        InitialLoadingState()
                    }
                } else if (state.messages.isEmpty() && !state.isLoading) {
                    item {
                        EmptyState()
                    }
                } else {
                    items(
                        items = groupMessagesByDate(state.messages),
                        key = { item ->
                            when (item) {
                                is ChatItem.DateSeparator -> "date_${item.date}"
                                is ChatItem.Message -> "msg_${item.message.chatMessageId}"
                            }
                        }
                    ) { item ->
                        when (item) {
                            is ChatItem.DateSeparator -> DateSeparator(date = item.date)
                            is ChatItem.Message -> MessageItem(message = item.message)
                        }
                    }
                }
            }

            if (state.error) {
                ErrorOverlay(onDismiss = viewModel::clearError)
            }
        }

        ChatInput(
            message = messageText,
            onMessageChange = { messageText = it },
            onSendMessage = {
                if (messageText.isNotBlank() && !state.isSending) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.sendMessage(messageText.trim())
                    messageText = ""
                    keyboardController?.hide()
                }
            },
            isSending = state.isSending,
            enabled = !state.error
        )
    }
}

@Composable
private fun ChatHeader(onNavigateBack: () -> Unit) {
    Surface(
        color = Green5E,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White
                )
            }

            Text(
                text = stringResource(R.string.support),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Green5E,
                strokeWidth = 2.dp
            )
            Text(
                text = stringResource(R.string.loading_more),
                color = Gray59,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun InitialLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = Green5E,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.loading_chat),
                color = Gray59,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.no_messages_yet),
                color = Gray59,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorOverlay(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.connection_error),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.check_connection),
                    fontSize = 14.sp,
                    color = Gray59,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Green5E)
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

@Composable
private fun DateSeparator(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.9f),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp
        ) {
            Text(
                text = date,
                fontSize = 12.sp,
                color = Color(0xFF596269),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun MessageItem(message: ChatMessage) {
    val isFromUser = !message.isWrittenByModerator
    val time = formatTime(message.sentDate)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isFromUser) 48.dp else 0.dp,
                end = if (isFromUser) 0.dp else 48.dp
            ),
        contentAlignment = if (isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isFromUser) 18.dp else 4.dp,
                bottomEnd = if (isFromUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromUser) Green5E else Color(0xFFD3DDD7)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isFromUser) Color.White else Color(0xFF252c1c),
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = if (isFromUser) Color(0xFFd5ded2) else Color(0xFF8e9488),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun ChatInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isSending: Boolean,
    enabled: Boolean = true
) {
    Surface(
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .alpha(if (enabled) 1f else 0.6f),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { if (it.length <= 1000) onMessageChange(it) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.enter_message),
                        color = Gray59
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { if (message.isNotBlank() && !isSending && enabled) onSendMessage() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green5E,
                    unfocusedBorderColor = Color(0xFFE1E8EE)
                ),
                maxLines = 4,
                enabled = enabled
            )

            Spacer(modifier = Modifier.width(12.dp))

            FloatingActionButton(
                onClick = onSendMessage,
                modifier = Modifier.size(56.dp),
                containerColor = if (message.isNotBlank() && !isSending && enabled)
                    Green5E else Color(0xFFBDBDBD),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                ),
                shape = CircleShape
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.send),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private sealed class ChatItem {
    data class DateSeparator(val date: String) : ChatItem()
    data class Message(val message: ChatMessage) : ChatItem()
}

private fun groupMessagesByDate(messages: List<ChatMessage>): List<ChatItem> {
    val result = mutableListOf<ChatItem>()
    var lastDate: String? = null

    messages.forEach { message ->
        val messageDate = formatDate(message.sentDate)
        if (messageDate != lastDate) {
            result.add(ChatItem.DateSeparator(messageDate))
            lastDate = messageDate
        }
        result.add(ChatItem.Message(message))
    }

    return result
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale("uk", "UA"))
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}

private fun formatTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        ""
    }
}