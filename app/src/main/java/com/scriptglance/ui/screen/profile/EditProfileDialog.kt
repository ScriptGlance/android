package com.scriptglance.ui.screen.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scriptglance.R
import com.scriptglance.ui.common.components.UserAvatar
import com.scriptglance.ui.theme.Green3959
import com.scriptglance.ui.theme.White

@Composable
fun EditProfileDialog(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit,
    onProfileUpdated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onAvatarSelected(uri)
    }

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            Toast.makeText(
                context,
                context.getString(R.string.edit_profile_success_message),
                Toast.LENGTH_SHORT
            ).show()
            onProfileUpdated()
            onDismissRequest()
            viewModel.resetUpdateStatus()
        }
    }

    LaunchedEffect(uiState.hasUpdateError) {
        if (uiState.hasUpdateError) {
            Toast.makeText(
                context,
                context.getString(R.string.edit_profile_error_generic),
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetUpdateStatus()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!uiState.isUpdating) {
                onDismissRequest()
            }
        },
        containerColor = White,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = stringResource(R.string.edit_profile_dialog_title),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.isLoadingInitialData) {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 32.dp))
                } else {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        UserAvatar(
                            avatarUrl = if (uiState.selectedAvatarUri != null) null else uiState.currentAvatarUrl,
                            firstName = if (uiState.currentAvatarUrl == null) uiState.firstName else "",
                            lastName = if (uiState.currentAvatarUrl == null) uiState.lastName else "",
                            size = 100.dp,
                            contentDescription = stringResource(R.string.dashboard_avatar_content_description),
                            selectedLocalUri = uiState.selectedAvatarUri
                        )

                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(White)
                                .padding(0.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = White,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudUpload,
                                contentDescription = stringResource(R.string.edit_profile_upload_avatar_description),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    EditProfileTextField(
                        value = uiState.firstName,
                        onValueChange = viewModel::onFirstNameChanged,
                        label = stringResource(R.string.edit_profile_first_name_label)
                    )
                    Spacer(Modifier.height(16.dp))
                    EditProfileTextField(
                        value = uiState.lastName,
                        onValueChange = viewModel::onLastNameChanged,
                        label = stringResource(R.string.edit_profile_last_name_label)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.saveProfile(context) },
                enabled = !uiState.isUpdating && !uiState.isLoadingInitialData,
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(48.dp)
                    .background(Green3959, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.textButtonColors(contentColor = White)
            ) {
                if (uiState.isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        stringResource(R.string.edit_profile_save_button),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !uiState.isUpdating,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
                    .background(LightGray, RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.DarkGray)
            ) {
                Text(stringResource(R.string.button_cancel), fontWeight = FontWeight.Medium)
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp)
    )
}


@Composable
private fun EditProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green3959,
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
            focusedLabelColor = Green3959,
            cursorColor = Green3959,
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            disabledContainerColor = White,
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}