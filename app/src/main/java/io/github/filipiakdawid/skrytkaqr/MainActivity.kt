package io.github.filipiakdawid.skrytkaqr

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.filipiakdawid.skrytkaqr.ui.ParcelViewModel
import io.github.filipiakdawid.skrytkaqr.ui.Screen
import io.github.filipiakdawid.skrytkaqr.ui.qrDetailRoute
import io.github.filipiakdawid.skrytkaqr.ui.screens.AboutScreen
import io.github.filipiakdawid.skrytkaqr.ui.screens.ActiveScreen
import io.github.filipiakdawid.skrytkaqr.ui.screens.QrDetailScreen
import io.github.filipiakdawid.skrytkaqr.ui.screens.SettingsScreen
import io.github.filipiakdawid.skrytkaqr.ui.screens.TrashScreen
import io.github.filipiakdawid.skrytkaqr.ui.theme.SkrytkaQRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SkrytkaQRTheme { SkrytkaQRApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkrytkaQRApp() {
    val navController = rememberNavController()
    val viewModel: ParcelViewModel = viewModel()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
        }

    LaunchedEffect(Unit) {
        if (!viewModel.hasSmsPermission()) {
            permissionLauncher.launch(Manifest.permission.READ_SMS)
        }
    }

    val bottomNavItems =
        listOf(
            Triple(Screen.Active, stringResource(R.string.nav_active), Icons.Default.Inbox),
            Triple(Screen.Trash, stringResource(R.string.nav_trash), Icons.Default.Delete),
            Triple(Screen.Settings, stringResource(R.string.nav_settings), Icons.Default.Settings),
        )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar =
        currentDestination?.hierarchy?.any { d ->
            bottomNavItems.any { it.first.route == d.route }
        } == true

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { (screen, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Active.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Active.route) {
                ActiveScreen(
                    viewModel = viewModel,
                    onParcelClick = { parcel -> navController.navigate(qrDetailRoute(parcel.parcel.id)) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                )
            }
            composable(Screen.Trash.route) {
                TrashScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                )
            }
            composable(Screen.About.route) {
                val context = LocalContext.current
                val versionName =
                    remember {
                        context.packageManager
                            .getPackageInfo(context.packageName, 0)
                            .versionName.orEmpty()
                    }
                AboutScreen(versionName = versionName)
            }
            composable(
                route = Screen.QrDetail.route,
                arguments = listOf(navArgument("parcelId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val parcelId =
                    backStackEntry.arguments
                        ?.getLong("parcelId") ?: return@composable
                QrDetailScreen(
                    parcelId = parcelId,
                    viewModel = viewModel,
                    onTrashed = { navController.popBackStack() },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    }
}
