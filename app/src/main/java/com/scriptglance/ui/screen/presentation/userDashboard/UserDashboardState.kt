package com.scriptglance.ui.screen.presentation.userDashboard

import com.scriptglance.data.model.presentation.PresentationItem
import com.scriptglance.data.model.presentation.PresentationStats
import com.scriptglance.data.model.profile.User

data class UserDashboardState(
    val isLoading: Boolean = false,
    val userProfile: User? = null,
    val stats: PresentationStats? = null,
    val presentations: List<PresentationItem> = emptyList(),
    val filters: DashboardFilters = DashboardFilters(),
    val sort: PresentationSort = PresentationSort.BY_UPDATED,
    val searchQuery: String = "",
    val limit: Int = 20,
    val offset: Int = 0,
    val canLoadMore: Boolean = true,
    val createdPresentationId: Int? = null,
)