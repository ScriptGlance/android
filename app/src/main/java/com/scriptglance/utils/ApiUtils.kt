package com.scriptglance.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.scriptglance.data.model.api.ApiResponse
import com.scriptglance.data.model.api.ApiResult
import retrofit2.Response
import java.io.IOException


fun <T> handleApiResponse(response: ApiResponse<T?>): ApiResult<T?> {
    return if (response.error) {
        ApiResult.Error(response.errorCode, response.description)
    } else {
        ApiResult.Success(response.data)
    }
}

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T?> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            ApiResult.Success(body)
        } else {
            val errorBody = response.errorBody()?.string()
            val (code, description) = parseErrorCode(errorBody)
            ApiResult.Error(code, description)
        }
    } catch (e: retrofit2.HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val (code, description) = parseErrorCode(errorBody)
        ApiResult.Error(code ?: e.code(), description ?: "Http error: ${e.code()}")
    } catch (_: IOException) {
        ApiResult.Error(-1, "No internet connection")
    } catch (e: Exception) {
        ApiResult.Error(-1, e.localizedMessage)
    }
}

fun parseErrorCode(json: String?): Pair<Int?, String?> {
    if (json.isNullOrEmpty()) return Pair(null, null)
    return try {
        val apiResponse = Gson().fromJson(json, ApiResponse::class.java)
        Pair(apiResponse.errorCode, apiResponse.description)
    } catch (e: JsonSyntaxException) {
        Pair(null, null)
    }
}

suspend inline fun <reified T> apiFlow(
    noinline onSuccess: suspend (T?) -> Unit = {},
    noinline apiCall: suspend () -> Response<ApiResponse<T?>>,
): ApiResult<T?> {
    val rawResult = safeApiCall(apiCall)
    val parsed: ApiResult<T?> = when (rawResult) {
        is ApiResult.Success -> {
            val response = rawResult.data
            if (response != null) {
                handleApiResponse(response)
            } else {
                ApiResult.Error(-1, "No response received")
            }
        }
        is ApiResult.Error -> ApiResult.Error(rawResult.code, rawResult.description)
    }
    if (parsed is ApiResult.Success) {
        onSuccess(parsed.data)
    }
    return parsed
}
