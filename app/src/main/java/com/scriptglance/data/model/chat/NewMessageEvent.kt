package com.scriptglance.data.model.chat

import com.google.gson.annotations.SerializedName

data class NewMessageEvent(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("chat_message_id")
    val chatMessageId: Int,
    @SerializedName("text")
    val text: String,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("is_written_by_moderator")
    val isWrittenByModerator: Boolean,
    @SerializedName("sent_date")
    val sentDate: String,
    @SerializedName("chat_id")
    val chatId: Int?,
    @SerializedName("user_first_name")
    val userFirstName: String,
    @SerializedName("user_last_name")
    val userLastName: String,
    @SerializedName("is_assigned")
    val isAssigned: Boolean?,
    @SerializedName("is_new_chat")
    val isNewChat: Boolean?
)