package com.scriptglance.data.model.chat

import com.google.gson.annotations.SerializedName

data class UnreadCountData(
    @SerializedName("unread_count")
    val unreadCount: Int
)