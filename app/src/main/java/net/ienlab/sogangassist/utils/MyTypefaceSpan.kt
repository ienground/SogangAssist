package net.ienlab.sogangassist.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

import androidx.collection.LruCache
import androidx.core.content.res.ResourcesCompat

class MyTypefaceSpan(context: Context, typefaceId: Int): MetricAffectingSpan() {

    private var mTypeface: Typeface? = ResourcesCompat.getFont(context, typefaceId) ?: Typeface.DEFAULT

    override fun updateMeasureState(p: TextPaint) {
        p.typeface = mTypeface

        // Note: This flag is required for proper typeface rendering
        p.flags = p.flags or Paint.SUBPIXEL_TEXT_FLAG
    }

    override fun updateDrawState(tp: TextPaint) {
        tp.typeface = mTypeface

        // Note: This flag is required for proper typeface rendering
        tp.flags = tp.flags or Paint.SUBPIXEL_TEXT_FLAG
    }

    companion object {
        /** An `LruCache` for previously loaded typefaces.  */
        private val sTypefaceCache = LruCache<String, Typeface>(12)
    }
}