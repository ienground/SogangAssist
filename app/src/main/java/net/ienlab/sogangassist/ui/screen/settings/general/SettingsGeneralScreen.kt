package net.ienlab.sogangassist.ui.screen.settings.general

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.SwitchPref
import com.jamal.composeprefs3.ui.prefs.TextPref
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.TAG
import net.ienlab.sogangassist.constant.Pref
import net.ienlab.sogangassist.dataStore
import net.ienlab.sogangassist.ui.AppViewModelProvider
import net.ienlab.sogangassist.ui.theme.AppTheme
import net.ienlab.sogangassist.ui.utils.AlertDialog
import org.json.JSONArray

@Composable
fun SettingsGeneralScreen(
    modifier: Modifier = Modifier,
    snackbarState: SnackbarHostState,
    viewModel: SettingsGeneralViewModel = viewModel(factory = AppViewModelProvider.factory)
) {
    SettingsGeneralScreenBody(
        snackbarState = snackbarState,
        onPreBackup = viewModel::preBackup,
        onBackup = viewModel::backup,
        onPreRestore = viewModel::preRestore,
        onRestoreData = viewModel::restoreData,
        onRestoreApply = viewModel::restoreApply,
    )
}

@Composable
fun SettingsGeneralScreenBody(
    modifier: Modifier = Modifier,
    snackbarState: SnackbarHostState,
    onPreBackup: () -> Intent,
    onBackup: suspend (Uri) -> Unit,
    onPreRestore: () -> Intent,
    onRestoreData: suspend (Uri) -> Unit,
    onRestoreApply: suspend () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val datastore = context.dataStore

    var showRestoreDialog by remember { mutableStateOf(false) }
    val isMaterialYouChecked by datastore.data.map { it[Pref.Key.MATERIAL_YOU] ?: Pref.Default.MATERIAL_YOU }.collectAsState(initial = Pref.Default.MATERIAL_YOU)
    val setRegisterAlert by datastore.data.map { it[Pref.Key.SET_REGISTER_ALERT] ?: Pref.Default.SET_REGISTER_ALERT }.collectAsState(initial = Pref.Default.SET_REGISTER_ALERT)

    val backupLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            coroutineScope.launch {
                onBackup(result.data?.data ?: Uri.EMPTY)
                snackbarState.showSnackbar(context.getString(R.string.backup_msg), duration = SnackbarDuration.Short)
            }
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            coroutineScope.launch {
                Dlog.d(TAG, "${result.data?.data}")
                onRestoreData(result.data?.data ?: Uri.EMPTY)
                showRestoreDialog = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        PrefsScreen(dataStore = datastore) {
            prefsItem {
                SwitchPref(
                    key = Pref.Key.MATERIAL_YOU.name,
                    title = stringResource(id = R.string.use_dynamic_colors),
                    summary = stringResource(id = if (isMaterialYouChecked) R.string.use_dynamic_colors_sum_on else R.string.use_dynamic_colors_sum_off),
                    defaultChecked = Pref.Default.MATERIAL_YOU,
                )
                SwitchPref(
                    key = Pref.Key.SET_REGISTER_ALERT.name,
                    title = stringResource(id = R.string.notify_when_lms),
                    summary = stringResource(id = if (setRegisterAlert) R.string.notify_when_lms_sum_on else R.string.notify_when_lms_sum_off),
                    defaultChecked = Pref.Default.SET_REGISTER_ALERT,
                )
                TextPref(
                    title = stringResource(id = R.string.backup),
                    summary = stringResource(id = R.string.backup_desc),
                    modifier = Modifier.clickable {
                        backupLauncher.launch(onPreBackup())
                    }
                )
                TextPref(
                    title = stringResource(id = R.string.restore),
                    summary = stringResource(id = R.string.restore_desc),
                    modifier = Modifier.clickable {
                        restoreLauncher.launch(onPreRestore())
                    }
                )
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            icon = Icons.Rounded.Notifications,
            title = stringResource(id = R.string.restore),
            content = {
                Text(
                    text = stringResource(id = R.string.restore_msg),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            textPositive = stringResource(id = R.string.agree),
            textNegative = stringResource(id = R.string.disagree),
            textNeutral = "",
            onPositive = {
                coroutineScope.launch {
                    onRestoreApply()
                    showRestoreDialog = false
                    snackbarState.showSnackbar(context.getString(R.string.restore_finish), duration = SnackbarDuration.Short)
                }
            },
            onNegative = { showRestoreDialog = false },
            onNeutral = null
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SettingsGeneralPreview() {
    AppTheme {
        SettingsGeneralScreenBody(
            onPreBackup = { Intent() }, onPreRestore = { Intent() },
            onBackup = {}, onRestoreApply = {}, onRestoreData = {}, snackbarState = SnackbarHostState()
        )
    }
}