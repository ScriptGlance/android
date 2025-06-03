package com.scriptglance.ui.screen.presentation.userDashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scriptglance.R
import com.scriptglance.data.model.presentation.PresentationItem
import com.scriptglance.ui.common.components.AppTextField
import com.scriptglance.ui.common.components.BeigeButton
import com.scriptglance.ui.common.components.GrayButton
import com.scriptglance.ui.common.components.RedButton
import com.scriptglance.ui.common.components.UserAvatar
import com.scriptglance.ui.screen.profile.EditProfileDialog
import com.scriptglance.ui.theme.Black
import com.scriptglance.ui.theme.RedEA
import com.scriptglance.ui.theme.White
import com.scriptglance.ui.theme.WhiteEA
import kotlinx.coroutines.flow.collectLatest

@Composable
fun UserDashboardScreenRoot(
    onPresentationClick: (Int) -> Unit,
    onLogout: () -> Unit,
) {
    val viewModel: UserDashboardViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    var filtersDialogVisible by remember { mutableStateOf(false) }
    var editProfileDialogVisible by remember { mutableStateOf(false) }
    var logoutDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.createdPresentationId) {
        state.createdPresentationId?.let {
            onPresentationClick(it)
        }
    }

    LaunchedEffect(true) {
        viewModel.onRefresh()
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteEA)
    ) {
        UserDashboardScreen(
            state = state,
            onPresentationClick = onPresentationClick,
            onFiltersClick = { filtersDialogVisible = true },
            onEditProfileClick = { editProfileDialogVisible = true },
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onSortChange = viewModel::onSortChange,
            onLoadMore = viewModel::loadMorePresentations,
            onCreatePresentation = viewModel::createPresentation,
            onLogoutClick = { logoutDialogVisible = true },
        )
        if (filtersDialogVisible) {
            FiltersDialog(
                currentFilters = state.filters,
                onApplyFilters = { appliedFilters ->
                    viewModel.onFiltersApply(appliedFilters)
                    filtersDialogVisible = false
                },
                onDismissRequest = { filtersDialogVisible = false }
            )
        }

        if (editProfileDialogVisible) {
            EditProfileDialog(
                onDismissRequest = { editProfileDialogVisible = false },
                onProfileUpdated = {
                    viewModel.fetchUserProfile()
                    viewModel.refreshPresentations()
                    editProfileDialogVisible = false
                }
            )
        }

        if (logoutDialogVisible) {
            AlertDialog(
                onDismissRequest = { logoutDialogVisible = false },
                text = {
                    Text(
                        stringResource(R.string.logout_confirm_message),
                        fontWeight = FontWeight.Medium
                    )
                },
                confirmButton = {
                    RedButton(
                        label = stringResource(R.string.logout),
                        modifier = Modifier.fillMaxWidth(0.4f),
                        onClick = {
                            logoutDialogVisible = false
                            viewModel.logout()
                            onLogout()
                        },
                    )
                },
                dismissButton = {
                    GrayButton(
                        modifier = Modifier.fillMaxWidth(0.4f),
                        label = stringResource(R.string.button_cancel),
                        onClick = { logoutDialogVisible = false },
                    )
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun UserDashboardScreen(
    state: UserDashboardState,
    onCreatePresentation: () -> Unit,
    onPresentationClick: (Int) -> Unit,
    onFiltersClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortChange: (PresentationSort) -> Unit,
    onLoadMore: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val userFirstName =
        state.userProfile?.firstName ?: stringResource(id = R.string.greeting_user_placeholder)

    LaunchedEffect(listState, state.presentations.size, state.canLoadMore, state.isLoading) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collectLatest { lastVisibleIndex ->
                if (lastVisibleIndex == null || state.presentations.isEmpty()) {
                    return@collectLatest
                }
                val lastIndex = state.presentations.lastIndex
                val prefetchDistance = (state.limit / 4).coerceAtLeast(5)
                if (
                    lastVisibleIndex >= lastIndex - prefetchDistance &&
                    state.canLoadMore &&
                    !state.isLoading
                ) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(bottom = 20.dp)
            ) {
                Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = stringResource(R.string.dashboard_logo_content_description),
                            modifier = Modifier.height(36.dp)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.greeting_format, userFirstName),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.dashboard_manage_presentations),
                        fontSize = 16.sp,
                        color = Color(0xFF4c6c5a),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    BeigeButton(
                        label = stringResource(R.string.dashboard_create_presentation_button),
                        onClick = onCreatePresentation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(top = 16.dp),
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 20.dp, end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatar(
                        avatarUrl = state.userProfile?.avatar,
                        firstName = state.userProfile?.firstName,
                        lastName = state.userProfile?.lastName,
                        size = 48.dp,
                        modifier = Modifier.clickable { onEditProfileClick() }
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .clickable(onClick = onLogoutClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = stringResource(R.string.logout),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }


        item {
            Row(
                Modifier
                    .background(WhiteEA)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(
                    16.dp,
                    Alignment.CenterHorizontally
                )
            ) {
                Spacer(Modifier.width(4.dp))
                DashboardStatCard(
                    icon = R.drawable.ic_presentation,
                    bg = Color(0xFFD3DDD7),
                    label = stringResource(R.string.stat_total_presentations),
                    value = state.stats?.presentationCount?.toString()
                        ?: stringResource(R.string.stat_default_value)
                )
                DashboardStatCard(
                    icon = R.drawable.ic_users,
                    bg = Color(0xFFE3E4EA),
                    label = stringResource(R.string.stat_invited_participants),
                    value = state.stats?.invitedParticipants?.toString()
                        ?: stringResource(R.string.stat_default_value)
                )
                DashboardStatCard(
                    icon = R.drawable.ic_video,
                    bg = Color(0xFFF1E8D6),
                    label = stringResource(R.string.stat_recordings_made),
                    value = state.stats?.recordingsMade?.toString()
                        ?: stringResource(R.string.stat_default_value)
                )
                Spacer(Modifier.width(4.dp))
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = stringResource(R.string.search_presentations_placeholder),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                IconButton(onClick = onFiltersClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = stringResource(R.string.filters_icon_content_description),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 12.dp
                    )
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PresentationSortTab(
                    stringResource(R.string.sort_tab_recent),
                    state.sort == PresentationSort.BY_UPDATED,
                    onClick = { onSortChange(PresentationSort.BY_UPDATED) })
                PresentationSortTab(
                    stringResource(R.string.sort_tab_alphabetical),
                    state.sort == PresentationSort.BY_NAME,
                    onClick = { onSortChange(PresentationSort.BY_NAME) })
                PresentationSortTab(
                    stringResource(R.string.sort_tab_newest),
                    state.sort == PresentationSort.BY_CREATED,
                    onClick = { onSortChange(PresentationSort.BY_CREATED) })
                PresentationSortTab(
                    stringResource(R.string.sort_tab_by_participants),
                    state.sort == PresentationSort.BY_PARTICIPANTS,
                    onClick = { onSortChange(PresentationSort.BY_PARTICIPANTS) })
            }
        }

        if (state.isLoading && state.presentations.isEmpty()) {
            item {
                Box(
                    Modifier
                        .fillParentMaxWidth()
                        .background(White)
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4c6c5a))
                }
            }
        } else if (state.presentations.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_presentations_found),
                    color = Color(0xFF595D75),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .background(White)
                        .padding(vertical = 60.dp)
                )
            }
        } else {
            items(
                items = state.presentations,
                key = { presentation -> presentation.presentationId }
            ) { pres ->
                PresentationListItem(
                    presentation = pres,
                    onClick = { onPresentationClick(pres.presentationId) },
                    dateFallbackString = stringResource(id = R.string.date_format_fallback)
                )
                Spacer(
                    Modifier
                        .height(0.5.dp)
                        .fillMaxWidth()
                        .background(WhiteEA.copy(alpha = 0.5f))
                )
            }

            if (state.isLoading && state.canLoadMore) {
                item {
                    Box(
                        Modifier
                            .fillParentMaxWidth()
                            .background(White)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4c6c5a))
                    }
                }
            }
        }
    }
}


