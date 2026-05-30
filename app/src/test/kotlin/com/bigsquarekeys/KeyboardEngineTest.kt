package com.bigsquarekeys

import android.util.DisplayMetrics
import com.bigsquarekeys.keyboard.KeyboardEngine
import com.bigsquarekeys.keyboard.layout.LatinLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardEngineTest {

    private val engine = KeyboardEngine()

    private fun displayMetrics(xdpi: Float = 400f, yDpi: Float = 400f, widthPx: Int = 1080) =
        DisplayMetrics().apply {
            this.xdpi = xdpi
            this.ydpi = yDpi
            this.widthPixels = widthPx
        }

    @Test
    fun `minimum key width is physically 10mm`() {
        val dm = displayMetrics(xdpi = 400f)
        val expected = (10f / 25.4f) * 400f
        assertEquals(expected, engine.minKeyWidthPx(dm), 0.01f)
    }

    @Test
    fun `key height is physically 10mm`() {
        val dm = displayMetrics(yDpi = 400f)
        val expected = (10f / 25.4f) * 400f
        assertEquals(expected, engine.keyHeightPx(dm), 0.01f)
    }

    @Test
    fun `5 columns selected for narrow screen`() {
        val dm = displayMetrics(xdpi = 400f, widthPx = 800)
        val cols = engine.columnsForWidth(800, dm)
        assertEquals(5, cols)
    }

    @Test
    fun `assignBounds sets bounds on every key`() {
        val layout = LatinLayout.build(5)
        val dm = displayMetrics()
        engine.assignBounds(layout, dm, dm.widthPixels)
        for (row in layout.rows) {
            for (key in row) {
                assertNotNull("Key '${key.label}' has no bounds", key.bounds)
            }
        }
    }

    @Test
    fun `total height grows with number of rows`() {
        val layout5 = LatinLayout.build(5)
        val layout6 = LatinLayout.build(6)
        val dm = displayMetrics()
        val h5 = engine.totalHeightPx(layout5, dm)
        val h6 = engine.totalHeightPx(layout6, dm)
        assertTrue("5-col layout should be taller than 6-col", h5 > h6)
    }

    @Test
    fun `keyAt returns correct key after bounds assignment`() {
        val layout = LatinLayout.build(5)
        val dm = displayMetrics()
        engine.assignBounds(layout, dm, dm.widthPixels)
        val firstKey = layout.rows[0][0]
        val bounds = firstKey.bounds!!
        val found = engine.keyAt(bounds.centerX(), bounds.centerY(), layout)
        assertEquals(firstKey.label, found?.label)
    }

    @Test
    fun `keys fill full screen width`() {
        val screenWidth = 1080
        val dm = displayMetrics(widthPx = screenWidth)
        val layout = LatinLayout.build(5)
        engine.assignBounds(layout, dm, screenWidth)
        // Last key in first row should end at screen width
        val lastKey = layout.rows[0].last()
        assertEquals(screenWidth.toFloat(), lastKey.bounds!!.right, 1f)
    }

    @Test
    fun `actual key width is at least 10mm`() {
        val screenWidth = 1080
        val dm = displayMetrics(xdpi = 400f, widthPx = screenWidth)
        val cols = engine.columnsForWidth(screenWidth, dm)
        val actualWidth = engine.baseKeyWidthPx(screenWidth, cols)
        val minWidth = engine.minKeyWidthPx(dm)
        assertTrue("Key width $actualWidth must be >= min 10mm ($minWidth)", actualWidth >= minWidth)
    }
}
