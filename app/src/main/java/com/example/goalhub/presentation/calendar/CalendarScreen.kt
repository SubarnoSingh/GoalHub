package com.example.goalhub.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.goalhub.domain.model.Target
import com.example.goalhub.domain.model.Task
import com.example.goalhub.ui.components.SoftCardPadded
import com.example.goalhub.ui.components.TargetCard
import com.example.goalhub.ui.components.TaskRow
import com.example.goalhub.ui.theme.Ink
import com.example.goalhub.ui.theme.MutedInk
import com.example.goalhub.ui.theme.Primary
import com.example.goalhub.ui.theme.PrimarySoft
import com.example.goalhub.ui.theme.StatusInProgress
import com.example.goalhub.ui.theme.StatusOverdue
import com.example.goalhub.util.formatFull
import com.example.goalhub.util.formatShort
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onOpenTarget: (Long) -> Unit,
    onOpenTask: (Long) -> Unit,
    viewModel: CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.goalhub.presentation.AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                // Month header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = viewModel::previousMonth) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous month")
                    }
                    Text(
                        text = uiState.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + uiState.month.year,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                    )
                    IconButton(onClick = viewModel::nextMonth) {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = "Next month")
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MutedInk,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            item {
                SoftCardPadded(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp, padding = PaddingValues(10.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        uiState.days.chunked(7).forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                week.forEach { date ->
                                    DayCell(
                                        date = date,
                                        inMonth = date.month == uiState.month.month,
                                        isToday = date == LocalDate.now(),
                                        isSelected = date == selected,
                                        markerCount = uiState.markerCount(date),
                                        onClick = { viewModel.select(date) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selected.formatFull(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                    )
                    TextButton(onClick = viewModel::goToToday) {
                        Text("Today")
                    }
                }
            }

            val dayTargets = uiState.targetsOn(selected)
            val dayTasks = uiState.tasksOn(selected)
            if (dayTargets.isEmpty() && dayTasks.isEmpty()) {
                item {
                    Text(
                        text = "Nothing scheduled for this day.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            } else {
                if (dayTargets.isNotEmpty()) {
                    item {
                        Text(
                            text = "Targets spanning this day",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MutedInk,
                        )
                    }
                    items(dayTargets, key = { "t${it.id}" }) { target ->
                        TargetCard(target = target, onClick = { onOpenTarget(target.id) })
                    }
                }
                if (dayTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Tasks due",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MutedInk,
                        )
                    }
                    items(dayTasks, key = { "k${it.id}" }) { task ->
                        TaskRow(task = task, onToggle = {}, onClick = { onOpenTask(task.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    markerCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                when {
                    isSelected -> Primary
                    isToday -> PrimarySoft
                    else -> Color.Transparent
                }
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    if (isToday && !isSelected) Primary else Color.Transparent
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    isToday -> Color.White
                    !inMonth -> MutedInk.copy(alpha = 0.4f)
                    else -> Ink
                },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(markerCount) { index ->
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == 0) StatusInProgress
                            else if (index == 1) com.example.goalhub.ui.theme.PriorityHigh
                            else Primary.copy(alpha = 0.5f)
                        ),
                )
            }
        }
    }
}
