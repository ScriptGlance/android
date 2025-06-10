package com.scriptglance.ui.screen.presentation.teleprompter

import com.scriptglance.data.model.presentation.Participant
import com.scriptglance.data.model.presentation.Presentation
import com.scriptglance.data.model.presentation.PresentationStructure
import com.scriptglance.data.model.presentation.PresentationsConfig
import com.scriptglance.data.model.profile.User
import com.scriptglance.data.model.teleprompter.PartReassignRequest
import com.scriptglance.data.model.teleprompter.PartWithWords
import com.scriptglance.data.model.teleprompter.PresentationActiveCurrentReadingPosition
import com.scriptglance.data.model.teleprompter.PresentationActiveJoinedUser
import com.scriptglance.data.model.teleprompter.PresentationPartFull
import com.scriptglance.data.model.teleprompter.ProcessedTexts
import com.scriptglance.data.model.teleprompter.ReadingConfirmationRequest
import com.scriptglance.data.model.teleprompter.SidebarPartItem

data class TeleprompterState(
    val isLoading: Boolean = true,
    val initialLoadComplete: Boolean = false,
    val error: Boolean = false,
    val isRecognizing: Boolean = false,
    val isSocketConnected: Boolean = true,
    val presentation: Presentation? = null,
    val participants: List<Participant> = emptyList(),
    val structure: List<PresentationPartFull>? = null,
    val config: PresentationsConfig? = null,
    val userProfile: User? = null,
    val currentPresentationStartDate: String? = null,
    val teleprompterActiveUsers: List<PresentationActiveJoinedUser> = emptyList(),
    val currentTeleprompterOwnerId: Int? = null,
    val initialReadingPosition: PresentationActiveCurrentReadingPosition? = null,
    val partsWithWords: List<PartWithWords> = emptyList(),
    val sidebarParts: List<SidebarPartItem> = emptyList(),
    val processedTexts: ProcessedTexts = ProcessedTexts(),
    val partReassignRequest: PartReassignRequest? = null,
    val readingConfirmationRequest: ReadingConfirmationRequest? = null
)