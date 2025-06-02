package com.scriptglance.data.model.presentation

import com.google.gson.annotations.SerializedName

data class PresentationPart(
    @SerializedName("part_name")
    val partName: String,
    @SerializedName("part_order")
    val partOrder: Int,
    @SerializedName("words_count")
    val wordsCount: Int,
    @SerializedName("text_preview")
    val textPreview: String,
    val assignee: PresentationOwner
)