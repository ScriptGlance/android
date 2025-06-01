package com.scriptglance.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.scriptglance.R
import com.scriptglance.ui.theme.Green45
import com.scriptglance.ui.theme.RedEA

@Composable
fun ErrorDialog(
    show: Boolean,
    message: String,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.error_title)
) {
    if (!show) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_error),
                        contentDescription = null,
                        tint = RedEA,
                        modifier = Modifier.size(33.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(title, fontSize = 26.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(16.dp))
                Text(message, fontSize = 18.sp, textAlign = TextAlign.Center, color = Green45)
                Spacer(Modifier.height(18.dp))
                BeigeButton(
                    onClick = onDismiss,
                    label = stringResource(R.string.ok),
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}