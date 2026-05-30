package com.bigsquarekeys.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.bigsquarekeys.keyboard.model.Key
import com.bigsquarekeys.keyboard.model.KeyboardLayout
import com.bigsquarekeys.keyboard.GAP_PX

private const val KEY_CORNER_RADIUS = 8f
private const val LABEL_SIZE_RATIO = 0.38f // fraction of key height

/**
 * Custom View that renders the keyboard on a Canvas and dispatches touch events.
 * No XML layout. Dimensions come from [KeyboardEngine] which uses physical DPI.
 */
class KeyboardView(context: Context) : View(context) {

    var layout: KeyboardLayout? = null
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var onKeyPressed: ((Key) -> Unit)? = null
    var isShifted: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2C2C2E")
        style = Paint.Style.FILL
    }
    private val specialKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1C1C1E")
        style = Paint.Style.FILL
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private val secondaryLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8E8E93")
        textAlign = Paint.Align.RIGHT
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val dm = resources.displayMetrics
        val keyHeightPx = (10f / 25.4f) * dm.ydpi
        val rows = layout?.rows?.size ?: 7
        val height = (rows * keyHeightPx + (rows - 1) * GAP_PX).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val kb = layout ?: return
        for (row in kb.rows) {
            for (key in row) {
                val bounds = key.bounds ?: continue
                drawKey(canvas, key, bounds)
            }
        }
    }

    private fun drawKey(canvas: Canvas, key: Key, bounds: RectF) {
        val isSpecial = key.action.javaClass.simpleName.let {
            it == "Delete" || it == "Enter" || it == "ShiftToggle" ||
                it == "SwitchToNumbers" || it == "SwitchLanguage"
        }
        val paint = if (isSpecial) specialKeyPaint else keyPaint
        canvas.drawRoundRect(bounds, KEY_CORNER_RADIUS, KEY_CORNER_RADIUS, paint)

        val labelSize = bounds.height() * LABEL_SIZE_RATIO
        labelPaint.textSize = labelSize

        val label = if (isShifted && key.label.length == 1) key.label.uppercase() else key.label
        canvas.drawText(
            label,
            bounds.centerX(),
            bounds.centerY() + labelSize * 0.35f,
            labelPaint,
        )

        key.secondaryLabel?.let { sec ->
            secondaryLabelPaint.textSize = labelSize * 0.45f
            canvas.drawText(sec, bounds.right - 4f, bounds.top + labelSize * 0.55f, secondaryLabelPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val kb = layout ?: return true
            val key = findKey(event.x, event.y, kb) ?: return true
            onKeyPressed?.invoke(key)
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun findKey(x: Float, y: Float, kb: KeyboardLayout): Key? {
        for (row in kb.rows) {
            for (key in row) {
                if (key.bounds?.contains(x, y) == true) return key
            }
        }
        return null
    }
}
