package net.ienlab.sogangassist.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class FadingEdgeTopRecyclerView(context: Context, attrs: AttributeSet): RecyclerView(context, attrs) {
    override fun getBottomFadingEdgeStrength(): Float = 0f
}