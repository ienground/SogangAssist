package net.ienlab.sogangassist.preference

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import net.ienlab.sogangassist.R


class AppInfoPreference(context: Context, attrs: AttributeSet): Preference(context, attrs) {
    init {
        widgetLayoutResource = R.layout.preference_app_info

    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        (holder.findViewById(R.id.typo) as TextView).typeface = Typeface.createFromAsset(context.assets, "fonts/gmsans_bold.otf")
        (holder.findViewById(R.id.version) as TextView).typeface = Typeface.createFromAsset(context.assets, "fonts/gmsans_bold.otf")
    }
}