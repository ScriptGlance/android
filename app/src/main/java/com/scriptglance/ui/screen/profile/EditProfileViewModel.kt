package com.scriptglance.ui.screen.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.profile.UserProfileUpdateData
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileState())
    val uiState: StateFlow<EditProfileState> = _uiState.asStateFlow()

    private var currentToken: String? = null

    init {
        viewModelScope.launch {
            currentToken = authRepository.getToken()
            loadUserProfile()
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            val token = currentToken ?: return@launch
            _uiState.update { it.copy(isLoadingInitialData = true, hasUpdateError = false) }
            when (val result = userRepository.getProfile(token)) {
                is ApiResult.Success -> {
                    val profile = result.data
                    _uiState.update {
                        it.copy(
                            isLoadingInitialData = false,
                            userProfile = profile,
                            currentAvatarUrl = profile?.avatar,
                            firstName = profile?.firstName ?: "",
                            lastName = profile?.lastName ?: ""
                        )
                    }
                }

                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingInitialData = false,
                            hasUpdateError = true
                        )
                    }
                }
            }
        }
    }

    fun onFirstNameChanged(name: String) {
        _uiState.update { it.copy(firstName = name) }
    }

    fun onLastNameChanged(name: String) {
        _uiState.update { it.copy(lastName = name) }
    }

    fun onPasswordChanged(pass: String) {
        _uiState.update { it.copy(password = pass) }
    }

    fun onAvatarSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedAvatarUri = uri) }
    }

    fun saveProfile(context: Context) {
        viewModelScope.launch {
            val token = currentToken ?: return@launch
            val currentState = _uiState.value

            if (currentState.firstName.isBlank() || currentState.lastName.isBlank()) {
                _uiState.update { it.copy(hasUpdateError = true) }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isUpdating = true,
                    hasUpdateError = false,
                    updateSuccess = false
                )
            }

            val avatarFile: File? = currentState.selectedAvatarUri?.let { uri ->
                uriToFile(context, uri)
            }

            val updateData = UserProfileUpdateData(
                avatar = avatarFile,
                firstName = currentState.firstName.takeIf { it != currentState.userProfile?.firstName },
                lastName = currentState.lastName.takeIf { it != currentState.userProfile?.lastName },
                password = currentState.password.takeIf { it.isNotBlank() }
            )

            if (updateData.avatar == null && updateData.firstName == null && updateData.lastName == null && updateData.password == null) {
                _uiState.update { it.copy(isUpdating = false, updateSuccess = true) }
                return@launch
            }


            when (val result = userRepository.updateProfile(token, updateData)) {
                is ApiResult.Success -> {
                    val updatedProfile = result.data
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            updateSuccess = true,
                            userProfile = updatedProfile ?: it.userProfile,
                            currentAvatarUrl = updatedProfile?.avatar ?: it.currentAvatarUrl,
                            selectedAvatarUri = null
                        )
                    }
                }

                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            hasUpdateError = true,
                        )
                    }
                }
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("avatar_", ".jpg", context.cacheDir)
            tempFile.deleteOnExit()
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun resetUpdateStatus() {
        _uiState.update { it.copy(updateSuccess = false, hasUpdateError = false) }
    }
}