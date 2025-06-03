package com.scriptglance.ui.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.scriptglance.ui.theme.*

enum class ButtonVariant {
    Green, White, Beige, Gray, Red
}

data class AppButtonColors(
    val background: androidx.compose.ui.graphics.Color,
    val content: androidx.compose.ui.graphics.Color,
    val disabledBackground: androidx.compose.ui.graphics.Color,
    val disabledContent: androidx.compose.ui.graphics.Color
)

fun buttonColors(variant: ButtonVariant): AppButtonColors = when (variant) {
    ButtonVariant.Green -> AppButtonColors(
        background = Green5E,
        content = White,
        disabledBackground = BeigeC1,
        disabledContent = Gray75
    )
    ButtonVariant.White -> AppButtonColors(
        background = White,
        content = Green45,
        disabledBackground = GrayF8,
        disabledContent = GrayBF
    )
    ButtonVariant.Beige -> AppButtonColors(
        background = BeigeE5,
        content = Green45,
        disabledBackground = BeigeC1,
        disabledContent = Gray75
    )
    ButtonVariant.Gray -> AppButtonColors(
        background = GrayE3,
        content = Black,
        disabledBackground = GrayBA,
        disabledContent = Black
    )
    ButtonVariant.Red -> AppButtonColors(
        background = RedEA,
        content = White,
        disabledBackground = RedC8,
        disabledContent = GrayE7
    )
}

@Composable
fun AppButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.White,
    enabled: Boolean = true,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val colors = buttonColors(variant)

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.background,
            contentColor = colors.content,
            disabledContainerColor = colors.disabledBackground,
            disabledContentColor = colors.disabledContent
        ),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = label,
            color = if (enabled) colors.content else colors.disabledContent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        content?.invoke(this)
    }
}

@Composable
fun GreenButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable RowScope.() -> Unit)? = null
) = AppButton(label, onClick, modifier, ButtonVariant.Green, enabled, content)

@Composable
fun WhiteButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable RowScope.() -> Unit)? = null
) = AppButton(label, onClick, modifier, ButtonVariant.White, enabled, content)

@Composable
fun GrayButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable RowScope.() -> Unit)? = null
) = AppButton(label, onClick, modifier, ButtonVariant.Gray, enabled, content)

@Composable
fun BeigeButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable RowScope.() -> Unit)? = null
) = AppButton(label, onClick, modifier, ButtonVariant.Beige, enabled, content)

@Composable
fun RedButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable RowScope.() -> Unit)? = null
) = AppButton(label, onClick, modifier, ButtonVariant.Red, enabled, content)
