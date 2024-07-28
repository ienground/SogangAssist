package net.ienlab.sogangassist.ui.screen.edit

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RemoveDone
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.ui.AppViewModelProvider
import net.ienlab.sogangassist.ui.navigation.NavigationDestination
import net.ienlab.sogangassist.ui.theme.AppTheme
import net.ienlab.sogangassist.ui.utils.ActionMenuItem
import net.ienlab.sogangassist.ui.utils.ActionsMenu
import net.ienlab.sogangassist.ui.utils.AlertDialog
import net.ienlab.sogangassist.ui.utils.DatePickerDialog
import net.ienlab.sogangassist.ui.utils.DeleteAlertDialog
import net.ienlab.sogangassist.ui.utils.TimePickerDialog
import net.ienlab.sogangassist.ui.utils.Utils.getDateLabel
import net.ienlab.sogangassist.ui.utils.Utils.rememberMyDatePickerState
import net.ienlab.sogangassist.utils.Utils.parseLongToLocalDate
import net.ienlab.sogangassist.utils.Utils.timeInMillis
import java.time.format.DateTimeFormatter

object LmsEditDestination: NavigationDestination {
    override val route: String = "lms_edit"
    const val itemIdArg = "itemId"
    const val initDateArg = "initDateArg"
    val routeWithArgs = "$route?${itemIdArg}={$itemIdArg}&${initDateArg}={${initDateArg}}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LmsEditScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    viewModel: LmsEditViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    BackHandler {
        viewModel.updateUiState(viewModel.uiState.item.copy(showSaveDialog = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { backDispatcher?.onBackPressed() }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    val items = arrayListOf(
                        ActionMenuItem.IconMenuItem.ShownIfRoom(
                            icon = Icons.Rounded.Save,
                            title = stringResource(id = R.string.save),
                            onClick = {
                                coroutineScope.launch {
                                    val result = viewModel.saveItem()
                                    if (result) {
                                        focusManager.clearFocus()
                                        navigateBack()
                                    } else {
                                        viewModel.updateUiState(viewModel.uiState.item.copy(showError = true))
                                    }
                                }
                            }
                        )
                    )
                    if (viewModel.uiState.item.id != -1L) {
                        items.add(0,
                            ActionMenuItem.IconMenuItem.ShownIfRoom(
                                icon = if (viewModel.uiState.item.isFinished) Icons.Rounded.RemoveDone else Icons.Rounded.DoneAll,
                                title = stringResource(id = R.string.mark_as_finish),
                                onClick = {
                                    coroutineScope.launch {
                                        if (viewModel.uiState.item.isFinished) {
                                            viewModel.updateUiState(viewModel.uiState.item.copy(isFinished = false))
                                        } else {
                                            viewModel.updateUiState(viewModel.uiState.item.copy(isFinished = true))
                                            val result = viewModel.saveItem()
                                            if (result) navigateBack()
                                            else viewModel.updateUiState(viewModel.uiState.item.copy(isFinished = false))
                                        }
                                    }
                                }
                            )
                        )
                        items.add(
                            ActionMenuItem.IconMenuItem.ShownIfRoom(
                                icon = Icons.Rounded.Delete,
                                title = stringResource(id = R.string.delete),
                                onClick = { viewModel.updateUiState(viewModel.uiState.item.copy(showDeleteDialog = true)) }
                            )
                        )
                    }
                    ActionsMenu(
                        items = items,
                        isOpen = false,
                        closeDropdown = {},
                        onToggleOverflow = {},
                        maxVisibleItems = 3
                    )
                }
            )
        },
        modifier = modifier,
    ) {
        LmsEditScreenBody(
            uiState = viewModel.uiState,
            onItemValueChanged = viewModel::updateUiState,
            classNames = viewModel.classNames,
            modifier = Modifier.padding(it)
        )
    }

    if (viewModel.uiState.item.showStartDatePicker) {
        val datePickerState = viewModel.uiState.item.startTime.let { rememberDatePickerState(initialSelectedDateMillis = it.timeInMillis()) }
        DatePickerDialog(
            state = datePickerState,
            title = stringResource(id = R.string.select_date),
            onCancel = { viewModel.updateUiState(viewModel.uiState.item.copy(showStartDatePicker = false)) },
            onConfirm = {
                viewModel.updateUiState(viewModel.uiState.item.copy(
                    showStartDatePicker = false,
                    showStartTimePicker = true,
                    tempDate = parseLongToLocalDate(datePickerState.selectedDateMillis ?: 0L)
                ))
            }
        )
    }

    if (viewModel.uiState.item.showStartTimePicker) {
        val timePickerState = viewModel.uiState.item.startTime.let { rememberTimePickerState(it.hour, it.minute, is24Hour = false) }
        TimePickerDialog(
            state = timePickerState,
            title = stringResource(id = R.string.select_time),
            onCancel = { viewModel.updateUiState(viewModel.uiState.item.copy(showStartTimePicker = false)) },
            onConfirm = {
                viewModel.updateUiState(viewModel.uiState.item.copy(
                    showStartTimePicker = false,
                    startTime = viewModel.uiState.item.tempDate.atTime(timePickerState.hour, timePickerState.minute)
                ))
            }
        )
    }

    if (viewModel.uiState.item.showEndDatePicker) {
        val datePickerState = viewModel.uiState.item.endTime.let {
            rememberMyDatePickerState(
                initialSelectedDateMillis = it.timeInMillis()
            )
        }
        DatePickerDialog(
            state = datePickerState,
            title = stringResource(id = R.string.select_date),
            onCancel = { viewModel.updateUiState(viewModel.uiState.item.copy(showEndDatePicker = false)) },
            onConfirm = {
                viewModel.updateUiState(viewModel.uiState.item.copy(
                    showEndDatePicker = false,
                    showEndTimePicker = true,
                    tempDate = parseLongToLocalDate(datePickerState.selectedDateMillis ?: 0L)
                ))
            }
        )
    }

    if (viewModel.uiState.item.showEndTimePicker) {
        val timePickerState = viewModel.uiState.item.endTime.let { rememberTimePickerState(it.hour, it.minute, is24Hour = false) }
        TimePickerDialog(
            state = timePickerState,
            title = stringResource(id = R.string.select_time),
            onCancel = { viewModel.updateUiState(viewModel.uiState.item.copy(showEndTimePicker = false)) },
            onConfirm = {
                viewModel.updateUiState(viewModel.uiState.item.copy(
                    showEndTimePicker = false,
                    endTime = viewModel.uiState.item.tempDate.atTime(timePickerState.hour, timePickerState.minute)
                ))
            }
        )
    }

    if (viewModel.uiState.item.showDeleteDialog) {
        DeleteAlertDialog(
            onCancel = { viewModel.updateUiState(viewModel.uiState.item.copy(showDeleteDialog = false)) },
            onConfirm = {
                coroutineScope.launch {
                    viewModel.deleteItem()
                }
                navigateBack()
            }
        )
    }

    if (viewModel.uiState.item.showSaveDialog) {
        AlertDialog(
            icon = Icons.Rounded.Save,
            title = stringResource(id = R.string.ask_to_save),
            content = {
                Text(
                    text = stringResource(id = R.string.ask_to_save_message),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                )
            },
            textNegative = stringResource(id = android.R.string.cancel),
            onNegative = {
                viewModel.updateUiState(viewModel.uiState.item.copy(showSaveDialog = false))
            },
            textNeutral = stringResource(id = R.string.not_save),
            onNeutral = {
                viewModel.updateUiState(viewModel.uiState.item.copy(showSaveDialog = false))
                focusManager.clearFocus()
                navigateBack()
            },
            textPositive = stringResource(id = R.string.save),
            onPositive = {
                coroutineScope.launch {
                    viewModel.updateUiState(viewModel.uiState.item.copy(showSaveDialog = false))
                    val result = viewModel.saveItem()
                    if (result) {
                        focusManager.clearFocus()
                        navigateBack()
                    } else {
                        viewModel.updateUiState(viewModel.uiState.item.copy(showError = true))
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LmsEditScreenBody(
    modifier: Modifier = Modifier,
    uiState: LmsUiState,
    onItemValueChanged: (LmsDetails) -> Unit,
    classNames: List<String>
) {
    val types = mapOf(
        Lms.Type.LESSON to stringResource(id = R.string.classtime),
        Lms.Type.SUP_LESSON to stringResource(id = R.string.sup_classtime),
        Lms.Type.HOMEWORK to stringResource(id = R.string.assignment),
        Lms.Type.ZOOM to stringResource(id = R.string.zoom),
        Lms.Type.TEAMWORK to stringResource(id = R.string.team_project),
        Lms.Type.EXAM to stringResource(id = R.string.exam),
    )
    val listLesson = listOf(Lms.Type.LESSON, Lms.Type.SUP_LESSON)
    val listAssignment = listOf(Lms.Type.HOMEWORK, Lms.Type.TEAMWORK)
    val pattern = remember { Regex("^\\d+\$") }
    val timeFormat = DateTimeFormatter.ofPattern(stringResource(id = R.string.apm_time_format))

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        LazyRow {
            item {
                Spacer(modifier = Modifier.width(8.dp))
            }
            items(items = types.toList(), key = { it.first }) { (key, value) ->
                FilterChip(
                    selected = uiState.item.type == key,
                    label = { Text(text = value) },
                    onClick = { onItemValueChanged(uiState.item.copy(type = key)) },
                    enabled = !uiState.item.isFinished,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        ExposedDropdownMenuBox(
            expanded = uiState.item.dropdownExpanded,
            onExpandedChange = { onItemValueChanged(uiState.item.copy(dropdownExpanded = it)) },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            TextField(
                value = uiState.item.className,
                onValueChange = { onItemValueChanged(uiState.item.copy(className = it, showError = false)) },
                trailingIcon = {
                    IconButton(onClick = { onItemValueChanged(uiState.item.copy(dropdownExpanded = !uiState.item.dropdownExpanded)) }) {
                        Icon(imageVector = if (uiState.item.dropdownExpanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown, contentDescription = null)
                    }
                },
                textStyle = TextStyle(fontSize = 32.sp),
                isError = uiState.item.showError && uiState.item.className.isEmpty(),
                singleLine = true,
                label = { Text(text = stringResource(id = R.string.class_name)) },
                enabled = !uiState.item.isFinished,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = uiState.item.dropdownExpanded,
                onDismissRequest = { onItemValueChanged(uiState.item.copy(dropdownExpanded = false)) }
            ) {
                classNames.fastForEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            onItemValueChanged(uiState.item.copy(className = option, dropdownExpanded = false))
                        }
                    )
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.item.type in listLesson,
                enter = fadeIn(tween(700)),
                exit = fadeOut(tween(700))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    TextField(
                        value = uiState.item.week,
                        onValueChange = { if (pattern.matches(it) || it.isEmpty()) onItemValueChanged(uiState.item.copy(week = it, showError = false)) },
                        label = { Text(text = stringResource(id = R.string.week)) },
                        suffix = { Text(text = stringResource(id = R.string.week)) },
                        isError = uiState.item.showError && (uiState.item.type in listLesson && uiState.item.week.isEmpty()),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 26.sp),
                        modifier = Modifier.weight(0.5f)
                    )
                    TextField(
                        value = uiState.item.lesson,
                        onValueChange = { if (pattern.matches(it) || it.isEmpty()) onItemValueChanged(uiState.item.copy(lesson = it, showError = false)) },
                        label = { Text(text = stringResource(id = R.string.lesson)) },
                        suffix = { Text(text = stringResource(id = R.string.lesson)) },
                        isError = uiState.item.showError && (uiState.item.type in listLesson && uiState.item.lesson.isEmpty()),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 26.sp),
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.item.type !in listLesson,
                enter = fadeIn(tween(700)),
                exit = fadeOut(tween(700))
            ) {
                TextField(
                    value = uiState.item.homeworkName,
                    onValueChange = { onItemValueChanged(uiState.item.copy(homeworkName = it, showError = false)) },
                    label = { Text(text = stringResource(id = R.string.assignment_name)) },
                    isError = uiState.item.showError && (uiState.item.type !in listLesson && uiState.item.homeworkName.isEmpty()),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 26.sp),
                    enabled = !uiState.item.isFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
        HorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = !uiState.item.isFinished) { onItemValueChanged(uiState.item.copy(isRenewAllowed = !uiState.item.isRenewAllowed)) }
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = stringResource(id = R.string.allow_auto_edit_lms),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.allow_auto_edit_lms),
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = uiState.item.isRenewAllowed,
                onCheckedChange = { onItemValueChanged(uiState.item.copy(isRenewAllowed = it)) },
                enabled = !uiState.item.isFinished,
            )
        }
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Today,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Column() {
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.item.type in listAssignment,
                    enter = expandVertically(spring(1.2f)),
                    exit = shrinkVertically(spring(1.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .clickable(enabled = !uiState.item.isFinished) { onItemValueChanged(uiState.item.copy(showStartDatePicker = true)) }
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.start_at),
                            fontSize = 18.sp,
                            color = if (uiState.item.startTime.isBefore(uiState.item.endTime)) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.item.startTime.let { "${getDateLabel(date = it.toLocalDate())} ${it.format(timeFormat)}" },
                            color = if (uiState.item.startTime.isBefore(uiState.item.endTime)) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .clickable(enabled = !uiState.item.isFinished) { onItemValueChanged(uiState.item.copy(showEndDatePicker = true)) }
                            .fillMaxWidth()
                    ) {
                        AnimatedContent(
                            targetState = stringResource(id = if (uiState.item.type == Lms.Type.ZOOM) R.string.start_at else R.string.end_at),
                            label = "start_end_time"
                        ) {
                            Text(
                                text = it,
                                fontSize = 18.sp,
                                color = if (uiState.item.type !in listAssignment || uiState.item.startTime.isBefore(uiState.item.endTime)) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.error,
                            )
                        }
                        Text(
                            text = uiState.item.endTime.let { "${getDateLabel(date = it.toLocalDate())} ${it.format(timeFormat)}" },
                            color = if (uiState.item.type !in listAssignment || uiState.item.startTime.isBefore(uiState.item.endTime)) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LmsEditScreenPreview() {
    AppTheme {
        LmsEditScreenBody(
            uiState = LmsUiState(
                item = LmsDetails(
                    id = 0,
                    type = Lms.Type.HOMEWORK,
                    className = "34"
                )
            ),
            onItemValueChanged = {},
            classNames = listOf("hi")
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LmsEditScreenPreview2() {
    AppTheme {
        LmsEditScreenBody(
            uiState = LmsUiState(
                item = LmsDetails(
                    id = 0,
                    type = Lms.Type.LESSON,
                    week = "2",
                    lesson = "3",
                    className = "34"
                )
            ),
            onItemValueChanged = {},
            classNames = listOf("hi")
        )
    }
}