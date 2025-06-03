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
import androidx.compose.foundation.layout.width
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
import com.bumptech.glide.integration.compose.placeholder
import com.scriptglance.R
import com.scriptglance.ui.common.components.AppButton
import com.scriptglance.ui.common.components.AppTextField
import com.scriptglance.ui.common.components.GrayButton
import com.scriptglance.ui.common.components.GreenButton
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
                            size = 70.dp,
                            selectedLocalUri = uiState.selectedAvatarUri
                        )

                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(25.dp)
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
                    AppTextField(
                        value = uiState.firstName,
                        onValueChange = viewModel::onFirstNameChanged,
                        placeholder = stringResource(R.string.edit_profile_first_name_label)
                    )
                    Spacer(Modifier.height(16.dp))
                    AppTextField(
                        value = uiState.lastName,
                        onValueChange = viewModel::onLastNameChanged,
                        placeholder = stringResource(R.string.edit_profile_last_name_label)
                    )
                }
            }
        },
        confirmButton = {
            GreenButton(
                onClick = { viewModel.saveProfile(context) },
                enabled = !uiState.isUpdating && !uiState.isLoadingInitialData,
                label = stringResource(R.string.edit_profile_save_button),
            )
        },
        dismissButton = {
            GrayButton(
                onClick = onDismissRequest,
                enabled = !uiState.isUpdating,
                label = stringResource(R.string.button_cancel),
            )
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp)
    )
}