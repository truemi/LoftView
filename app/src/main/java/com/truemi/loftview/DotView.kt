package com.truemi.loftview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

class DotView : View {

    internal var percent: Float = 0.0f
    internal var maxRadius = 10f
    internal var maxDist = 30f
    internal var mPaint: Paint

    init {
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.color = Color.GRAY
    }

    public constructor(context: Context) : super(context) {
        DotView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    fun setPercent(percent: Float) {
        this.percent = percent
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = (width / 2).toFloat()
        val centerY = (height / 2).toFloat()
        val fl = 255 * percent * 1.5f + 30
        mPaint.alpha = if (fl > 255) 255 else fl.toInt()
        if (percent <= 0.3f) {
            val radius = percent * 3.33f * maxRadius
            canvas.drawCircle(centerX, centerY, radius, mPaint)
        } else {//画三个个圆
            val afterPercent = (percent - 0.3f) / 0.7f
            if (afterPercent <= 1) {
                val radius = maxRadius - maxRadius / 2f * afterPercent
                Log.e("afterPercent--->", afterPercent.toString())
                canvas.drawCircle(centerX, centerY, radius, mPaint)
                canvas.drawCircle(centerX - afterPercent * maxDist, centerY, maxRadius / 2, mPaint)
                canvas.drawCircle(centerX + afterPercent * maxDist, centerY, maxRadius / 2, mPaint)
            } else if (afterPercent > 1) {
                var d = afterPercent - 1.0
                d = if (d > 1) 1.0 else d
                val fl = (1 - d * 2) * 255
                mPaint.alpha = if (fl < 60) 0 else fl.toInt()
                canvas.drawCircle(centerX, centerY, maxRadius / 2, mPaint)
                canvas.drawCircle(centerX - maxDist, centerY, maxRadius / 2, mPaint)
                canvas.drawCircle(centerX + maxDist, centerY, maxRadius / 2, mPaint)
            }
        }
    }
}