package net.ienlab.sogangassist.decorators

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

/**
 * Span to draw a dot centered under a section of text
 */
class MyDotSpan : LineBackgroundSpan {

    private val radius: Float
    private val color: Int
    private val amount: Int

    constructor(radius: Float, color: Int, amount: Int) {
        this.radius = radius
        this.color = color
        this.amount = amount
    }

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


        if (amount <= 3) {
            for (i in 0 until amount) {
                canvas.drawCircle(((left + right) / 2).toFloat() - radius * 1.5f * (amount - 1) + 3 * radius * i, bottom + 2 * radius, radius, paint)
            }
        } else {
            for (i in 0 until 3) {
                canvas.drawCircle(((left + right) / 2).toFloat() - radius * 1.5f * 3 + 3 * radius * i, bottom + 2 * radius, radius, paint)
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
