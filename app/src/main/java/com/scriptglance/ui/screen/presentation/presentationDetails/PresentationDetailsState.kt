package com.scriptglance.ui.screen.presentation.presentationDetails

import com.scriptglance.data.model.presentation.JoinedUser
import com.scriptglance.data.model.presentation.Participant
import com.scriptglance.data.model.presentation.Presentation
import com.scriptglance.data.model.presentation.PresentationStructure
import com.scriptglance.data.model.presentation.PresentationsConfig
import com.scriptglance.data.model.profile.User

data class PresentationDetailsState(
    val isLoading: Boolean = true,
    val userProfile: User? = null,
    val presentation: Presentation? = null,
    val participants: List<Participant> = emptyList(),
    val structure: PresentationStructure? = null,
    val config: PresentationsConfig? = null,
    val joinedUsers: List<JoinedUser> = emptyList(),
    val isPresentationStarted: Boolean = false,
    val error: Boolean = false,
    val isEditNameModalOpen: Boolean = false,
    val isDeleteModalOpen: Boolean = false,
    val isInviteModalOpen: Boolean = false,
    val inviteLink: String = "",
    val isPremiumModalOpen: Boolean = false,
    val wasPresentationDeleted: Boolean = false,
    var participantToDeleteId: Int? = null,
    var isDeleteParticipantDialogOpen: Boolean = false,
    var editProfileDialogOpen: Boolean = false,
)