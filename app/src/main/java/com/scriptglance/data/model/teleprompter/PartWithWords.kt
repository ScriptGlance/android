package com.scriptglance.data.model.teleprompter

import com.scriptglance.data.model.presentation.Participant

data class PartWithWords(
    val partId: Int,
    val partOrder: Int,
    val partName: String,
    val partText: String,
    val wordsArray: List<String>,
    val renderableWords: List<RenderableWord>,
    val assignedParticipant: Participant?
)