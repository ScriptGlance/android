package com.scriptglance.ui.screen.chat

import com.scriptglance.data.model.chat.ChatMessage

data class UserChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isSending: Boolean = false,
    val error: Boolean = false,
    val hasMore: Boolean = true,
    val unreadCount: Int = 0
)