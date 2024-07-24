package net.ienlab.sogangassist.constant

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object Pref {
    object Key {
        val NOTIFY_1HOUR_HW = booleanPreferencesKey("notify_1hour_hw")
        val NOTIFY_2HOUR_HW = booleanPreferencesKey("notify_2hour_hw")
        val NOTIFY_6HOUR_HW = booleanPreferencesKey("notify_6hour_hw")
        val NOTIFY_12HOUR_HW = booleanPreferencesKey("notify_12hour_hw")
        val NOTIFY_24HOUR_HW = booleanPreferencesKey("notify_24hour_hw")

        val NOTIFY_1HOUR_LEC = booleanPreferencesKey("notify_1hour_lec")
        val NOTIFY_2HOUR_LEC = booleanPreferencesKey("notify_2hour_lec")
        val NOTIFY_6HOUR_LEC = booleanPreferencesKey("notify_6hour_lec")
        val NOTIFY_12HOUR_LEC = booleanPreferencesKey("notify_12hour_lec")
        val NOTIFY_24HOUR_LEC = booleanPreferencesKey("notify_24hour_lec")

        val NOTIFY_3MIN_ZOOM = booleanPreferencesKey("notify_3min_zoom")
        val NOTIFY_5MIN_ZOOM = booleanPreferencesKey("notify_5min_zoom")
        val NOTIFY_10MIN_ZOOM = booleanPreferencesKey("notify_10min_zoom")
        val NOTIFY_20MIN_ZOOM = booleanPreferencesKey("notify_20min_zoom")
        val NOTIFY_30MIN_ZOOM = booleanPreferencesKey("notify_30min_zoom")

        val NOTIFY_3MIN_EXAM = booleanPreferencesKey("notify_3min_exam")
        val NOTIFY_5MIN_EXAM = booleanPreferencesKey("notify_5min_exam")
        val NOTIFY_10MIN_EXAM = booleanPreferencesKey("notify_10min_exam")
        val NOTIFY_20MIN_EXAM = booleanPreferencesKey("notify_20min_exam")
        val NOTIFY_30MIN_EXAM = booleanPreferencesKey("notify_30min_exam")

        val IS_FIRST_VISIT = booleanPreferencesKey("isFirstVisit")
        val LAST_VERSION = intPreferencesKey("lastVersion")
        val FULL_AD_CHARGE = intPreferencesKey("fullAdCharge")
        val SET_REGISTER_ALERT = booleanPreferencesKey("setRegisterAlert")

        val ALLOW_MORNING_REMINDER = booleanPreferencesKey("allow_morning_reminder")
        val ALLOW_NIGHT_REMINDER = booleanPreferencesKey("allow_night_reminder")
        val TIME_MORNING_REMINDER = intPreferencesKey("time_morning_reminder")
        val TIME_NIGHT_REMINDER = intPreferencesKey("time_night_reminder")

        val DND_START_TIME = intPreferencesKey("dndStartTime")
        val DND_END_TIME = intPreferencesKey("dndEndTime")
        val DND_CHECK = booleanPreferencesKey("dnd_time_check")
        val CALENDAR_ICON_SHOW = booleanPreferencesKey("calendar_icon_show")
        val WIDGET_DARK_MODE = booleanPreferencesKey("widget_dark_mode")

        val MATERIAL_YOU = booleanPreferencesKey("is_material_you")
    }

    object Default {
        const val NOTIFY = true
        const val MATERIAL_YOU = true
    }
}