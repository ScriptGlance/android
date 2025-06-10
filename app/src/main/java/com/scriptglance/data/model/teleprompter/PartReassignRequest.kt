package com.scriptglance.data.model.teleprompter

import com.scriptglance.data.model.presentation.Participant
import com.scriptglance.utils.constants.PartReassignReason

data class PartReassignRequest(
    val part: SidebarPartItem?,
    val reason: PartReassignReason,
    val availableParticipants: List<Participant>,
    val missingParticipant: Participant?,
)