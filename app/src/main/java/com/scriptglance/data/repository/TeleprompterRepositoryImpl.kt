package com.scriptglance.data.repository

import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.teleprompter.ConfirmActiveReaderRequest
import com.scriptglance.data.model.teleprompter.ParticipantVideoCount
import com.scriptglance.data.model.teleprompter.SetActiveReaderRequest
import com.scriptglance.data.model.teleprompter.SetRecordingModeRequest
import com.scriptglance.data.remote.ApiService
import com.scriptglance.domain.repository.TeleprompterRepository
import com.scriptglance.utils.apiFlow
import com.scriptglance.utils.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeleprompterRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : TeleprompterRepository {

    override suspend fun startPresentation(token: String, presentationId: Int): ApiResult<Unit?> =
        safeApiCall { apiService.startPresentation(bearer(token), presentationId) }

    override suspend fun stopPresentation(token: String, presentationId: Int): ApiResult<Unit?> =
        safeApiCall { apiService.stopPresentation(bearer(token), presentationId) }

    override suspend fun setRecordingMode(
        token: String,
        presentationId: Int,
        isActive: Boolean
    ): ApiResult<Unit?> = safeApiCall {
        apiService.setRecordingMode(
            bearer(token),
            presentationId,
            SetRecordingModeRequest(isActive)
        )
    }

    override suspend fun getParticipantsVideosLeft(
        token: String,
        presentationId: Int
    ): ApiResult<List<ParticipantVideoCount>?> = apiFlow {
        apiService.getParticipantsVideosLeft(bearer(token), presentationId)
    }

    override suspend fun setActiveReader(
        token: String,
        presentationId: Int,
        newReaderId: Int
    ): ApiResult<Unit?> = safeApiCall {
        apiService.setActiveReader(
            bearer(token),
            presentationId,
            SetActiveReaderRequest(newReaderId)
        )
    }

    override suspend fun confirmActiveReader(
        token: String,
        presentationId: Int,
        isFromStartPosition: Boolean
    ): ApiResult<Unit?> = safeApiCall {
        apiService.confirmActiveReader(
            bearer(token),
            presentationId,
            ConfirmActiveReaderRequest(isFromStartPosition)
        )
    }

    private fun bearer(token: String) = "Bearer $token"
}