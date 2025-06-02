package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName

data class PresentationOwner(
    val avatar: String?,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("has_premium")
    val hasPremium: Boolean,
    val email: String,
    @SerializedName("registered_at")
    val registeredAt: String,
    @SerializedName("is_temporary_password")
    val isTemporaryPassword: Boolean
)