package com.studio.nuxcontrollerv2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class KnobView(context: Context, private val label: String, private val onValueChange: (Int) -> Unit) : View(context) {

    private var value = 0f
    private var startAngle = -135f
    private var sweepAngle = 270f
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 12f; color = Color.parseColor("#333333")
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 12f; strokeCap = Paint.Cap.ROUND; color = Color.parseColor("#FF6B35")
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL; color = Color.WHITE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 42f; textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        val padding = 35f
        val oval = RectF(padding, padding, size - padding, size - padding)
        canvas.drawArc(oval, startAngle, sweepAngle, false, trackPaint)
        val progressAngle = sweepAngle * (value / 100f)
        canvas.drawArc(oval, startAngle, progressAngle, false, progressPaint)
        val dotAngle = Math.toRadians((startAngle + progressAngle).toDouble())
        val radius = (size - padding * 2) / 2
        val cx = width / 2f + radius * cos(dotAngle).toFloat()
        val cy = height / 2f + radius * sin(dotAngle).toFloat()
        canvas.drawCircle(cx, cy, 10f, dotPaint)
        canvas.drawText("${value.toInt()}", width / 2f, height / 2f + 14f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val cx = width / 2f; val cy = height / 2f
                val angle = Math.toDegrees(atan2((event.y - cy).toDouble(), (event.x - cx).toDouble())).toFloat()
                var normalized = (angle - startAngle + 360) % 360
                if (normalized > sweepAngle) normalized = if (normalized < 180) 0f else sweepAngle
                value = (normalized / sweepAngle * 100).coerceIn(0f, 100f)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                onValueChange(value.toInt())
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = 280
        setMeasuredDimension(size, size)
    }
}
