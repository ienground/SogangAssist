package net.ienlab.sogangassist

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import net.ienlab.sogangassist.constant.SharedDefault
import net.ienlab.sogangassist.constant.SharedKey

class MyApplication: Application() {

    companion object {

        private var appInstance: MyApplication? = null
        private lateinit var sharedPreferences: SharedPreferences
        private var sharedPreferencesEditor: SharedPreferences.Editor? = null

    }

    override fun onCreate() {
        super.onCreate()

        appInstance = this
        sharedPreferences = getSharedPreferences(packageName + "_preferences", Context.MODE_PRIVATE)

        if (sharedPreferences.getBoolean(SharedKey.MATERIAL_YOU, SharedDefault.MATERIAL_YOU)) DynamicColors.applyToActivitiesIfAvailable(this)
    }



}