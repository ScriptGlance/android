package com.scriptglance.domain.repository

import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.chat.ChatMessage
import com.scriptglance.data.model.chat.UnreadCountData

interface ChatRepository {
    suspend fun getUserActiveChatMessages(
        token: String,
        offset: Int,
        limit: Int
    ): ApiResult<List<ChatMessage>?>
    
    suspend fun sendUserActiveChatMessage(
        token: String,
        text: String
    ): ApiResult<ChatMessage?>
    
    suspend fun getUserActiveUnreadCount(token: String): ApiResult<UnreadCountData?>
    
    suspend fun markUserActiveChatAsRead(token: String): ApiResult<Unit?>
}