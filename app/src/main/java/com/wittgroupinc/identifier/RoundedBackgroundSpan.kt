package com.wittgroupinc.identifier

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.text.style.ReplacementSpan

/**
 * Created by Pawan Gupta on 26/05/18
 */
class RoundedBackgroundSpan : ReplacementSpan {
    @ColorRes
    internal var textColor = android.R.color.white
    @ColorRes
    internal var backgroundColor = android.R.color.black
    internal var context: Context

    constructor(context: Context) {
        this.context = context
    }

    constructor(context: Context, @ColorRes textColor: Int, @ColorRes backgroundColor: Int) {
        this.textColor = textColor
        this.backgroundColor = backgroundColor
        this.context = context
    }


    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        return (PADDING_X.toFloat() + paint.measureText(text.subSequence(start, end).toString()) + PADDING_X.toFloat()).toInt()
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val width = paint.measureText(text.subSequence(start, end).toString())
        val rect = RectF(x, top.toFloat(), x + width + (2 * PADDING_X).toFloat(), bottom.toFloat())
        paint.color = ContextCompat.getColor(context, backgroundColor)
        canvas.drawRoundRect(rect, CORNER_RADIUS.toFloat(), CORNER_RADIUS.toFloat(), paint)
        paint.color = ContextCompat.getColor(context, textColor)
        canvas.drawText(text, start, end, x + PADDING_X, y.toFloat(), paint)
    }

    companion object {

        private val CORNER_RADIUS = 28
        private val PADDING_X = 12
    }
}
