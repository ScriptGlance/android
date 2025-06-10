package com.scriptglance.data.model.teleprompter

data class ReadingConfirmationRequest(
    val part: SidebarPartItem?,
    val timeToConfirmSeconds: Int,
    val canContinueFromLastPosition: Boolean,
)