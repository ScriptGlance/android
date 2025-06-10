package com.scriptglance.ui.screen.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.chat.ChatMessage
import com.scriptglance.domain.manager.socket.ChatSocketManager
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val chatSocketManager: ChatSocketManager
) : ViewModel() {

    companion object {
        private const val TAG = "UserChatViewModel"
        private const val MESSAGES_LIMIT = 20
    }

    private val _state = MutableStateFlow(UserChatState())
    val state: StateFlow<UserChatState> = _state.asStateFlow()

    private var token: String? = null
    private var newMessageListener: Emitter.Listener? = null
    private var chatClosedListener: Emitter.Listener? = null

    fun initializeChat() {
        viewModelScope.launch {
            token = authRepository.getToken()
            if (token != null) {
                setupSocket()
                loadMessages(0, false)
                markAsRead()
            }
        }
    }

    private suspend fun setupSocket() {
        try {
            chatSocketManager.connect()
            chatSocketManager.onConnect {
                chatSocketManager.joinUserChat()
            }


            newMessageListener = chatSocketManager.onNewMessage { event ->
                val message = ChatMessage(
                    userId = event.userId,
                    chatMessageId = event.chatMessageId,
                    text = event.text,
                    isWrittenByModerator = event.isWrittenByModerator,
                    sentDate = event.sentDate
                )

                _state.update { currentState ->
                    val messageExists = currentState.messages.any {
                        it.chatMessageId == message.chatMessageId
                    }
                    if (!messageExists) {
                        currentState.copy(
                            messages = currentState.messages + message
                        )
                    } else {
                        currentState
                    }
                }

                markAsRead()
            }

            chatClosedListener = chatSocketManager.onChatClosed {
                Log.d(TAG, "Chat closed by moderator - clearing chat")
                clearChat()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup socket", e)
            _state.update { it.copy(error = true) }
        }
    }

    fun loadMessages(offset: Int, isLoadingMore: Boolean) {
        val currentToken = token ?: return

        viewModelScope.launch {
            if (isLoadingMore) {
                _state.update { it.copy(isLoadingMore = true) }
            } else {
                _state.update { it.copy(isLoading = true) }
            }

            when (val result = chatRepository.getUserActiveChatMessages(
                token = currentToken,
                offset = offset,
                limit = MESSAGES_LIMIT
            )) {
                is ApiResult.Success -> {
                    val newMessages = result.data ?: emptyList()
                    _state.update { currentState ->
                        val allMessages = if (isLoadingMore) {
                            (newMessages + currentState.messages).distinctBy { it.chatMessageId }
                        } else {
                            newMessages
                        }

                        currentState.copy(
                            messages = allMessages.sortedBy { it.sentDate },
                            isLoading = false,
                            isLoadingMore = false,
                            hasMore = newMessages.size >= MESSAGES_LIMIT,
                            error = false
                        )
                    }
                }

                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = true
                        )
                    }
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val currentToken = token ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }

            when (val result = chatRepository.sendUserActiveChatMessage(currentToken, text)) {
                is ApiResult.Success -> {
                    val newMessage = result.data ?: return@launch
                    _state.update { currentState ->
                        val messageExists = currentState.messages.any {
                            it.chatMessageId == newMessage.chatMessageId
                        }
                        if (!messageExists) {
                            currentState.copy(
                                messages = currentState.messages + newMessage,
                                isSending = false,
                                error = false
                            )
                        } else {
                            currentState.copy(isSending = false, error = false)
                        }
                    }
                }

                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isSending = false,
                            error = true
                        )
                    }
                }
            }
        }
    }

    fun markAsRead() {
        val currentToken = token ?: return

        viewModelScope.launch {
            chatRepository.markUserActiveChatAsRead(currentToken)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = false) }
    }

    private fun clearChat() {
        _state.update { currentState ->
            currentState.copy(
                messages = emptyList(),
                isLoading = false,
                isLoadingMore = false,
                isSending = false,
                error = false,
                hasMore = true,
                unreadCount = 0
            )
        }
        Log.d(TAG, "Chat cleared - all messages removed")
    }


    fun refreshChat() {
        _state.update { it.copy(messages = emptyList(), hasMore = true, error = false) }
        loadMessages(0, false)
    }

    override fun onCleared() {
        super.onCleared()
        newMessageListener?.let { chatSocketManager.offNewMessage(it) }
        chatClosedListener?.let { chatSocketManager.offChatClosed(it) }
    }
}