package com.example.goalhub.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.goalhub.presentation.calendar.CalendarScreen
import com.example.goalhub.presentation.create.CreateItemScreen
import com.example.goalhub.presentation.goals.GoalDetailScreen
import com.example.goalhub.presentation.goals.GoalsScreen
import com.example.goalhub.presentation.goals.ProjectDetailScreen
import com.example.goalhub.presentation.home.HomeScreen
import com.example.goalhub.presentation.more.MoreScreen
import com.example.goalhub.presentation.targets.TargetDetailScreen
import com.example.goalhub.presentation.targets.TargetsScreen
import com.example.goalhub.presentation.tasks.TasksScreen
import com.example.goalhub.ui.components.CreateItemType
import com.example.goalhub.ui.components.CreateMenuFab
import com.example.goalhub.ui.theme.Primary
import com.example.goalhub.ui.theme.PrimarySoft

object Routes {
    const val HOME = "home"
    const val GOALS = "goals"
    const val TARGETS = "targets"
    const val TASKS = "tasks"
    const val MORE = "more"
    const val CALENDAR = "calendar"

    const val GOAL_PATTERN = "goal/{goalId}"
    fun goal(goalId: Long) = "goal/$goalId"

    const val PROJECT_PATTERN = "project/{projectId}"
    fun project(projectId: Long) = "project/$projectId"

    const val TARGET_PATTERN = "target/{targetId}"
    fun target(targetId: Long) = "target/$targetId"

    const val CREATE_PATTERN = "create?type={type}&id={id}&goal={goal}&project={project}&target={target}"

    fun create(
        type: CreateItemType,
        id: Long? = null,
        goal: Long? = null,
        project: Long? = null,
        target: Long? = null,
    ): String = buildString {
        append("create?type=${type.name.lowercase()}")
        append("&id=${id ?: -1}")
        append("&goal=${goal ?: -1}")
        append("&project=${project ?: -1}")
        append("&target=${target ?: -1}")
    }

    private val tabRoutes = setOf(HOME, GOALS, TARGETS, TASKS, MORE)
    fun isTabRoute(route: String?): Boolean = route in tabRoutes
}

enum class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    HOME(Routes.HOME, "Home", Icons.Rounded.Home),
    GOALS(Routes.GOALS, "Goals", Icons.Rounded.Flag),
    TARGETS(Routes.TARGETS, "Targets", Icons.Rounded.CheckCircle),
    TASKS(Routes.TASKS, "Tasks", Icons.Rounded.TaskAlt),
    MORE(Routes.MORE, "More", Icons.Rounded.MoreHoriz),
}

@Composable
fun GoalHubApp() {
    val navController = rememberNavController()
    GoalHubNavHost(navController)
}

