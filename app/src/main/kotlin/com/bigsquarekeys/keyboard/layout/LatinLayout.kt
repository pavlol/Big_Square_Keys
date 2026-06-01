package com.bigsquarekeys.keyboard.layout

import com.bigsquarekeys.keyboard.model.Key
import com.bigsquarekeys.keyboard.model.KeyAction
import com.bigsquarekeys.keyboard.model.KeyboardLayout

/**
 * Wrapped-QWERTY layout. Letters follow QWERTY order, wrapped into 5 or 6 columns.
 * Every row sums to exactly [columns] column-slots so keys fill full screen width.
 *
 * 5-column:
 *   Q W E R T  /  Y U I O P  /  A S D F G  /  H J K L ⌫
 *   ⇧ Z X C V  /  B N M . ↵  /  123 , SPC ! 🌐
 *
 * 6-column:
 *   Q W E R T Y  /  U I O P A S  /  D F G H J K  /  L Z X C V B
 *   N M , . ⌫ ↵  /  123 ⇧ SPC(×2) ! 🌐
 */
object LatinLayout {

    fun build(columns: Int): KeyboardLayout = when (columns) {
        5 -> build5()
        else -> build6()
    }

    private fun letter(c: Char) = Key(c.lowercaseChar().toString(), KeyAction.CommitChar(c.lowercaseChar()))

    private fun build5(): KeyboardLayout {
        val rows = listOf(
            // Row 0 — 5 slots
            listOf(letter('Q'), letter('W'), letter('E'), letter('R'), letter('T')),
            // Row 1 — 5 slots
            listOf(letter('Y'), letter('U'), letter('I'), letter('O'), letter('P')),
            // Row 2 — 5 slots
            listOf(letter('A'), letter('S'), letter('D'), letter('F'), letter('G')),
            // Row 3 — 5 slots
            listOf(letter('H'), letter('J'), letter('K'), letter('L'), Key("⌫", KeyAction.Delete)),
            // Row 4 — 5 slots
            listOf(Key("⇧", KeyAction.ShiftToggle), letter('Z'), letter('X'), letter('C'), letter('V')),
            // Row 5 — 5 slots
            listOf(letter('B'), letter('N'), letter('M'), Key(".", KeyAction.CommitChar('.')), Key("↵", KeyAction.Enter)),
            // Row 6 — 5 slots (Space = 1 slot)
            listOf(
                Key("123", KeyAction.SwitchToNumbers),
                Key(",", KeyAction.CommitChar(',')),
                Key("Space", KeyAction.Space),
                Key("!", KeyAction.CommitChar('!')),
                Key("🌐", KeyAction.SwitchLanguage),
            ),
        )
        return KeyboardLayout(rows, columns = 5)
    }

    private fun build6(): KeyboardLayout {
        val rows = listOf(
            // Row 0 — 6 slots
            listOf(letter('Q'), letter('W'), letter('E'), letter('R'), letter('T'), letter('Y')),
            // Row 1 — 6 slots
            listOf(letter('U'), letter('I'), letter('O'), letter('P'), letter('A'), letter('S')),
            // Row 2 — 6 slots
            listOf(letter('D'), letter('F'), letter('G'), letter('H'), letter('J'), letter('K')),
            // Row 3 — 6 slots
            listOf(letter('L'), letter('Z'), letter('X'), letter('C'), letter('V'), letter('B')),
            // Row 4 — 6 slots
            listOf(letter('N'), letter('M'), Key(",", KeyAction.CommitChar(',')), Key(".", KeyAction.CommitChar('.')), Key("⌫", KeyAction.Delete), Key("↵", KeyAction.Enter)),
            // Row 5 — 6 slots: 123(1) ⇧(1) Space(2) !(1) 🌐(1)
            listOf(
                Key("123", KeyAction.SwitchToNumbers),
                Key("⇧", KeyAction.ShiftToggle),
                Key("Space", KeyAction.Space, widthMultiplier = 2f),
                Key("!", KeyAction.CommitChar('!')),
                Key("🌐", KeyAction.SwitchLanguage),
            ),
        )
        return KeyboardLayout(rows, columns = 6)
    }
}
