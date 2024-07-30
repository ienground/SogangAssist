package net.ienlab.sogangassist.ui.utils

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.core.util.toRange
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.TAG
import net.ienlab.sogangassist.ui.screen.edit.LmsDetails
import net.ienlab.sogangassist.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SingleRowCalendar(
    modifier: Modifier = Modifier,
    windowSize: WindowSizeClass,
    scrollState: LazyListState,
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onSelectedDate: (LocalDate) -> Unit,
    lmsMap: Map<LocalDate, List<LmsDetails>>
) {
    var week by remember { mutableStateOf<List<LocalDate>>(listOf()) }

    LaunchedEffect(selectedDate) {
        val _week = arrayListOf<LocalDate>()
        (0 until 7).forEach { _week.add(selectedDate.plusDays(-selectedDate.dayOfWeek.value % 7L + it)) }
        week = _week
    }

    LazyRow(
        state = scrollState,
        horizontalArrangement = Arrangement.spacedBy(if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) 0.dp else 16.dp),
        modifier = modifier
    ) {
        items(items = week, key = { it.dayOfWeek }) { date ->
            SingleRowCalendarItem(
                date = date,
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onSelectedDate = onSelectedDate,
                count = lmsMap[date]?.filter { !it.isFinished }?.size ?: 0,
                modifier = Modifier.padding(start = if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) 16.dp else 0.dp)
            )
        }
        item {
            Spacer(modifier = Modifier.width(if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) 16.dp else 0.dp))
        }
    }
}

@Composable
fun SingleRowCalendarItem(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    date: LocalDate,
    selectedDate: LocalDate,
    onSelectedDate: (LocalDate) -> Unit,
    count: Int
) {
    val dayFormat = DateTimeFormatter.ofPattern("EEE")
    val monthFormat = DateTimeFormatter.ofPattern("M")
    val dateFormat = DateTimeFormatter.ofPattern("dd")

    val cardContainerColor by animateColorAsState(targetValue = if (date.compareTo(selectedDate) == 0) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer, label = "")

    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = cardContainerColor), modifier = modifier.size(80.dp)) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxSize()
            .clickable {
                onSelectedDate(date)
            }) {
            Text(text = date.format(dayFormat))
            BadgedBox(
                badge = {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = count != 0,
                        enter = fadeIn(tween(700)),
                        exit = fadeOut(tween(700))
                    ) {
                        Badge(
                            modifier = Modifier.size(16.dp)
                        ) {
                            AnimatedContent(
                                targetState = count,
                                label = "calendar_badge"
                            ) {
                                Text(
                                    text = it.toString(),
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    AnimatedVisibility(
                        visible = date.month != currentMonth.month
                    ) {
                        Text(text = "${date.format(monthFormat)} /", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.wrapContentHeight(align = Alignment.Top))
                    }
                    Text(text = date.format(dateFormat), fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.wrapContentHeight(align = Alignment.Top))
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SingleRowCalendarPreview() {
    AppTheme {
        Column {
            SingleRowCalendar(
                windowSize = previewDeviceSize(),
                scrollState = rememberLazyListState(),
                currentMonth = YearMonth.now(),
                selectedDate = LocalDate.now(),
                onSelectedDate = {},
                lmsMap = mapOf(
                    LocalDate.now() to listOf(LmsDetails(), LmsDetails()),
                    LocalDate.now().plusDays(4) to listOf(LmsDetails(), LmsDetails()),
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}