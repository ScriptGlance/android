package com.scriptglance.data.model.presentation

data class PresentationActiveData(
    val currentReadingPosition: PresentationActiveCurrentReadingPosition,
    val structure: List<PresentationActiveStructureItem>,
    val currentPresentationStartDate: String?,
    val currentOwnerUserId: Long,
    val joinedUsers: List<PresentationActiveJoinedUser>
)