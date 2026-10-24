package com.example.goalhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.goalhub.domain.model.Priority
import com.example.goalhub.domain.model.Status
import com.example.goalhub.ui.theme.PriorityCritical
import com.example.goalhub.ui.theme.PriorityHigh
import com.example.goalhub.ui.theme.PriorityLow
import com.example.goalhub.ui.theme.PriorityMedium
import com.example.goalhub.ui.theme.StatusArchived
import com.example.goalhub.ui.theme.StatusCompleted
import com.example.goalhub.ui.theme.StatusInProgress
import com.example.goalhub.ui.theme.StatusNotStarted
import com.example.goalhub.ui.theme.StatusOverdue
import com.example.goalhub.ui.theme.StatusPaused

private val StatusColors: Map<Status, Color> = mapOf(
    Status.NOT_STARTED to StatusNotStarted,
    Status.IN_PROGRESS to StatusInProgress,
    Status.COMPLETED to StatusCompleted,
    Status.PAUSED to StatusPaused,
    Status.ARCHIVED to StatusArchived,
    Status.OVERDUE to StatusOverdue,
)

@Composable
fun StatusChip(
    status: Status,
    modifier: Modifier = Modifier,
    onLightSurface: Boolean = true,
) {
    val color = StatusColors[status] ?: StatusNotStarted
    val text = if (status == Status.OVERDUE) "Overdue" else status.label
    val (bg, fg) = if (onLightSurface) {
        color.copy(alpha = 0.13f) to color
    } else {
        Color.White.copy(alpha = 0.22f) to Color.White
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (!onLightSurface) {
            androidx.compose.foundation.layout.Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

private val PriorityColors: Map<Priority, Color> = mapOf(
    Priority.LOW to PriorityLow,
    Priority.MEDIUM to PriorityMedium,
    Priority.HIGH to PriorityHigh,
    Priority.CRITICAL to PriorityCritical,
)

@Composable
fun PriorityBadge(
    priority: Priority,
    modifier: Modifier = Modifier,
) {
    val color = PriorityColors[priority] ?: PriorityMedium
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = priority.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

/** Small label chip for the category of an item. */
@Composable
fun CategoryChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onLightSurface: Boolean = true,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (onLightSurface) color.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (onLightSurface) color else Color.White,
        )
    }
}
