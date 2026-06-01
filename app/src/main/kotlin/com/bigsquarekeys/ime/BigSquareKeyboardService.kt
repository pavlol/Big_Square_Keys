package com.bigsquarekeys.ime

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.bigsquarekeys.keyboard.InputRouter
import com.bigsquarekeys.keyboard.KeyboardEngine
import com.bigsquarekeys.keyboard.KeyboardView
import com.bigsquarekeys.keyboard.layout.KeyboardMode
import com.bigsquarekeys.keyboard.layout.LayoutProvider
import com.bigsquarekeys.keyboard.model.KeyAction
import com.bigsquarekeys.language.DEFAULT_LANGUAGE
import com.bigsquarekeys.language.SUPPORTED_LANGUAGES
import com.bigsquarekeys.settings.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class BigSquareKeyboardService : InputMethodService() {

    @Inject lateinit var engine: KeyboardEngine
    @Inject lateinit var layoutProvider: LayoutProvider
    @Inject lateinit var inputRouter: InputRouter
    @Inject lateinit var prefs: UserPreferences

    private lateinit var container: FrameLayout
    private lateinit var keyboardView: KeyboardView

    /** Non-null when the language picker overlay is visible. */
    private var pickerOverlay: View? = null

    private var currentMode = KeyboardMode.ALPHA
    private var isShifted = false

    /** Index into prefs.enabledLanguages for the currently active language. */
    private var currentLanguageIndex = 0

    // ── IME lifecycle ────────────────────────────────────────────────────────

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onCreateInputView(): View {
        container = FrameLayout(this)

        keyboardView = KeyboardView(this).apply {
            onKeyPressed = ::handleKey
        }
        container.addView(
            keyboardView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
        refreshLayout()
        return container
    }

    override fun onStartInputView(
        info: android.view.inputmethod.EditorInfo?,
        restarting: Boolean,
    ) {
        super.onStartInputView(info, restarting)
        // Re-read language list in case user changed it in Settings
        val languages = prefs.enabledLanguages
        if (currentLanguageIndex >= languages.size) currentLanguageIndex = 0
        hideLanguagePicker()
        refreshLayout()
    }

    // ── Layout helpers ───────────────────────────────────────────────────────

    private fun currentLocale(): Locale {
        val languages = prefs.enabledLanguages
        val code = languages.getOrElse(currentLanguageIndex) { DEFAULT_LANGUAGE }
        return Locale(code)
    }

    /** Two-letter uppercase code shown on the globe key, e.g. "EN", "DE", "RU". */
    private fun currentLanguageCode(): String {
        val languages = prefs.enabledLanguages
        return languages.getOrElse(currentLanguageIndex) { DEFAULT_LANGUAGE }.uppercase()
    }

    private fun refreshLayout() {
        val dm = resources.displayMetrics
        val columns = engine.columnsForWidth(dm.widthPixels, dm)
        val layout = layoutProvider.getLayout(currentLocale(), currentMode, columns)
        engine.assignBounds(layout, dm, dm.widthPixels)
        keyboardView.languageLabel = currentLanguageCode()
        keyboardView.layout = layout
        keyboardView.isShifted = isShifted
    }

    // ── Language picker ──────────────────────────────────────────────────────

    /**
     * Globe key pressed:
     *  - If picker is open → close it (toggle off).
     *  - If only one language is enabled → hand off to the next installed IME.
     *  - Otherwise → show the language picker overlay.
     */
    private fun switchLanguage() {
        if (pickerOverlay != null) {
            hideLanguagePicker()
            return
        }

        val languages = prefs.enabledLanguages
        if (languages.size <= 1) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                switchToNextInputMethod(false)
            } else {
                @Suppress("DEPRECATION")
                imm.switchToNextInputMethod(window.window?.attributes?.token, false)
            }
            return
        }

        showLanguagePicker()
    }

    private fun showLanguagePicker() {
        val languages = prefs.enabledLanguages

        val overlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#E61C1C1E")) // 90 % opaque dark
            gravity = Gravity.CENTER
        }

        for ((index, code) in languages.withIndex()) {
            val lang = SUPPORTED_LANGUAGES.find { it.code == code }
            val nativeName = lang?.nativeName ?: code.uppercase()
            val isActive = (index == currentLanguageIndex)

            val btn = TextView(this).apply {
                text = nativeName
                textSize = 20f
                setTextColor(if (isActive) Color.parseColor("#FFD60A") else Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(48, 28, 48, 28)
                if (isActive) setBackgroundColor(Color.parseColor("#3A3A3C"))

                val pickedIndex = index          // capture for lambda
                setOnClickListener {
                    currentLanguageIndex = pickedIndex
                    currentMode = KeyboardMode.ALPHA
                    hideLanguagePicker()
                    refreshLayout()
                }
            }

            overlay.addView(
                btn,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }

        container.addView(
            overlay,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        pickerOverlay = overlay
    }

    private fun hideLanguagePicker() {
        pickerOverlay?.let { container.removeView(it) }
        pickerOverlay = null
    }

    // ── Key handling ─────────────────────────────────────────────────────────

    private fun handleKey(key: com.bigsquarekeys.keyboard.model.Key) {
        // Any key except the globe key dismisses the picker
        if (pickerOverlay != null && key.action != KeyAction.SwitchLanguage) {
            hideLanguagePicker()
        }

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
