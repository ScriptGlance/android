package com.scriptglance.domain.repository

import com.scriptglance.data.model.ApiResult
import com.scriptglance.data.model.presentation.AcceptInvitationsResponse
import com.scriptglance.data.model.presentation.GetPresentationsParams
import com.scriptglance.data.model.presentation.InvitationResponse
import com.scriptglance.data.model.presentation.Participant
import com.scriptglance.data.model.presentation.Presentation
import com.scriptglance.data.model.presentation.PresentationActiveData
import com.scriptglance.data.model.presentation.PresentationItem
import com.scriptglance.data.model.presentation.PresentationStats
import com.scriptglance.data.model.presentation.PresentationStructure
import com.scriptglance.data.model.presentation.PresentationsConfig

interface PresentationsRepository {

    suspend fun getStats(token: String): ApiResult<PresentationStats?>

    suspend fun getPresentations(
        token: String,
        params: GetPresentationsParams
    ): ApiResult<List<PresentationItem>?>

    suspend fun createPresentation(token: String): ApiResult<Presentation?>

    suspend fun getPresentation(token: String, id: Int): ApiResult<Presentation?>

    suspend fun updatePresentationName(
        token: String,
        id: Int,
        name: String
    ): ApiResult<Presentation?>

    suspend fun deletePresentation(token: String, id: Int): ApiResult<Unit?>

    suspend fun getParticipants(token: String, presentationId: Int): ApiResult<List<Participant>?>

    suspend fun deleteParticipant(token: String, participantId: Int): ApiResult<Unit?>

    suspend fun inviteParticipant(
        token: String,
        presentationId: Int
    ): ApiResult<InvitationResponse?>

    suspend fun acceptInvitation(
        token: String,
        invitationToken: String
    ): ApiResult<AcceptInvitationsResponse?>

    suspend fun getPresentationStructure(token: String, id: Int): ApiResult<PresentationStructure?>

    suspend fun getConfig(token: String): ApiResult<PresentationsConfig?>

    suspend fun getActivePresentation(
        token: String,
        presentationId: Int
    ): ApiResult<PresentationActiveData?>
}
