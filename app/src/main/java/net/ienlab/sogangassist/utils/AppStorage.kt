package net.ienlab.sogangassist.utils

import android.content.Context
import android.content.SharedPreferences

class AppStorage(context: Context) {
    var pref: SharedPreferences = context.getSharedPreferences("app_storage", Context.MODE_PRIVATE)
    val PURCHASED_REMOVE_ADS = "remove_ads"

    fun purchasedAds(): Boolean {
        return pref.getBoolean(PURCHASED_REMOVE_ADS, false)
    }

    fun setPurchasedAds(flag: Boolean) {
        val editor = pref.edit()
        editor.putBoolean(PURCHASED_REMOVE_ADS, flag)
        editor.apply()
    }

    companion object {
        val ADS_FREE = "ads_free"
    }
}

