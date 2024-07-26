package net.ienlab.sogangassist.ui.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFilterNotNull
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.TAG
import net.ienlab.sogangassist.ui.theme.AppTheme
import net.ienlab.sogangassist.ui.theme.ColorSaturday
import net.ienlab.sogangassist.ui.theme.ColorSunday
import net.ienlab.sogangassist.ui.utils.Utils.UpdateEffect
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.TemporalField
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
    isFolded: Boolean,
    config: HorizontalCalendarConfig = HorizontalCalendarConfig()
) {
    val initialPage = (currentDate.year - config.yearRange.first) * 12 + currentDate.monthValue - 1
    var currentPage by remember { mutableIntStateOf(initialPage) }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { (config.yearRange.second - config.yearRange.first + 1) * 12 }
    )

    LaunchedEffect(Unit) {
        onSelectedDate(currentDate)
    }

    LaunchedEffect(pagerState.currentPage) {
        val addMonth = (pagerState.currentPage - currentPage).toLong()
        onAddMonth(addMonth)
        currentPage = pagerState.currentPage
    }

    UpdateEffect(selectedDate) {
        val page = (selectedDate.year - config.yearRange.first) * 12 + selectedDate.monthValue - 1
        currentPage = page
        pagerState.animateScrollToPage(page)
    }

    HorizontalPager(
        state = pagerState
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
    onSelectedDate: (LocalDate) -> Unit
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
                    modifier = Modifier.fillMaxWidth()
                        .background(if (selectedDate in list) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent, RoundedCornerShape(16.dp))
                ) {
                    list.forEach { day ->
                        CalendarDay(
                            date = day,
                            selected = selectedDate == day,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectedDate(day ?: list.fastFilterNotNull().first() ) }
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
    date: LocalDate?
) {
    Text(
        text = date?.dayOfMonth?.toString() ?: "",
        textAlign = TextAlign.Center,
        fontSize = if (date?.isEqual(LocalDate.now()) == true) 18.sp else 14.sp,
        fontWeight = if (date?.isEqual(LocalDate.now()) == true) FontWeight.Bold else FontWeight.Normal,
        color = when (date?.dayOfWeek) {
            DayOfWeek.SATURDAY -> ColorSaturday
            DayOfWeek.SUNDAY -> ColorSunday
            else -> MaterialTheme.colorScheme.onBackground
        },
        modifier = modifier
            .background(if (selected) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent, RoundedCornerShape(16.dp))
            .aspectRatio(1f)
            .wrapContentHeight(align = Alignment.CenterVertically)
    )
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}