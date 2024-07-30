package net.ienlab.sogangassist.ui.utils

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.data.Permissions
import net.ienlab.sogangassist.service.LMSListenerService
import net.ienlab.sogangassist.ui.theme.AppTheme
import net.ienlab.sogangassist.utils.Utils.checkPermissions
import net.ienlab.sogangassist.utils.Utils.isNotificationPermissionGranted
import net.ienlab.sogangassist.utils.Utils.isNotificationPolicyGranted
import net.ienlab.sogangassist.utils.Utils.isPackageInstalled
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: @Composable (RowScope) -> Unit,
) {
    val calendar = Calendar.getInstance()
    val subTitle = when (calendar[Calendar.HOUR_OF_DAY]) {
        in 6..10 -> stringResource(R.string.user_hello_morning)
        in 11..16 -> stringResource(R.string.user_hello_afternoon)
        in 17..20 -> stringResource(R.string.user_hello_evening)
        else -> stringResource(R.string.user_hello_night)
    }
    TopAppBar(title = {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxHeight()
        ) {
            Text(
                text = subTitle,
                fontSize = 14.sp,
            )
            AnimatedContent(targetState = title, label = "",) {
                Text(
                    text = it,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .wrapContentWidth()
                        .fillMaxWidth()
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )
            }
        }
    }, actions = actions, modifier = modifier.height(80.dp))
}

@Preview
@Composable
private fun AppBarPreview() {
    AppTheme {
        AppBar(modifier = Modifier, title = "titledd", actions = {})
    }
}

@Composable
fun BaseDialog(
    modifier: Modifier = Modifier,
    icon: ImageVector?,
    title: String?,
    content: @Composable (modifier: Modifier) -> Unit,
    onCancel: () -> Unit,
    buttons: @Composable (modifier: Modifier) -> Unit
) {
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = true)) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = modifier
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
            ) {
                icon?.let {
                    Icon(
                        imageVector = it, contentDescription = title,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                title?.let {
                    AutoSizeText(
                        text = it,
                        style = MaterialTheme.typography.displayLarge,
                        maxTextSize = 24.sp,
                        alignment = Alignment.Center,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth()
                            .height(24.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    )
                }
                content(Modifier.padding(bottom = 16.dp))
                buttons(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp)
                        .height(40.dp)
                        .padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    modifier: Modifier = Modifier,
    state: TimePickerState,
    title: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    var isTimePickerDial by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = true)) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = modifier
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            Column {
                Text(text = title, modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp))
                if (isTimePickerDial) {
                    TimePicker(state = state, modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp))
                } else {
                    TimeInput(state = state, modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp))
                }
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    IconButton(onClick = {
                        isTimePickerDial = !isTimePickerDial
                    }) {
                        AnimatedContent(
                            targetState = if (isTimePickerDial) Icons.Rounded.Keyboard else Icons.Rounded.AccessTime,
                            label = "time_picker_dial"
                        ) {
                            Icon(imageVector = it, contentDescription = "", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onCancel) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                    TextButton(onClick = onConfirm) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(state: DatePickerState, title: String, onCancel: () -> Unit, onConfirm: () -> Unit) {
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = true),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 6.dp,
        confirmButton = {},
        modifier = Modifier
            .scale(0.85f)
            .background(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface
            ),
    ) {
        Text(
            text = title, modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp)
        )
        DatePicker(state = state, title = null, modifier = Modifier)
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onCancel) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        }
    }
}

@Composable
fun AlertDialog(icon: ImageVector?, title: String?, content: @Composable (modifier: Modifier) -> Unit, onCancel: () -> Unit, onConfirm: (() -> Unit)?) {
    BaseDialog(icon = icon, title = title, content = content, onCancel = onCancel, buttons = {
        Row(modifier = it) {
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onCancel) { Text(stringResource(id = android.R.string.cancel)) }
            if (onConfirm != null) TextButton(onClick = onConfirm) { Text(stringResource(id = android.R.string.ok)) }
        }
    })
}

@Composable
fun DeleteAlertDialog(onCancel: () -> Unit, onConfirm: () -> Unit, content: @Composable () -> Unit = {}) {
    AlertDialog(icon = Icons.Rounded.Delete, title = stringResource(id = R.string.delete_title), content = {
        Text(text = stringResource(id = R.string.cannot_be_undone), modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth())
        content()
    }, onCancel = onCancel, onConfirm = onConfirm)
}

@Composable
fun AlertDialog(icon: ImageVector, title: String, content: @Composable (Modifier) -> Unit, textNeutral: String, onNeutral: (() -> Unit)?, textNegative: String, onNegative: () -> Unit, textPositive: String, onPositive: (() -> Unit)?) {
    BaseDialog(icon = icon, title = title, content = content, onCancel = onNegative, buttons = {
        Row(modifier = it) {
            if (onNeutral != null) TextButton(onClick = onNeutral) { Text(textNeutral) }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onNegative) { Text(textNegative) }
            if (onPositive != null) TextButton(onClick = onPositive) { Text(textPositive) }
        }
    })
}

