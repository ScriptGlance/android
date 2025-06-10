package com.scriptglance.domain.repository

import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.profile.User
import com.scriptglance.data.model.profile.UserProfileUpdateData

interface UserRepository {
    suspend fun getProfile(token: String): ApiResult<User?>
    suspend fun updateProfile(token: String, data: UserProfileUpdateData): ApiResult<User?>
}