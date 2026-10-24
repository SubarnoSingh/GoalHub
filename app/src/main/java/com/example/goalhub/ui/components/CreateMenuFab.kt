package com.example.goalhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.goalhub.ui.theme.Gradients
import com.example.goalhub.ui.theme.Ink
import com.example.goalhub.ui.theme.MutedInk
import com.example.goalhub.ui.theme.SoftShadow

enum class CreateItemType(val label: String, val icon: ImageVector) {
    GOAL("Goal", Icons.Rounded.Flag),
    PROJECT("Project", Icons.Rounded.RocketLaunch),
    TARGET("Target", Icons.Rounded.CheckCircle),
    TASK("Task", Icons.Rounded.TaskAlt),
}

/**
 * The central "+" action. Tapping it slides up a calm bottom sheet with the
 * four things you can create: Goal / Project / Target / Task.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMenuFab(
    onCreate: (CreateItemType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = modifier
            .size(62.dp)
            .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = SoftShadow, spotColor = SoftShadow)
            .clip(CircleShape)
            .background(Brush.linearGradient(Gradients.Work))
            .clickable { showSheet = true },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Create",
            tint = Color.White,
            modifier = Modifier.size(30.dp),
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
            ) {
                Text(
                    text = "Create",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "What are you working toward?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk,
                )
                Spacer(Modifier.height(20.dp))
                CreateItemType.entries.forEach { type ->
                    val colors = Gradients.forCategory(
                        when (type) {
                            CreateItemType.GOAL -> com.example.goalhub.domain.model.Category.STUDY
                            CreateItemType.PROJECT -> com.example.goalhub.domain.model.Category.PROJECTS
                            CreateItemType.TARGET -> com.example.goalhub.domain.model.Category.STUDY
                            CreateItemType.TASK -> com.example.goalhub.domain.model.Category.FINANCE
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                showSheet = false
                                onCreate(type)
                            }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(colors)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = type.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = type.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink,
                        )
                    }
                    if (type != CreateItemType.entries.last()) {
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}
