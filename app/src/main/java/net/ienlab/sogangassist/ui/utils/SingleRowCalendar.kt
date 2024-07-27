package net.ienlab.sogangassist.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.toRange
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.TAG
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
    scrollState: LazyListState,
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onSelectedDate: (LocalDate) -> Unit
) {
    var week by remember { mutableStateOf<List<LocalDate>>(listOf()) }

    LaunchedEffect(selectedDate) {
        val _week = arrayListOf<LocalDate>()
        (0 until 7).forEach { _week.add(selectedDate.plusDays(-selectedDate.dayOfWeek.value % 7L + it)) }
        week = _week
    }

    LazyRow(state = scrollState, modifier = modifier) {
        items(items = week, key = { it.dayOfWeek }) {
            SingleRowCalendarItem(
                date = it,
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onSelectedDate = onSelectedDate,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        item {
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
fun SingleRowCalendarItem(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    date: LocalDate,
    selectedDate: LocalDate,
    onSelectedDate: (LocalDate) -> Unit
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

@Preview(showBackground = true)
@Composable
private fun SingleRowCalendarPreview() {
    AppTheme {
        Column {
            SingleRowCalendar(
                scrollState = rememberLazyListState(),
                currentMonth = YearMonth.now(),
                selectedDate = LocalDate.now(),
                onSelectedDate = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}