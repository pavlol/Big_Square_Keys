package com.bigsquarekeys.ime

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bigsquarekeys.keyboard.InputRouter
import com.bigsquarekeys.keyboard.KeyboardEngine
import com.bigsquarekeys.keyboard.KeyboardView
import com.bigsquarekeys.keyboard.layout.KeyboardMode
import com.bigsquarekeys.keyboard.layout.LayoutProvider
import com.bigsquarekeys.keyboard.model.KeyAction
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class BigSquareKeyboardService : InputMethodService() {

    @Inject lateinit var engine: KeyboardEngine
    @Inject lateinit var layoutProvider: LayoutProvider
    @Inject lateinit var inputRouter: InputRouter

    private lateinit var keyboardView: KeyboardView
    private var currentMode = KeyboardMode.ALPHA
    private var isShifted = false

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onCreateInputView(): View {
        keyboardView = KeyboardView(this).apply {
            onKeyPressed = ::handleKey
        }
        refreshLayout()
        return keyboardView
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        refreshLayout()
    }

    private fun refreshLayout() {
        val dm = resources.displayMetrics
        val columns = engine.columnsForWidth(dm.widthPixels, dm)
        val locale = currentLocale()

        val layout = layoutProvider.getLayout(locale, currentMode, columns)
        engine.assignBounds(layout, dm, dm.widthPixels)
        keyboardView.layout = layout
        keyboardView.isShifted = isShifted
    }

    private fun currentLocale(): Locale {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val subtype = imm.currentInputMethodSubtype ?: return Locale.getDefault()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale.forLanguageTag(subtype.languageTag)
        } else {
            @Suppress("DEPRECATION")
            Locale(subtype.locale)
        }
    }

    private fun switchLanguage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchToNextInputMethod(false)
        } else {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            @Suppress("DEPRECATION")
            imm.switchToNextInputMethod(window.window?.attributes?.token, false)
        }
    }

    private fun handleKey(key: com.bigsquarekeys.keyboard.model.Key) {
        when (val action = key.action) {
            KeyAction.ShiftToggle -> {
                isShifted = !isShifted
                keyboardView.isShifted = isShifted
            }
            KeyAction.SwitchToNumbers -> {
                currentMode = KeyboardMode.NUMBERS
                refreshLayout()
            }
            KeyAction.SwitchToSymbols -> {
                currentMode = KeyboardMode.SYMBOLS
                refreshLayout()
            }
            KeyAction.SwitchToAlpha -> {
                currentMode = KeyboardMode.ALPHA
                refreshLayout()
            }
            KeyAction.SwitchLanguage -> switchLanguage()
            else -> {
                inputRouter.route(action, currentInputConnection, isShifted)
                if (isShifted && action is KeyAction.CommitChar) {
                    isShifted = false
                    keyboardView.isShifted = false
                }
            }
        }
    }
}
