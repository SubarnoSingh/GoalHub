package com.example.goalhub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.goalhub.ui.theme.Primary

/**
 * Circular checkbox with a springy little check. Use it everywhere completion
 * is toggled (tasks, subtasks, milestones) for a consistent feel.
 */
@Composable
fun RoundCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    activeColor: Color = Primary,
) {
    val fill by animateColorAsState(
        targetValue = if (checked) activeColor else Color.Transparent,
        animationSpec = tween(200),
        label = "checkboxFill",
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) activeColor else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "checkboxBorder",
    )
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(fill)
            .border(2.dp, borderColor, CircleShape)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = fadeIn(tween(120)) + scaleIn(initialScale = 0.3f, animationSpec = tween(220)),
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Rounded.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.58f),
            )
        }
    }
}
