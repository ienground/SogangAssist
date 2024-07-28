package net.ienlab.sogangassist.ui.screen.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.NotificationAdd
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.map
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.Pref
import net.ienlab.sogangassist.data.Permissions
import net.ienlab.sogangassist.dataStore
import net.ienlab.sogangassist.icon.MyIconPack
import net.ienlab.sogangassist.icon.myiconpack.Lms
import net.ienlab.sogangassist.service.LMSListenerService
import net.ienlab.sogangassist.ui.AppViewModelProvider
import net.ienlab.sogangassist.ui.navigation.NavigationDestination
import net.ienlab.sogangassist.ui.screen.home.list.LmsList
import net.ienlab.sogangassist.ui.screen.home.list.LmsListDestination
import net.ienlab.sogangassist.ui.theme.AppTheme
import net.ienlab.sogangassist.ui.utils.ActionMenuItem
import net.ienlab.sogangassist.ui.utils.ActionsMenu
import net.ienlab.sogangassist.ui.utils.AppBar
import net.ienlab.sogangassist.ui.utils.BaseDialog
import net.ienlab.sogangassist.ui.utils.HorizontalCalendar
import net.ienlab.sogangassist.ui.utils.PermissionDialog
import net.ienlab.sogangassist.ui.utils.SingleRowCalendar
import net.ienlab.sogangassist.ui.utils.Utils.UpdateEffect
import net.ienlab.sogangassist.ui.utils.Utils.lastVisibleItemIndex
import net.ienlab.sogangassist.utils.Utils.checkPermissions
import net.ienlab.sogangassist.utils.Utils.fromHtml
import net.ienlab.sogangassist.utils.Utils.readTextFromRaw
import net.ienlab.sogangassist.utils.Utils.timeInMillis
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

object HomeDestination: NavigationDestination {
    override val route: String = "home"
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToItemDetail: (Long, LocalDate) -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val calendarScrollState = rememberLazyListState(initialFirstVisibleItemScrollOffset = LocalDate.now().dayOfWeek.value % 7)
    var enabledSize by rememberSaveable { mutableIntStateOf(0) }
    val uiStateList by viewModel.uiStateList.collectAsState()

    // changelog
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0L))
    } else {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    var showChangelogDialog by remember { mutableStateOf(false) }
    val lastVersion by context.dataStore.data.map { it[Pref.Key.LAST_VERSION] ?: -1 }.collectAsState(initial = -999)
    val currentVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode.toInt() else info.versionCode

    // check init launch
    val isFirstLaunch by context.dataStore.data.map { it[Pref.Key.IS_FIRST_VISIT] ?: true }.collectAsState(initial = false)

    // permission
    val permissions = listOf(
        Permissions(icon = MyIconPack.Lms, title = stringResource(id = R.string.lms_install), content = stringResource(id = R.string.lms_install_desc), listOf(
            LMSListenerService.LMS_PACKAGE_NAME
        )),
        Permissions(icon = Icons.Rounded.Notifications, title = stringResource(id = R.string.read_notification), content = stringResource(id = R.string.read_notification_desc), listOf(
            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
        )),
        Permissions(icon = Icons.Rounded.NotificationAdd, title = stringResource(id = R.string.post_notification), content = stringResource(id = R.string.post_notification_desc), listOf(Manifest.permission.POST_NOTIFICATIONS)),
        Permissions(icon = Icons.Rounded.Notifications, title = stringResource(id = R.string.ignore_battery_optimize), content = stringResource(id = R.string.ignore_battery_optimize_desc), listOf(
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        )),
    )
    var showPermissionDialog by remember { mutableStateOf(!checkPermissions(context, permissions)) }

    LaunchedEffect(lastVersion) {
        if (lastVersion != -999) {
            if (currentVersion > lastVersion && !isFirstLaunch) {
                context.dataStore.edit { it[Pref.Key.LAST_VERSION] = currentVersion }
                showChangelogDialog = true

            }
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                title =
                when (enabledSize) {
                    0 -> stringResource(id = R.string.event_count_none)
                    1 -> stringResource(id = R.string.event_count_one)
                    else -> stringResource(id = R.string.event_count, enabledSize)
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        ActionsMenu(
                            items = listOf(
                                ActionMenuItem.IconMenuItem.ShownIfRoom(
                                    title = stringResource(id = R.string.move_today),
                                    icon = Icons.Rounded.Today,
                                    onClick = {
                                        viewModel.updateUiState(viewModel.uiState.item.copy(selectedDate = LocalDate.now()))
                                    }
                                ),
                                ActionMenuItem.IconMenuItem.ShownIfRoom(
                                    title = stringResource(id = R.string.settings),
                                    icon = Icons.Rounded.Settings,
                                    onClick = navigateToSettings
                                ),
                            ),
                            isOpen = false,
                            closeDropdown = {},
                            onToggleOverflow = {},
                            maxVisibleItems = 2
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(id = R.string.add_reminder))},
                icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = stringResource(R.string.add_reminder)) },
                onClick = { navigateToItemDetail(-1, viewModel.uiState.item.selectedDate) }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        modifier = modifier.statusBarsPadding()
    ) {
        HomeScreenBody(
            uiState = viewModel.uiState,
            uiStateList = uiStateList,
            onItemValueChanged = viewModel::updateUiState,
            navigateToItemDetail = navigateToItemDetail,
            onMonthChanged = viewModel::updateUiStateList,
            calendarScrollState = calendarScrollState,
            setEnabledSize = { if (it != enabledSize) enabledSize = it; },
            modifier = Modifier.padding(it)
        )
    }

    if (showPermissionDialog) {
        PermissionDialog(
            permissions = permissions,
            onConfirm = { showPermissionDialog = false }
        )
    }

    if (showChangelogDialog) {
        BaseDialog(
            icon = Icons.Rounded.Alarm,
            title = "${stringResource(id = R.string.app_name)} ${stringResource(id = R.string.versionName)} ${stringResource(id = R.string.update_log)}",
            content = {
                Text(
                    text = fromHtml(readTextFromRaw(context.resources, R.raw.changelog)).toString(),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) },
            onCancel = { showChangelogDialog = false },
            buttons = {
                Row(modifier = it) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { showChangelogDialog = false }) { Text(stringResource(id = R.string.close)) }
                }
            }
        )
    }
}

