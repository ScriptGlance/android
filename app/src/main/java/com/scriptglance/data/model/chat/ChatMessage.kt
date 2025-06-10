package com.scriptglance.data.model.chat

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("chat_message_id")
    val chatMessageId: Int,
    @SerializedName("text")
    val text: String,
    @SerializedName("is_written_by_moderator")
    val isWrittenByModerator: Boolean,
    @SerializedName("sent_date")
    val sentDate: String
)