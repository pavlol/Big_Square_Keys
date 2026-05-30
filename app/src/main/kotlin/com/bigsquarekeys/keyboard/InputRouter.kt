package com.bigsquarekeys.keyboard

import android.view.inputmethod.InputConnection
import com.bigsquarekeys.keyboard.model.KeyAction
import javax.inject.Inject

/**
 * Translates a [KeyAction] into the appropriate [InputConnection] call.
 * Never reads surrounding text or stores any user input.
 */
class InputRouter @Inject constructor() {

    fun route(action: KeyAction, ic: InputConnection?, isShifted: Boolean) {
        ic ?: return
        when (action) {
            is KeyAction.CommitChar -> {
                val c = if (isShifted) action.char.uppercaseChar() else action.char
                ic.commitText(c.toString(), 1)
            }
            is KeyAction.CommitText -> ic.commitText(action.text, 1)
            KeyAction.Space -> ic.commitText(" ", 1)
            KeyAction.Delete -> ic.deleteSurroundingText(1, 0)
            KeyAction.DeleteWord -> ic.deleteSurroundingText(1, 0) // simple fallback
            KeyAction.Enter -> ic.commitText("\n", 1)
            // Non-text actions are handled by BigSquareKeyboardService
            else -> Unit
        }
    }
}
