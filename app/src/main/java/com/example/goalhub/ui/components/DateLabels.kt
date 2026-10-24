package com.example.goalhub.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.goalhub.util.rangeLabel
import com.example.goalhub.util.relativeLabel
import java.time.LocalDate

/** "Aug 15 → Aug 20" with a small calendar icon. */
@Composable
fun DateRangeLabel(
    start: LocalDate,
    end: LocalDate,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showRelative: Boolean = false,
    today: LocalDate = LocalDate.now(),
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Rounded.Event,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = start.rangeLabel(end),
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            fontWeight = FontWeight.Medium,
        )
        if (showRelative) {
            Spacer(Modifier.width(10.dp))
            Text(
                text = "· ${end.relativeLabel(today)}",
                style = MaterialTheme.typography.labelMedium,
                color = if (end < today) MaterialTheme.colorScheme.error else tint,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** "Due today" / "5 days left" / "3 days overdue". */
@Composable
fun DueLabel(
    date: LocalDate,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val label = date.relativeLabel(today)
    val color = when {
        date < today -> MaterialTheme.colorScheme.error
        date == today -> com.example.goalhub.ui.theme.StatusInProgress
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = modifier,
    )
}
