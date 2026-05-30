package com.bigsquarekeys.keyboard.model

/**
 * A full keyboard layout: an ordered list of rows, each row an ordered list of keys.
 * [columns] is the number of columns this layout was designed for (5 or 6).
 */
data class KeyboardLayout(
    val rows: List<List<Key>>,
    val columns: Int,
)
