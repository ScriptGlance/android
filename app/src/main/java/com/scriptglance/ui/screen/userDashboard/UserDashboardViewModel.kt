package com.scriptglance.ui.screen.userDashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.presentation.GetPresentationsParams
import com.scriptglance.data.model.profile.UserProfileUpdateData
import com.scriptglance.domain.manager.socket.ChatSocketManager
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.domain.repository.ChatRepository
import com.scriptglance.domain.repository.PresentationsRepository
import com.scriptglance.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.emitter.Emitter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardFilters(
    val type: PresentationType = PresentationType.ALL,
    val lastChange: LastChange = LastChange.ALL_TIME,
    val owner: OwnerType = OwnerType.ALL
)

enum class PresentationType { INDIVIDUAL, GROUP, ALL }
enum class LastChange { TODAY, LAST_WEEK, LAST_MONTH, LAST_YEAR, ALL_TIME }
enum class OwnerType { ME, OTHERS, ALL }

enum class PresentationSort {
    BY_UPDATED,
    BY_NAME,
    BY_CREATED,
    BY_PARTICIPANTS
}

@HiltViewModel
class UserDashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val presentationsRepository: PresentationsRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val chatSocketManager: ChatSocketManager
) : ViewModel() {

    private val _state = MutableStateFlow(UserDashboardState(isLoading = true))
    val state: StateFlow<UserDashboardState> = _state.asStateFlow()

    private var currentToken: String? = null
    private var newMessageListener: Emitter.Listener? = null
    private var chatClosedListener: Emitter.Listener? = null
    private var isSocketSetupInitialized = false


    init {
        viewModelScope.launch {
            currentToken = authRepository.getToken()
            if (currentToken.isNullOrBlank()) {
                _state.update { it.copy(isLoading = false, userProfile = null) }
            } else {
                setupChatSocket()
            }
        }
    }

    fun onScreenResumed() {
        viewModelScope.launch {
            setupChatSocket()
            refresh()
        }
    }

    private suspend fun setupChatSocket() {
        try {
            chatSocketManager.connect()
            chatSocketManager.onConnect(chatSocketManager::joinUserChat)

            newMessageListener?.let { chatSocketManager.offNewMessage(it) }
            chatClosedListener?.let { chatSocketManager.offChatClosed(it) }

            newMessageListener = chatSocketManager.onNewMessage { event ->
                fetchUnreadCount()
            }

            chatClosedListener = chatSocketManager.onChatClosed {
                _state.update { it.copy(chatUnreadCount = 0) }
            }

            isSocketSetupInitialized = true
        } catch (_: Exception) {
        }
    }

    private fun fetchAll() {
        val token = currentToken ?: return
        _state.update { it.copy(isLoading = true) }
        fetchUserProfile(token)
        fetchStats(token)
        fetchUnreadCount()
        refreshPresentations()
    }

    private fun fetchUnreadCount() {
        val token = currentToken ?: return

        viewModelScope.launch {
            when (val result = chatRepository.getUserActiveUnreadCount(token)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(chatUnreadCount = result.data?.unreadCount ?: 0) }
                }

                is ApiResult.Error -> {

                }
            }
        }
    }

    fun fetchUserProfile(token: String = currentToken ?: "") {
        if (token.isBlank()) {
            _state.update { it.copy(userProfile = null, isLoading = false) }
            return
        }
        viewModelScope.launch {
            when (val result = userRepository.getProfile(token)) {
                is ApiResult.Success -> _state.update {
                    it.copy(userProfile = result.data)
                }

                is ApiResult.Error -> _state.update {
                    it.copy(userProfile = null)
                }
            }
        }
    }

    fun fetchStats(token: String = currentToken ?: "") {
        if (token.isBlank()) {
            _state.update { it.copy(stats = null, isLoading = false) }
            return
        }
        viewModelScope.launch {
            when (val result = presentationsRepository.getStats(token)) {
                is ApiResult.Success -> _state.update {
                    it.copy(stats = result.data)
                }

                is ApiResult.Error -> _state.update { it.copy(stats = null) }
            }
        }
    }

    fun refreshPresentations() {
        _state.update {
            it.copy(
                presentations = emptyList(),
                offset = 0,
                canLoadMore = true,
                isLoading = false
            )
        }
        loadMorePresentations()
    }

    fun loadMorePresentations() {
        val token = currentToken
        if (token.isNullOrBlank()) {
            _state.update { it.copy(isLoading = false, canLoadMore = false) }
            return
        }

        val currentState = _state.value

        if (currentState.isLoading || !currentState.canLoadMore) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val params = buildParams(currentState).copy(
                limit = currentState.limit,
                offset = currentState.presentations.size
            )
            when (val result = presentationsRepository.getPresentations(token, params)) {
                is ApiResult.Success -> {
                    val received = result.data ?: emptyList()
                    val newList = currentState.presentations + received
                    val canLoad = received.size == currentState.limit
                    _state.update {
                        it.copy(
                            presentations = newList,
                            isLoading = false,
                            offset = newList.size,
                            canLoadMore = canLoad
                        )
                    }
                }

                is ApiResult.Error -> _state.update {
                    it.copy(isLoading = false, canLoadMore = false)
                }
            }
        }
    }

    fun createPresentation() {
        viewModelScope.launch {
            val token = currentToken ?: return@launch
            val result = presentationsRepository.createPresentation(token)
            when (result) {
                is ApiResult.Success -> {
                    _state.update { it.copy(createdPresentationId = result.data?.presentationId) }
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        refreshPresentations()
    }

    fun onSortChange(sort: PresentationSort) {
        _state.update { it.copy(sort = sort) }
        refreshPresentations()
    }

    fun onFiltersApply(newFilters: DashboardFilters) {
        _state.update { it.copy(filters = newFilters) }
        refreshPresentations()
    }

    private fun refresh() {
        _state.update {
            it.copy(
                userProfile = null,
                stats = null,
                presentations = emptyList(),
                offset = 0,
                canLoadMore = true,
                isLoading = true
            )
        }
        fetchAll()
    }

    private fun buildParams(state: UserDashboardState): GetPresentationsParams {
        val f = state.filters
        return GetPresentationsParams(
            type = when (f.type) {
                PresentationType.INDIVIDUAL -> "individual"
                PresentationType.GROUP -> "group"
                PresentationType.ALL -> "all"
            },
            lastChange = when (f.lastChange) {
                LastChange.TODAY -> "today"
                LastChange.LAST_WEEK -> "lastWeek"
                LastChange.LAST_MONTH -> "lastMonth"
                LastChange.LAST_YEAR -> "lastYear"
                LastChange.ALL_TIME -> "allTime"
            },
            owner = when (f.owner) {
                OwnerType.ME -> "me"
                OwnerType.OTHERS -> "others"
                OwnerType.ALL -> "all"
            },
            search = state.searchQuery.ifBlank { null },
            sort = when (state.sort) {
                PresentationSort.BY_UPDATED -> "byUpdatedAt"
                PresentationSort.BY_NAME -> "byName"
                PresentationSort.BY_CREATED -> "byCreatedAt"
                PresentationSort.BY_PARTICIPANTS -> "byParticipantsCount"
            },
            limit = state.limit,
            offset = state.offset
        )
    }

    fun logout() {
        MainScope().launch {
            currentToken?.let {
                userRepository.updateProfile(
                    it,
                    UserProfileUpdateData(fcmToken = "")
                )
            }
            authRepository.removeToken()
        }
    }

    override fun onCleared() {
        super.onCleared()
        newMessageListener?.let { chatSocketManager.offNewMessage(it) }
        chatClosedListener?.let { chatSocketManager.offChatClosed(it) }
        chatSocketManager.disconnect()
        isSocketSetupInitialized = false
    }
}