package net.ienlab.sogangassist.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

open class FadingEdgeLeftRecyclerView(context: Context, attrs: AttributeSet): RecyclerView(context, attrs) {
    override fun getLeftFadingEdgeStrength(): Float = 0f
}