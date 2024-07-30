package net.ienlab.sogangassist.ui.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.ienlab.sogangassist.ui.screen.edit.LmsDetails
import net.ienlab.sogangassist.ui.theme.AppTheme
import net.ienlab.sogangassist.ui.theme.ColorSaturday
import net.ienlab.sogangassist.ui.theme.ColorSunday
import net.ienlab.sogangassist.ui.utils.Utils.UpdateEffect
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class HorizontalCalendarConfig(
    val yearRange: Pair<Int, Int> = Pair(2000, 2099)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalCalendar(
    modifier: Modifier = Modifier,
    currentDate: LocalDate = LocalDate.now(),
    selectedDate: LocalDate,
    onSelectedDate: (LocalDate) -> Unit,
    onAddMonth: (Long) -> Unit,
    lmsList: List<LmsDetails>,
    config: HorizontalCalendarConfig = HorizontalCalendarConfig()
) {
    val initialPage = (currentDate.year - config.yearRange.first) * 12 + currentDate.monthValue - 1
    var currentPage by rememberSaveable { mutableIntStateOf(initialPage) }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { (config.yearRange.second - config.yearRange.first + 1) * 12 }
    )

    LaunchedEffect(Unit) {
        onSelectedDate(currentDate)
    }
    LaunchedEffect(pagerState.currentPage) {
        val addMonth = (pagerState.currentPage - currentPage).toLong() // 현재 값이 두 번 빠짐.
        onAddMonth(addMonth)
        currentPage = pagerState.currentPage
        onSelectedDate(currentDate.plusMonths(addMonth))
    }
    UpdateEffect(selectedDate) {
        val page = (selectedDate.year - config.yearRange.first) * 12 + selectedDate.monthValue - 1
        currentPage = page
        pagerState.animateScrollToPage(page)
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        val date = LocalDate.of(
            config.yearRange.first + page / 12,
            page % 12 + 1,
            1
        )
        if (page in pagerState.currentPage - 1..pagerState.currentPage + 1) { // 페이징 성능 개선을 위한 조건문
            CalendarMonthItem(
                currentDate = date,
                selectedDate = selectedDate,
                onSelectedDate = onSelectedDate,
                lmsMap = lmsList.groupBy { it.endTime.toLocalDate() },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun CalendarHeader(
    modifier: Modifier = Modifier,
) {

}

@Composable
fun CalendarMonthItem(
    modifier: Modifier = Modifier,
    currentDate: LocalDate,
    selectedDate: LocalDate = LocalDate.now(),
    onSelectedDate: (LocalDate) -> Unit,
    lmsMap: Map<LocalDate, List<LmsDetails>>
) {
    val dayLast by remember { mutableIntStateOf(currentDate.lengthOfMonth())}
    val firstDayOfWeek by remember { mutableIntStateOf(currentDate.withDayOfMonth(1).dayOfWeek.value) }
    val lastDayOfWeek by remember { mutableIntStateOf(currentDate.withDayOfMonth(dayLast).dayOfWeek.value)}
    val days by remember { mutableStateOf(IntRange(1, dayLast).toList())}
    var dayList by remember { mutableStateOf(arrayListOf<List<LocalDate?>>())}

    LaunchedEffect(currentDate) {
        val _dayList = arrayListOf<LocalDate?>()
        for (i in 0 until firstDayOfWeek % 7) {
            _dayList.add(null)
        }
        for (day in days) {
            _dayList.add(currentDate.withDayOfMonth(day))
        }
        for (i in 0 until 6 - lastDayOfWeek % 7) {
            _dayList.add(null)
        }
        dayList = _dayList.chunked(7) as ArrayList
        if (dayList.size < 6) dayList.add(listOf(null, null, null, null, null, null, null))
    }

    Column(
        modifier = modifier
    ) {
        DayOfWeekView(
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn {
            items(items = dayList, key = { it }) { list ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selectedDate in list) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    list.forEach { date ->
                        CalendarDay(
                            date = date,
                            selected = selectedDate == date,
                            count = lmsMap[date]?.filter { !it.isFinished }?.size ?: 0,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(date != null) {
                                    date?.let { onSelectedDate(it) }
                                }
                                .weight(1 / 7f, fill = true)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayOfWeekView(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
    ) {
        DayOfWeek.entries.forEach { _dayOfWeek ->
            val dayOfWeek = DayOfWeek.of((_dayOfWeek.value + 5) % 7 + 1)
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = when (dayOfWeek) {
                    DayOfWeek.SATURDAY -> ColorSaturday
                    DayOfWeek.SUNDAY -> ColorSunday
                    else -> MaterialTheme.colorScheme.onBackground
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CalendarDay(
    modifier: Modifier = Modifier,
    selected: Boolean,
    date: LocalDate?,
    count: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .background(
                if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .aspectRatio(1f)
    ) {
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = date?.dayOfMonth?.toString() ?: "",
            textAlign = TextAlign.Center,
            fontSize = if (date?.isEqual(LocalDate.now()) == true) 18.sp else 14.sp,
            fontWeight = if (date?.isEqual(LocalDate.now()) == true) FontWeight.Bold else FontWeight.Normal,
            color =
            if (selected) MaterialTheme.colorScheme.onSecondary
            else when (date?.dayOfWeek) {
                DayOfWeek.SATURDAY -> ColorSaturday
                DayOfWeek.SUNDAY -> ColorSunday
                else -> MaterialTheme.colorScheme.onBackground
            },
            modifier = Modifier
        )
        val alpha by animateFloatAsState(targetValue = if (count >= 1) 1f else 0f, label = "alpha")
        Box(
            modifier = Modifier
                .alpha(alpha)
                .size(8.dp)
                .background(
                    if (selected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.error,
                    CircleShape
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarPreview() {
    AppTheme {
        Column {
            CalendarHeader(

            )
            CalendarMonthItem(
                currentDate = LocalDate.now(),
                onSelectedDate = {},
                lmsMap = mapOf(
                    LocalDate.now().plusDays(1) to listOf(LmsDetails(), LmsDetails()),
                    LocalDate.now() to listOf(LmsDetails(), LmsDetails()),
                    LocalDate.now().minusDays(2) to listOf(LmsDetails(), LmsDetails()),
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}