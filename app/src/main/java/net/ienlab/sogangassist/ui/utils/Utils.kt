package net.ienlab.sogangassist.ui.utils

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.icon.MyIconPack
import net.ienlab.sogangassist.icon.myiconpack.Assignment
import net.ienlab.sogangassist.icon.myiconpack.LiveClass
import net.ienlab.sogangassist.icon.myiconpack.Team
import net.ienlab.sogangassist.icon.myiconpack.Test
import net.ienlab.sogangassist.icon.myiconpack.Video
import net.ienlab.sogangassist.icon.myiconpack.VideoSup
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min

object Utils {
    @Composable
    fun UpdateEffect(key: Any, block: suspend CoroutineScope.() -> Unit) {
        var isTriggered by remember { mutableStateOf(false) }

        LaunchedEffect(key) {
            if (isTriggered) {
                block()
            } else {
                isTriggered = true
            }
        }
    }

    fun LazyListState.lastVisibleItemIndex(): Int = if (layoutInfo.visibleItemsInfo.isNotEmpty()) layoutInfo.visibleItemsInfo.last().index else -1
    fun LazyListState.lastVisibleItemScrollOffset(): Int = if (layoutInfo.visibleItemsInfo.isNotEmpty()) layoutInfo.visibleItemsInfo.last().offset else -1

    fun LazyListState.isFirstItemDisappear(): Boolean = (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0)
    fun LazyListState.isLastItemDisappear(lastIndex: Int): Boolean = (lastVisibleItemIndex() == lastIndex && lastVisibleItemScrollOffset() == layoutInfo.viewportEndOffset - layoutInfo.visibleItemsInfo.first().size)

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
        val dateFormat = DateTimeFormatter.ofPattern(stringResource(id = R.string.dateFormat))
        val dateFormatNoYear = DateTimeFormatter.ofPattern(stringResource(id = R.string.dateFormatNoYear))
        val current = LocalDate.now()
        return if (date.year == current.year) {
            when {
                date.isEqual(current) -> stringResource(R.string.today)
                date.isAfter(current) -> stringResource(R.string.tomorrow)
                date.isBefore(current) -> stringResource(R.string.yesterday)
                else -> date.format(dateFormatNoYear)
            }
        } else {
            date.format(dateFormat)
        }
    }

    @Composable
    fun animateAlignmentAsState(
        targetAlignment: Alignment,
    ): State<Alignment> {
        val biased = targetAlignment as BiasAlignment
        val horizontal by animateFloatAsState(biased.horizontalBias, label = "horizontal")
        val vertical by animateFloatAsState(biased.verticalBias, label = "vertical")
        return derivedStateOf { BiasAlignment(horizontal, vertical) }
    }
}