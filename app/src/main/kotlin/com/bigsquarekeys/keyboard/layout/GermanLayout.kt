package com.bigsquarekeys.keyboard.layout

import com.bigsquarekeys.keyboard.model.Key
import com.bigsquarekeys.keyboard.model.KeyAction
import com.bigsquarekeys.keyboard.model.KeyboardLayout

/**
 * German QWERTZ layout with umlauts and ß.
 * 30 characters: 26 standard + ä ö ü ß.
 * Note: Y and Z are swapped compared to QWERTY (German standard).
 *
 * 5-column layout:
 *   q w e r t  /  z u i o p  /  ü a s d f
 *   g h j k l  /  ö ä y x c  /  v b n m ß
 *   , . ? ⌫ ↵  /  123 ⇧ SPC ! 🌐
 *
 * 6-column layout:
 *   q w e r t z  /  u i o p ü a  /  s d f g h j
 *   k l ö ä y x  /  c v b n m ß  /  , . - ? ⌫ ↵
 *   123 ⇧ SPC(×2) ! 🌐
 */
object GermanLayout {

    fun build(columns: Int): KeyboardLayout = when (columns) {
        5 -> build5()
        else -> build6()
    }

    private fun letter(c: Char) = Key(c.lowercaseChar().toString(), KeyAction.CommitChar(c.lowercaseChar()))

    private fun build5(): KeyboardLayout {
        val rows = listOf(
            listOf(letter('q'), letter('w'), letter('e'), letter('r'), letter('t')),
            listOf(letter('z'), letter('u'), letter('i'), letter('o'), letter('p')),
            listOf(letter('ü'), letter('a'), letter('s'), letter('d'), letter('f')),
            listOf(letter('g'), letter('h'), letter('j'), letter('k'), letter('l')),
            listOf(letter('ö'), letter('ä'), letter('y'), letter('x'), letter('c')),
            listOf(letter('v'), letter('b'), letter('n'), letter('m'), letter('ß')),
            listOf(
                Key(",", KeyAction.CommitChar(',')),
                Key(".", KeyAction.CommitChar('.')),
                Key("?", KeyAction.CommitChar('?')),
                Key("⌫", KeyAction.Delete),
                Key("↵", KeyAction.Enter),
            ),
            listOf(
                Key("123", KeyAction.SwitchToNumbers),
                Key("⇧", KeyAction.ShiftToggle),
                Key("Space", KeyAction.Space),
                Key("!", KeyAction.CommitChar('!')),
                Key("🌐", KeyAction.SwitchLanguage),
            ),
        )
        return KeyboardLayout(rows, columns = 5)
    }

    private fun build6(): KeyboardLayout {
        val rows = listOf(
            listOf(letter('q'), letter('w'), letter('e'), letter('r'), letter('t'), letter('z')),
            listOf(letter('u'), letter('i'), letter('o'), letter('p'), letter('ü'), letter('a')),
            listOf(letter('s'), letter('d'), letter('f'), letter('g'), letter('h'), letter('j')),
            listOf(letter('k'), letter('l'), letter('ö'), letter('ä'), letter('y'), letter('x')),
            listOf(letter('c'), letter('v'), letter('b'), letter('n'), letter('m'), letter('ß')),
            listOf(
                Key(",", KeyAction.CommitChar(',')),
                Key(".", KeyAction.CommitChar('.')),
                Key("-", KeyAction.CommitChar('-')),
                Key("?", KeyAction.CommitChar('?')),
                Key("⌫", KeyAction.Delete),
                Key("↵", KeyAction.Enter),
            ),
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
