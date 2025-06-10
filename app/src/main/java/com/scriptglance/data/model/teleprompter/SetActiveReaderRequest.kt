package com.scriptglance.data.model.teleprompter

import com.google.gson.annotations.SerializedName

data class SetActiveReaderRequest(
    @SerializedName("new_reader_id")
    val newReaderId: Int
)