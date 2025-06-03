package com.scriptglance.ui.screen.presentation.presentationDetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.scriptglance.R
import com.scriptglance.ui.common.components.AppTextField
import com.scriptglance.ui.common.components.GrayButton
import com.scriptglance.ui.common.components.GreenButton
import com.scriptglance.ui.theme.Green45

@Composable
fun EditPresentationNameModal(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    var nameError by remember { mutableStateOf<String?>(null) }

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
                    text = stringResource(R.string.edit_title_modal_header),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green45
                )

                Spacer(modifier = Modifier.height(16.dp))

                val emptyNameError = stringResource(R.string.empty_title_error)
                AppTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        nameError =
                            if (it.isBlank()) emptyNameError else null
                    },
                    placeholder = stringResource(R.string.presentation_title_placeholder),
                    error = nameError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GrayButton(
                        label = stringResource(R.string.cancel),
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    val emptyNameError = stringResource(R.string.empty_title_error)
                    GreenButton(
                        label = stringResource(R.string.save),
                        onClick = {
                            if (newName.isNotBlank()) {
                                onConfirm(newName)
                            } else {
                                nameError = emptyNameError
                            }
                        },
                        enabled = newName.isNotBlank() && newName != currentName,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}