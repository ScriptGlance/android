package com.scriptglance.data.model.api

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val data: T? = null,
    val error: Boolean = false,
    @SerializedName("error_code")
    val errorCode: Int? = null,
    val description: String? = null
)