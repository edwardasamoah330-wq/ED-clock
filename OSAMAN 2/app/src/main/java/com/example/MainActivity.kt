package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.ActionFeedback
import com.example.ui.ChromaViewModel
import com.example.ui.clock.ColorEngine
import com.example.ui.clock.rememberLiveClockMillis
import com.example.ui.screens.ClockHomeScreen
import com.example.ui.screens.ExtrasStudioScreen
import com.example.ui.screens.ImmersiveScreensaverOverlay
import com.example.ui.screens.ScreensaverHubScreen
import com.example.ui.screens.WallpaperStudioScreen
import com.example.ui.theme.ChromaClockTheme

const val ROUTE_CLOCK = "clock"
const val ROUTE_WALLPAPERS = "wallpapers"
const val ROUTE_SCREENSAVER = "screensaver"
const val ROUTE_EXTRAS = "extras"

private data class NavDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val appDestinations = listOf(
    NavDestination(ROUTE_CLOCK, R.string.nav_clock, Icons.Filled.Schedule, Icons.Outlined.Schedule),
    NavDestination(ROUTE_WALLPAPERS, R.string.nav_wallpapers, Icons.Filled.Wallpaper, Icons.Outlined.Wallpaper),
    NavDestination(ROUTE_SCREENSAVER, R.string.nav_screensaver, Icons.Filled.Bedtime, Icons.Outlined.Bedtime),
    NavDestination(ROUTE_EXTRAS, R.string.nav_studio, Icons.Filled.Apps, Icons.Outlined.Apps)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChromaClockRoot()
        }
    }
}

@Composable
fun ChromaClockRoot(viewModel: ChromaViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val feedback by viewModel.feedback.collectAsStateWithLifecycle()

    val nowMillis = rememberLiveClockMillis()
    val palette = remember(nowMillis / 40L, settings) {
        ColorEngine.computePalette(nowMillis, settings)
    }

    var isScreensaverActive by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedback) {
        when (val f = feedback) {
            is ActionFeedback.Success -> {
                snackbarHostState.showSnackbar(f.message)
                viewModel.clearFeedback()
            }
            is ActionFeedback.Error -> {
                snackbarHostState.showSnackbar(f.message)
                viewModel.clearFeedback()
            }
            else -> Unit
        }
    }

    ChromaClockTheme(
        darkTheme = true,
        accentPrimary = palette.primary,
        accentSecondary = palette.secondary
    ) {
        if (isScreensaverActive) {
            ImmersiveScreensaverOverlay(
                nowMillis = nowMillis,
                settings = settings,
                palette = palette,
                onExit = { isScreensaverActive = false }
            )
        } else {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_CLOCK

            val navigateTo: (String) -> Unit = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useRail = maxWidth >= 700.dp

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        if (!useRail) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                            ) {
                                appDestinations.forEach { dest ->
                                    val selected = currentRoute == dest.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTo(dest.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = stringResource(dest.labelRes)
                                            )
                                        },
                                        label = { Text(stringResource(dest.labelRes), maxLines = 1) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = palette.primary,
                                            selectedTextColor = palette.primary,
                                            indicatorColor = palette.primary.copy(alpha = 0.18f)
                                        ),
                                        modifier = Modifier.testTag("nav_${dest.route}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                appDestinations.forEach { dest ->
                                    val selected = currentRoute == dest.route
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateTo(dest.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = stringResource(dest.labelRes)
                                            )
                                        },
                                        label = { Text(stringResource(dest.labelRes)) },
                                        modifier = Modifier.testTag("rail_${dest.route}")
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            NavHost(
                                navController = navController,
                                startDestination = ROUTE_CLOCK,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable(ROUTE_CLOCK) {
                                    ClockHomeScreen(
                                        nowMillis = nowMillis,
                                        settings = settings,
                                        palette = palette,
                                        viewModel = viewModel,
                                        onLaunchScreensaver = { isScreensaverActive = true }
                                    )
                                }
                                composable(ROUTE_WALLPAPERS) {
                                    WallpaperStudioScreen(
                                        nowMillis = nowMillis,
                                        settings = settings,
                                        palette = palette,
                                        viewModel = viewModel
                                    )
                                }
                                composable(ROUTE_SCREENSAVER) {
                                    ScreensaverHubScreen(
                                        nowMillis = nowMillis,
                                        settings = settings,
                                        palette = palette,
                                        viewModel = viewModel,
                                        onLaunchScreensaver = { isScreensaverActive = true }
                                    )
                                }
                                composable(ROUTE_EXTRAS) {
                                    ExtrasStudioScreen(
                                        nowMillis = nowMillis,
                                        settings = settings,
                                        timerState = timerState,
                                        palette = palette,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
