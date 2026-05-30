package com.bigsquarekeys.keyboard

import android.graphics.RectF
import android.util.DisplayMetrics
import com.bigsquarekeys.keyboard.model.Key
import com.bigsquarekeys.keyboard.model.KeyboardLayout
import javax.inject.Inject

private const val KEY_SIZE_MM = 10f
private const val MM_PER_INCH = 25.4f
const val GAP_PX = 4f

/**
 * Computes key sizes and positions.
 *
 * Key HEIGHT is physically 10 mm (from device ydpi).
 * Key WIDTH expands to fill full screen width, but never narrower than 10 mm.
 * The number of columns is chosen so that at minimum 10 mm width fits.
 * Fewer columns → wider keys → more rows.
 */
class KeyboardEngine @Inject constructor() {

    /** Minimum physical key width in pixels (10 mm). */
    fun minKeyWidthPx(dm: DisplayMetrics): Float = (KEY_SIZE_MM / MM_PER_INCH) * dm.xdpi

    /** Physical key height in pixels (10 mm). */
    fun keyHeightPx(dm: DisplayMetrics): Float = (KEY_SIZE_MM / MM_PER_INCH) * dm.ydpi

    /**
     * Maximum columns that still keep each key >= 10 mm wide.
     * Actual key width will then be expanded to fill the screen exactly.
     * Clamped to [4, 8].
     */
    fun columnsForWidth(screenWidthPx: Int, dm: DisplayMetrics): Int {
        val minKw = minKeyWidthPx(dm)
        // columns * minKw + (columns-1) * GAP_PX <= screenWidthPx
        // columns * (minKw + GAP_PX) <= screenWidthPx + GAP_PX
        val maxCols = ((screenWidthPx + GAP_PX) / (minKw + GAP_PX)).toInt()
        return maxCols.coerceIn(4, 8)
    }

    /**
     * The actual key width after expanding to fill [screenWidthPx].
     * For [columns] column-slots: columns * baseW + (columns-1) * GAP = screenWidth
     */
    fun baseKeyWidthPx(screenWidthPx: Int, columns: Int): Float =
        (screenWidthPx - (columns - 1) * GAP_PX) / columns

    /**
     * Assigns pixel [RectF] bounds to every key in [layout] so they fill [screenWidthPx].
     * A key with widthMultiplier=2 spans 2 column slots (width = 2*baseW + 1*GAP).
     */
    fun assignBounds(layout: KeyboardLayout, dm: DisplayMetrics, screenWidthPx: Int) {
        val baseKw = baseKeyWidthPx(screenWidthPx, layout.columns)
        val kh = keyHeightPx(dm)

        var rowTop = 0f
        for (row in layout.rows) {
            var colLeft = 0f
            for (key in row) {
                val keyWidth = key.widthMultiplier * baseKw + (key.widthMultiplier - 1) * GAP_PX
                key.bounds = RectF(colLeft, rowTop, colLeft + keyWidth, rowTop + kh)
                colLeft += keyWidth + GAP_PX
            }
            rowTop += kh + GAP_PX
        }
    }

    /** Total keyboard height in pixels. */
    fun totalHeightPx(layout: KeyboardLayout, dm: DisplayMetrics): Int {
        val kh = keyHeightPx(dm)
        val rows = layout.rows.size
        return (rows * kh + (rows - 1) * GAP_PX).toInt()
    }

    /** Returns the key at pixel coordinate ([x], [y]), or null. */
    fun keyAt(x: Float, y: Float, layout: KeyboardLayout): Key? {
        for (row in layout.rows) {
            for (key in row) {
                if (key.bounds?.contains(x, y) == true) return key
            }
        }
        return null
    }
}