@Composable
fun FiltersDialog(
    currentFilters: DashboardFilters,
    onApplyFilters: (DashboardFilters) -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedType by remember(currentFilters.type) { mutableStateOf(currentFilters.type) }
    var selectedDate by remember(currentFilters.lastChange) { mutableStateOf(currentFilters.lastChange) }
    var selectedOwner by remember(currentFilters.owner) { mutableStateOf(currentFilters.owner) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                stringResource(R.string.filters_dialog_title),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = screenHeight * 0.6f)
            ) {
                FilterSection(title = stringResource(R.string.filter_type_label)) {
                    PresentationType.entries.forEach { type ->
                        FilterRadioButtonItem(
                            text = stringResource(
                                id = when (type) {
                                    PresentationType.INDIVIDUAL -> R.string.filter_presentation_type_individual
                                    PresentationType.GROUP -> R.string.filter_presentation_type_group
                                    PresentationType.ALL -> R.string.filter_presentation_type_all_types
                                }
                            ),
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                FilterSection(title = stringResource(R.string.filter_last_change_label)) {
                    LastChange.entries.forEach { change ->
                        FilterRadioButtonItem(
                            text = stringResource(
                                id = when (change) {
                                    LastChange.TODAY -> R.string.filter_last_change_today
                                    LastChange.LAST_WEEK -> R.string.filter_last_change_last_week
                                    LastChange.LAST_MONTH -> R.string.filter_last_change_last_month
                                    LastChange.LAST_YEAR -> R.string.filter_last_change_last_year
                                    LastChange.ALL_TIME -> R.string.filter_last_change_all_time
                                }
                            ),
                            selected = selectedDate == change,
                            onClick = { selectedDate = change }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                FilterSection(title = stringResource(R.string.filter_owner_label)) {
                    OwnerType.entries.forEach { owner ->
                        FilterRadioButtonItem(
                            text = stringResource(
                                id = when (owner) {
                                    OwnerType.ME -> R.string.filter_owner_type_me
                                    OwnerType.OTHERS -> R.string.filter_owner_type_others
                                    OwnerType.ALL -> R.string.filter_owner_type_all_authors
                                }
                            ),
                            selected = selectedOwner == owner,
                            onClick = { selectedOwner = owner }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onApplyFilters(DashboardFilters(selectedType, selectedDate, selectedOwner))
            }) {
                Text(stringResource(R.string.button_apply), fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    stringResource(R.string.button_cancel),
                    fontWeight = FontWeight.Medium,
                    color = RedEA
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun FilterSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color(0xFF333333))
        Spacer(Modifier.height(10.dp))
        Column(content = content)
    }
}

@Composable
private fun FilterRadioButtonItem(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 15.sp, color = Color(0xFF333333))
    }
}


@Composable
fun DashboardStatCard(icon: Int, bg: Color, label: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .width(135.dp)
            .height(105.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            Modifier
                .size(42.dp)
                .background(bg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
        }
        Column {
            Text(
                label,
                color = Color(0xFF727474),
                fontSize = 13.sp,
                maxLines = 2
            )
            Text(
                value,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
    }
}

@Composable
fun PresentationSortTab(title: String, selected: Boolean, onClick: () -> Unit) {
    val textColor = if (selected) Color(0xFF395917) else Color(0xFF888888)
    val backgroundColor = if (selected) Color(0xFFF3F2ED) else Color.Transparent
    Text(
        text = title,
        color = textColor,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        fontSize = 15.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    )
}

@Composable
fun PresentationListItem(
    presentation: PresentationItem,
    onClick: () -> Unit,
    dateFallbackString: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color.White)
            .padding(vertical = 18.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(52.dp)
                .background(Color(0xFFF1E8D6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_presentation),
                contentDescription = stringResource(R.string.presentation_list_item_icon_description),
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = presentation.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (presentation.participantCount > 1)
                    stringResource(
                        R.string.presentation_shared_info_format,
                        presentation.participantCount
                    )
                else stringResource(R.string.presentation_individual_info),
                fontSize = 14.sp,
                color = Color(0xFF777777)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(115.dp)
        ) {
            if (presentation.modifiedAt.isNotBlank()) {
                Text(
                    text = stringResource(R.string.presentation_last_modified_label),
                    color = Color(0xFF999999),
                    fontSize = 12.sp,
                    textAlign = TextAlign.End
                )
                Text(
                    text = formatDate(presentation.modifiedAt, dateFallbackString),
                    color = Color(0xFF666666),
                    fontSize = 13.sp,
                    textAlign = TextAlign.End
                )
                Spacer(Modifier.height(6.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                val ownerFirstName = presentation.owner.firstName
                val ownerLastName = presentation.owner.lastName
                val ownerNameDisplay =
                    listOfNotNull(ownerFirstName, ownerLastName).joinToString(" ").trim()


                if (ownerNameDisplay.isNotEmpty()) {
                    Text(
                        text = ownerNameDisplay,
                        fontSize = 13.sp,
                        color = Color(0xFF555555),
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                }

                UserAvatar(
                    avatarUrl = presentation.owner.avatar,
                    firstName = ownerFirstName,
                    lastName = ownerLastName,
                    size = 30.dp,
                )
            }
        }
    }
}

fun formatDate(dateStr: String?, fallback: String): String {
    return if (dateStr.isNullOrBlank()) fallback
    else {
        try {
            dateStr.substring(0, 10).split("-").reversed().joinToString(".")
        } catch (_: Exception) {
            fallback
        }
    }
}