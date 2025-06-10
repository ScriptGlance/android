package com.scriptglance.data.model.teleprompter

data class PartReassignRequiredPayload(
    val userId: Int,
    val partId: Int,
    val reason: String
)