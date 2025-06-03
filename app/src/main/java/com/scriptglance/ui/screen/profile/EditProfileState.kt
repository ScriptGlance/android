package com.scriptglance.ui.screen.profile

import android.net.Uri
import com.scriptglance.data.model.profile.User

data class EditProfileState(
    val isLoadingInitialData: Boolean = true,
    val isUpdating: Boolean = false,
    val userProfile: User? = null,
    val currentAvatarUrl: String? = null,
    val selectedAvatarUri: Uri? = null,
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val hasUpdateError: Boolean = false,
    val updateSuccess: Boolean = false
)