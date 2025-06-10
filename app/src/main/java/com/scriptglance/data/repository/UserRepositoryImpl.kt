package com.scriptglance.data.repository

import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.profile.User
import com.scriptglance.data.model.profile.UserProfileUpdateData
import com.scriptglance.data.remote.ApiService
import com.scriptglance.domain.repository.UserRepository
import com.scriptglance.utils.apiFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getProfile(token: String): ApiResult<User?> {
        return apiFlow<User>(apiCall = { apiService.getProfile("Bearer $token") })
    }

    override suspend fun updateProfile(token: String, data: UserProfileUpdateData): ApiResult<User?> {
        val parts = mutableListOf<MultipartBody.Part>()
        val fields = mutableMapOf<String, RequestBody>()

        data.firstName?.let {
            fields["first_name"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        data.lastName?.let {
            fields["last_name"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        data.password?.let {
            fields["password"] = it.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        data.avatar?.let { file ->
            if (file.exists()) {
                val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                parts.add(MultipartBody.Part.createFormData("avatar", file.name, reqFile))
            }
        }

        return apiFlow {
            apiService.updateProfile("Bearer $token", fields, parts)
        }
    }
}
