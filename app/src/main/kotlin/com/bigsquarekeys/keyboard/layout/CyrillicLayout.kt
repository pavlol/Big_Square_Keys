package com.bigsquarekeys.keyboard.layout

import com.bigsquarekeys.keyboard.model.Key
import com.bigsquarekeys.keyboard.model.KeyAction
import com.bigsquarekeys.keyboard.model.KeyboardLayout

/**
 * Russian ЙЦУКЕН keyboard layout wrapped into 5 or 6 columns.
 * 33 Cyrillic letters arranged in traditional Russian keyboard order.
 *
 * 6-column layout:
 *   Й Ц У К Е Н  /  Г Ш Щ З Х Ъ  /  Ф Ы В А П Р
 *   О Л Д Ж Э Я  /  Ч С М И Т Ь  /  Б Ю , . ⌫ ↵
 *   123 ⇧ [SPC×2] ! 🌐
 *
 * 5-column layout:
 *   Й Ц У К Е  /  Н Г Ш Щ З  /  Х Ъ Ф Ы В
 *   А П Р О Л  /  Д Ж Э Я Ч  /  С М И Т Ь
 *   Б Ю . ⌫ ↵  /  123 ⇧ SPC ! 🌐
 */
object CyrillicLayout {

    fun build(columns: Int): KeyboardLayout = when (columns) {
        5 -> build5()
        else -> build6()
    }

    private fun letter(c: Char) = Key(c.lowercaseChar().toString(), KeyAction.CommitChar(c.lowercaseChar()))

    private fun build5(): KeyboardLayout {
        val rows = listOf(
            listOf(letter('й'), letter('ц'), letter('у'), letter('к'), letter('е')),
            listOf(letter('н'), letter('г'), letter('ш'), letter('щ'), letter('з')),
            listOf(letter('х'), letter('ъ'), letter('ф'), letter('ы'), letter('в')),
            listOf(letter('а'), letter('п'), letter('р'), letter('о'), letter('л')),
            listOf(letter('д'), letter('ж'), letter('э'), letter('я'), letter('ч')),
            listOf(letter('с'), letter('м'), letter('и'), letter('т'), letter('ь')),
            listOf(letter('б'), letter('ю'), Key(".", KeyAction.CommitChar('.')), Key("⌫", KeyAction.Delete), Key("↵", KeyAction.Enter)),
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
            listOf(letter('й'), letter('ц'), letter('у'), letter('к'), letter('е'), letter('н')),
            listOf(letter('г'), letter('ш'), letter('щ'), letter('з'), letter('х'), letter('ъ')),
            listOf(letter('ф'), letter('ы'), letter('в'), letter('а'), letter('п'), letter('р')),
            listOf(letter('о'), letter('л'), letter('д'), letter('ж'), letter('э'), letter('я')),
            listOf(letter('ч'), letter('с'), letter('м'), letter('и'), letter('т'), letter('ь')),
            listOf(letter('б'), letter('ю'), Key(",", KeyAction.CommitChar(',')), Key(".", KeyAction.CommitChar('.')), Key("⌫", KeyAction.Delete), Key("↵", KeyAction.Enter)),
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
