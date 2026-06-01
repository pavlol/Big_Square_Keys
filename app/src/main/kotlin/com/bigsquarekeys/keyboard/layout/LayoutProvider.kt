package com.bigsquarekeys.keyboard.layout

import com.bigsquarekeys.keyboard.model.KeyboardLayout
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class KeyboardMode { ALPHA, NUMBERS, SYMBOLS }

/** Scripts that need dedicated layouts instead of Latin QWERTY. */
private val CYRILLIC_LOCALES  = setOf("ru", "bg", "sr", "mk", "be")
private val UKRAINIAN_LOCALES = setOf("uk")
private val GERMAN_LOCALES    = setOf("de")
private val RTL_LOCALES       = setOf("ar", "he", "fa", "ur")

@Singleton
class LayoutProvider @Inject constructor() {

    fun getLayout(locale: Locale, mode: KeyboardMode, columns: Int): KeyboardLayout =
        when (mode) {
            KeyboardMode.NUMBERS -> NumberLayout.build()
            KeyboardMode.SYMBOLS -> SymbolLayout.build()
            KeyboardMode.ALPHA   -> alphaLayout(locale, columns)
        }

    private fun alphaLayout(locale: Locale, columns: Int): KeyboardLayout {
        val lang = locale.language.lowercase()
        val cols = columns.coerceIn(5, 6)
        return when {
            lang in CYRILLIC_LOCALES  -> CyrillicLayout.build(cols)
            lang in UKRAINIAN_LOCALES -> UkrainianLayout.build(cols)
            lang in GERMAN_LOCALES    -> GermanLayout.build(cols)
            lang in RTL_LOCALES       -> LatinLayout.build(cols) // Arabic/Hebrew: v1.1
            else                      -> LatinLayout.build(cols) // All Latin scripts
        }
    }
}
