package com.scriptglance.data.model.presentation

data class GetPresentationsParams(
    val limit: Int,
    val offset: Int,
    val search: String? = null,
    val sort: String? = null,
    val owner: String? = null,
    val lastChange: String? = null,
    val type: String? = null
)