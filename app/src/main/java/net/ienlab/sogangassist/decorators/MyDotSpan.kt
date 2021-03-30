package net.ienlab.sogangassist.decorators

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.text.style.LineBackgroundSpan
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.activity.TAG
import net.ienlab.sogangassist.data.LMSClass
import java.util.*
import kotlin.collections.ArrayList

/**
 * Span to draw a dot centered under a section of text
 */
class MyDotSpan(private val radius: Float, private val color: Int, amount: IntArray) : LineBackgroundSpan {

    private val finishedAmount: Int = amount[1]
    private val unfinishedAmount: Int = amount[0]

    override fun drawBackground(canvas: Canvas, paint: Paint, left: Int, right: Int, top: Int, baseline: Int, bottom: Int, charSequence: CharSequence, start: Int, end: Int, lineNum: Int) {
        val oldColor = paint.color
        val oldStrokeWidth = paint.strokeWidth
        val oldStyle = paint.style

        if (color != 0) {
            paint.color = color
        }

        val wholeAmount = finishedAmount + unfinishedAmount

        if (wholeAmount <= 3) {
            paint.strokeWidth = 5f
            paint.style = Paint.Style.STROKE

            for (i in 0 until unfinishedAmount) {
                canvas.drawCircle(((left + right) / 2).toFloat() - radius * 1.5f * (wholeAmount - 1) + 3 * radius * i, bottom + 2 * radius, radius, paint)
            }

            paint.strokeWidth = oldStrokeWidth
            paint.style = oldStyle

            for (i in 0 until finishedAmount) {
                canvas.drawCircle(((left + right) / 2).toFloat() - radius * 1.5f * (wholeAmount - 1) + 3 * radius * (i + unfinishedAmount), bottom + 2 * radius, radius, paint)
            }
        } else {
            paint.strokeWidth = 5f
            paint.style = Paint.Style.STROKE

            if (unfinishedAmount >= 3) { // unfinish가 3 이상이 아니라면 섞여 있다.
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE

                for (i in 0 until 3) {
                    canvas.drawCircle(((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * i, bottom + 2 * radius, radius, paint)
                }

                paint.strokeWidth = oldStrokeWidth
                paint.style = oldStyle
            } else { // 1 4 라면 표시는 1 2
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE

                for (i in 0 until unfinishedAmount) {
                    canvas.drawCircle(((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * i, bottom + 2 * radius, radius, paint)
                }

                paint.strokeWidth = oldStrokeWidth
                paint.style = oldStyle

                for (i in 0 until 3 - unfinishedAmount) {
                    canvas.drawCircle(((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * (i + unfinishedAmount), bottom + 2 * radius, radius, paint)
                }
            }

            canvas.drawRect(((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * 3 - radius, bottom + 2 * radius - radius / 4,
                ((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * 3 + radius, bottom + 2 * radius + radius / 4, paint)

            canvas.drawRect(((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * 3 - radius / 4, bottom + radius,
                ((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * 3 + radius / 4, bottom + 3 * radius, paint)

        }

        paint.color = oldColor
        paint.strokeWidth = oldStrokeWidth
        paint.style = oldStyle
    }
}

class MyDotSpan2(private val context: Context, private val size: Float, private val data: List<LMSClass>) : LineBackgroundSpan {

//    private val finishedAmount: Int = amount[1]
//    private val unfinishedAmount: Int = amount[0]
    override fun drawBackground(canvas: Canvas, paint: Paint, left: Int, right: Int, top: Int, baseline: Int, bottom: Int, charSequence: CharSequence, start: Int, end: Int, lineNum: Int) {
        val finishedItem = ArrayList<LMSClass>()
        val unfinishedItem = ArrayList<LMSClass>()
        data.forEach { if (it.isFinished) finishedItem.add(it) else unfinishedItem.add(it) }

        val oldColor = paint.color
        val oldStrokeWidth = paint.strokeWidth
        val oldStyle = paint.style

        paint.color = ContextCompat.getColor(context, R.color.colorAccent)

        if (data.size <= 3) {
//            paint.strokeWidth = 5f
//            paint.style = Paint.Style.STROKE

            unfinishedItem.forEachIndexed { i, it ->
                val image = when (it.type) {
                    LMSClass.TYPE_LESSON -> R.drawable.ic_video
                    LMSClass.TYPE_SUP_LESSON -> R.drawable.ic_video_sup
                    LMSClass.TYPE_HOMEWORK -> R.drawable.ic_assignment
                    LMSClass.TYPE_ZOOM -> R.drawable.ic_groups
                    else -> R.drawable.ic_icon
                }

                for (k in 0..5) canvas.drawBitmap(getBitmapFromVectorDrawable(context, image, size.toInt(), size.toInt(), R.color.colorPrimary),
                    (left + right) * 0.5f - size * 0.5f * (1 + 1.5f * (data.size - 1)) + size * 1.5f * i, bottom + size / 2f, paint)
            }

            paint.strokeWidth = oldStrokeWidth
            paint.style = oldStyle

//            for (i in 0 until finishedItem.size) {
//                canvas.drawCircle(((left + right) / 2).toFloat() - radius * 1.5f * (data.size - 1) + 3 * radius * (i + unfinishedItem.size), bottom + 2 * radius, radius, paint)
//            }

            finishedItem.forEachIndexed { i, it ->
                val image = when (it.type) {
                    LMSClass.TYPE_LESSON -> R.drawable.ic_video
                    LMSClass.TYPE_SUP_LESSON -> R.drawable.ic_video_sup
                    LMSClass.TYPE_HOMEWORK -> R.drawable.ic_assignment
                    LMSClass.TYPE_ZOOM -> R.drawable.ic_groups
                    else -> R.drawable.ic_icon
                }

                for (k in 0..5) canvas.drawBitmap(getBitmapFromVectorDrawable(context, image, size.toInt(), size.toInt(), R.color.colorAccent),
                    (left + right) * 0.5f - size * 0.5f * (1 + 1.5f * (data.size - 1)) + size * 1.5f * (i + unfinishedItem.size), bottom + size / 2f, paint)
            }
        } else {
            paint.strokeWidth = 5f
            paint.style = Paint.Style.STROKE

            if (unfinishedItem.size >= 3) { // unfinish가 3 이상이 아니라면 섞여 있다.
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE

                for (i in 0 until 3) {
//                    canvas.drawCircle(((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * i, bottom + 2 * radius, radius, paint)
                }

                paint.strokeWidth = oldStrokeWidth
                paint.style = oldStyle
            } else { // 1 4 라면 표시는 1 2
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE

                for (i in 0 until unfinishedItem.size) {
//                    canvas.drawCircle(((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * i, bottom + 2 * radius, radius, paint)
                }

                paint.strokeWidth = oldStrokeWidth
                paint.style = oldStyle

                for (i in 0 until 3 - unfinishedItem.size) {
//                    canvas.drawCircle(((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * (i + unfinishedItem.size), bottom + 2 * radius, radius, paint)
                }
            }

//            canvas.drawRect(((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * 3 - radius, bottom + 2 * radius - radius / 4,
//                ((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * 3 + radius, bottom + 2 * radius + radius / 4, paint)
//
//            canvas.drawRect(((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * 3 - radius / 4, bottom + radius,
//                ((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * 3 + radius / 4, bottom + 3 * radius, paint)

        }

        paint.color = oldColor
        paint.strokeWidth = oldStrokeWidth
        paint.style = oldStyle
    }

//    fun getScaledDrawableBitmap(context: Context, resId: Int, width: Float, height: Float): Bitmap {
//        val bitmap = (ContextCompat.getDrawable(context, resId) as BitmapDrawable).bitmap.scale(width.toInt(), height.toInt(), true)
//
//        val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(outputBitmap)
//        val paint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
//        val rect = Rect(0, 0, bitmap.width, bitmap.height)
//
//        paint.isAntiAlias = true
//        canvas.drawBitmap(bitmap, rect, rect, paint)
//
//        return outputBitmap
//    }

    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int, width: Int, height: Int, colorInt: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val color = ContextCompat.getColor(context, colorInt)
        val paint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }

        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)

        val bitmapResult = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvasResult = Canvas(bitmapResult)
        canvasResult.drawBitmap(bitmap, 0f, 0f, paint)

        return bitmapResult
    }
}
