package com.scriptglance.ui.screen.presentation.presentationDetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.scriptglance.R
import com.scriptglance.data.model.presentation.JoinedUser
import com.scriptglance.data.model.presentation.Participant
import com.scriptglance.data.model.presentation.Presentation
import com.scriptglance.data.model.presentation.PresentationPart
import com.scriptglance.data.model.presentation.PresentationsConfig
import com.scriptglance.ui.common.components.AppTextField
import com.scriptglance.ui.common.components.BeigeButton
import com.scriptglance.ui.common.components.ErrorDialog
import com.scriptglance.ui.common.components.GreenButton
import com.scriptglance.ui.common.components.RedButton
import com.scriptglance.ui.common.components.UserAvatar
import com.scriptglance.ui.screen.presentation.presentationDetails.components.ConfirmDeleteParticipantDialog
import com.scriptglance.ui.screen.presentation.presentationDetails.components.EditPresentationNameModal
import com.scriptglance.ui.screen.presentation.presentationDetails.components.InviteParticipantModal
import com.scriptglance.ui.screen.presentation.presentationDetails.components.PremiumModal
import com.scriptglance.ui.screen.profile.EditProfileDialog
import com.scriptglance.ui.theme.BeigeE5
import com.scriptglance.ui.theme.BeigeF3
import com.scriptglance.ui.theme.Gray59
import com.scriptglance.ui.theme.Green45
import com.scriptglance.ui.theme.Green5E
import com.scriptglance.ui.theme.GreenD3
import com.scriptglance.ui.theme.RedEA
import com.scriptglance.ui.theme.WhiteEA
import androidx.compose.runtime.DisposableEffect


@Composable
fun PresentationDetailsScreen(
    goBack: () -> Unit,
    goToTeleprompter: (Int) -> Unit,
    viewModel: PresentationDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.wasPresentationDeleted) {
        if (state.wasPresentationDeleted) {
            goBack()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                viewModel.onScreenResumed()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteEA)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { goBack() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Green5E
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = stringResource(R.string.dashboard_logo_content_description),
                        modifier = Modifier.height(36.dp)
                    )
                }

                UserAvatar(
                    selectedLocalUri = null,
                    avatarUrl = state.userProfile?.avatar,
                    firstName = state.userProfile?.firstName,
                    lastName = state.userProfile?.lastName,
                    size = 40.dp,
                    modifier = Modifier.clickable { viewModel.showEditProfileDialog() },
                )
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green5E)
                }
            } else if (state.presentation != null) {
                val presentation = state.presentation!!

                PresentationHeader(
                    presentation = presentation,
                    isOwner = presentation.owner.userId == state.userProfile?.userId,
                    isPresentationStarted = state.isPresentationStarted,
                    onEditClick = { viewModel.showEditNameModal() },
                    onDeleteClick = { viewModel.showDeleteModal() },
                    onJoinClick = { goToTeleprompter(presentation.presentationId) },
                    hasContent = state.structure?.structure?.any { it.textPreview.isNotBlank() } == true,
                    isGroup = state.participants.size > 1
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ParticipantsCard(
                            participants = state.participants,
                            joinedUsers = state.joinedUsers,
                            presentation = presentation,
                            isOwner = presentation.owner.userId == state.userProfile?.userId,
                            onInviteClick = { viewModel.inviteParticipant() },
                            onDeleteParticipant = { viewModel.showDeleteParticipantDialog(it) }
                        )
                    }

                    item {
                        StructureCard(
                            structure = state.structure?.structure?.filter { it.textPreview.isNotBlank() }
                                ?: emptyList(),
                            totalWordsCount = state.structure?.totalWordsCount,
                            config = state.config,
                            participants = state.participants,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        if (state.isEditNameModalOpen) {
            EditPresentationNameModal(
                currentName = state.presentation?.name ?: "",
                onConfirm = { viewModel.updatePresentationName(it) },
                onDismiss = { viewModel.hideEditNameModal() }
            )
        }

        if (state.isDeleteModalOpen) {
            DeleteConfirmationModal(
                presentationName = state.presentation?.name ?: "",
                onConfirm = { viewModel.deletePresentation() },
                onDismiss = { viewModel.hideDeleteModal() }
            )
        }

        if (state.isInviteModalOpen) {
            InviteParticipantModal(
                inviteLink = state.inviteLink,
                onDismiss = { viewModel.hideInviteModal() }
            )
        }

        if (state.isPremiumModalOpen) {
            PremiumModal(onDismiss = { viewModel.hidePremiumModal() })
        }

        if (state.isDeleteParticipantDialogOpen) {
            ConfirmDeleteParticipantDialog(
                onConfirm = { viewModel.confirmDeleteParticipant() },
                onDismiss = { viewModel.hideDeleteParticipantDialog() }
            )
        }

        if (state.editProfileDialogOpen) {
            EditProfileDialog(
                onDismissRequest = { viewModel.hideEditProfileDialog() },
                onProfileUpdated = {
                    viewModel.fetchProfile()
                    viewModel.fetchPresentationData()
                }
            )
        }

        ErrorDialog(
            show = state.error,
            message = stringResource(R.string.error),
            onDismiss = {}
        )

    }
}

@Composable
fun PresentationHeader(
    presentation: Presentation,
    isOwner: Boolean,
    isPresentationStarted: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onJoinClick: () -> Unit,
    isGroup: Boolean,
    hasContent: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = presentation.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Green5E,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isPresentationStarted) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(12.dp)
                        .background(Green5E, CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                )
            }

            if (isOwner) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = Green5E
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = RedEA
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .background(BeigeE5, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(if (isGroup) R.string.presentation_type_group else R.string.presentation_type_individual),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gray59
                )
            }

            GreenButton(
                label = stringResource(R.string.join),
                onClick = onJoinClick,
                enabled = hasContent
            )

        }
    }
}


