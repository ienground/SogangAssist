package net.ienlab.sogangassist

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder


class AppInfoPreference(context: Context, attrs: AttributeSet): Preference(context, attrs) {
    init {
        widgetLayoutResource = R.layout.settings_app_title

    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        (holder.findViewById(R.id.typo) as TextView).typeface = Typeface.createFromAsset(context.assets, "fonts/gmsans_bold.otf")
    }




}