package net.ienlab.sogangassist.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class FadingEdgeBottomRecyclerView(context: Context, attrs: AttributeSet): RecyclerView(context, attrs) {
    override fun getTopFadingEdgeStrength(): Float = 0f
}