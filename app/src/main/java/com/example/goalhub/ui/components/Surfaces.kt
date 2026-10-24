package com.example.goalhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.goalhub.ui.theme.CardSurface
import com.example.goalhub.ui.theme.SoftLine
import com.example.goalhub.ui.theme.SoftShadow

/** Standard horizontal padding for screens. */
val ScreenHorizontalPadding: PaddingValues = PaddingValues(horizontal = 20.dp)

/** Standard content spacing between sections. */
val ScreenSpacing = 20.dp

/**
 * The signature card of the app: white (or surface) rounded rectangle with a
 * soft border and a gentle shadow. The reference design's "soft card" look.
 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    surface: Color = CardSurface,
    borderColor: Color = SoftLine,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val base = Modifier
        .shadow(elevation = elevation, shape = shape, ambientColor = SoftShadow, spotColor = SoftShadow)
        .clip(shape)
        .background(surface)
        .border(1.dp, borderColor, shape)
    if (onClick != null) {
        Box(modifier = base.clip(shape).clickable(onClick = onClick), content = content)
    } else {
        Box(modifier = base, content = content)
    }
}

/** Convenience wrapper: card with internal padding for text-heavy content. */
@Composable
fun SoftCardPadded(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    SoftCard(modifier = modifier, cornerRadius = cornerRadius, onClick = onClick) {
        Box(Modifier.padding(padding), content = content)
    }
}

/** Very light "tinted" container used for inline controls (search, chips area). */
@Composable
fun TintedSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color),
        content = content,
    )
}
