package com.scriptglance.domain.repository

import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.teleprompter.ParticipantVideoCount

interface TeleprompterRepository {

    suspend fun startPresentation(token: String, presentationId: Int): ApiResult<Unit?>

    suspend fun stopPresentation(token: String, presentationId: Int): ApiResult<Unit?>

    suspend fun setRecordingMode(
        token: String,
        presentationId: Int,
        isActive: Boolean
    ): ApiResult<Unit?>

    suspend fun getParticipantsVideosLeft(
        token: String,
        presentationId: Int
    ): ApiResult<List<ParticipantVideoCount>?>

    suspend fun setActiveReader(
        token: String,
        presentationId: Int,
        newReaderId: Int
    ): ApiResult<Unit?>

    suspend fun confirmActiveReader(
        token: String,
        presentationId: Int,
        isFromStartPosition: Boolean
    ): ApiResult<Unit?>
}