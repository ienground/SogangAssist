package net.ienlab.sogangassist

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.PreferencesProto.StringSet
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesMigration as SharedPreferencesDetailMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.material.color.DynamicColors
import net.ienlab.sogangassist.constant.Pref
import net.ienlab.sogangassist.data.AppContainer
import net.ienlab.sogangassist.data.AppDataContainer
import net.ienlab.sogangassist.data.lms.Lms

const val TAG = "SogangAssistTAG"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesDetailMigration(context = context, sharedPreferencesName = "${context.packageName}_preferences",
                migrate = { view, pref ->
                    pref.toMutablePreferences().apply {
                        var notifyLecture = 0b11111
                        var notifyHw = 0b11111
                        var notifyZoom = 0b11111
                        var notifyExam = 0b11111

                        val keyNotifyLecture = listOf(Pref.Key.NOTIFY_1HOUR_LEC.name, Pref.Key.NOTIFY_2HOUR_LEC.name, Pref.Key.NOTIFY_6HOUR_LEC.name, Pref.Key.NOTIFY_12HOUR_LEC.name, Pref.Key.NOTIFY_24HOUR_LEC.name)
                        val keyNotifyHw = listOf(Pref.Key.NOTIFY_1HOUR_HW.name, Pref.Key.NOTIFY_2HOUR_HW.name, Pref.Key.NOTIFY_6HOUR_HW.name, Pref.Key.NOTIFY_12HOUR_HW.name, Pref.Key.NOTIFY_24HOUR_HW.name)
                        val keyNotifyZoom = listOf(Pref.Key.NOTIFY_3MIN_ZOOM.name, Pref.Key.NOTIFY_5MIN_ZOOM.name, Pref.Key.NOTIFY_10MIN_ZOOM.name, Pref.Key.NOTIFY_20MIN_ZOOM.name, Pref.Key.NOTIFY_30MIN_ZOOM.name)
                        val keyNotifyExam = listOf(Pref.Key.NOTIFY_3MIN_EXAM.name, Pref.Key.NOTIFY_5MIN_EXAM.name, Pref.Key.NOTIFY_10MIN_EXAM.name, Pref.Key.NOTIFY_20MIN_EXAM.name, Pref.Key.NOTIFY_30MIN_EXAM.name)

                        val map = view.getAll()
                        map.forEach { (key, value) ->
                            when (key) {
                                in keyNotifyLecture -> notifyLecture -= if (!(value as Boolean)) (1 shl keyNotifyLecture.indexOf(key)) else 0
                                in keyNotifyHw -> notifyHw -= if (!(value as Boolean)) (1 shl keyNotifyHw.indexOf(key)) else 0
                                in keyNotifyZoom -> notifyZoom -= if (!(value as Boolean)) (1 shl keyNotifyZoom.indexOf(key)) else 0
                                in keyNotifyExam -> notifyExam -= if (!(value as Boolean)) (1 shl keyNotifyExam.indexOf(key)) else 0
                                else -> {
                                    when (value) {
                                        is Boolean -> set(booleanPreferencesKey(key), value)
                                        is Int -> set(intPreferencesKey(key), value)
                                        is Long -> set(longPreferencesKey(key), value)
                                        is Float -> set(floatPreferencesKey(key), value)
                                        is String -> set(stringPreferencesKey(key), value)
                                        is ByteArray -> set(byteArrayPreferencesKey(key), value)
                                        is Double -> set(doublePreferencesKey(key), value)
                                    }
                                }
                            }
                        }
                        set(Pref.Key.NOTIFY_LECTURE, notifyLecture)
                        set(Pref.Key.NOTIFY_HOMEWORK, notifyHw)
                        set(Pref.Key.NOTIFY_ZOOM, notifyZoom)
                        set(Pref.Key.NOTIFY_EXAM, notifyExam)

                    }.toPreferences()
                }
            ),
        )
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