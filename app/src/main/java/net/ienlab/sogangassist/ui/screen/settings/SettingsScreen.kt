package net.ienlab.sogangassist.ui.screen.settings

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.map
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.Pref
import net.ienlab.sogangassist.dataStore
import net.ienlab.sogangassist.ui.navigation.NavigationDestination
import net.ienlab.sogangassist.ui.navigation.SettingsNavigationGraph
import net.ienlab.sogangassist.ui.theme.AppTheme
import net.ienlab.sogangassist.ui.utils.AlertDialog

object SettingsDestination: NavigationDestination {
    override val route: String = "settings"
    val homeRoute: String = "${route}_home"
    val generalRoute: String = "${route}_general"
    val notiRoute: String = "${route}_notification"
    val infoRoute: String = "${route}_info"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    windowSize: WindowSizeClass,
    navigateBack: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var selectedRoute by rememberSaveable { mutableStateOf("") }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = true,
//                visible = currentDestination?.route != SettingsDestination.setHomeRoute,
                enter = expandVertically(animationSpec = tween(700)),
                exit = shrinkVertically(animationSpec = tween(700))
            ) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            backDispatcher?.onBackPressed()
                        }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    title = {}
                )
            }
        },
        modifier = Modifier
    ) {
        when (windowSize.widthSizeClass) {
            WindowWidthSizeClass.Compact -> {
                SettingsNavigationGraph(
                    windowSize = windowSize,
                    navController = navController,
                    modifier = Modifier.padding(it)
                )
            }
            WindowWidthSizeClass.Medium -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(it)
                ) {
                    SettingsHomeScreen(
                        navController = navController,
                        isSelectable = true,
                        selectedRoute = selectedRoute,
                        setSelectedRoute = { selectedRoute = it },
                        modifier = Modifier.weight(0.5f)
                    )
                    SettingsNavigationGraph(
                        windowSize = windowSize,
                        navController = navController,
                        enterTransition = fadeIn(animationSpec = tween(700)),
                        exitTransition = fadeOut(animationSpec = tween(700)),
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }
            WindowWidthSizeClass.Expanded -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(it)
                ) {
                    SettingsHomeScreen(
                        navController = navController,
                        isSelectable = true,
                        selectedRoute = selectedRoute,
                        setSelectedRoute = { selectedRoute = it },
                        modifier = Modifier.weight(0.4f)
                    )
                    SettingsNavigationGraph(
                        windowSize = windowSize,
                        navController = navController,
                        enterTransition = fadeIn(animationSpec = tween(700)),
                        exitTransition = fadeOut(animationSpec = tween(700)),
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }

    }
}

@Composable
fun SettingsHomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isSelectable: Boolean = false,
    selectedRoute: String = "",
    setSelectedRoute: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val datastore = context.dataStore
//    val iabStorage = context.iabStorage
    val isDynamicColor by datastore.data.map { it[Pref.Key.MATERIAL_YOU] ?: Pref.Default.MATERIAL_YOU }.collectAsState(initial = Pref.Default.MATERIAL_YOU)
