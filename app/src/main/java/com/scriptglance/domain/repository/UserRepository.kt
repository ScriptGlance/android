package com.scriptglance.domain.repository

import com.scriptglance.data.model.ApiResult
import com.scriptglance.data.model.profile.UserProfile
import com.scriptglance.data.model.profile.UserProfileUpdateData

interface UserRepository {
    suspend fun getProfile(token: String): ApiResult<UserProfile?>
    suspend fun updateProfile(token: String, data: UserProfileUpdateData): ApiResult<UserProfile?>
}