package net.ienlab.sogangassist.preference

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import net.ienlab.sogangassist.R


class AppInfoPreference(context: Context, attrs: AttributeSet): Preference(context, attrs) {
    init {
        widgetLayoutResource = R.layout.preference_app_info

    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        (holder.findViewById(R.id.typo) as TextView).typeface = ResourcesCompat.getFont(context, R.font.pretendard_black) ?: Typeface.DEFAULT
        (holder.findViewById(R.id.version) as TextView).typeface = ResourcesCompat.getFont(context, R.font.pretendard_black) ?: Typeface.DEFAULT
    }
}