@Composable
fun PermissionRow(
    modifier: Modifier = Modifier,
    permissions: Permissions,
    isGranted: Boolean,
    isError: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(targetValue = if (isError) MaterialTheme.colorScheme.errorContainer else if (!isGranted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer, animationSpec = tween(700), label = "containerColor")
    val contentColor by animateColorAsState(targetValue = if (isError) MaterialTheme.colorScheme.error else if (!isGranted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondaryContainer, animationSpec = tween(700), label = "contentColor")
    Card(
        colors = CardColors(containerColor = containerColor, contentColor = contentColor, disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(enabled = !isGranted) { onClick() }
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = if (!isGranted) permissions.icon else Icons.Rounded.CheckCircle,
                label = "permission_icon"
            ) {
                Icon(
                    imageVector = it,
                    contentDescription = if (!isGranted) permissions.title else stringResource(id = R.string.allowed),
                )
            }
            Column {
                AnimatedContent(
                    targetState = if (!isGranted) permissions.title else stringResource(id = R.string.allowed),
                    label = "permission_title"
                ) {
                    Text(text = it, fontSize = 18.sp)
                }
                Text(text = permissions.content, lineHeight = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDialog(permissions: List<Permissions>, onConfirm: () -> Unit) {
    val context = LocalContext.current
    var isError by remember { mutableStateOf(false) }
    var isDialogCycle by remember { mutableStateOf(false) }
    val offset by animateDpAsState(targetValue = if (isDialogCycle) 10.dp else 20.dp, animationSpec = repeatable(iterations = 5, animation = tween(70, easing = LinearEasing), repeatMode = RepeatMode.Reverse), finishedListener = {
        isDialogCycle = false
    }, label = "permission_dialog_animation")

    BaseDialog(
        icon = Icons.Rounded.Security,
        title = stringResource(id = R.string.permission_title),
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = it.padding(horizontal = 16.dp)) {
                permissions.forEach { permission ->
                    if (permission.permissions.any { it == Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE }) {
                        var isGranted by remember { mutableStateOf(isNotificationPermissionGranted(context)) }
                        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                            isGranted = isNotificationPermissionGranted(context)
                        }
                        PermissionRow(permissions = permission, isGranted = isGranted, isError = !isGranted && isError, onClick = {
                            launcher.launch(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }, modifier = Modifier.fillMaxWidth())
                    } else if (permission.permissions.any { it == Manifest.permission.ACCESS_NOTIFICATION_POLICY }) {
                        var isGranted by remember { mutableStateOf(isNotificationPolicyGranted(context)) }
                        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                            isGranted = isNotificationPolicyGranted(context)
                        }
                        PermissionRow(permissions = permission, isGranted = isGranted, isError = !isGranted && isError, onClick = {
                            launcher.launch(Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                        }, modifier = Modifier.fillMaxWidth())
                    } else if (permission.permissions.any { it == Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS }) {
                        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                        var isGranted by remember { mutableStateOf(pm.isIgnoringBatteryOptimizations(context.packageName)) }
                        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                            isGranted = pm.isIgnoringBatteryOptimizations(context.packageName)
                        }
                        PermissionRow(permissions = permission, isGranted = isGranted, isError = !isGranted && isError, onClick = {
                            launcher.launch(
                                Intent().apply {
                                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                    data = Uri.parse("package:${context.packageName}")
                                }
                            )
                        }, modifier = Modifier.fillMaxWidth())
                    } else if (permission.permissions.any { it == LMSListenerService.LMS_PACKAGE_NAME }) {
                        var isGranted by remember { mutableStateOf(isPackageInstalled(context, LMSListenerService.LMS_PACKAGE_NAME)) }
                        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                            isGranted = isPackageInstalled(context, LMSListenerService.LMS_PACKAGE_NAME)
                        }
                        PermissionRow(permissions = permission, isGranted = isGranted, isError = !isGranted && isError, onClick = {
                            try {
                                launcher.launch(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${LMSListenerService.LMS_PACKAGE_NAME}")))
                            } catch (e: ActivityNotFoundException) {
                                launcher.launch(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${LMSListenerService.LMS_PACKAGE_NAME}")))
                            }
                        }, modifier = Modifier.fillMaxWidth())
                    } else {
                        val permissionState = rememberMultiplePermissionsState(permissions = permission.permissions)
                        PermissionRow(permissions = permission, isGranted = permissionState.allPermissionsGranted, isError = !permissionState.allPermissionsGranted && isError, onClick = {
                            permissionState.launchMultiplePermissionRequest()
                        }, modifier = Modifier.fillMaxWidth())

                        LaunchedEffect(permissionState.allPermissionsGranted) {
                            if (permissionState.allPermissionsGranted) {
                                permission.launcher()
                            }
                        }
                    }
                }
            }
        },
        onCancel = {},
        buttons = {
            Row(modifier = it) {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    if (checkPermissions(context, permissions)) {
                        onConfirm()
                    } else {
                        isError = true
                        isDialogCycle = true
                    }
                }) { Text(stringResource(id = R.string.close)) }
            }
        },
        modifier = Modifier.offset(x = offset - 20.dp)
    )
}
