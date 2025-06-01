package com.scriptglance.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptglance.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MainState>(MainState.Loading)
    val state: StateFlow<MainState> = _state

    init {
        viewModelScope.launch {
            val isAuth = try {
                authRepository.isAuthenticated()
            } catch (_: Exception) {
                false
            }
            _state.value = if (isAuth) MainState.Authenticated else MainState.Unauthenticated
        }
    }
}
