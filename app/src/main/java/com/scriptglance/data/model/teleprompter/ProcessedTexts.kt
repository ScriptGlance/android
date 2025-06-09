package com.scriptglance.data.model.teleprompter

data class ProcessedTexts(
    val processedWordsIndices: Map<Int, Int> = emptyMap(),
    val currentHighlightedPartId: Int? = null,
    val currentSpeakerUserId: Int? = null
)