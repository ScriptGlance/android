package com.scriptglance.data.model.profile

import com.google.gson.annotations.SerializedName
import java.io.File

data class UserProfileUpdateData(
    val avatar: File? = null,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    val password: String? = null
)