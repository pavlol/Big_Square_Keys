package com.bigsquarekeys.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import com.bigsquarekeys.keyboard.model.Key
import com.bigsquarekeys.keyboard.model.KeyAction
import com.bigsquarekeys.keyboard.model.KeyboardLayout
import com.bigsquarekeys.keyboard.GAP_PX

private const val KEY_CORNER_RADIUS = 8f
private const val LABEL_SIZE_RATIO  = 0.38f  // fraction of key height

/** Delay before backspace starts repeating on long-press (ms). */
private const val LONG_PRESS_DELAY_MS  = 400L
/** Interval between repeated deletes while backspace is held (ms). */
private const val REPEAT_INTERVAL_MS   = 55L

/**
 * Custom View that renders the keyboard on a Canvas and dispatches touch events.
 * No XML layout. Dimensions come from [KeyboardEngine] which uses physical DPI.
 *
 * Long-pressing the ⌫ key triggers continuous deletion at [REPEAT_INTERVAL_MS]
 * per character after an initial [LONG_PRESS_DELAY_MS] delay.
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

    /**
     * Label shown on the globe/language key — updated by the service to the
     * current language code, e.g. "EN", "DE", "RU".
     */
    var languageLabel: String = "🌐"
        set(value) {
            field = value
            invalidate()
        }

    // ── Long-press delete ────────────────────────────────────────────────────

    private val handler = Handler(Looper.getMainLooper())

    /** Key that was pressed on ACTION_DOWN (used to fire on ACTION_UP). */
    private var downKey: Key? = null

    /** True once the backspace long-press repeat has started firing. */
    private var isRepeatActive = false

    /**
     * Runnable that fires a delete and re-schedules itself while the key is held.
     * Stopped by clearing [isRepeatActive] and removing callbacks in ACTION_UP/CANCEL.
     */
    private val repeatDeleteRunnable = object : Runnable {
        override fun run() {
            val key = downKey ?: return
            if (key.action == KeyAction.Delete && isRepeatActive) {
                onKeyPressed?.invoke(key)
                handler.postDelayed(this, REPEAT_INTERVAL_MS)
            }
        }
    }

    // ── Paints ───────────────────────────────────────────────────────────────

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2C2C2E")
        style = Paint.Style.FILL
    }
    private val specialKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1C1C1E")
        style = Paint.Style.FILL
    }
    /** Blue background for the ⇧ key when shift is active. */
    private val shiftActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0A84FF")
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

    // ── Measure ──────────────────────────────────────────────────────────────

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val dm = resources.displayMetrics
        val keyHeightPx = (10f / 25.4f) * dm.ydpi
        val rows = layout?.rows?.size ?: 7
        val height = (rows * keyHeightPx + (rows - 1) * GAP_PX).toInt()
        setMeasuredDimension(width, height)
    }

    // ── Draw ─────────────────────────────────────────────────────────────────

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
        val isShiftKey = key.action == KeyAction.ShiftToggle
        val isSpecial  = isShiftKey ||
            key.action == KeyAction.Delete ||
            key.action == KeyAction.Enter  ||
            key.action == KeyAction.SwitchToNumbers ||
            key.action == KeyAction.SwitchToSymbols ||
            key.action == KeyAction.SwitchToAlpha   ||
            key.action == KeyAction.SwitchLanguage

        val bgPaint = when {
            isShiftKey && isShifted -> shiftActivePaint
            isSpecial               -> specialKeyPaint
            else                    -> keyPaint
        }
        canvas.drawRoundRect(bounds, KEY_CORNER_RADIUS, KEY_CORNER_RADIUS, bgPaint)

        val displayLabel = when {
            key.action == KeyAction.SwitchLanguage     -> languageLabel
            isShifted && key.label.length == 1         -> key.label.uppercase()
            else                                       -> key.label
        }

        val labelSize = bounds.height() * LABEL_SIZE_RATIO
        labelPaint.textSize = labelSize
        canvas.drawText(
            displayLabel,
            bounds.centerX(),
            bounds.centerY() + labelSize * 0.35f,
            labelPaint,
        )

        key.secondaryLabel?.let { sec ->
            secondaryLabelPaint.textSize = labelSize * 0.45f
            canvas.drawText(sec, bounds.right - 4f, bounds.top + labelSize * 0.55f, secondaryLabelPaint)
        }
    }

    // ── Touch ─────────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val kb  = layout ?: return true
                val key = findKey(event.x, event.y, kb) ?: return true
                downKey = key
                isRepeatActive = false

                if (key.action == KeyAction.Delete) {
                    // After LONG_PRESS_DELAY_MS, activate repeat and fire the first extra delete
                    handler.postDelayed({
                        isRepeatActive = true
                        repeatDeleteRunnable.run()
                    }, LONG_PRESS_DELAY_MS)
                }
            }

            MotionEvent.ACTION_UP -> {
                handler.removeCallbacksAndMessages(null)
                val wasRepeat = isRepeatActive
                isRepeatActive = false
                val key = downKey
                downKey = null

                if (!wasRepeat && key != null) {
                    // Normal short tap — fire the key once
                    onKeyPressed?.invoke(key)
                    performClick()
                }
                // If it was a long-press delete, the runnable already handled everything;
                // we intentionally do NOT fire an extra single delete on finger-up.
            }

            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacksAndMessages(null)
                isRepeatActive = false
                downKey = null
            }
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