@Composable
fun ParticipantsCard(
    participants: List<Participant>,
    joinedUsers: List<JoinedUser>,
    presentation: Presentation,
    isOwner: Boolean,
    onInviteClick: () -> Unit,
    onDeleteParticipant: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.participants),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gray59
                )

                if (isOwner) {
                    BeigeButton(
                        label = stringResource(R.string.invite),
                        onClick = onInviteClick
                    )
                }
            }

            participants.forEach { participant ->
                ParticipantItem(
                    participant = participant,
                    presentation = presentation,
                    isJoined = joinedUsers.any { it.userId == participant.user.userId },
                    isOwner = isOwner,
                    onDeleteClick = { onDeleteParticipant(participant.participantId) }
                )
            }
        }
    }
}

@Composable
fun ParticipantItem(
    participant: Participant,
    presentation: Presentation,
    isJoined: Boolean,
    isOwner: Boolean,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            selectedLocalUri = null,
            avatarUrl = participant.user.avatar,
            firstName = participant.user.firstName,
            lastName = participant.user.lastName,
            size = 42.dp,
            defaultBackgroundColor = Color(participant.color.toColorInt())
        )

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = "${participant.user.firstName} ${participant.user.lastName}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(
                    if (participant.user.userId == presentation.owner.userId)
                        R.string.owner else R.string.participant
                ),
                fontSize = 13.sp,
                color = Gray59
            )
        }

        if (isJoined) {
            Box(
                modifier = Modifier
                    .background(GreenD3, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                Text(
                    text = stringResource(R.string.joined_presentation),
                    fontSize = 13.sp,
                    color = Green45
                )
            }
        }

        if (isOwner && participant.user.userId != presentation.owner.userId) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.delete),
                    tint = Gray59
                )
            }
        }
    }
}

@Composable
fun StructureCard(
    structure: List<PresentationPart>,
    totalWordsCount: Int?,
    config: PresentationsConfig?,
    participants: List<Participant>,
    viewModel: PresentationDetailsViewModel
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.presentation_structure),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gray59,
                    modifier = Modifier.weight(1f)
                )

                if (totalWordsCount != null && config != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_time),
                            contentDescription = null,
                            tint = Gray59,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = viewModel.formatDuration(context, totalWordsCount, config),
                            fontSize = 14.sp,
                            color = Gray59
                        )
                    }
                }
            }

            if (structure.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_structure_message),
                        fontSize = 16.sp,
                        color = Gray59,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                structure.forEachIndexed { index, part ->
                    StructurePartItem(
                        part = part,
                        index = index,
                        participants = participants
                    )
                }
            }
        }
    }
}

@Composable
fun StructurePartItem(
    part: PresentationPart,
    index: Int,
    participants: List<Participant>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BeigeF3)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .padding(end = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.part_title, index + 1),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val participantColor = participants
                        .find { it.user.userId == part.assignee.userId }
                        ?.color
                        ?.toColorInt()
                        ?.let { Color(it) }
                        ?: Green45

                    UserAvatar(
                        selectedLocalUri = null,
                        avatarUrl = part.assignee.avatar,
                        firstName = part.assignee.firstName,
                        lastName = part.assignee.lastName,
                        defaultBackgroundColor = participantColor,
                        size = 30.dp,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${part.assignee.firstName} ${part.assignee.lastName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                modifier = Modifier.weight(0.65f)
            ) {
                Text(
                    text = part.textPreview,
                    fontSize = 15.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationModal(
    presentationName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var inputName by remember { mutableStateOf("") }
    val isInputValid = inputName == presentationName

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.delete_modal_header),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedEA
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.delete_confirmation_message),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.enter_name_to_confirm),
                    fontSize = 14.sp,
                    color = Gray59,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    placeholder = presentationName
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BeigeButton(
                        label = stringResource(R.string.cancel),
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    RedButton(
                        label = stringResource(R.string.delete),
                        onClick = onConfirm,
                        enabled = isInputValid,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}