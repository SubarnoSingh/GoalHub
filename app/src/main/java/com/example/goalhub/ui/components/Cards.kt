package com.example.goalhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.goalhub.domain.model.Goal
import com.example.goalhub.domain.model.Milestone
import com.example.goalhub.domain.model.Note
import com.example.goalhub.domain.model.Project
import com.example.goalhub.domain.model.Status
import com.example.goalhub.domain.model.Subtask
import com.example.goalhub.domain.model.Target
import com.example.goalhub.domain.model.Task
import com.example.goalhub.ui.theme.Gradients
import com.example.goalhub.ui.theme.Ink
import com.example.goalhub.ui.theme.MutedInk
import com.example.goalhub.ui.theme.Primary
import com.example.goalhub.ui.theme.SoftLine
import com.example.goalhub.util.formatShort
import com.example.goalhub.util.rangeLabel
import com.example.goalhub.util.relativeLabel
import java.time.LocalDate

// ---------------------------------------------------------------------------
// Goals
// ---------------------------------------------------------------------------

/**
 * Large colorful goal card for the home dashboard's horizontal rail.
 * Gradient background, big white typography, animated progress.
 */
@Composable
fun GoalHeroCard(
    goal: Goal,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = Gradients.forGoal(goal.gradientIndex, goal.status == Status.COMPLETED)
    Box(
        modifier = modifier
            .width(268.dp)
            .height(168.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(colors))
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${goal.start.formatShort()} → ${goal.end.formatShort()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedProgressBar(
                    progress = goal.progress,
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    height = 9.dp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${goal.progress}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    if (goal.milestones.isNotEmpty()) {
                        Text(
                            text = "${goal.milestones.count { it.completed }}/${goal.milestones.size} milestones",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }
    }
}

/** Compact goal row used in the Goals tab. */
@Composable
fun GoalListCard(
    goal: Goal,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onMenu: (() -> Unit)? = null,
) {
    val colors = Gradients.forGoal(goal.gradientIndex, goal.status == Status.COMPLETED)
    SoftCardPadded(modifier = modifier.fillMaxWidth(), cornerRadius = 22.dp, onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    DateRangeLabel(start = goal.start, end = goal.end, showRelative = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(goal.status)
                    if (onMenu != null) {
                        IconButton(onClick = onMenu, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.MoreHoriz,
                                contentDescription = "More",
                                tint = MutedInk,
                            )
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "${goal.progress}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.first(),
                )
                AnimatedProgressBar(
                    progress = goal.progress,
                    modifier = Modifier.weight(1f),
                    height = 7.dp,
                    color = colors.first(),
                    trackColor = colors.first().copy(alpha = 0.15f),
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MutedInk,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Projects
// ---------------------------------------------------------------------------

@Composable
fun ProjectCard(
    project: Project,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = Gradients.forGoal(project.gradientIndex, project.status == Status.COMPLETED)
    SoftCardPadded(modifier = modifier.fillMaxWidth(), cornerRadius = 22.dp, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(colors)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.RocketLaunch,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "${project.start.formatShort()} → ${project.end.formatShort()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedInk,
                    )
                    Text(
                        text = "${project.progress}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.first(),
                    )
                    StatusChip(project.status)
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MutedInk,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Targets
// ---------------------------------------------------------------------------

@Composable
fun TargetCard(
    target: Target,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = Gradients.forGoal(target.gradientIndex, target.status == Status.COMPLETED)
    SoftCardPadded(modifier = modifier.fillMaxWidth(), cornerRadius = 22.dp, onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = target.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(10.dp))
                StatusChip(target.status)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                DateRangeLabel(start = target.start, end = target.end, showRelative = true)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "${target.progress}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.first(),
                )
                AnimatedProgressBar(
                    progress = target.progress,
                    modifier = Modifier.weight(1f),
                    height = 6.dp,
                    color = colors.first(),
                    trackColor = colors.first().copy(alpha = 0.15f),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tasks
// ---------------------------------------------------------------------------

@Composable
fun TaskRow(
    task: Task,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RoundCheckbox(
            checked = task.completed,
            onCheckedChange = { onToggle() },
            size = 24.dp,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (task.completed) MutedInk else Ink,
                textDecoration = if (task.completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (task.due != null) {
                Text(
                    text = task.due.relativeLabel(LocalDate.now()),
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        task.completed -> MutedInk.copy(alpha = 0.7f)
                        task.due < LocalDate.now() -> MaterialTheme.colorScheme.error
                        else -> MutedInk
                    },
                )
            }
        }
        PriorityBadge(task.priority)
    }
}

// ---------------------------------------------------------------------------
// Milestones / subtasks / notes
// ---------------------------------------------------------------------------

@Composable
fun MilestoneRow(
    milestone: Milestone,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RoundCheckbox(
            checked = milestone.completed,
            onCheckedChange = { onToggle() },
            size = 22.dp,
        )
        Text(
            text = milestone.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (milestone.completed) MutedInk else Ink,
            textDecoration = if (milestone.completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = "Delete",
                tint = MutedInk,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun SubtaskRow(
    subtask: Subtask,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RoundCheckbox(
            checked = subtask.completed,
            onCheckedChange = { onToggle() },
            size = 22.dp,
        )
        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (subtask.completed) MutedInk else Ink,
            textDecoration = if (subtask.completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = "Delete",
                tint = MutedInk,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun NoteRow(
    note: Note,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.StickyNote2,
            contentDescription = null,
            tint = Primary.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = note.text,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = "Delete",
                tint = MutedInk,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** "Part of: Learn Android Development" link row used on detail screens. */
@Composable
fun ParentLinkRow(
    label: String,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    SoftCardPadded(modifier = modifier.fillMaxWidth(), cornerRadius = 18.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                imageVector = Icons.Rounded.Flag,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(18.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedInk,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MutedInk,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
