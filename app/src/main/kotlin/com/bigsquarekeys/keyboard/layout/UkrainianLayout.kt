package com.bigsquarekeys.keyboard.layout

import com.bigsquarekeys.keyboard.model.Key
import com.bigsquarekeys.keyboard.model.KeyAction
import com.bigsquarekeys.keyboard.model.KeyboardLayout

/**
 * Ukrainian ЙЦУКЕН keyboard layout.
 * Ukrainian has 32 letters, including Ї, І, Є, Ґ not present in Russian,
 * and lacks Russian-only letters Ъ, Ы, Ё, Э.
 *
 * 6-column layout:
 *   й ц у к е н  /  г ш щ з х ї  /  ф і в а п р
 *   о л д ж є я  /  ч с м и т ь  /  б ю , . ⌫ ↵
 *   123 ⇧ [SPC×2] ! 🌐
 *
 * 5-column layout:
 *   й ц у к е  /  н г ш щ з  /  х ї ф і в
 *   а п р о л  /  д ж є я ч  /  с м и т ь
 *   б ю . ⌫ ↵  /  123 ⇧ SPC ! 🌐
 */
object UkrainianLayout {

    fun build(columns: Int): KeyboardLayout = when (columns) {
        5 -> build5()
        else -> build6()
    }

    private fun letter(c: Char) = Key(c.lowercaseChar().toString(), KeyAction.CommitChar(c.lowercaseChar()))

    private fun build5(): KeyboardLayout {
        val rows = listOf(
            listOf(letter('й'), letter('ц'), letter('у'), letter('к'), letter('е')),
            listOf(letter('н'), letter('г'), letter('ш'), letter('щ'), letter('з')),
            listOf(letter('х'), letter('ї'), letter('ф'), letter('і'), letter('в')),
            listOf(letter('а'), letter('п'), letter('р'), letter('о'), letter('л')),
            listOf(letter('д'), letter('ж'), letter('є'), letter('я'), letter('ч')),
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
            listOf(letter('г'), letter('ш'), letter('щ'), letter('з'), letter('х'), letter('ї')),
            listOf(letter('ф'), letter('і'), letter('в'), letter('а'), letter('п'), letter('р')),
            listOf(letter('о'), letter('л'), letter('д'), letter('ж'), letter('є'), letter('я')),
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
