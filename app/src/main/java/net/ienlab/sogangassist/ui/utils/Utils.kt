package net.ienlab.sogangassist.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.CoroutineScope
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.TAG
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.icon.MyIconPack
import net.ienlab.sogangassist.icon.myiconpack.Assignment
import net.ienlab.sogangassist.icon.myiconpack.LiveClass
import net.ienlab.sogangassist.icon.myiconpack.Team
import net.ienlab.sogangassist.icon.myiconpack.Test
import net.ienlab.sogangassist.icon.myiconpack.Video
import net.ienlab.sogangassist.icon.myiconpack.VideoSup
import net.ienlab.sogangassist.ui.utils.Utils.pxToDp
import net.ienlab.sogangassist.utils.Utils.parseLongToLocalDateTime
import net.ienlab.sogangassist.utils.Utils.timeInMillis
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object Utils {
    @Composable
    fun dpToPx(size: Dp): Float = with (LocalDensity.current) { size.toPx() }
    @Composable
    fun pxToDp(size: Float): Dp = with (LocalDensity.current) { size.toDp() }
    @Composable
    fun pxToSp(size: Float): TextUnit = with (LocalDensity.current) { size.toSp() }

    fun LazyListState.lastVisibleItemIndex(): Int = if (layoutInfo.visibleItemsInfo.isNotEmpty()) layoutInfo.visibleItemsInfo.last().index else -1
    fun LazyListState.lastVisibleItemScrollOffset(): Int = if (layoutInfo.visibleItemsInfo.isNotEmpty()) layoutInfo.visibleItemsInfo.last().offset else -1

    fun LazyListState.isFirstItemDisappear(): Boolean = (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0)
    fun LazyListState.isLastItemDisappear(lastIndex: Int): Boolean = (lastVisibleItemIndex() == lastIndex && lastVisibleItemScrollOffset() == layoutInfo.viewportEndOffset - layoutInfo.visibleItemsInfo.first().size)

    @Composable
    fun UpdateEffect(key1: Any, block: suspend CoroutineScope.() -> Unit) {
        var isTriggered by remember { mutableStateOf(false) }

        LaunchedEffect(key1) {
            if (isTriggered) {
                block()
            } else {
                isTriggered = true
            }
        }
    }

    @Composable
    fun UpdateEffect(key1: Any, key2: Any, block: suspend CoroutineScope.() -> Unit) {
        var isTriggered by remember { mutableStateOf(false) }

        LaunchedEffect(key1, key2) {
            if (isTriggered) {
                block()
            } else {
                isTriggered = true
            }
        }
    }

    @Composable
    fun UpdateEffect(key1: Any, key2: Any, key3: Any, block: suspend CoroutineScope.() -> Unit) {
        var isTriggered by remember { mutableStateOf(false) }

        LaunchedEffect(key1, key2, key3) {
            if (isTriggered) {
                block()
            } else {
                isTriggered = true
            }
        }
    }

    @Composable
    fun UpdateEffect(vararg key: Any, block: suspend CoroutineScope.() -> Unit) {
        var isTriggered by remember { mutableStateOf(false) }

        LaunchedEffect(key) {
            if (isTriggered) {
                block()
            } else {
                isTriggered = true
            }
        }
    }


    @Composable
    fun leftTimeToLabel(endTime: LocalDateTime): String {
        val dayLeft = ChronoUnit.DAYS.between(endTime, LocalDateTime.now())
        val hourLeft = ChronoUnit.HOURS.between(endTime, LocalDateTime.now()) % 24
        val minuteLeft = ChronoUnit.MINUTES.between(endTime, LocalDateTime.now()) % 60

        return if (endTime.isBefore(LocalDateTime.now())) {
            when {
                dayLeft > 0 -> stringResource(id = R.string.time_past_day, dayLeft)
                hourLeft > 0 && minuteLeft > 0 -> stringResource(id = R.string.time_past_hour_min, hourLeft, minuteLeft)
                hourLeft > 0 -> stringResource(id = R.string.time_past_hour, hourLeft)
                else -> stringResource(id = R.string.time_past_min, minuteLeft)
            }
        } else {
            when {
                dayLeft < 0 -> stringResource(id = R.string.time_left_day, -dayLeft)
                hourLeft < 0 && minuteLeft < 0 -> stringResource(id = R.string.time_left_hour_min, -hourLeft, -minuteLeft)
                hourLeft < 0 -> stringResource(id = R.string.time_left_hour, -hourLeft)
                else -> stringResource(id = R.string.time_left_min, -minuteLeft)
            }
        }
    }

    @Composable
    fun getTypeIcon(type: Int) = when (type) {
        Lms.Type.LESSON -> MyIconPack.Video
        Lms.Type.SUP_LESSON -> MyIconPack.VideoSup
        Lms.Type.HOMEWORK -> MyIconPack.Assignment
        Lms.Type.EXAM -> MyIconPack.Test
        Lms.Type.TEAMWORK -> MyIconPack.Team
        Lms.Type.ZOOM -> MyIconPack.LiveClass
        else -> MyIconPack.Video
    }

    @Composable
    fun getDateLabel(date: LocalDate): String {
        val dateFormat = DateTimeFormatter.ofPattern(stringResource(id = R.string.date_format))
        val dateFormatNoYear = DateTimeFormatter.ofPattern(stringResource(id = R.string.date_format_no_year))
        val current = LocalDate.now()
        return if (date.year == current.year) {
            when (ChronoUnit.DAYS.between(current, date)) {
                0L -> stringResource(R.string.today)
                1L -> stringResource(R.string.tomorrow)
                -1L -> stringResource(R.string.yesterday)
                else -> date.format(dateFormatNoYear)
            }
        } else {
            date.format(dateFormat)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun rememberMyDatePickerState(
        @Suppress("AutoBoxing") initialSelectedDateMillis: Long? = null,
        @Suppress("AutoBoxing") initialDisplayedMonthMillis: Long? = initialSelectedDateMillis,
        yearRange: IntRange = DatePickerDefaults.YearRange,
        initialDisplayMode: DisplayMode = DisplayMode.Picker,
        selectableDates: SelectableDates = DatePickerDefaults.AllDates
    ): DatePickerState {
        return rememberDatePickerState(
            initialDisplayedMonthMillis = initialDisplayedMonthMillis,
            yearRange = yearRange,
            initialDisplayMode = initialDisplayMode,
            selectableDates = selectableDates,
            initialSelectedDateMillis = initialSelectedDateMillis?.let { parseLongToLocalDateTime(it).timeInMillis(zoneId = ZoneId.of("UTC")) }
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun previewDeviceSize(): WindowSizeClass {
    val dm = LocalContext.current.resources.displayMetrics
    val widthPixels = pxToDp(size = dm.widthPixels.toFloat())
    val heightPixels = pxToDp(size = dm.heightPixels.toFloat())

    return WindowSizeClass.calculateFromSize(DpSize(widthPixels, heightPixels))
}