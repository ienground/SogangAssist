package net.ienlab.sogangassist.ui.navigation

import android.os.Bundle
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
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
                navigateToItemDetail = { navController.navigate("${LmsEditDestination.route}?${LmsEditDestination.itemIdArg}=${it}") }
            )
//                windowSize = windowSize,
//                onAction = { action ->
//                    when (action) {
//                        UiAction.NavigateToSettings -> {
//                            navController.navigate(SettingsDestination.route)
//                        }
//                        UiAction.NavigateToDeskclock -> {
//                            navController.navigate(DeskclockDestination.route)
//                        }
//                        UiAction.NavigateToGuide -> {
//                            navController.navigate(GuideDestination.route)
//                        }
//                    }
//                },
//                bundle = bundle
//            )
        }

        composable(
            route = LmsEditDestination.routeWithArgs,
            arguments = listOf(navArgument(LmsEditDestination.itemIdArg) { type = NavType.LongType} )
        ) {
            LmsEditScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}