@Composable
fun HomeScreenBody(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    uiStateList: HomeUiStateList,
    onItemValueChanged: (HomeDetails) -> Unit,
    onMonthChanged: () -> Unit,
    navigateToItemDetail: (Long, LocalDate) -> Unit,
    setEnabledSize: (Int) -> Unit,
    calendarScrollState: LazyListState
) {
    val navController = rememberNavController()

    LaunchedEffect(uiState.item.selectedDate) {
        if (uiState.item.selectedDate.dayOfWeek.value % 7 !in calendarScrollState.firstVisibleItemIndex .. calendarScrollState.lastVisibleItemIndex()) {
            calendarScrollState.animateScrollToItem(uiState.item.selectedDate.dayOfWeek.value % 7)
        }
    }

    LaunchedEffect(uiState.item.currentMonth) {
        onMonthChanged()
    }

    UpdateEffect(uiState.item.selectedDate) {
        onItemValueChanged(uiState.item.copy(currentMonth = YearMonth.from(uiState.item.selectedDate)))
        navController.navigate("${LmsListDestination.route}?${LmsListDestination.timeArg}=${uiState.item.selectedDate.timeInMillis()}")
    }

    Column(
        modifier = modifier
    ) {
        val angle by animateFloatAsState(targetValue = if (uiState.item.isCalendarExpand) 180f else 0f, label = "angle", animationSpec = spring())
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { onItemValueChanged(uiState.item.copy(isCalendarExpand = !uiState.item.isCalendarExpand)) }
        ) {
            Text(
                text = uiState.item.currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f)
            )
            IconButton(onClick = {
                onItemValueChanged(uiState.item.copy(isCalendarExpand = !uiState.item.isCalendarExpand))
            }) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(angle)
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.item.isCalendarExpand,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            HorizontalCalendar(
                currentDate = uiState.item.selectedDate,
                selectedDate = uiState.item.selectedDate,
                onSelectedDate = {
                    onItemValueChanged(uiState.item.copy(selectedDate = it))
                },
                onAddMonth = {
                    onItemValueChanged(uiState.item.copy(currentMonth = uiState.item.currentMonth.plusMonths(it)))
                },
                lmsList = uiStateList.lmsList
            )
        }
        SingleRowCalendar(
            scrollState = calendarScrollState,
            currentMonth = uiState.item.currentMonth,
            selectedDate = uiState.item.selectedDate,
            onSelectedDate = {
                onItemValueChanged(uiState.item.copy(selectedDate = it))
            },
            lmsMap = uiStateList.lmsList.groupBy { it.endTime.toLocalDate() },
            modifier = Modifier.padding(top = 16.dp)
        )

        if (!LocalInspectionMode.current)
        NavHost(
            navController = navController,
            startDestination = "${LmsListDestination.route}?${LmsListDestination.timeArg}=${LocalDate.now().timeInMillis()}",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            composable(
                route = LmsListDestination.routeWithArgs,
                arguments = listOf(navArgument(LmsListDestination.timeArg) { type = NavType.LongType }),
                enterTransition = { fadeIn(tween(700)) },
                exitTransition = { fadeOut(tween(700)) }
            ) {
                LmsList(
                    navigateToItemDetail = navigateToItemDetail,
                    setEnabledSize = setEnabledSize
                )
            }
        }

    }
}


@Preview(showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreenBody(
            uiState = HomeUiState(
                item = HomeDetails(
                    isCalendarExpand = true
                )
            ),
            uiStateList = HomeUiStateList(),
            onItemValueChanged = {},
            onMonthChanged = {},
            navigateToItemDetail = { _, _ -> },
            calendarScrollState = rememberLazyListState(),
            setEnabledSize = {}
        )
    }
}