package com.bigsquarekeys.keyboard.model

import android.graphics.RectF

/**
 * A single keyboard key.
 *
 * [widthMultiplier] = 1f for a standard square key.
 *                   = 2f for Space or Enter spanning two columns.
 * [bounds] is populated by KeyboardEngine after layout computation — null until then.
 */
data class Key(
    val label: String,
    val action: KeyAction,
    val widthMultiplier: Float = 1f,
    val secondaryLabel: String? = null,
    var bounds: RectF? = null,
)
