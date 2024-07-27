package net.ienlab.sogangassist.ui.screen.settings.general

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.SwitchPref
import com.jamal.composeprefs3.ui.prefs.TextPref
import kotlinx.coroutines.flow.map
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.Pref
import net.ienlab.sogangassist.dataStore
import net.ienlab.sogangassist.ui.theme.AppTheme

@Composable
fun SettingsGeneralScreen(
    modifier: Modifier = Modifier,
) {
    SettingsGeneralScreenBody(
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsGeneralScreenBody(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        val context = LocalContext.current
        val datastore = context.dataStore

        val isMaterialYouChecked by datastore.data.map { it[Pref.Key.MATERIAL_YOU] ?: Pref.Default.MATERIAL_YOU }.collectAsState(initial = Pref.Default.MATERIAL_YOU)
        val setRegisterAlert by datastore.data.map { it[Pref.Key.SET_REGISTER_ALERT] ?: Pref.Default.SET_REGISTER_ALERT }.collectAsState(initial = Pref.Default.SET_REGISTER_ALERT)

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
                    summary = stringResource(id = R.string.backup_desc)
                )
                TextPref(
                    title = stringResource(id = R.string.restore),
                    summary = stringResource(id = R.string.restore_desc)
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SettingsGeneralPreview() {
    AppTheme {
        SettingsGeneralScreenBody()
    }
}