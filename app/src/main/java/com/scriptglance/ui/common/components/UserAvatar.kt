package com.scriptglance.ui.common.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.scriptglance.utils.constants.UPLOADS_BASE_URL

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    selectedLocalUri: Uri? = null,
    avatarUrl: String?,
    firstName: String?,
    lastName: String?,
    size: Dp,
    defaultBackgroundColor: Color = Color(0xFF5E7158),
    textColor: Color = Color.White
) {
    val modelToLoad: Any? = remember(selectedLocalUri, avatarUrl) {
        if (selectedLocalUri != null) {
            selectedLocalUri
        } else {
            avatarUrl?.let {
                if (it.startsWith("http://") || it.startsWith("https://")) {
                    it
                } else {
                    UPLOADS_BASE_URL + it.removePrefix("/")
                }
            }
        }
    }

    val initials = remember(firstName, lastName) {
        when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() ->
                "${firstName.first()}${lastName.first()}"
            !firstName.isNullOrBlank() -> "${firstName.first()}"
            !lastName.isNullOrBlank() -> "${lastName.first()}"
            else -> "?"
        }.uppercase()
    }

    val fontSize = with(LocalDensity.current) { (size.toPx() * 0.4f).toSp() }

    val initialsPlaceholder: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(defaultBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Box(modifier = modifier) {
        if (modelToLoad != null) {
            GlideImage(
                model = modelToLoad,
                contentDescription = "$firstName $lastName",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(2.dp, defaultBackgroundColor, CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            initialsPlaceholder()
        }
    }
}
