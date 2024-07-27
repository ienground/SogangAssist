package net.ienlab.sogangassist.ui.screen.settings.info

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.TextPref
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.dataStore
import net.ienlab.sogangassist.ui.theme.AppTheme
import net.ienlab.sogangassist.ui.utils.BaseDialog
import net.ienlab.sogangassist.utils.Utils.fromHtml
import net.ienlab.sogangassist.utils.Utils.readTextFromRaw

private const val PRESS_VERSION_INTERVAL = 700

@Composable
fun SettingsInfoScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val datastore = context.dataStore
    val coroutineScope = rememberCoroutineScope()
    var lastPressVersionTime = System.currentTimeMillis()
    var pressVersionCount = 0

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        var showUpdateLogDialog by remember { mutableStateOf(false) }

        val emailSubject = "${stringResource(R.string.real_app_name)} ${BuildConfig.VERSION_NAME} ${stringResource(R.string.ask)}"
        val emailText = "${stringResource(R.string.email_text)}\n${Build.BRAND} ${Build.MODEL} Android ${Build.VERSION.RELEASE}\n_\n"

        PrefsScreen(dataStore = datastore) {
            prefsItem {
                TextPref(
                    title = stringResource(id = R.string.app_version),
                    summary = "${stringResource(id = R.string.versionName)} (${BuildConfig.VERSION_CODE})",
                    modifier = Modifier.clickable {
                        if (System.currentTimeMillis() - lastPressVersionTime > PRESS_VERSION_INTERVAL) {
                            pressVersionCount++
                        } else {
                            pressVersionCount = 0
                        }

                        if (pressVersionCount > 6) {
                            pressVersionCount = 0
                            /*
                            if (!isDevmodeEnabled) {
                                coroutineScope.launch {
                                    context.dataStore.edit { it[Pref.Key.DEV_MODE] = true }
                                    Toast.makeText(context, devModeEnableMsg, Toast.LENGTH_SHORT).show()
                                }
                            }

                             */
                        }
                    }
                )
                TextPref(
                    title = stringResource(id = R.string.changelog),
                    summary = stringResource(id = R.string.pref_changelog_exp),
                    modifier = Modifier.clickable { showUpdateLogDialog = true }
                )
                TextPref(
                    title = stringResource(id = R.string.ask_to_dev),
                    summary = stringResource(id = R.string.pref_ask_exp),
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("my@ien.zone"))
                            putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                            putExtra(Intent.EXTRA_TEXT, emailText)
                            type = "message/rfc822"
                        }
                        context.startActivity(intent)
                    }
                )
                TextPref(
                    title = stringResource(id = R.string.open_source_license),
                    summary = stringResource(id = R.string.pref_opens_exp),
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                    }
                )
                /*
                TextPref(
                    title = stringResource(R.string.privacy_policy),
                    summary = "개인정보 처리방침을 확인할 수 있습니다.",
                    modifier = Modifier.clickable {
                        navigateToPolicy()
                    }
                )

                 */
            }
        }

        if (showUpdateLogDialog) {
            BaseDialog(
                icon = Icons.Rounded.Alarm,
                title = "${stringResource(id = R.string.real_app_name)} ${stringResource(id = R.string.versionName)} ${stringResource(id = R.string.changelog)}",
                content = {
                    Text(
                        text = fromHtml(readTextFromRaw(context.resources, R.raw.changelog)).toString(),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) },
                onCancel = { showUpdateLogDialog = false },
                buttons = {
                    Row(modifier = it) {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { showUpdateLogDialog = false }) { Text(stringResource(id = R.string.close)) }
                    }
                }
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SettingsInfoPreview() {
    AppTheme {
        SettingsInfoScreen(
        )
    }
}