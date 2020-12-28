package net.ienlab.sogangassist.decorators

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

/**
 * Span to draw a dot centered under a section of text
 */
class MyDotSpan(private val radius: Float, private val color: Int, amount: IntArray) : LineBackgroundSpan {

    private val finishedAmount: Int = amount[1]
    private val unfinishedAmount: Int = amount[0]

    override fun drawBackground(
            canvas: Canvas, paint: Paint,
            left: Int, right: Int, top: Int, baseline: Int, bottom: Int,
            charSequence: CharSequence,
            start: Int, end: Int, lineNum: Int
    ) {
        val oldColor = paint.color
        val oldStrokeWidth = paint.strokeWidth
        val oldStyle = paint.style

        if (color != 0) {
            paint.color = color
        }

//        if (!isPosted) {
//            paint.strokeWidth = 5f
//            paint.style = Paint.Style.STROKE
//        }

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

    companion object {

        /**
         * Default radius used
         */
        val DEFAULT_RADIUS = 3f
    }
}
