package com.scriptglance.data.repository

import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.chat.ChatMessage
import com.scriptglance.data.model.chat.SendUserActiveChatMessageRequest
import com.scriptglance.data.model.chat.UnreadCountData
import com.scriptglance.data.remote.ApiService
import com.scriptglance.domain.repository.ChatRepository
import com.scriptglance.utils.apiFlow
import com.scriptglance.utils.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val api: ApiService
) : ChatRepository {

    private fun bearer(token: String) = "Bearer $token"

    override suspend fun getUserActiveChatMessages(
        token: String,
        offset: Int,
        limit: Int
    ): ApiResult<List<ChatMessage>?> = apiFlow {
        api.getUserActiveChatMessages(bearer(token), offset, limit)
    }

    override suspend fun sendUserActiveChatMessage(
        token: String,
        text: String
    ): ApiResult<ChatMessage?> = apiFlow {
        api.sendUserActiveChatMessage(bearer(token), SendUserActiveChatMessageRequest(text))
    }

    override suspend fun getUserActiveUnreadCount(token: String): ApiResult<UnreadCountData?> =
        apiFlow {
            api.getUserActiveUnreadCount(bearer(token))
        }

    override suspend fun markUserActiveChatAsRead(token: String): ApiResult<Unit?> = safeApiCall {
        api.markUserActiveChatAsRead(bearer(token))
    }
}