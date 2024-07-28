package net.ienlab.sogangassist.ui.screen.settings.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.SwitchPref
import com.jamal.composeprefs3.ui.prefs.TextPref
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.Pref
import net.ienlab.sogangassist.dataStore
import net.ienlab.sogangassist.icon.MyIconPack
import net.ienlab.sogangassist.icon.myiconpack.Assignment
import net.ienlab.sogangassist.icon.myiconpack.LiveClass
import net.ienlab.sogangassist.icon.myiconpack.Test
import net.ienlab.sogangassist.icon.myiconpack.Video
import net.ienlab.sogangassist.ui.theme.AppTheme
import net.ienlab.sogangassist.ui.utils.AlertDialog
import net.ienlab.sogangassist.ui.utils.TimePickerDialog
import net.ienlab.sogangassist.ui.utils.Utils.UpdateEffect
import net.ienlab.sogangassist.utils.Utils.notifyToList
import net.ienlab.sogangassist.utils.Utils.setDayReminder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsNotificationsScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val datastore = context.dataStore
    val coroutineScope = rememberCoroutineScope()
    val timeFormatter = DateTimeFormatter.ofPattern(stringResource(id = R.string.apm_time_format))
    val hours = listOf(1, 2, 6, 12, 24)
    val minutes = listOf(3, 5, 10, 20, 30)

    var showDndTimeDialog by remember { mutableStateOf(false) }
    var showDndTimeSelectDialog by remember { mutableStateOf("") }
    var showNotifyTimeSelectDialog by remember { mutableStateOf("") }
    var showReminderTimeSelectDialog by remember { mutableStateOf("") }

    val dndCheck by datastore.data.map { it[Pref.Key.DND_CHECK] ?: Pref.Default.DND_CHECK }.collectAsState(initial = Pref.Default.DND_CHECK)
    val dndStartTime by datastore.data.map { it[Pref.Key.DND_START_TIME] ?: Pref.Default.DND_START_TIME }.collectAsState(initial = Pref.Default.DND_START_TIME)
    val dndEndTime by datastore.data.map { it[Pref.Key.DND_END_TIME] ?: Pref.Default.DND_END_TIME }.collectAsState(initial = Pref.Default.DND_END_TIME)
    var dndTempStartTime by remember { mutableIntStateOf(0) }
    var dndTempEndTime by remember { mutableIntStateOf(0) }

    val notifyLecture by datastore.data.map { it[Pref.Key.NOTIFY_LECTURE] ?: Pref.Default.NOTIFY_ALLOWED }.collectAsState(initial = Pref.Default.NOTIFY_ALLOWED)
    val notifyHomework by datastore.data.map { it[Pref.Key.NOTIFY_HOMEWORK] ?: Pref.Default.NOTIFY_ALLOWED }.collectAsState(initial = Pref.Default.NOTIFY_ALLOWED)
    val notifyZoom by datastore.data.map { it[Pref.Key.NOTIFY_ZOOM] ?: Pref.Default.NOTIFY_ALLOWED }.collectAsState(initial = Pref.Default.NOTIFY_ALLOWED)
    val notifyExam by datastore.data.map { it[Pref.Key.NOTIFY_EXAM] ?: Pref.Default.NOTIFY_ALLOWED }.collectAsState(initial = Pref.Default.NOTIFY_ALLOWED)

    val enableMorningReminder by datastore.data.map { it[Pref.Key.ALLOW_MORNING_REMINDER] ?: Pref.Default.ALLOW_MORNING_REMINDER }.collectAsState(initial = Pref.Default.ALLOW_MORNING_REMINDER)
    val morningReminderTime by datastore.data.map { it[Pref.Key.TIME_MORNING_REMINDER] ?: Pref.Default.TIME_MORNING_REMINDER }.collectAsState(initial = Pref.Default.TIME_MORNING_REMINDER)
    val enableNightReminder by datastore.data.map { it[Pref.Key.ALLOW_NIGHT_REMINDER] ?: Pref.Default.ALLOW_NIGHT_REMINDER }.collectAsState(initial = Pref.Default.ALLOW_NIGHT_REMINDER)
    val nightReminderTime by datastore.data.map { it[Pref.Key.TIME_NIGHT_REMINDER] ?: Pref.Default.TIME_NIGHT_REMINDER }.collectAsState(initial = Pref.Default.TIME_NIGHT_REMINDER)

    UpdateEffect(enableMorningReminder, morningReminderTime) {
        setDayReminder(context, enableMorningReminder, enableNightReminder, morningReminderTime, nightReminderTime)
    }
    UpdateEffect(enableNightReminder, nightReminderTime) {
        setDayReminder(context, enableMorningReminder, enableNightReminder, morningReminderTime, nightReminderTime)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        PrefsScreen(dataStore = datastore) {
            prefsGroup(title = context.getString(R.string.dnd_time_category)) {
                prefsItem {
                    SwitchPref(
                        key = Pref.Key.DND_CHECK.name,
                        title = stringResource(id = R.string.dnd_time_check),
                        summary = stringResource(id = if (dndCheck) R.string.dnd_time_check_on else R.string.dnd_time_check_off),
                        defaultChecked = Pref.Default.DND_CHECK
                    )
                    TextPref(
                        title = stringResource(id = R.string.dnd_time),
                        summary = "${LocalDateTime.now().withHour(dndStartTime / 60).withMinute(dndStartTime % 60).format(timeFormatter)} ~ ${LocalDateTime.now().withHour(dndEndTime / 60).withMinute(dndEndTime % 60).format(timeFormatter)}",
                        modifier = Modifier
                            .alpha(if (dndCheck) 1f else 0.5f)
                            .clickable(enabled = dndCheck) { showDndTimeDialog = true },
                    )
                }
            }
            prefsGroup(title = context.getString(R.string.item_notification_category)) {
                prefsItem {
                    TextPref(
                        title = stringResource(id = R.string.notify_lec),
                        summary = notifyLecture.notifyToList().let {
                            if (it.none { it }) {
                                stringResource(id = R.string.notify_lec_off)
                            } else {
                                val enabled = arrayListOf<Int>()
                                hours.forEachIndexed { index, hour -> if (it[index]) enabled.add(hour) }
                                stringResource(id = R.string.notify_lec_on, enabled.joinToString())
                            }
                        },
                        modifier = Modifier
                            .clickable { showNotifyTimeSelectDialog = Pref.Key.NOTIFY_LECTURE.name },
                    )
                    TextPref(
                        title = stringResource(id = R.string.notify_hw),
                        summary = notifyHomework.notifyToList().let {
                            if (it.none { it }) {
                                stringResource(id = R.string.notify_hw_off)
                            } else {
                                val enabled = arrayListOf<Int>()
                                hours.forEachIndexed { index, hour -> if (it[index]) enabled.add(hour) }
                                stringResource(id = R.string.notify_hw_on, enabled.joinToString())
                            }
                        },
                        modifier = Modifier
                            .clickable { showNotifyTimeSelectDialog = Pref.Key.NOTIFY_HOMEWORK.name },
                    )
                    TextPref(
                        title = stringResource(id = R.string.notify_zoom),
                        summary = notifyZoom.notifyToList().let {
                            if (it.none { it }) {
                                stringResource(id = R.string.notify_zoom_off)
                            } else {
                                val enabled = arrayListOf<Int>()
                                minutes.forEachIndexed { index, minute -> if (it[index]) enabled.add(minute) }
                                stringResource(id = R.string.notify_zoom_on, enabled.joinToString())
                            }
                        },
                        modifier = Modifier
                            .clickable { showNotifyTimeSelectDialog = Pref.Key.NOTIFY_ZOOM.name },
                    )
                    TextPref(
                        title = stringResource(id = R.string.notify_exam),
                        summary = notifyExam.notifyToList().let {
                            if (it.none { it }) {
                                stringResource(id = R.string.notify_exam_off)
                            } else {
                                val enabled = arrayListOf<Int>()
                                minutes.forEachIndexed { index, minute -> if (it[index]) enabled.add(minute) }
                                stringResource(id = R.string.notify_exam_on, enabled.joinToString())
                            }
                        },
                        modifier = Modifier
                            .clickable { showNotifyTimeSelectDialog = Pref.Key.NOTIFY_EXAM.name },
                    )
                }
            }
            prefsGroup(title = context.getString(R.string.reminder_category)) {
                prefsItem {
                    SwitchPref(
                        key = Pref.Key.ALLOW_MORNING_REMINDER.name,
                        title = stringResource(id = R.string.morning_reminder),
                        summary = stringResource(id = if (enableMorningReminder) R.string.morning_reminder_desc_on else R.string.morning_reminder_desc_off),
                        defaultChecked = Pref.Default.ALLOW_MORNING_REMINDER
                    )
                    TextPref(
                        title = stringResource(id = R.string.morning_reminder_time),
                        summary = LocalDateTime.now().withHour(morningReminderTime / 60).withMinute(morningReminderTime % 60).format(timeFormatter),
                        modifier = Modifier
                            .alpha(if (enableMorningReminder) 1f else 0.5f)
                            .clickable(enabled = enableMorningReminder) {
                                showReminderTimeSelectDialog = Pref.Key.TIME_MORNING_REMINDER.name
                            },
                    )
                    SwitchPref(
                        key = Pref.Key.ALLOW_NIGHT_REMINDER.name,
                        title = stringResource(id = R.string.night_reminder),
                        summary = stringResource(id = if (enableNightReminder) R.string.night_reminder_desc_on else R.string.night_reminder_desc_off),
                        defaultChecked = Pref.Default.ALLOW_NIGHT_REMINDER
                    )
                    TextPref(
                        title = stringResource(id = R.string.night_reminder_time),
                        summary = LocalDateTime.now().withHour(nightReminderTime / 60).withMinute(nightReminderTime % 60).format(timeFormatter),
                        modifier = Modifier
                            .alpha(if (enableNightReminder) 1f else 0.5f)
                            .clickable(enabled = enableNightReminder) {
                                showReminderTimeSelectDialog = Pref.Key.TIME_NIGHT_REMINDER.name
                            },
                    )
                }
            }
        }
    }

    if (showDndTimeDialog) {
        LaunchedEffect(Unit) {
            dndTempStartTime = dndStartTime
            dndTempEndTime = dndEndTime
        }

        AlertDialog(
            icon = Icons.Rounded.DoNotDisturbOn,
            title = stringResource(id = R.string.dnd_time),
            content = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = it
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { showDndTimeSelectDialog = Pref.Key.DND_START_TIME.name }
                            .weight(0.5f)
                    ) {
                       Text(text = stringResource(id = R.string.start_at))
                       Text(
                           text = LocalDateTime.now().withHour(dndTempStartTime / 60).withMinute(dndTempStartTime % 60).format(timeFormatter),
                           fontWeight = FontWeight.Bold,
                           fontSize = 24.sp
                       )
                    }
                    Column(
                        modifier = Modifier
                            .clickable { showDndTimeSelectDialog = Pref.Key.DND_END_TIME.name }
                            .weight(0.5f)
                    ) {
                       Text(text = stringResource(id = R.string.end_at))
                       Text(
                           text = LocalDateTime.now().withHour(dndTempEndTime / 60).withMinute(dndTempEndTime % 60).format(timeFormatter),
                           fontWeight = FontWeight.Bold,
                           fontSize = 24.sp
                       )
                    }
                }
            },
            onCancel = { showDndTimeDialog = false },
            onConfirm = {
                coroutineScope.launch {
                    datastore.edit {
                        it[intPreferencesKey(Pref.Key.DND_START_TIME.name)] = dndTempStartTime
                        it[intPreferencesKey(Pref.Key.DND_END_TIME.name)] = dndTempEndTime
                    }
                    showDndTimeDialog = false
                }
            }
        )
    }

    if (showDndTimeSelectDialog != "") {
        val time: Int
        val setTime: (Int) -> Unit
        val title: String

        when (showDndTimeSelectDialog) {
            Pref.Key.DND_START_TIME.name -> {
                time = dndTempStartTime
                setTime = { dndTempStartTime = it }
                title = stringResource(id = R.string.start_at)
            }
            else -> {
                time = dndTempEndTime
                setTime = { dndTempEndTime = it }
                title = stringResource(id = R.string.end_at)
            }
        }

        val timePickerState = rememberTimePickerState(
            initialHour = time / 60,
            initialMinute = time % 60,
            is24Hour = false
        )

        TimePickerDialog(
            state = timePickerState,
            title = title,
            onCancel = { showDndTimeSelectDialog = "" },
            onConfirm = {
                setTime(timePickerState.hour * 60 + timePickerState.minute)
                showDndTimeSelectDialog = ""
            }
        )
    }
    
    if (showNotifyTimeSelectDialog != "") {
        val icon: ImageVector
        val title: String
        val options: List<String>
        var current by remember { mutableIntStateOf(0) }

        when (showNotifyTimeSelectDialog) {
            Pref.Key.NOTIFY_LECTURE.name -> {
                icon = MyIconPack.Video
                title = stringResource(id = R.string.notify_lec)
                options = hours.map { stringResource(id = R.string.pref_hour_format, it) }
                current = notifyLecture
            }
            Pref.Key.NOTIFY_HOMEWORK.name -> {
                icon = MyIconPack.Assignment
                title = stringResource(id = R.string.notify_hw)
                options = hours.map { stringResource(id = R.string.pref_hour_format, it) }
                current = notifyHomework
            }
            Pref.Key.NOTIFY_ZOOM.name -> {
                icon = MyIconPack.LiveClass
                title = stringResource(id = R.string.notify_zoom)
                options = minutes.map { stringResource(id = R.string.pref_minute_format, it) }
                current = notifyZoom
            }
            Pref.Key.NOTIFY_EXAM.name -> {
                icon = MyIconPack.Test
                title = stringResource(id = R.string.notify_exam)
                options = minutes.map { stringResource(id = R.string.pref_minute_format, it) }
                current = notifyExam
            }
            else -> {
                icon = MyIconPack.Video
                options = hours.map { stringResource(id = R.string.pref_hour_format, it) }
                title = ""
                current = notifyLecture
            }
        }

        AlertDialog(
            icon = icon,
            title = title,
            content = {
                MultiChoiceSegmentedButtonRow {
                    options.forEachIndexed { index, option ->
                        val isChecked = current.and(2.0.pow(index).toInt()) != 0
                        OutlinedIconToggleButton(
                            checked = isChecked,
                            onCheckedChange = {
                                current += (2.0.pow(index).toInt() * if (it) 1 else -1)
                            },
                            shape = when (index) {
                                0 -> RoundedCornerShape(60.dp, 0.dp, 0.dp, 60.dp)
                                options.lastIndex -> RoundedCornerShape(0.dp, 60.dp, 60.dp, 0.dp)
                                else -> RoundedCornerShape(0.dp)
                            },
                            modifier = Modifier
                                .zIndex(if (isChecked) 1f else 0f)
                                .padding(0.dp)
                                .height(38.dp)
                                .weight(1f),
                            border = BorderStroke(if (isChecked) 1.dp else 0.5.dp, if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.outline,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                checkedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                checkedContentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(text = option)
                        }
                    }
                }
            },
            onCancel = { showNotifyTimeSelectDialog = "" },
            onConfirm = {
                coroutineScope.launch {
                    datastore.edit { it[intPreferencesKey(showNotifyTimeSelectDialog)] = current }
                    showNotifyTimeSelectDialog = ""
                }
            }
        )
    }

    if (showReminderTimeSelectDialog != "") {
        val time: Int
        val title: String

        when (showReminderTimeSelectDialog) {
            Pref.Key.TIME_MORNING_REMINDER.name -> {
                time = morningReminderTime
                title = stringResource(id = R.string.morning_reminder_time)
            }
            else -> {
                time = nightReminderTime
                title = stringResource(id = R.string.night_reminder_time)
            }
        }

        val timePickerState = rememberTimePickerState(
            initialHour = time / 60,
            initialMinute = time % 60,
            is24Hour = false
        )

        TimePickerDialog(
            state = timePickerState,
            title = title,
            onCancel = { showReminderTimeSelectDialog = "" },
            onConfirm = {
                coroutineScope.launch {
                    datastore.edit { it[intPreferencesKey(showReminderTimeSelectDialog)] = timePickerState.hour * 60 + timePickerState.minute }
                    showReminderTimeSelectDialog = ""
                }
            }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SettingsInfoPreview() {
    AppTheme {
        SettingsNotificationsScreen(
        )
    }
}