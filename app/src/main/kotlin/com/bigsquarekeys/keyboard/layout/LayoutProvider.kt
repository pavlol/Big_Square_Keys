package com.bigsquarekeys.keyboard.layout

import com.bigsquarekeys.keyboard.model.KeyboardLayout
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class KeyboardMode { ALPHA, NUMBERS, SYMBOLS }

@Singleton
class LayoutProvider @Inject constructor() {

    fun getLayout(locale: Locale, mode: KeyboardMode, columns: Int): KeyboardLayout =
        when (mode) {
            KeyboardMode.ALPHA -> LatinLayout.build(columns.coerceIn(5, 6))
            KeyboardMode.NUMBERS -> NumberLayout.build()
            KeyboardMode.SYMBOLS -> NumberLayout.build() // placeholder until SymbolLayout exists
        }
}
