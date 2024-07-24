package net.ienlab.sogangassist

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.material.color.DynamicColors
import net.ienlab.sogangassist.data.AppContainer
import net.ienlab.sogangassist.data.AppDataContainer
import net.ienlab.sogangassist.data.lms.Lms

const val TAG = "SogangAssistTAG"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "${context.packageName}_preferences"))
    }
)
val Context.widgetStore: DataStore<Preferences> by preferencesDataStore(
    name = "widget",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "WidgetPreferences"))
    }
)


class MyApplication: Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}