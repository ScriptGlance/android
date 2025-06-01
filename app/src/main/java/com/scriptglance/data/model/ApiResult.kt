package com.scriptglance.data.model

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T?) : ApiResult<T?>()
    data class Error(val code: Int?, val description: String?) : ApiResult<Nothing>()
}