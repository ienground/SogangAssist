package net.ienlab.sogangassist.ui.navigation

import android.app.Activity
import android.os.Bundle
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import net.ienlab.sogangassist.ui.navigation.NavigationDestination
import net.ienlab.sogangassist.ui.screen.edit.LmsEditDestination
import net.ienlab.sogangassist.ui.screen.edit.LmsEditScreen
import net.ienlab.sogangassist.ui.screen.home.HomeDestination
import net.ienlab.sogangassist.ui.screen.home.HomeScreen
import net.ienlab.sogangassist.ui.screen.settings.SettingsDestination
import net.ienlab.sogangassist.ui.screen.settings.SettingsEmptyScreen
import net.ienlab.sogangassist.ui.screen.settings.SettingsHomeScreen
import net.ienlab.sogangassist.ui.screen.settings.SettingsScreen
import net.ienlab.sogangassist.ui.screen.settings.general.SettingsGeneralScreen
import net.ienlab.sogangassist.ui.screen.settings.info.SettingsInfoScreen
import net.ienlab.sogangassist.ui.screen.settings.info.SettingsPrivacyPolicyScreen
import net.ienlab.sogangassist.ui.screen.settings.notifications.SettingsNotificationsScreen

object RootDestination: NavigationDestination {
    override val route: String = "root"
}

sealed interface UiAction {
    data object NavigateToSettings: UiAction
    data object NavigateToDeskclock: UiAction
    data object NavigateToGuide: UiAction
}

typealias OnAction = (UiAction) -> Unit

@Composable
fun RootNavigationGraph(
    windowSize: WindowSizeClass,
    navController: NavHostController,
    bundle: Bundle,
) {
    NavHost(navController = navController, route = RootDestination.route, startDestination = HomeDestination.route) {
        composable(
            route = HomeDestination.route
        ) {
            HomeScreen(
                navigateToItemDetail = { id, date ->navController.navigate("${LmsEditDestination.route}?${LmsEditDestination.itemIdArg}=${id}&${LmsEditDestination.initDateArg}=${date}") },
                navigateToSettings = { navController.navigate(SettingsDestination.route) }
            )
        }

        composable(
            route = LmsEditDestination.routeWithArgs,
            arguments = listOf(navArgument(LmsEditDestination.itemIdArg) { type = NavType.LongType} )
        ) {
            LmsEditScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = SettingsDestination.route,
        ) {
            SettingsScreen(
                windowSize = windowSize,
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun SettingsNavigationGraph(
    modifier: Modifier = Modifier,
    windowSize: WindowSizeClass,
    navController: NavHostController,
    snackbarState: SnackbarHostState,
    enterTransition: EnterTransition = slideInHorizontally(tween(700), initialOffsetX = { it }),
    exitTransition: ExitTransition = slideOutHorizontally(tween(700), targetOffsetX = { it })
) {
    val context = LocalContext.current

    NavHost(navController = navController, route = SettingsDestination.route, startDestination = SettingsDestination.homeRoute, modifier = modifier) {
        composable(route = SettingsDestination.homeRoute) {
            when (windowSize.widthSizeClass) {
                WindowWidthSizeClass.Compact -> { SettingsHomeScreen(navController = navController) }
                WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> { SettingsEmptyScreen(modifier = Modifier.fillMaxSize()) }
            }
        }
        composable(
            route = SettingsDestination.notiRoute,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition }
        ) {
            SettingsNotificationsScreen()
        }
        composable(
            route = SettingsDestination.generalRoute,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition }
        ) {
            SettingsGeneralScreen(
                snackbarState = snackbarState
            )
        }
        composable(
            route = SettingsDestination.infoRoute,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition }
        ) {
            SettingsInfoScreen(
                navigateToPolicy = { navController.navigate(SettingsDestination.policyRoute) },
            )
        }
        composable(
            route = SettingsDestination.policyRoute,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition }
        ) {
            SettingsPrivacyPolicyScreen()
        }
        /*
        composable(
            route = SettingsDestination.policyRoute,
            enterTransition = { enterTransition },
            exitTransition = { exitTransition }
        ) {
            SettingsPrivacyPolicyScreen()
        }

         */
    }
}