@Composable
private fun GoalHubNavHost(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = Routes.isTabRoute(currentRoute)
    // The "+" FAB is a create action — it belongs on the content tabs, not on
    // the More/settings page where it would float over the list.
    val showFab = showBottomBar && currentRoute != Routes.MORE

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                GoalHubBottomBar(navController, currentRoute)
            }
        },
        floatingActionButton = {
            if (showFab) {
                CreateMenuFab(
                    onCreate = { type ->
                        navController.navigate(Routes.create(type)) {
                            launchSingleTop = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenGoal = { navController.navigate(Routes.goal(it)) },
                    onOpenTarget = { navController.navigate(Routes.target(it)) },
                    onOpenCalendar = { navController.navigate(Routes.CALENDAR) },
                    onSeeAllGoals = { navController.navigate(Routes.GOALS) { launchSingleTop = true } },
                    onSeeAllTasks = { navController.navigate(Routes.TASKS) { launchSingleTop = true } },
                )
            }
            composable(Routes.GOALS) {
                GoalsScreen(
                    onOpenGoal = { navController.navigate(Routes.goal(it)) },
                    onOpenProject = { navController.navigate(Routes.project(it)) },
                    onCreate = { type, goalId -> navController.navigate(Routes.create(type, goal = goalId)) },
                )
            }
            composable(Routes.TARGETS) {
                TargetsScreen(
                    onOpenTarget = { navController.navigate(Routes.target(it)) },
                )
            }
            composable(Routes.TASKS) {
                TasksScreen(
                    onOpenTask = { navController.navigate(Routes.create(CreateItemType.TASK, id = it)) },
                )
            }
            composable(Routes.MORE) {
                MoreScreen()
            }
            composable(Routes.CALENDAR) {
                CalendarScreen(
                    onBack = { navController.popBackStack() },
                    onOpenTarget = { navController.navigate(Routes.target(it)) },
                    onOpenTask = { navController.navigate(Routes.create(CreateItemType.TASK, id = it)) },
                )
            }
            composable(
                route = Routes.GOAL_PATTERN,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType }),
            ) { entry ->
                val goalId = entry.arguments?.getLong("goalId") ?: return@composable
                GoalDetailScreen(
                    goalId = goalId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.create(CreateItemType.GOAL, id = goalId)) },
                    onDelete = { navController.popBackStack() },
                    onOpenTarget = { navController.navigate(Routes.target(it)) },
                    onOpenProject = { navController.navigate(Routes.project(it)) },
                    onCreateTarget = { navController.navigate(Routes.create(CreateItemType.TARGET, goal = goalId)) },
                    onCreateProject = { navController.navigate(Routes.create(CreateItemType.PROJECT, goal = goalId)) },
                    onCreateTask = { navController.navigate(Routes.create(CreateItemType.TASK, goal = goalId)) },
                )
            }
            composable(
                route = Routes.PROJECT_PATTERN,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType }),
            ) { entry ->
                val projectId = entry.arguments?.getLong("projectId") ?: return@composable
                ProjectDetailScreen(
                    projectId = projectId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.create(CreateItemType.PROJECT, id = projectId)) },
                    onDelete = { navController.popBackStack() },
                    onOpenGoal = { goalId -> navController.navigate(Routes.goal(goalId)) },
                    onOpenTarget = { navController.navigate(Routes.target(it)) },
                    onCreateTarget = { navController.navigate(Routes.create(CreateItemType.TARGET, project = projectId)) },
                    onCreateTask = { navController.navigate(Routes.create(CreateItemType.TASK, project = projectId)) },
                )
            }
            composable(
                route = Routes.TARGET_PATTERN,
                arguments = listOf(navArgument("targetId") { type = NavType.LongType }),
            ) { entry ->
                val targetId = entry.arguments?.getLong("targetId") ?: return@composable
                TargetDetailScreen(
                    targetId = targetId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.create(CreateItemType.TARGET, id = targetId)) },
                    onDelete = { navController.popBackStack() },
                    onOpenGoal = { navController.navigate(Routes.goal(it)) },
                    onOpenProject = { navController.navigate(Routes.project(it)) },
                    onCreateTask = { navController.navigate(Routes.create(CreateItemType.TASK, target = targetId)) },
                )
            }
            composable(
                route = Routes.CREATE_PATTERN,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("id") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("goal") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("project") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("target") { type = NavType.LongType; defaultValue = -1L },
                ),
            ) { entry ->
                val args = entry.arguments
                val type = CreateItemType.entries.firstOrNull {
                    it.name.lowercase() == args?.getString("type")
                } ?: CreateItemType.TASK
                val id = args?.getLong("id")?.takeIf { it > 0 }
                val goal = args?.getLong("goal")?.takeIf { it > 0 }
                val project = args?.getLong("project")?.takeIf { it > 0 }
                val target = args?.getLong("target")?.takeIf { it > 0 }
                CreateItemScreen(
                    type = type,
                    editId = id,
                    goalLink = goal,
                    projectLink = project,
                    targetLink = target,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun GoalHubBottomBar(
    navController: NavHostController,
    currentRoute: String?,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        BottomNavItem.entries.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = PrimarySoft,
                ),
            )
        }
    }
}
