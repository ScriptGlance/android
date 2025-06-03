package com.scriptglance.data.repository

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
import com.scriptglance.data.model.presentation.UpdatePresentationRequest
import com.scriptglance.data.remote.ApiService
import com.scriptglance.domain.repository.PresentationsRepository
import com.scriptglance.utils.apiFlow
import com.scriptglance.utils.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentationsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : PresentationsRepository {

    override suspend fun getStats(token: String): ApiResult<PresentationStats?> =
        apiFlow { apiService.getStats(bearer(token)) }

    override suspend fun getPresentations(token: String, params: GetPresentationsParams): ApiResult<List<PresentationItem>?> =
        apiFlow { apiService.getPresentations(bearer(token), params.limit, params.offset, params.search, params.sort, params.owner, params.lastChange, params.type) }

    override suspend fun createPresentation(token: String): ApiResult<Presentation?> =
        apiFlow { apiService.createPresentation(bearer(token)) }

    override suspend fun getPresentation(token: String, id: Int): ApiResult<Presentation?> =
        apiFlow { apiService.getPresentation(bearer(token), id) }

    override suspend fun updatePresentationName(token: String, id: Int, name: String): ApiResult<Presentation?> =
        apiFlow { apiService.updatePresentationName(bearer(token), id, UpdatePresentationRequest(name)) }

    override suspend fun deletePresentation(token: String, id: Int): ApiResult<Unit?> =
        safeApiCall { apiService.deletePresentation(bearer(token), id) }

    override suspend fun getParticipants(token: String, presentationId: Int): ApiResult<List<Participant>?> =
        apiFlow { apiService.getParticipants(bearer(token), presentationId) }

    override suspend fun deleteParticipant(token: String, participantId: Int): ApiResult<Unit?> =
        apiFlow { apiService.deleteParticipant(bearer(token), participantId) }

    override suspend fun inviteParticipant(token: String, presentationId: Int): ApiResult<InvitationResponse?> =
        apiFlow { apiService.inviteParticipant(bearer(token), presentationId) }

    override suspend fun acceptInvitation(token: String, invitationToken: String): ApiResult<AcceptInvitationsResponse?> =
        apiFlow { apiService.acceptInvitation(bearer(token), invitationToken) }

    override suspend fun getPresentationStructure(token: String, id: Int): ApiResult<PresentationStructure?> =
        apiFlow { apiService.getPresentationStructure(bearer(token), id) }

    override suspend fun getConfig(token: String): ApiResult<PresentationsConfig?> =
        apiFlow { apiService.getConfig(bearer(token)) }

    override suspend fun getActivePresentation(token: String, presentationId: Int): ApiResult<PresentationActiveData?> =
        apiFlow { apiService.getActivePresentation(bearer(token), presentationId) }

    private fun bearer(token: String) = "Bearer $token"
}