//    val paidProOnce by iabStorage.data.map { it[Iab.Key.IAB_PRO_ONCE] ?: false }.collectAsState(initial = false)
//    val paidProMonth by iabStorage.data.map { it[Iab.Key.IAB_PRO_MONTH] ?: false }.collectAsState(initial = false)
    var showAppInfoDialog by remember { mutableStateOf(false) }

    val defaultCategories = listOf(
        SettingsCategory(
            icon = Icons.Rounded.Settings,
            title = stringResource(id = R.string.settings_general),
            content = getMenuContent(R.string.use_dynamic_colors, R.string.notify_when_lms, R.string.backup),
            route = SettingsDestination.generalRoute
        ),
        SettingsCategory(
            icon = Icons.Rounded.Notifications,
            title = stringResource(id = R.string.notifications),
            content = getMenuContent(R.string.dnd_time, R.string.notify_exam, R.string.morning_reminder),
            route = SettingsDestination.notiRoute
        ),
        SettingsCategory(
            icon = Icons.Rounded.Info,
            title = stringResource(id = R.string.info_and_ask),
            content = getMenuContent(R.string.changelog, R.string.ask_to_dev),
            route = SettingsDestination.infoRoute
        ),
    )
    val categories = remember { mutableStateListOf<SettingsCategory>() }

    LaunchedEffect(Unit) {
        categories.addAll(defaultCategories)
    }

    /*
    LaunchedEffect(paidProOnce, paidProOnce) {
        val existed = categories.any { it.route == SettingsDestination.proRoute }
        val category =
            SettingsCategory(
                icon = MyIconPack.Calarm,
                title = context.getString(R.string.calarm_pro),
                content = context.getString(R.string.calarm_pro_desc),
                route = SettingsDestination.proRoute
            )

        if ((!(paidProMonth || paidProOnce)) && !existed) {
            categories.add(0, category)
        } else if ((paidProMonth || paidProOnce) && existed) {
            categories.removeIf { it.route == SettingsDestination.proRoute }
        }
    }

    LaunchedEffect(isDevmodeEnabled) {
        val existed = categories.any { it.route == SettingsDestination.devRoute }
        if (isDevmodeEnabled && !existed) {
            categories.add(
                SettingsCategory(
                    icon = Icons.Rounded.DeveloperMode,
                    title = context.getString(R.string.dev_mode),
                    content = context.getString(R.string.dev_mode),
                    route = SettingsDestination.devRoute
                )
            )
        } else if (!isDevmodeEnabled && existed) {
            categories.removeIf { it.route == SettingsDestination.devRoute }
        }
    }

     */

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { showAppInfoDialog = true }
                .padding(vertical = 16.dp)
        ) {
            if (isDynamicColor) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .size(45.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = stringResource(id = R.string.real_app_name),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(32.dp)
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_icon_color),
                    contentDescription = stringResource(id = R.string.real_app_name),
                    modifier = Modifier.size(45.dp)
                )
            }
            Text(
                text = stringResource(id = R.string.real_app_name),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.versionName),
                color = MaterialTheme.colorScheme.primary,
            )
            Image(
                painter = painterResource(id = R.drawable.img_dev),
                contentDescription = stringResource(id = R.string.real_app_name),
                modifier = Modifier
                    .padding(top = 16.dp)
                    .width(140.dp)
            )
        }
        categories.fastForEach { category ->
            SettingsCategoryRow(
                icon = category.icon,
                title = category.title,
                content = category.content,
                isSelected = isSelectable && selectedRoute == category.route,
                onClick = {
                    navController.navigate(category.route)
                    setSelectedRoute(category.route)
                }
            )
        }
    }

    if (showAppInfoDialog) {
        AlertDialog(
            icon = Icons.Rounded.Apps,
            title = stringResource(id = R.string.real_app_name),
            content = {
                Text(text = stringResource(id = R.string.dev_ienlab))
            },
            onNeutral = null,
            onPositive = null,
            onNegative = { showAppInfoDialog = false },
            textNegative = stringResource(id = android.R.string.ok),
            textPositive = "",
            textNeutral = ""
        )
    }
}

@Composable
fun SettingsEmptyScreen(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.select_the_menu_settings),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

data class SettingsCategory(
    val icon: ImageVector,
    val title: String,
    val content: String,
    val route: String
)

@Composable
fun SettingsCategoryRow(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    content: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        animationSpec = tween(700),
        label = "background_color"
    )
    val iconBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(700),
        label = "icon_background_color"
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconBackgroundColor)
        ) {
            Icon(
                imageVector = icon, contentDescription = title,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .size(24.dp)
            )
        }
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = content,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )
        }
    }
}

@Composable
private fun getMenuContent(vararg ids: Int): String {
    val builder = StringBuilder()
    ids.forEachIndexed { index, id ->
        builder.append(stringResource(id))
        if (index != ids.lastIndex) {
            builder.append(" · ")
        }
    }
    return builder.toString()
}

@Preview(showBackground = true)
@Composable
private fun SettingsCategoryRowPreview() {
    AppTheme {
        SettingsCategoryRow(
            icon = Icons.Rounded.AddAPhoto,
            title = stringResource(id = R.string.settings),
            content = getMenuContent(R.string.use_dynamic_colors, R.string.settings_general),
            onClick = {}
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SettingsHomePreview() {
    AppTheme {
        SettingsHomeScreen(
            navController = rememberNavController()
        )
    }
}