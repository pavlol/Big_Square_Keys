package com.bigsquarekeys.keyboard.layout

import com.bigsquarekeys.keyboard.model.Key
import com.bigsquarekeys.keyboard.model.KeyAction
import com.bigsquarekeys.keyboard.model.KeyboardLayout

/** Phone-style numeric keypad (3 columns + operator column = 4 cols). */
object NumberLayout {

    fun build(): KeyboardLayout {
        val rows = listOf(
            listOf(
                Key("1", KeyAction.CommitChar('1')),
                Key("2", KeyAction.CommitChar('2')),
                Key("3", KeyAction.CommitChar('3')),
                Key("+", KeyAction.CommitChar('+')),
            ),
            listOf(
                Key("4", KeyAction.CommitChar('4')),
                Key("5", KeyAction.CommitChar('5')),
                Key("6", KeyAction.CommitChar('6')),
                Key("-", KeyAction.CommitChar('-')),
            ),
            listOf(
                Key("7", KeyAction.CommitChar('7')),
                Key("8", KeyAction.CommitChar('8')),
                Key("9", KeyAction.CommitChar('9')),
                Key("*", KeyAction.CommitChar('*')),
            ),
            listOf(
                Key("#+=", KeyAction.SwitchToSymbols),
                Key("0", KeyAction.CommitChar('0')),
                Key("⌫", KeyAction.Delete),
                Key("↵", KeyAction.Enter),
            ),
            listOf(
                Key("ABC", KeyAction.SwitchToAlpha, widthMultiplier = 2f),
                Key(".", KeyAction.CommitChar('.')),
                Key(",", KeyAction.CommitChar(',')),
            ),
        )
        return KeyboardLayout(rows, columns = 4)
    }
}
