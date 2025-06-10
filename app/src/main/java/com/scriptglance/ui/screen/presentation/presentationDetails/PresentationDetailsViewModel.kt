package com.scriptglance.ui.screen.presentation.presentationDetails

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptglance.R
import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.presentation.PresentationEvent
import com.scriptglance.data.model.presentation.*
import com.scriptglance.domain.manager.socket.PresentationSocketManager
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.domain.repository.PresentationsRepository
import com.scriptglance.domain.repository.UserRepository
import com.scriptglance.utils.constants.API_BASE_URL
import com.scriptglance.utils.constants.PresentationEventType
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class PresentationDetailsViewModel @Inject constructor(
    private val presentationsRepository: PresentationsRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val presentationSocketManager: PresentationSocketManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(PresentationDetailsState())
    val state: StateFlow<PresentationDetailsState> = _state.asStateFlow()

    private var presentationId: Int = 0
    private var token: String? = null
    private var presentationEventListener: Emitter.Listener? = null
    private var isInitialized = false

    init {
        presentationId = savedStateHandle.get<Int>("presentationId") ?: 0
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            token = authRepository.getToken()
            if (!token.isNullOrEmpty()) {
                fetchProfile()
                fetchPresentationData()
                setupSocketListener()
                isInitialized = true
            } else {
                _state.update { it.copy(isLoading = false, error = true) }
            }
        }
    }

    fun onScreenResumed() {
        viewModelScope.launch {
            if (isInitialized) {
                _state.update { it.copy(isLoading = true) }
                fetchPresentationData()
                reconnectSocket()
            }
        }
    }

    private suspend fun reconnectSocket() {
        try {
            presentationEventListener?.let {
                presentationSocketManager.offPresentationEvent(it)
            }
            presentationSocketManager.disconnect()

            kotlinx.coroutines.delay(200)

            setupSocketListener()
        } catch (e: Exception) {
            android.util.Log.e("PresentationDetailsVM", "Error reconnecting socket: ${e.message}")
        }
    }

    private suspend fun setupSocketListener() {
        if (presentationSocketManager.isConnected().not()) {
            presentationSocketManager.onConnect {
                subscribeToPresentation()
            }
            presentationSocketManager.connect()
        } else {
            subscribeToPresentation()
        }
    }

    private fun subscribeToPresentation() {
        presentationEventListener = presentationSocketManager.onPresentationEvent { event ->
            handlePresentationEvent(event)
        }
        presentationSocketManager.subscribePresentation(presentationId)
    }

    private fun handlePresentationEvent(event: PresentationEvent) {
        when (PresentationEventType.fromEvent(event.eventType)) {
            PresentationEventType.NAME_CHANGED -> fetchPresentationDetails()
            PresentationEventType.PARTICIPANTS_CHANGED -> fetchParticipants()
            PresentationEventType.TEXT_CHANGED -> fetchStructure()
            PresentationEventType.PRESENTATION_STARTED ->
                _state.update { it.copy(isPresentationStarted = true) }

            PresentationEventType.PRESENTATION_STOPPED ->
                _state.update { it.copy(isPresentationStarted = false) }

            PresentationEventType.JOINED_USERS_CHANGED -> fetchJoinedUsers()
            else -> {}
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result = userRepository.getProfile(currentToken)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(userProfile = result.data) }
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                }
            }
        }
    }

    fun fetchPresentationData() {
        fetchPresentationDetails()
        fetchParticipants()
        fetchStructure()
        fetchConfig()
        fetchJoinedUsers()
    }

    private fun fetchPresentationDetails() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result =
                presentationsRepository.getPresentation(currentToken, presentationId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(presentation = result.data, isLoading = false) }
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true, isLoading = false) }
                }
            }
        }
    }

    private fun fetchParticipants() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result =
                presentationsRepository.getParticipants(currentToken, presentationId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(participants = result.data ?: emptyList()) }
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                }
            }
        }
    }

    private fun fetchStructure() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result =
                presentationsRepository.getPresentationStructure(currentToken, presentationId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(structure = result.data) }
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                }
            }
        }
    }

    private fun fetchConfig() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result = presentationsRepository.getConfig(currentToken)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(config = result.data) }
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                }
            }
        }
    }

    private fun fetchJoinedUsers() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result =
                presentationsRepository.getActivePresentation(currentToken, presentationId)) {
                is ApiResult.Success -> {
                    val activeData = result.data
                    val joinedUsers =
                        activeData?.joinedUsers?.map { JoinedUser(it.userId) } ?: emptyList()
                    val isStarted = activeData?.currentPresentationStartDate != null
                    _state.update {
                        it.copy(
                            joinedUsers = joinedUsers,
                            isPresentationStarted = isStarted
                        )
                    }
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                }
            }
        }
    }

    fun updatePresentationName(newName: String) {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result = presentationsRepository.updatePresentationName(
                currentToken,
                presentationId,
                newName
            )) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            presentation = result.data,
                            isEditNameModalOpen = false
                        )
                    }
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                }
            }
        }
    }

    fun deletePresentation() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (presentationsRepository.deletePresentation(currentToken, presentationId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isDeleteModalOpen = false,
                            wasPresentationDeleted = true
                        )
                    }
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                }
            }
        }
    }

    fun inviteParticipant() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch

            val maxFreeParticipantsCount =
                state.value.config?.premiumConfig?.maxFreeParticipantsCount
                    ?: 0
            if (state.value.userProfile?.hasPremium == false &&
                state.value.participants.size >= maxFreeParticipantsCount
            ) {
                _state.update { it.copy(isPremiumModalOpen = true) }
                return@launch
            }

            when (val result =
                presentationsRepository.inviteParticipant(currentToken, presentationId)) {
                is ApiResult.Success -> {
                    val invitationCode = result.data?.invitationCode ?: ""
                    val link = "${API_BASE_URL}invite/$invitationCode"
                    _state.update {
                        it.copy(
                            inviteLink = link,
                            isInviteModalOpen = true
                        )
                    }
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                }
            }
        }
    }

    fun deleteParticipant(participantId: Int) {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (presentationsRepository.deleteParticipant(currentToken, participantId)) {
                is ApiResult.Success -> {
                    fetchParticipants()
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                }
            }
        }
    }

    fun showEditNameModal() {
        _state.update { it.copy(isEditNameModalOpen = true) }
    }

    fun hideEditNameModal() {
        _state.update { it.copy(isEditNameModalOpen = false) }
    }

    fun showDeleteModal() {
        _state.update { it.copy(isDeleteModalOpen = true) }
    }

    fun hideDeleteModal() {
        _state.update { it.copy(isDeleteModalOpen = false) }
    }

    fun hideInviteModal() {
        _state.update { it.copy(isInviteModalOpen = false) }
    }

    fun hidePremiumModal() {
        _state.update { it.copy(isPremiumModalOpen = false) }
    }

    fun showDeleteParticipantDialog(participantId: Int) {
        _state.update { it.copy(isDeleteParticipantDialogOpen = true, participantToDeleteId = participantId) }
    }

    fun hideDeleteParticipantDialog() {
        _state.update { it.copy(isDeleteParticipantDialogOpen = false, participantToDeleteId = null) }
    }

    fun showEditProfileDialog() {
        _state.update { it.copy(editProfileDialogOpen = true) }
    }

    fun hideEditProfileDialog() {
        _state.update { it.copy(editProfileDialogOpen = false) }
    }

    fun confirmDeleteParticipant() {
        val id = state.value.participantToDeleteId ?: return
        deleteParticipant(id)
        _state.update { it.copy(isDeleteParticipantDialogOpen = false, participantToDeleteId = null) }
    }

    override fun onCleared() {
        super.onCleared()
        presentationEventListener?.let {
            presentationSocketManager.offPresentationEvent(it)
        }
        presentationSocketManager.disconnect()
    }

    fun formatDuration(
        context: Context,
        wordsCount: Int,
        config: PresentationsConfig,
    ): String {
        val minWPM = config.wordsPerMinuteMin
        val maxWPM = config.wordsPerMinuteMax

        val min = ceil(wordsCount / maxWPM.toFloat()).toInt()
        val max = ceil(wordsCount / minWPM.toFloat()).toInt()

        val minutesString = context.resources.getQuantityString(
            R.plurals.minutes,
            max,
        )

        return if (max > min) {
            context.getString(R.string.minutes_range, min, max, minutesString)
        } else {
            context.getString(R.string.minutes_single, max, minutesString)
        }
    }
}