package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName

data class PresentationStructure(
    @SerializedName("total_words_count")
    val totalWordsCount: Int,
    val structure: List<PresentationPart>
)